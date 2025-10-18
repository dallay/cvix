/** biome-ignore-all lint/suspicious/noExplicitAny: In this context, null is used to test robustness */
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createLoggerName, type LogEntry, Logger, LogLevel } from "../src";

// Mock LogManager interface for testing
interface MockLogManager {
	processLog: ReturnType<typeof vi.fn>;
	isLevelEnabled: ReturnType<typeof vi.fn>;
}

describe("Logger", () => {
	let logger: Logger;
	let mockLogManager: MockLogManager;

	beforeEach(() => {
		// Create mock LogManager
		mockLogManager = {
			processLog: vi.fn(),
			isLevelEnabled: vi.fn().mockReturnValue(true), // Default to enabled
		};

		// Create logger with mock LogManager
		logger = new Logger("test.logger", mockLogManager);
	});

	describe("constructor", () => {
		it("should create logger with immutable name property", () => {
			const loggerName = "api.services.auth";
			const testLogger = new Logger(loggerName, mockLogManager);

			expect(testLogger.name).toBe(createLoggerName(loggerName));

			// Verify name property is defined and has correct type
			expect(typeof testLogger.name).toBe("string");

			// The readonly modifier is enforced at compile time by TypeScript
			// This ensures the property cannot be reassigned in TypeScript code
		});

		it("should create logger with different names as separate instances", () => {
			const logger1 = new Logger("logger1", mockLogManager);
			const logger2 = new Logger("logger2", mockLogManager);

			expect(logger1.name).toBe(createLoggerName("logger1"));
			expect(logger2.name).toBe(createLoggerName("logger2"));
			expect(logger1.name).not.toBe(logger2.name);
		});

		it("should handle logger creation without LogManager", () => {
			// Test graceful degradation when no LogManager is provided
			expect(() => {
				const testLogger = new Logger("test.logger");
				expect(testLogger.name).toBe(createLoggerName("test.logger"));
			}).not.toThrow();
		});
	});

	describe("log level methods", () => {
		it("should have trace method that accepts message and args", () => {
			const message = "trace message";
			const args = ["arg1", { key: "value" }, 123];

			logger.trace(message, ...args);

			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.TRACE,
			);
			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.TRACE,
				loggerName: logger.name,
				message,
				args,
			});
		});

		it("should have debug method that accepts message and args", () => {
			const message = "debug message";
			const args = ["arg1", { key: "value" }];

			logger.debug(message, ...args);

			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.DEBUG,
			);
			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.DEBUG,
				loggerName: logger.name,
				message,
				args,
			});
		});

		it("should have info method that accepts message and args", () => {
			const message = "info message";
			const args = [42];

			logger.info(message, ...args);

			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.INFO,
			);
			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.INFO,
				loggerName: logger.name,
				message,
				args,
			});
		});

		it("should have warn method that accepts message and args", () => {
			const message = "warn message";
			const args = [];

			logger.warn(message, ...args);

			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.WARN,
			);
			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.WARN,
				loggerName: logger.name,
				message,
				args,
			});
		});

		it("should have error method that accepts message and args", () => {
			const message = "error message";
			const args = [new Error("test error")];

			logger.error(message, ...args);

			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.ERROR,
			);
			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.ERROR,
				loggerName: logger.name,
				message,
				args,
			});
		});

		it("should have fatal method that accepts message and args", () => {
			const message = "fatal message";
			const args = ["critical", "failure"];

			logger.fatal(message, ...args);

			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.FATAL,
			);
			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.FATAL,
				loggerName: logger.name,
				message,
				args,
			});
		});

		it("should handle empty args array correctly", () => {
			const message = "message without args";

			logger.info(message);

			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.INFO,
				loggerName: logger.name,
				message,
				args: [],
			});
		});

		it("should handle various argument types", () => {
			const message = "message with various args";
			const args = [
				"string",
				123,
				true,
				null,
				undefined,
				{ object: "value" },
				["array", "items"],
				new Error("error object"),
			];

			logger.warn(message, ...args);

			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.WARN,
				loggerName: logger.name,
				message,
				args,
			});
		});
	});

	describe("log method delegation", () => {
		it("should create LogEntry with correct timestamp", () => {
			const beforeTime = new Date();
			logger.info("test message");
			const afterTime = new Date();

			const logEntry = mockLogManager.processLog.mock.calls[0][0] as LogEntry;
			expect(logEntry.timestamp).toBeInstanceOf(Date);
			expect(logEntry.timestamp.getTime()).toBeGreaterThanOrEqual(
				beforeTime.getTime(),
			);
			expect(logEntry.timestamp.getTime()).toBeLessThanOrEqual(
				afterTime.getTime(),
			);
		});

		it("should create LogEntry with correct logger name", () => {
			const loggerName = "specific.logger.name";
			const specificMockLogManager = {
				processLog: vi.fn(),
				isLevelEnabled: vi.fn().mockReturnValue(true),
			};
			const specificLogger = new Logger(loggerName, specificMockLogManager);

			specificLogger.info("test message");

			const logEntry = specificMockLogManager.processLog.mock
				.calls[0][0] as LogEntry;
			expect(logEntry.loggerName).toBe(createLoggerName(loggerName));
		});

		it("should pass message and args correctly to LogEntry", () => {
			const message = "test message with args";
			const args = ["arg1", { complex: "object" }, 42, true];

			logger.debug(message, ...args);

			const logEntry = mockLogManager.processLog.mock.calls[0][0] as LogEntry;
			expect(logEntry.message).toBe(message);
			expect(logEntry.args).toEqual(args);
		});

		it("should delegate to LogManager for each log level", () => {
			const message = "test message";
			const args = ["arg1", "arg2"];

			// Test each log level method delegates correctly
			logger.trace(message, ...args);
			logger.debug(message, ...args);
			logger.info(message, ...args);
			logger.warn(message, ...args);
			logger.error(message, ...args);
			logger.fatal(message, ...args);

			// Verify LogManager.processLog was called 6 times (once for each level)
			expect(mockLogManager.processLog).toHaveBeenCalledTimes(6);

			// Verify each call had the correct level
			const calls = mockLogManager.processLog.mock.calls;
			expect(calls[0][0].level).toBe(LogLevel.TRACE);
			expect(calls[1][0].level).toBe(LogLevel.DEBUG);
			expect(calls[2][0].level).toBe(LogLevel.INFO);
			expect(calls[3][0].level).toBe(LogLevel.WARN);
			expect(calls[4][0].level).toBe(LogLevel.ERROR);
			expect(calls[5][0].level).toBe(LogLevel.FATAL);
		});

		it("should use public log method for programmatic logging", () => {
			const message = "programmatic log message";
			const args = ["test", "args"];

			logger.log(LogLevel.INFO, message, args);

			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.INFO,
			);
			expect(mockLogManager.processLog).toHaveBeenCalledWith({
				timestamp: expect.any(Date),
				level: LogLevel.INFO,
				loggerName: logger.name,
				message,
				args,
			});
		});
	});

	describe("performance optimization and level checking", () => {
		it("should check level before creating LogEntry", () => {
			// Mock level as disabled
			mockLogManager.isLevelEnabled.mockReturnValue(false);

			logger.info("test message", "arg1", "arg2");

			// Should check level but not process log
			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.INFO,
			);
			expect(mockLogManager.processLog).not.toHaveBeenCalled();
		});

		it("should skip log processing when level is disabled", () => {
			// Test all levels being disabled
			mockLogManager.isLevelEnabled.mockReturnValue(false);

			logger.trace("trace message");
			logger.debug("debug message");
			logger.info("info message");
			logger.warn("warn message");
			logger.error("error message");
			logger.fatal("fatal message");

			// All levels should be checked
			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledTimes(6);
			// But no logs should be processed
			expect(mockLogManager.processLog).not.toHaveBeenCalled();
		});

		it("should process log when level is enabled", () => {
			// Mock level as enabled
			mockLogManager.isLevelEnabled.mockReturnValue(true);

			logger.info("test message", "arg1");

			expect(mockLogManager.isLevelEnabled).toHaveBeenCalledWith(
				logger.name,
				LogLevel.INFO,
			);
			expect(mockLogManager.processLog).toHaveBeenCalledTimes(1);
		});
	});

	describe("error handling and graceful degradation", () => {
		it("should not throw error when LogManager is not available", () => {
			// Create a new logger without LogManager to test real behavior
			const realLogger = new Logger("real.logger");

			// This should not throw an error even though LogManager is not available
			expect(() => {
				realLogger.info("test message");
				realLogger.error("error message", new Error("test"));
				realLogger.debug("debug message", { data: "value" });
			}).not.toThrow();
		});

		it("should handle LogManager.isLevelEnabled throwing error", () => {
			mockLogManager.isLevelEnabled.mockImplementation(() => {
				throw new Error("Level check failed");
			});

			expect(() => {
				logger.info("test message");
			}).not.toThrow();

			// Should not call processLog when level check fails
			expect(mockLogManager.processLog).not.toHaveBeenCalled();
		});

		it("should handle LogManager.processLog throwing error", () => {
			mockLogManager.processLog.mockImplementation(() => {
				throw new Error("Process log failed");
			});

			expect(() => {
				logger.info("test message");
			}).not.toThrow();

			// Should still call processLog even if it throws
			expect(mockLogManager.processLog).toHaveBeenCalled();
		});

		it("should handle invalid log level gracefully", () => {
			expect(() => {
				logger.log(null as any, "test message", []);
				logger.log(undefined as any, "test message", []);
				logger.log("invalid" as any, "test message", []);
			}).not.toThrow();

			// Should not call LogManager methods for invalid levels
			expect(mockLogManager.isLevelEnabled).not.toHaveBeenCalled();
			expect(mockLogManager.processLog).not.toHaveBeenCalled();
		});

		it("should handle invalid message gracefully", () => {
			expect(() => {
				logger.info(null as any);
				logger.info(undefined as any);
			}).not.toThrow();

			// Should not call LogManager methods for invalid messages
			expect(mockLogManager.isLevelEnabled).not.toHaveBeenCalled();
			expect(mockLogManager.processLog).not.toHaveBeenCalled();
		});

		it("should convert non-string messages to strings", () => {
			const numberMessage = 123;
			const objectMessage = { key: "value" };

			logger.info(numberMessage as any);
			logger.info(objectMessage as any);

			expect(mockLogManager.processLog).toHaveBeenCalledWith(
				expect.objectContaining({
					message: "123",
				}),
			);

			expect(mockLogManager.processLog).toHaveBeenCalledWith(
				expect.objectContaining({
					message: "[object Object]",
				}),
			);
		});

		it("should handle non-array args gracefully", () => {
			// Test with non-array args (this shouldn't happen in normal usage but tests robustness)
			const loggerWithMockedInternal = new Logger("test", mockLogManager);

			// Access the private method for testing
			(loggerWithMockedInternal as any).logInternal(
				LogLevel.INFO,
				"test",
				"not-an-array",
			);

			expect(mockLogManager.processLog).toHaveBeenCalledWith(
				expect.objectContaining({
					args: [],
				}),
			);
		});
	});

	describe("logger name immutability", () => {
		it("should maintain immutable logger name across all operations", () => {
			const originalName = logger.name;

			// Perform various logging operations
			logger.trace("trace");
			logger.debug("debug");
			logger.info("info");
			logger.warn("warn");
			logger.error("error");
			logger.fatal("fatal");

			// Name should remain unchanged
			expect(logger.name).toBe(originalName);
			expect(logger.name).toBe(createLoggerName("test.logger"));
		});

		it("should use logger name in all log entries", () => {
			const loggerName = "hierarchical.logger.name";
			const namedLogger = new Logger(loggerName, mockLogManager);

			namedLogger.info("test message");

			const logEntry = mockLogManager.processLog.mock.calls[0][0] as LogEntry;
			expect(logEntry.loggerName).toBe(createLoggerName(loggerName));
		});
	});
});
