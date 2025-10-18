import type { LogEntry, Transport } from "../types";
import { LOG_LEVEL_NAMES, LogLevel } from "../types";

/**
 * Universal console transport that works in both browser and Node.js environments.
 * Automatically detects the environment and applies appropriate formatting.
 */
export class ConsoleTransport implements Transport {
	/**
	 * Environment detection using feature detection for window and document objects.
	 * This approach is more reliable than checking for specific runtime environments.
	 */
	private readonly isBrowser =
		typeof window !== "undefined" && typeof window.document !== "undefined";

	/**
	 * Process and output a log entry to the console.
	 * Routes to environment-specific formatting based on detected environment.
	 *
	 * @param entry The log entry to process and output
	 */
	log(entry: LogEntry): void {
		if (this.isBrowser) {
			this.logToBrowser(entry);
		} else {
			this.logToNode(entry);
		}
	}

	/**
	 * Output log entry to browser console with CSS styling.
	 * Uses %c placeholders for CSS styling and appropriate console methods.
	 *
	 * @param entry The log entry to format and output
	 */
	private logToBrowser(entry: LogEntry): void {
		const timestamp = entry.timestamp.toISOString();
		const levelName = LOG_LEVEL_NAMES[entry.level];
		const consoleMethod = this.getConsoleMethod(entry.level);

		// CSS styles for different components
		const timestampStyle = "color: #666; font-size: 0.9em;";
		const levelStyle = this.getBrowserLevelStyle(entry.level);
		const loggerNameStyle = "color: #0066cc; font-weight: bold;";
		const messageStyle = "color: inherit;";

		// Format: timestamp [LEVEL] loggerName message ...args
		const formattedMessage = `%c${timestamp} %c[${levelName}] %c${entry.loggerName} %c${entry.message}`;

		consoleMethod(
			formattedMessage,
			timestampStyle,
			levelStyle,
			loggerNameStyle,
			messageStyle,
			...entry.args,
		);
	}

	/**
	 * Output log entry to Node.js console with ANSI color codes.
	 * Uses ANSI escape sequences for terminal colors and formatting.
	 *
	 * @param entry The log entry to format and output
	 */
	private logToNode(entry: LogEntry): void {
		const timestamp = entry.timestamp.toISOString();
		const levelName = LOG_LEVEL_NAMES[entry.level];
		const consoleMethod = this.getConsoleMethod(entry.level);

		// ANSI color codes
		const colors = {
			reset: "\x1b[0m",
			timestamp: "\x1b[90m", // bright black (gray)
			level: this.getNodeLevelColor(entry.level),
			loggerName: "\x1b[94m", // bright blue
		};

		// Format: timestamp [LEVEL] loggerName message
		const formattedMessage = `${colors.timestamp}${timestamp}${colors.reset} ${colors.level}[${levelName}]${colors.reset} ${colors.loggerName}${entry.loggerName}${colors.reset} ${entry.message}`;

		// Handle additional arguments - format complex objects as JSON in Node.js
		const formattedArgs = entry.args.map((arg) => {
			if (typeof arg === "object" && arg !== null) {
				try {
					return JSON.stringify(arg, null, 2);
				} catch {
					return String(arg);
				}
			}
			return arg;
		});

		consoleMethod(formattedMessage, ...formattedArgs);
	}

	/**
	 * Map log levels to appropriate console methods.
	 * Ensures proper categorization in browser dev tools and Node.js output.
	 *
	 * @param level The log level to map
	 * @returns The appropriate console method
	 */
	private getConsoleMethod(level: LogLevel): (...data: unknown[]) => void {
		switch (level) {
			case LogLevel.TRACE:
			case LogLevel.DEBUG:
				return console.debug || console.log;
			case LogLevel.INFO:
				return console.info || console.log;
			case LogLevel.WARN:
				return console.warn || console.log;
			case LogLevel.ERROR:
			case LogLevel.FATAL:
				return console.error || console.log;
			default:
				return console.log;
		}
	}

	/**
	 * Get CSS styles for different log levels in browser environment.
	 *
	 * @param level The log level
	 * @returns CSS style string for the level
	 */
	private getBrowserLevelStyle(level: LogLevel): string {
		switch (level) {
			case LogLevel.TRACE:
				return "color: #999; font-weight: normal;";
			case LogLevel.DEBUG:
				return "color: #666; font-weight: normal;";
			case LogLevel.INFO:
				return "color: #0066cc; font-weight: bold;";
			case LogLevel.WARN:
				return "color: #ff8800; font-weight: bold;";
			case LogLevel.ERROR:
				return "color: #cc0000; font-weight: bold;";
			case LogLevel.FATAL:
				return "color: #ffffff; background-color: #cc0000; font-weight: bold; padding: 2px 4px;";
			default:
				return "color: inherit; font-weight: normal;";
		}
	}

	/**
	 * Get ANSI color codes for different log levels in Node.js environment.
	 *
	 * @param level The log level
	 * @returns ANSI color code string for the level
	 */
	private getNodeLevelColor(level: LogLevel): string {
		switch (level) {
			case LogLevel.TRACE:
				return "\x1b[37m"; // white
			case LogLevel.DEBUG:
				return "\x1b[90m"; // bright black (gray)
			case LogLevel.INFO:
				return "\x1b[94m"; // bright blue
			case LogLevel.WARN:
				return "\x1b[93m"; // bright yellow
			case LogLevel.ERROR:
				return "\x1b[91m"; // bright red
			case LogLevel.FATAL:
				return "\x1b[97m\x1b[41m"; // bright white on red background
			default:
				return "\x1b[0m"; // reset
		}
	}
}
