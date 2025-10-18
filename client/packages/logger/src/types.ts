/**
 * Log levels with numeric values for efficient comparison.
 * Higher numeric values indicate higher severity.
 *
 * Performance optimization: Numeric values enable fast comparison operations
 * using >= operator instead of string comparisons, improving log level checking performance.
 */
export enum LogLevel {
	TRACE = 0,
	DEBUG = 1,
	INFO = 2,
	WARN = 3,
	ERROR = 4,
	FATAL = 5,
}

/**
 * Mapping of log levels to their string representations for display purposes
 */
export const LOG_LEVEL_NAMES = {
	[LogLevel.TRACE]: "TRACE",
	[LogLevel.DEBUG]: "DEBUG",
	[LogLevel.INFO]: "INFO",
	[LogLevel.WARN]: "WARN",
	[LogLevel.ERROR]: "ERROR",
	[LogLevel.FATAL]: "FATAL",
} as const;

/**
 * Branded type for logger names to provide type safety
 */
export type LoggerName = string & { readonly __brand: unique symbol };

/**
 * Represents a single log entry with all necessary information.
 */
export interface LogEntry {
	/** Timestamp when the log entry was created */
	timestamp: Date;
	/** Log level of this entry */
	level: LogLevel;
	/** Name of the logger that created this entry */
	loggerName: LoggerName;
	/** Primary log message */
	message: string;
	/** Additional arguments passed to the log method */
	args: unknown[];
}

/**
 * Interface for log output destinations (transports).
 * Transports are responsible for formatting and outputting log entries.
 */
export interface Transport {
	/**
	 * Process and output a log entry.
	 * @param entry The log entry to process
	 */
	log(entry: LogEntry): void;

	/**
	 * Optional method to check if transport is available/healthy
	 */
	isAvailable?(): boolean;

	/**
	 * Optional cleanup method for transport resources
	 */
	dispose?(): void;
}

/**
 * Configuration interface for the LogManager.
 * Defines the root log level, hierarchical level overrides, and transports.
 */
export interface LoggerConfiguration {
	/** Root log level applied to all loggers unless overridden */
	level: LogLevel;
	/** Optional hierarchical level overrides using dot-notation logger names */
	levels?: Record<string, LogLevel>;
	/** Array of transports that will receive log entries */
	transports: readonly Transport[];
}

/**
 * Utility type for log method signatures
 */
export type LogMethod = (message: string, ...args: unknown[]) => void;

/**
 * Utility type for creating logger name from string
 * Normalizes the name by trimming whitespace and providing fallback for empty names
 */
export const createLoggerName = (name: string): LoggerName => {
	const normalized = name?.trim();
	return (normalized || "unknown") as LoggerName;
};

/**
 * Type guard to check if a value is a valid LogLevel
 */
export const isLogLevel = (value: unknown): value is LogLevel => {
	return (
		typeof value === "number" &&
		value >= LogLevel.TRACE &&
		value <= LogLevel.FATAL &&
		Number.isInteger(value)
	);
};

/**
 * Utility type for log level configuration
 */
export type LogLevelConfig = Partial<Record<string, LogLevel>>;

/**
 * Result type for operations that might fail
 */
export type LoggerResult<T> =
	| {
			success: true;
			data: T;
	  }
	| {
			success: false;
			error: string;
	  };

/**
 * Structured error types for better error handling
 */
export enum LoggerErrorType {
	CONFIGURATION_INVALID = "CONFIGURATION_INVALID",
	TRANSPORT_FAILURE = "TRANSPORT_FAILURE",
	LEVEL_INVALID = "LEVEL_INVALID",
	LOGGER_NAME_INVALID = "LOGGER_NAME_INVALID",
}

export interface LoggerError {
	type: LoggerErrorType;
	message: string;
	details?: Record<string, unknown>;
}

/**
 * Custom error class for logger-specific errors
 */
export class LoggerConfigurationError extends Error {
	public readonly type: LoggerErrorType;
	public readonly details?: Record<string, unknown>;

	constructor(
		type: LoggerErrorType,
		message: string,
		details?: Record<string, unknown>,
	) {
		super(message);
		this.name = "LoggerConfigurationError";
		this.type = type;
		this.details = details;
	}
}
