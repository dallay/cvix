import type { LoggerConfiguration, Transport } from "./types";
import { LogLevel } from "./types";

/**
 * Builder pattern for creating LoggerConfiguration objects.
 * Provides a fluent API for configuration setup using method chaining.
 *
 * @example
 * // Example usage of LoggerConfigurationBuilder:
 * import { LoggerConfigurationBuilder } from './LoggerConfigurationBuilder';
 * import { LogLevel } from './types';
 *
 * const transport = {
 *   log: (entry) => {
 *     // Custom transport logic
 *   }
 * };
 *
 * const config = new LoggerConfigurationBuilder()
 *   .withLevel(LogLevel.DEBUG) // Set the root log level
 *   .withTransport(transport) // Add a transport
 *   .withLoggerLevel('api', LogLevel.INFO) // Override log level for 'api' logger
 *   .withLoggerLevel('db', LogLevel.ERROR) // Override log level for 'db' logger
 *   .build(); // Build the final LoggerConfiguration object
 *
 * // The resulting config can be passed to LogManager.configure(config)
 */
export class LoggerConfigurationBuilder {
	private config: Partial<LoggerConfiguration> = {
		level: LogLevel.INFO,
		levels: {},
		transports: [],
	};

	/**
	 * Set the root log level
	 */
	withLevel(level: LogLevel): this {
		this.config.level = level;
		return this;
	}

	/**
	 * Add a hierarchical level override
	 */
	withLoggerLevel(loggerName: string, level: LogLevel): this {
		this.config.levels[loggerName] = level;
		return this;
	}

	/**
	 * Add multiple logger level overrides
	 */
	withLoggerLevels(levels: Record<string, LogLevel>): this {
		this.config.levels = { ...this.config.levels, ...levels };
		return this;
	}

	/**
	 * Add a transport
	 */
	withTransport(transport: Transport): this {
		if (!this.config.transports) {
			this.config.transports = [];
		}
		this.config.transports = [...this.config.transports, transport];
		return this;
	}

	/**
	 * Add multiple transports
	 */
	withTransports(transports: Transport[]): this {
		this.config.transports = this.config.transports.concat(transports);
		return this;
	}

	/**
	 * Build the final configuration
	 */
	build(): LoggerConfiguration {
		if (!this.config.transports?.length) {
			throw new Error("At least one transport must be configured");
		}

		return {
			level: this.config.level || LogLevel.INFO,
			levels: this.config.levels,
			transports: this.config.transports,
		};
	}

	/**
	 * Create a development configuration with common settings
	 */
	static development(): LoggerConfigurationBuilder {
		return new LoggerConfigurationBuilder().withLevel(LogLevel.DEBUG);
	}

	/**
	 * Create a production configuration with common settings
	 */
	static production(): LoggerConfigurationBuilder {
		return new LoggerConfigurationBuilder().withLevel(LogLevel.INFO);
	}
}
