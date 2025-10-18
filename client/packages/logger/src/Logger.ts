import type { LogEntry, LoggerName, LogLevel } from "./types";
import { createLoggerName, LogLevel as LogLevelEnum } from "./types";

declare global {
	interface GlobalThis {
		__LOGGER_DEBUG__?: boolean;
		__LOGGER_MANAGER__?: unknown;
	}
}

/**
 * Interface for LogManager to avoid circular dependencies
 */
interface ILogManager {
	/**
	 * Processes a log entry by routing it to the appropriate transports and handling log logic.
	 *
	 * @param entry - The log entry to process.
	 * @returns void
	 */
	processLog(entry: LogEntry): void;

	/**
	 * Determines if a given log level is enabled for the specified logger name.
	 *
	 * @param loggerName - The name of the logger to check.
	 * @param level - The log level to check.
	 * @returns True if the log level is enabled, false otherwise.
	 */
	isLevelEnabled(loggerName: LoggerName, level: LogLevel): boolean;
}

/**
 * Logger class provides level-specific logging methods.
 * Each logger instance has an immutable name and delegates log processing to LogManager.
 */
export class Logger {
	/**
	 * Immutable logger name used for hierarchical configuration and identification.
	 */
	public readonly name: LoggerName;

	/**
	 * LogManager instance for processing log entries
	 */
	private readonly logManager: ILogManager | null;

	/**
	 * Creates a new Logger instance with the specified name.
	 * @param name The logger name, typically using dot-notation for hierarchy
	 * @param logManager Optional LogManager instance for dependency injection
	 */
	constructor(name: string, logManager?: ILogManager) {
		this.name = createLoggerName(name);
		this.logManager = logManager || this.getLogManager();
	}

	/**
	 * Log a TRACE level message.
	 * TRACE is the lowest severity level, typically used for detailed diagnostic information.
	 * @param message The log message
	 * @param args Additional arguments to include in the log entry
	 */
	public trace(message: string, ...args: unknown[]): void {
		this.logInternal(LogLevelEnum.TRACE, message, args);
	}

	/**
	 * Log a DEBUG level message.
	 * DEBUG is used for diagnostic information useful during development.
	 * @param message The log message
	 * @param args Additional arguments to include in the log entry
	 */
	public debug(message: string, ...args: unknown[]): void {
		this.logInternal(LogLevelEnum.DEBUG, message, args);
	}

	/**
	 * Log an INFO level message.
	 * INFO is used for general informational messages about application flow.
	 * @param message The log message
	 * @param args Additional arguments to include in the log entry
	 */
	public info(message: string, ...args: unknown[]): void {
		this.logInternal(LogLevelEnum.INFO, message, args);
	}

	/**
	 * Log a WARN level message.
	 * WARN is used for potentially harmful situations that don't prevent operation.
	 * @param message The log message
	 * @param args Additional arguments to include in the log entry
	 */
	public warn(message: string, ...args: unknown[]): void {
		this.logInternal(LogLevelEnum.WARN, message, args);
	}

	/**
	 * Log an ERROR level message.
	 * ERROR is used for error events that might still allow the application to continue.
	 * @param message The log message
	 * @param args Additional arguments to include in the log entry
	 */
	public error(message: string, ...args: unknown[]): void {
		this.logInternal(LogLevelEnum.ERROR, message, args);
	}

	/**
	 * Log a FATAL level message.
	 * FATAL is the highest severity level, used for severe errors that may cause termination.
	 * @param message The log message
	 * @param args Additional arguments to include in the log entry
	 */
	public fatal(message: string, ...args: unknown[]): void {
		this.logInternal(LogLevelEnum.FATAL, message, args);
	}

	/**
	 * Public log method for testing and advanced usage.
	 * Allows logging at any level programmatically.
	 * @param level The log level for this entry
	 * @param message The log message
	 * @param args Additional arguments to include in the log entry
	 */
	public log(level: LogLevel, message: string, args: unknown[]): void {
		this.logInternal(level, message, args);
	}

	/**
	 * Private method that creates a LogEntry and delegates to LogManager for processing.
	 * This method is responsible for creating the log entry with proper timestamp and
	 * routing it to the LogManager for level checking and transport routing.
	 *
	 * Performance optimizations implemented:
	 * - Early return when LogManager is not available (graceful degradation)
	 * - Level checking before LogEntry creation to skip message processing when disabled
	 * - LogEntry creation only when level is enabled to avoid unnecessary object allocation
	 * - Comprehensive error handling and null checks
	 *
	 * @param level The log level for this entry
	 * @param message The log message
	 * @param args Additional arguments to include in the log entry
	 */
	private logInternal(level: LogLevel, message: string, args: unknown[]): void {
		if (!this.isValidLogInput(level, message) || !this.logManager) {
			return;
		}

		if (!this.isLogLevelEnabled(level)) {
			return;
		}

		this.createAndProcessLogEntry(level, message, args);
	}

	/**
	 * Create and process log entry with error handling
	 *
	 * In development mode, errors are logged to the console for debugging.
	 */
	private createAndProcessLogEntry(
		level: LogLevel,
		message: string,
		args: unknown[],
	): void {
		try {
			const safeMessage = this.sanitizeMessage(message);
			const safeArgs = this.sanitizeArgs(args);
			const entry = this.createLogEntry(level, safeMessage, safeArgs);
			this.processLogEntry(entry);
		} catch (error) {
			// Log error in development mode for debugging
			const isDev =
				(typeof process !== "undefined" &&
					process.env &&
					process.env.NODE_ENV === "development") ||
				(typeof globalThis !== "undefined" &&
					globalThis.__LOGGER_DEBUG__ === true);
			if (isDev) {
				console.error("[Logger] Log entry processing error:", error);
			}
		}
	}

	/**
	 * Validate log input parameters
	 */
	private isValidLogInput(level: LogLevel, message: unknown): boolean {
		return (
			level !== undefined &&
			level !== null &&
			typeof level === "number" &&
			message !== undefined &&
			message !== null
		);
	}

	/**
	 * Sanitize message parameter
	 */
	private sanitizeMessage(message: unknown): string {
		return typeof message === "string" ? message : String(message);
	}

	/**
	 * Sanitize args parameter
	 */
	private sanitizeArgs(args: unknown): unknown[] {
		return Array.isArray(args) ? args : [];
	}

	/**
	 * Check if log level is enabled
	 */
	private isLogLevelEnabled(level: LogLevel): boolean {
		try {
			return this.logManager?.isLevelEnabled(this.name, level) ?? false;
		} catch (_error) {
			return false;
		}
	}

	/**
	 * Create log entry object
	 */
	private createLogEntry(
		level: LogLevel,
		message: string,
		args: unknown[],
	): LogEntry {
		return {
			timestamp: new Date(),
			level,
			loggerName: this.name,
			message,
			args,
		};
	}

	/**
	 * Process log entry through LogManager
	 */
	private processLogEntry(entry: LogEntry): void {
		try {
			this.logManager?.processLog(entry);
		} catch (_error) {
			// Graceful degradation: processing failures are silently ignored
		}
	}

	/**
	 * Safely attempt to get LogManager instance.
	 * This method encapsulates the LogManager access logic and provides
	 * comprehensive error handling and graceful degradation.
	 * @returns LogManager instance or null if not available
	 */
	private getLogManager(): ILogManager | null {
		try {
			if (typeof globalThis === "undefined") {
				return null;
			}

			const logManagerModule = globalThis.__LOGGER_MANAGER__;
			return this.validateLogManagerInterface(logManagerModule);
		} catch (_error) {
			// Graceful degradation: any error in LogManager access results in null
			return null;
		}
	}

	/**
	 * Validate that the LogManager interface is properly implemented
	 */
	private validateLogManagerInterface(manager: unknown): ILogManager | null {
		if (!manager || typeof manager !== "object") {
			return null;
		}

		const candidate = manager as Record<string, unknown>;

		if (
			typeof candidate.processLog !== "function" ||
			typeof candidate.isLevelEnabled !== "function"
		) {
			return null;
		}

		return candidate as unknown as ILogManager;
	}
}
