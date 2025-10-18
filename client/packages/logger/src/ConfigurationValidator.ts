import type { LoggerConfiguration, LogLevel, Transport } from "./types";
import {
	LoggerConfigurationError,
	LoggerErrorType,
	LogLevel as LogLevelEnum,
} from "./types";

/**
 * Validates logger configuration objects to ensure they meet the required structure
 * and contain valid values for all properties.
 *
 * @remarks
 * This class provides static methods to validate logger configuration, transports, log levels, and hierarchical overrides.
 */

/**
 * Validates a complete logger configuration object.
 *
 * @param config - The logger configuration to validate.
 * @throws LoggerConfigurationError if the configuration is invalid.
 */
export function validateLoggerConfiguration(config: LoggerConfiguration): void {
	validateConfigExists(config);
	validateTransports(config.transports);
	validateRootLevel(config.level);
	validateHierarchicalLevels(config.levels);
}

function validateConfigExists(
	config: unknown,
): asserts config is LoggerConfiguration {
	if (!config || typeof config !== "object") {
		throw new LoggerConfigurationError(
			LoggerErrorType.CONFIGURATION_INVALID,
			"LoggerConfiguration cannot be null or undefined",
		);
	}
}

function validateTransports(transports: readonly Transport[]): void {
	if (!Array.isArray(transports) || transports.length === 0) {
		throw new LoggerConfigurationError(
			LoggerErrorType.CONFIGURATION_INVALID,
			"LoggerConfiguration must have at least one transport",
		);
	}
	for (const [index, transport] of transports.entries()) {
		if (!transport || typeof transport.log !== "function") {
			throw new LoggerConfigurationError(
				LoggerErrorType.CONFIGURATION_INVALID,
				`Transport at index ${index} must have a log method`,
			);
		}
	}
}

function validateRootLevel(level: LogLevel): void {
	if (!isValidLogLevel(level)) {
		throw new LoggerConfigurationError(
			LoggerErrorType.LEVEL_INVALID,
			`Invalid root log level: ${level}`,
		);
	}
}

function validateHierarchicalLevels(levels?: Record<string, LogLevel>): void {
	if (!levels) return;
	for (const [name, level] of Object.entries(levels)) {
		if (!isValidLogLevel(level)) {
			throw new LoggerConfigurationError(
				LoggerErrorType.LEVEL_INVALID,
				`Invalid log level for '${name}': ${level}`,
			);
		}
	}
}

function isValidLogLevel(level: unknown): level is LogLevel {
	return (
		typeof level === "number" &&
		Number.isInteger(level) &&
		level >= LogLevelEnum.TRACE &&
		level <= LogLevelEnum.FATAL &&
		Object.values(LogLevelEnum).includes(level)
	);
}
