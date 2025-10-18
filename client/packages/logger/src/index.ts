/**
 * @loomify/logger - Universal TypeScript logging library
 *
 * This package provides a lightweight, powerful, and universal logging system
 * that works seamlessly in both Node.js and browser environments.
 *
 * Key features:
 * - Hierarchical logger naming with configurable log levels
 * - Environment-aware output formatting (CSS in browser, ANSI in Node.js)
 * - Performance-optimized with level checking and caching
 * - Zero external dependencies
 * - Pluggable transport system
 * - Full TypeScript support
 *
 * @example
 * ```typescript
 * import { LogManager, LogLevel, ConsoleTransport } from '@loomify/logger';
 *
 * // Configure the logger system
 * LogManager.configure({
 *   level: LogLevel.INFO,
 *   levels: {
 *     'api.database': LogLevel.WARN,
 *     'ui.animation': LogLevel.ERROR,
 *   },
 *   transports: [new ConsoleTransport()],
 * });
 *
 * // Use loggers in your application
 * const logger = LogManager.getLogger('api.services.auth');
 * logger.info('User authentication successful', { userId: '123' });
 * ```
 */

// Export main classes
export { Logger } from "./Logger";
export { LoggerConfigurationBuilder } from "./LoggerConfigurationBuilder";
export { LogManager } from "./LogManager";
// Export transports
export { ConsoleTransport } from "./transports/index";
// Export type definitions for public API
export type {
	LogEntry,
	LoggerConfiguration,
	LoggerName,
	LogMethod,
	Transport,
} from "./types";

export {
	createLoggerName,
	LOG_LEVEL_NAMES,
	LogLevel,
} from "./types";
