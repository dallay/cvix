import type { Logger } from "./Logger";
import type { LoggerConfiguration, LogLevel } from "./types";

/**
 * Manages the internal state for the logger system, including configuration, logger instances, and level caches.
 *
 * The LoggerState class is responsible for:
 * - Storing and updating the active LoggerConfiguration
 * - Caching Logger instances for efficient retrieval and reuse
 * - Caching effective log levels for performance optimization
 * - Enforcing cache limits to prevent memory leaks in long-running applications
 * - Providing methods to reset, clear, and inspect cache statistics
 *
 * This class is used internally by the logging infrastructure to ensure consistent state management and fast logger lookups.
 * It is not intended for direct use by application code; instead, use the public LogManager API for logger operations.
 *
 * Important notes:
 * - LoggerState automatically invalidates level caches when configuration changes.
 * - Cache limits are enforced using a simple FIFO eviction policy.
 * - Resetting LoggerState will clear all configuration and caches, returning the system to an unconfigured state.
 */
export class LoggerState {
	private config: LoggerConfiguration | null = null;
	private readonly loggers = new Map<string, Logger>();
	private readonly levelCache = new Map<string, LogLevel>();
	private readonly cacheConfig = {
		maxLoggers: 1000,
		maxLevelCache: 500,
	};

	getConfig(): LoggerConfiguration | null {
		return this.config;
	}

	setConfig(config: LoggerConfiguration): void {
		this.config = config;
		this.levelCache.clear(); // Invalidate cache on config change
	}

	isConfigured(): boolean {
		return this.config !== null;
	}

	getLogger(name: string): Logger | undefined {
		return this.loggers.get(name);
	}

	setLogger(name: string, logger: Logger): void {
		this.enforceLoggerCacheLimit();
		this.loggers.set(name, logger);
	}

	getCachedLevel(name: string): LogLevel | undefined {
		return this.levelCache.get(name);
	}

	setCachedLevel(name: string, level: LogLevel): void {
		this.enforceLevelCacheLimit();
		this.levelCache.set(name, level);
	}

	/**
	 * Clears only the level cache. Use clearAllCaches() to clear all caches.
	 */
	clearLevelCache(): void {
		this.levelCache.clear();
	}

	/**
	 * Clears all relevant caches: logger instances and level cache.
	 * Does not reset configuration.
	 */
	clearAllCaches(): void {
		this.loggers.clear();
		this.levelCache.clear();
	}

	reset(): void {
		this.config = null;
		this.loggers.clear();
		this.levelCache.clear();
	}

	getCacheStats() {
		return {
			loggerCacheSize: this.loggers.size,
			levelCacheSize: this.levelCache.size,
		} as const;
	}

	private enforceLoggerCacheLimit(): void {
		if (this.loggers.size >= this.cacheConfig.maxLoggers) {
			const firstKey = this.loggers.keys().next().value;
			if (firstKey) {
				this.loggers.delete(firstKey);
			}
		}
	}

	private enforceLevelCacheLimit(): void {
		if (this.levelCache.size >= this.cacheConfig.maxLevelCache) {
			const firstKey = this.levelCache.keys().next().value;
			if (firstKey) {
				this.levelCache.delete(firstKey);
			}
		}
	}
}
