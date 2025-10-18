import {
	ConsoleTransport,
	createLoggerName,
	LOG_LEVEL_NAMES,
	LogLevel,
	LogManager,
} from "./dist/logger.js";

function assert(condition, message) {
	if (!condition) {
		throw new Error(`Assertion failed: ${message}`);
	}
}

const testBuiltExports = () => {
	try {
		console.log("Testing built package exports...");

		// Test LogLevel enum
		assert(
			typeof LogLevel.INFO === "number",
			"LogLevel.INFO should be a number",
		);
		assert(LogLevel.INFO === 2, "LogLevel.INFO should be 2");

		// Test LOG_LEVEL_NAMES
		assert(
			LOG_LEVEL_NAMES[LogLevel.INFO] === "INFO",
			"LOG_LEVEL_NAMES[LogLevel.INFO] should be 'INFO'",
		);
		assert(
			Object.values(LOG_LEVEL_NAMES).includes("ERROR"),
			"LOG_LEVEL_NAMES should include 'ERROR'",
		);

		// Test LogManager
		const transport = new ConsoleTransport();
		LogManager.configure({
			level: LogLevel.INFO,
			transports: [transport],
		});

		// Test Logger
		const logger = LogManager.getLogger("test.built.exports");
		let didNotThrow = true;
		try {
			logger.info("Built package exports are working correctly!");
		} catch (_err) {
			didNotThrow = false;
		}
		assert(didNotThrow, "logger.info should not throw an error");

		// Test utility functions
		const loggerName = createLoggerName("test.logger");
		assert(
			typeof loggerName === "string",
			"createLoggerName should return a string",
		);
		assert(
			loggerName === "test.logger",
			"createLoggerName should trim and normalize the name",
		);

		console.log("✅ All built package exports are working correctly!");
	} catch (error) {
		console.error("❌ Built package exports test failed:", error);
		process.exit(1);
	}
};

testBuiltExports();
