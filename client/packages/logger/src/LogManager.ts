import { Logger } from "./Logger";
import type {
	LogEntry,
	LoggerConfiguration,
	LoggerName,
	LogLevel,
} from "./types";
import { LogLevel as LogLevelEnum } from "./types";

/**
 * Internal state for the logger system.
 * Encapsulates configuration, caches, and logger instances.
 */
interface LoggerState {
	config: LoggerConfiguration | null;
	loggers: Map<string, Logger>;
	levelCache: Map<string, LogLevel>;
}

/**
 * Global feature flags and singleton references for logger.
 */
interface LoggerGlobalFlags {
	__LOGGER_MANAGER__?: typeof LogManager;
	__LOGGER_ENHANCED_ERROR_HANDLING__?: boolean;
	__LOGGER_DEBUG__?: boolean;
}

declare global {
	// eslint-disable-next-line no-var
	var __LOGGER_MANAGER__: typeof LogManager | undefined;
	// eslint-disable-next-line no-var
	var __LOGGER_ENHANCED_ERROR_HANDLING__: boolean | undefined;
	// eslint-disable-next-line no-var
	var __LOGGER_DEBUG__: boolean | undefined;
}

/**
 * Singleton global state for logger configuration and caches.
 *
 * This object is initialized once and shared across the LogManager module, providing
 * centralized storage for the active LoggerConfiguration, cached Logger instances, and
 * cached effective log levels. Its lifecycle matches the module's lifecycle: it is created
 * at module load and persists for the duration of the process.
 *
 * Thread Safety:
 * - In Node.js and most JavaScript environments, modules are singletons per process/thread.
 * - This state is NOT safe for direct concurrent mutation in multi-threaded environments (e.g., worker threads).
 * - For web or serverless environments, each instance will have its own copy; no cross-request sharing occurs.
 * - If using in a concurrent or distributed context, ensure external synchronization or isolation.
 */
const loggerState: LoggerState = {
	config: null,
	loggers: new Map<string, Logger>(),
	levelCache: new Map<string, LogLevel>(),
};

/**
 * Configure the LogManager with the provided configuration.
 * This method must be called before using any loggers.
 * Clears all caches to ensure new configuration takes effect immediately.
 *
 * @param config The logger configuration containing root level, hierarchical overrides, and transports
 */

/**
 * Helper to clear logger state and global LogManager reference, then return early from configure.
 */
function cleanupLoggerStateAndReturn(): void {
	loggerState.config = null;
	loggerState.levelCache.clear();
	if (
		typeof globalThis !== "undefined" &&
		(globalThis as LoggerGlobalFlags).__LOGGER_MANAGER__
	) {
		delete (globalThis as LoggerGlobalFlags).__LOGGER_MANAGER__;
	}
}

const configure = (config: LoggerConfiguration): void => {
	// Enhanced error handling: validate config before applying
	const enhancedErrorHandling = (globalThis as LoggerGlobalFlags)
		.__LOGGER_ENHANCED_ERROR_HANDLING__;

	// If enhanced error handling is enabled, validate config
	if (enhancedErrorHandling) {
		// Null or undefined config
		if (config == null) {
			cleanupLoggerStateAndReturn();
			return;
		}

		// Validate level
		const validLevel = typeof config.level === "number" && config.level >= 0;
		if (!validLevel) {
			cleanupLoggerStateAndReturn();
			return;
		}

		// Validate transports
		const validTransports =
			Array.isArray(config.transports) &&
			config.transports.length > 0 &&
			config.transports.every((t) => t && typeof t.log === "function");
		if (!validTransports) {
			cleanupLoggerStateAndReturn();
			return;
		}

		// Validate levels object if present
		if (
			config.levels != null &&
			(typeof config.levels !== "object" ||
				Array.isArray(config.levels) ||
				Object.values(config.levels).some(
					(v) => typeof v !== "number" || v < 0,
				))
		) {
			cleanupLoggerStateAndReturn();
			return;
		}
	}

	loggerState.config = config;
	loggerState.levelCache.clear();

	// Register LogManager globally for test and runtime access
	if (typeof globalThis !== "undefined") {
		(globalThis as LoggerGlobalFlags).__LOGGER_MANAGER__ = LogManager;
	}

	// Note: We don't clear the logger cache as Logger instances are stateless
	// and their behavior is determined by the current configuration
};

/**
 * Get or create a Logger instance for the specified name.
 * Logger instances are cached to prevent repeated object creation.
 *
 * @param name The logger name, typically using dot-notation for hierarchy (e.g., "api.services.auth")
 * @returns A Logger instance for the specified name
 */
const getLogger = (name: string): Logger => {
	// Enhanced error handling: normalize logger name
	const enhancedErrorHandling = (globalThis as LoggerGlobalFlags)
		.__LOGGER_ENHANCED_ERROR_HANDLING__;
	let normalizedName: string;
	if (enhancedErrorHandling) {
		if (name == null || (typeof name === "string" && name.trim() === "")) {
			normalizedName = "unknown";
		} else if (typeof name !== "string") {
			// If name is not a string, convert to string, but treat null/undefined as "unknown"
			normalizedName = String(name);
		} else {
			normalizedName = name.trim();
		}
	} else {
		normalizedName = name;
	}

	// Check cache first for performance
	let logger = loggerState.loggers.get(normalizedName);

	if (!logger) {
		// Create new logger instance and cache it
		logger = new Logger(normalizedName);
		loggerState.loggers.set(normalizedName, logger);
	}

	return logger;
};

/**
 * Check if a log level is enabled for a given logger name.
 * This method provides early level checking without creating LogEntry objects.
 *
 * @param loggerName The logger name to check
 * @param level The log level to check
 * @returns True if the level is enabled, false otherwise
 */
const isLevelEnabled = (loggerName: LoggerName, level: LogLevel): boolean => {
	if (!loggerState.config) {
		return false;
	}

	const enhancedErrorHandling = (globalThis as LoggerGlobalFlags)
		.__LOGGER_ENHANCED_ERROR_HANDLING__;
	if (enhancedErrorHandling) {
		// Invalid loggerName: null, undefined, not a string, empty, or whitespace
		if (
			loggerName == null ||
			typeof loggerName !== "string" ||
			loggerName.trim() === ""
		) {
			return false;
		}
		// Invalid level: not a number or negative
		if (typeof level !== "number" || level < 0) {
			return false;
		}
	}

	const effectiveLevel = getEffectiveLevel(loggerName);
	return level >= effectiveLevel;
};

/**
 * Process a log entry by checking its effective level and routing to configured transports.
 * This method implements the core logging logic including:
 * - Level checking for performance optimization
 * - Graceful degradation when not configured
 * - Error isolation between transports
 *
 * @param entry The log entry to process
 */
const processLog = (entry: LogEntry): void => {
	// Graceful degradation: if not configured, silently ignore
	if (!loggerState.config) {
		return;
	}

	// Enhanced error handling: validate entry before processing
	const enhancedErrorHandling = (globalThis as LoggerGlobalFlags)
		.__LOGGER_ENHANCED_ERROR_HANDLING__;
	if (enhancedErrorHandling) {
		// Null, undefined, or not an object
		if (!entry || typeof entry !== "object") {
			return;
		}
		// Missing required properties
		if (!("loggerName" in entry) || !("level" in entry)) {
			return;
		}
	}

	// Performance optimization: skip processing if log level is below effective level
	if (!isLevelEnabled(entry.loggerName, entry.level)) {
		return;
	}

	// Route to all configured transports with error isolation
	loggerState.config.transports.forEach((transport) => {
		try {
			transport.log(entry);
		} catch (error) {
			// Error isolation: transport failures don't affect other transports or application flow
			console.error("Logger transport error:", error);
		}
	});
};

/**
 * Resolve the effective log level for a given logger name using hierarchical inheritance.
 * Implements caching for performance optimization.
 *
 * Algorithm:
 * 1. Check cache first for performance
 * 2. Look for exact match in configuration
 * 3. Traverse hierarchy from most specific to least specific using dot-notation
 * 4. Fall back to root level if no hierarchy matches
 * 5. Cache result for future lookups
 *
 * Examples:
 * - "api.services.auth" -> check "api.services.auth", "api.services", "api", then root
 * - "ui.components" -> check "ui.components", "ui", then root
 *
 * @param loggerName The logger name to resolve level for
 * @returns The effective log level for the logger
 */
const getEffectiveLevel = (loggerName: LoggerName): LogLevel => {
	const name = loggerName as string;

	// Check cache first for performance optimization
	const cachedLevel = loggerState.levelCache.get(name);
	if (cachedLevel !== undefined) {
		return cachedLevel;
	}

	// If no configuration, return INFO as safe default
	if (!loggerState.config) {
		const defaultLevel = LogLevelEnum.INFO;
		loggerState.levelCache.set(name, defaultLevel);
		return defaultLevel;
	}

	let effectiveLevel: LogLevel;

	// Check for exact match first
	if (
		loggerState.config.levels &&
		loggerState.config.levels[name] !== undefined
	) {
		effectiveLevel = loggerState.config.levels[name];
	} else {
		// Traverse hierarchy from most specific to least specific
		effectiveLevel = resolveHierarchicalLevel(name);
	}

	// Cache the result for future lookups to avoid repeated hierarchy traversal
	loggerState.levelCache.set(name, effectiveLevel);

	return effectiveLevel;
};

/**
 * Resolve log level by traversing the dot-notation hierarchy.
 * Starts from the full name and removes segments from the end until a match is found.
 * Falls back to root level if no hierarchy matches.
 *
 * This method implements the core hierarchical inheritance logic:
 * - Child loggers inherit from parent loggers when no specific config exists
 * - Traversal goes from most specific to least specific
 * - Root level serves as the ultimate fallback
 *
 * Examples:
 * - "api.services.auth" -> check "api.services.auth", "api.services", "api", then root
 * - "ui.components.button" -> check "ui.components.button", "ui.components", "ui", then root
 *
 * @param name The logger name to resolve
 * @returns The resolved log level from hierarchy or root level
 */
const resolveHierarchicalLevel = (name: string): LogLevel => {
	if (!loggerState.config || !loggerState.config.levels) {
		return loggerState.config?.level ?? LogLevelEnum.INFO;
	}

	// Split name into segments for hierarchy traversal
	const segments = name.split(".");

	// Check each level of the hierarchy from most specific to least specific
	// Start from the second-to-last segment (since we already checked the full name)
	for (let i = segments.length - 1; i > 0; i--) {
		const parentName = segments.slice(0, i).join(".");
		const parentLevel = loggerState.config.levels[parentName];

		if (parentLevel !== undefined) {
			return parentLevel;
		}
	}

	// No hierarchy match found, fall back to root level
	return loggerState.config.level;
};

/**
 * Clear all caches. Useful for testing and configuration changes.
 * This method is primarily intended for internal use and testing.
 * Clears the level cache to force re-resolution of effective levels.
 */
const clearCaches = (): void => {
	loggerState.levelCache.clear();
	// Reset per-logger level overrides to an empty object for type safety
	if (
		loggerState.config &&
		typeof loggerState.config === "object" &&
		"levels" in loggerState.config
	) {
		const configCopy = { ...loggerState.config, levels: {} };
		loggerState.config = configCopy as LoggerConfiguration;
	}
	// Note: We typically don't clear logger cache as instances are stateless
	// and their behavior is determined by the current configuration
};

/**
 * Get current configuration. Primarily for testing and debugging.
 * @returns Current configuration or null if not configured
 */
const getConfiguration = (): LoggerConfiguration | null => {
	return loggerState.config;
};

/**
 * Check if LogManager is configured.
 * @returns True if LogManager has been configured, false otherwise
 */
const isConfigured = (): boolean => {
	return loggerState.config !== null;
};

/**
 * Reset the LogManager to its initial, unconfigured state.
 * Clears all configuration, caches, and logger instances.
 * Primarily intended for testing purposes to ensure a clean slate between tests.
 */
const reset = (): void => {
	loggerState.config = null;
	loggerState.loggers.clear();
	loggerState.levelCache.clear();

	// Remove the global reference to ensure a fully unconfigured state
	if (
		typeof globalThis !== "undefined" &&
		(globalThis as Record<string, unknown>).__LOGGER_MANAGER__
	) {
		delete (globalThis as Record<string, unknown>).__LOGGER_MANAGER__;
	}
};

/**
 * LogManager provides centralized logging configuration and management.
 * Implemented as a module with functions rather than a static class for better TypeScript practices.
 */
export const LogManager = {
	configure,
	getLogger,
	processLog,
	isLevelEnabled,
	clearCaches,
	getConfiguration,
	isConfigured,
	reset,
} as const;
