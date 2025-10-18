/** biome-ignore-all lint/suspicious/noExplicitAny: Allow 'any' usage for flexible test scenarios */
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { LogEntry, LoggerConfiguration, Transport } from "../src";
import { Logger, type LoggerName, LogLevel, LogManager } from "../src";

// Mock Logger class to control its behavior in LogManager tests
vi.mock("../src/Logger.ts", async () => {
	const actual = await vi.importActual("../src/Logger.ts");
	const MockLogger = vi.fn(function (this: any, name: string) {
		// Call the original Logger constructor to ensure proper initialization
		Object.assign(this, new (actual as any).Logger(name));
		// Override methods with vi.fn() for spying
		this.trace = vi.fn(this.trace);
		this.debug = vi.fn(this.debug);
		this.info = vi.fn(this.info);
		this.warn = vi.fn(this.warn);
		this.error = vi.fn(this.error);
		this.fatal = vi.fn(this.fatal);
		this.log = vi.fn(this.log);
	});
	// Set the prototype to the actual Logger prototype for `instanceof` checks
	MockLogger.prototype = (actual as any).Logger.prototype;
	return { Logger: MockLogger };
});

const cacheLogger = "test.cache" as LoggerName;
const uiComponents = "ui.components" as LoggerName;
const api = "api" as LoggerName;
const anyLogger = "any.logger" as LoggerName;
const apiAuth = "api.auth" as LoggerName;
const jwt = "api.auth.jwt" as LoggerName;
const button = "ui.components.button" as LoggerName;
const testReset = "test.reset" as LoggerName;
describe("LogManager", () => {
	let mockTransport: Transport;

	beforeEach(() => {
		// Reset LogManager state before each test
		LogManager.reset();
		// Clear Logger mock calls
		(Logger as any).mockClear();
		// Ensure enhanced error handling is off by default for most tests
		delete (globalThis as any).__LOGGER_ENHANCED_ERROR_HANDLING__;

		mockTransport = {
			log: vi.fn(),
		};
	});

	afterEach(() => {
		// Clean up global state after each test
		LogManager.reset();
		delete (globalThis as any).__LOGGER_ENHANCED_ERROR_HANDLING__;
	});

	describe("configure", () => {
		it("should configure the LogManager with valid configuration", () => {
			const config: LoggerConfiguration = {
				level: LogLevel.INFO,
				transports: [mockTransport],
				levels: { api: LogLevel.DEBUG },
			};
			LogManager.configure(config);
			expect(LogManager.getConfiguration()).toEqual(config);
			expect(LogManager.isConfigured()).toBe(true);
		});

		it("should clear level cache when configured", () => {
			// Configure with a specific logger level to populate the cache
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
				levels: { "test.cache": LogLevel.TRACE },
			});
			// Verify that TRACE is enabled for 'test.cache'
			expect(LogManager.isLevelEnabled(cacheLogger, LogLevel.TRACE)).toBe(true);

			// Reconfigure with a new configuration that does NOT have 'test.cache' explicitly set
			// and has a root level of INFO (which is higher than TRACE)
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
			});
			// After reconfiguration, the cache should be cleared, so 'test.cache' should now inherit
			// from the root INFO level, meaning TRACE is no longer enabled.
			expect(LogManager.isLevelEnabled(cacheLogger, LogLevel.TRACE)).toBe(
				false,
			);
		});

		it("should register LogManager globally", () => {
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
			});
			expect((globalThis as Record<string, unknown>).__LOGGER_MANAGER__).toBe(
				LogManager,
			);
		});

		describe("enhanced error handling for configure", () => {
			beforeEach(() => {
				(globalThis as any).__LOGGER_ENHANCED_ERROR_HANDLING__ = true;
			});

			it("should set config to null and clear cache for null config", () => {
				LogManager.configure(null as any);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});

			it("should set config to null and clear cache for undefined config", () => {
				LogManager.configure(undefined as any);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});

			it("should set config to null and clear cache for invalid root level (not a number)", () => {
				const config: LoggerConfiguration = {
					level: "invalid" as any,
					transports: [mockTransport],
				};
				LogManager.configure(config);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});

			it("should set config to null and clear cache for invalid root level (negative number)", () => {
				const config: LoggerConfiguration = {
					level: -1 as LogLevel,
					transports: [mockTransport],
				};
				LogManager.configure(config);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});

			it("should set config to null and clear cache for invalid transports (not array)", () => {
				const config: LoggerConfiguration = {
					level: LogLevel.INFO,
					transports: "invalid" as any,
				};
				LogManager.configure(config);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});

			it("should set config to null and clear cache for empty transports array", () => {
				const config: LoggerConfiguration = {
					level: LogLevel.INFO,
					transports: [],
				};
				LogManager.configure(config);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});

			it("should set config to null and clear cache for transports with invalid log method", () => {
				const config: LoggerConfiguration = {
					level: LogLevel.INFO,
					transports: [{ log: "not a function" } as any],
				};
				LogManager.configure(config);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});

			it("should set config to null and clear cache for invalid levels object (not object)", () => {
				const config: LoggerConfiguration = {
					level: LogLevel.INFO,
					transports: [mockTransport],
					levels: [] as any,
				};
				LogManager.configure(config);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});

			it("should set config to null and clear cache for invalid levels object (invalid level value)", () => {
				const config: LoggerConfiguration = {
					level: LogLevel.INFO,
					transports: [mockTransport],
					levels: { api: "invalid" as any },
				};
				LogManager.configure(config);
				expect(LogManager.getConfiguration()).toBeNull();
				expect(LogManager.isConfigured()).toBe(false);
			});
		});
	});

	describe("getLogger", () => {
		it("should return a new Logger instance if not cached", () => {
			const loggerName = "new.logger";
			const logger = LogManager.getLogger(loggerName);
			expect(logger).toBeInstanceOf(Logger);
			expect(Logger).toHaveBeenCalledWith(loggerName);
			// Verify that the logger is now in the internal cache by trying to get it again
			const retrievedLogger = LogManager.getLogger(loggerName);
			expect(retrievedLogger).toBe(logger);
		});

		it("should return an existing Logger instance if cached", () => {
			const loggerName = "cached.logger";
			const firstLogger = LogManager.getLogger(loggerName);
			const secondLogger = LogManager.getLogger(loggerName);
			expect(secondLogger).toBe(firstLogger);
			expect(Logger).toHaveBeenCalledTimes(1); // Logger constructor should only be called once
		});

		describe("enhanced error handling for getLogger", () => {
			beforeEach(() => {
				(globalThis as any).__LOGGER_ENHANCED_ERROR_HANDLING__ = true;
			});

			it('should return logger named "unknown" for null name', () => {
				const logger = LogManager.getLogger(null as any);
				expect(logger).toBeInstanceOf(Logger);
				expect(Logger).toHaveBeenCalledWith("unknown");
				expect(logger.name).toBe("unknown");
			});

			it('should return logger named "unknown" for undefined name', () => {
				const logger = LogManager.getLogger(undefined as any);
				expect(logger).toBeInstanceOf(Logger);
				expect(Logger).toHaveBeenCalledWith("unknown");
				expect(logger.name).toBe("unknown");
			});

			it('should return logger named "unknown" for empty string name', () => {
				const logger = LogManager.getLogger("");
				expect(logger).toBeInstanceOf(Logger);
				expect(Logger).toHaveBeenCalledWith("unknown");
				expect(logger.name).toBe("unknown");
			});

			it('should return logger named "unknown" for whitespace string name', () => {
				const logger = LogManager.getLogger("   ");
				expect(logger).toBeInstanceOf(Logger);
				expect(Logger).toHaveBeenCalledWith("unknown");
				expect(logger.name).toBe("unknown");
			});

			it("should convert non-string name to string and trim", () => {
				(Logger as any).mockClear(); // Clear calls from previous tests
				const logger = LogManager.getLogger(123 as any);
				expect(logger).toBeInstanceOf(Logger);
				expect(Logger).toHaveBeenCalledWith("123");
				expect(logger.name).toBe("123");
			});
		});
	});

	describe("isLevelEnabled", () => {
		beforeEach(() => {
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
				levels: {
					api: LogLevel.DEBUG,
					"api.auth": LogLevel.TRACE,
					"ui.components": LogLevel.WARN,
				},
			});
		});

		it("should return false if LogManager is not configured", () => {
			LogManager.reset();
			expect(
				LogManager.isLevelEnabled("test" as LoggerName, LogLevel.INFO),
			).toBe(false);
		});

		it("should return true for levels at or above the root level", () => {
			expect(LogManager.isLevelEnabled(anyLogger, LogLevel.INFO)).toBe(true);
			expect(LogManager.isLevelEnabled(anyLogger, LogLevel.WARN)).toBe(true);
			expect(LogManager.isLevelEnabled(anyLogger, LogLevel.ERROR)).toBe(true);
			expect(LogManager.isLevelEnabled(anyLogger, LogLevel.FATAL)).toBe(true);
		});

		it("should return false for levels below the root level", () => {
			expect(LogManager.isLevelEnabled(anyLogger, LogLevel.TRACE)).toBe(false);
			expect(LogManager.isLevelEnabled(anyLogger, LogLevel.DEBUG)).toBe(false);
		});

		it("should respect specific logger levels", () => {
			expect(LogManager.isLevelEnabled(api, LogLevel.DEBUG)).toBe(true);
			expect(LogManager.isLevelEnabled(api, LogLevel.TRACE)).toBe(false); // Below api's DEBUG
			expect(LogManager.isLevelEnabled(uiComponents, LogLevel.WARN)).toBe(true);
			expect(LogManager.isLevelEnabled(uiComponents, LogLevel.INFO)).toBe(
				false,
			); // Below ui.components' WARN
		});

		it("should apply hierarchical inheritance", () => {
			// api.auth should inherit from api (TRACE)
			expect(LogManager.isLevelEnabled(apiAuth, LogLevel.TRACE)).toBe(true);
			expect(LogManager.isLevelEnabled(jwt, LogLevel.TRACE)).toBe(true); // Inherits from api.auth

			// ui.components.button should inherit from ui.components (WARN)
			expect(LogManager.isLevelEnabled(button, LogLevel.WARN)).toBe(true);
			expect(LogManager.isLevelEnabled(button, LogLevel.INFO)).toBe(false);
		});

		it("should cache effective levels for performance", () => {
			// Configure LogManager
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
				levels: {
					"api.auth.jwt": LogLevel.TRACE,
				},
			});

			// First call should calculate and cache the level
			expect(LogManager.isLevelEnabled(jwt, LogLevel.TRACE)).toBe(true);
			// Since we can't directly spy on an internal function, we rely on the fact
			// that subsequent calls for the same logger name and level should be fast
			// and not change the outcome, implying caching is working.
			// This test primarily ensures that the public API behaves as expected
			// when caching is implicitly in place.
			expect(LogManager.isLevelEnabled(jwt, LogLevel.TRACE)).toBe(true);
		});

		describe("enhanced error handling for isLevelEnabled", () => {
			beforeEach(() => {
				(globalThis as any).__LOGGER_ENHANCED_ERROR_HANDLING__ = true;
			});

			it("should return false for null loggerName", () => {
				expect(LogManager.isLevelEnabled(null as any, LogLevel.INFO)).toBe(
					false,
				);
			});

			it("should return false for undefined loggerName", () => {
				expect(LogManager.isLevelEnabled(undefined as any, LogLevel.INFO)).toBe(
					false,
				);
			});

			it("should return false for empty string loggerName", () => {
				expect(LogManager.isLevelEnabled("" as LoggerName, LogLevel.INFO)).toBe(
					false,
				);
			});

			it("should return false for whitespace string loggerName", () => {
				expect(
					LogManager.isLevelEnabled("   " as LoggerName, LogLevel.INFO),
				).toBe(false);
			});

			it("should return false for non-string loggerName", () => {
				expect(LogManager.isLevelEnabled(123 as any, LogLevel.INFO)).toBe(
					false,
				);
			});

			it("should return false for invalid level (not a number)", () => {
				expect(
					LogManager.isLevelEnabled("test" as LoggerName, "invalid" as any),
				).toBe(false);
			});

			it("should return false for invalid level (negative number)", () => {
				expect(
					LogManager.isLevelEnabled("test" as LoggerName, -1 as LogLevel),
				).toBe(false);
			});
		});
	});

	describe("processLog", () => {
		const mockLogEntry: LogEntry = {
			timestamp: new Date(),
			level: LogLevel.INFO,
			loggerName: "test.logger" as LoggerName,
			message: "Test message",
			args: [],
		};

		it("should do nothing if LogManager is not configured", () => {
			LogManager.reset();
			LogManager.processLog(mockLogEntry);
			expect(mockTransport.log).not.toHaveBeenCalled();
		});

		it("should do nothing if log level is disabled", () => {
			LogManager.configure({
				level: LogLevel.WARN, // INFO is below WARN
				transports: [mockTransport],
			});
			LogManager.processLog(mockLogEntry);
			expect(mockTransport.log).not.toHaveBeenCalled();
		});

		it("should call transport.log if log level is enabled", () => {
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
			});
			LogManager.processLog(mockLogEntry);
			expect(mockTransport.log).toHaveBeenCalledWith(mockLogEntry);
		});

		it("should call all configured transports", () => {
			const anotherMockTransport = { log: vi.fn() };
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport, anotherMockTransport],
			});
			LogManager.processLog(mockLogEntry);
			expect(mockTransport.log).toHaveBeenCalledWith(mockLogEntry);
			expect(anotherMockTransport.log).toHaveBeenCalledWith(mockLogEntry);
		});

		it("should handle transport errors gracefully (error isolation)", () => {
			const failingTransport = {
				log: vi.fn(() => {
					throw new Error("Transport failed");
				}),
			};
			const anotherMockTransport = { log: vi.fn() };
			const consoleErrorSpy = vi
				.spyOn(console, "error")
				.mockImplementation(() => {});

			LogManager.configure({
				level: LogLevel.INFO,
				transports: [failingTransport, anotherMockTransport],
			});
			LogManager.processLog(mockLogEntry);

			expect(failingTransport.log).toHaveBeenCalledWith(mockLogEntry);
			expect(anotherMockTransport.log).toHaveBeenCalledWith(mockLogEntry); // Other transport should still be called
			expect(consoleErrorSpy).toHaveBeenCalledWith(
				"Logger transport error:",
				expect.any(Error),
			);
			consoleErrorSpy.mockRestore();
		});

		describe("enhanced error handling for processLog", () => {
			beforeEach(() => {
				(globalThis as any).__LOGGER_ENHANCED_ERROR_HANDLING__ = true;
				LogManager.configure({
					level: LogLevel.INFO,
					transports: [mockTransport],
				});
			});

			it("should return for null entry", () => {
				LogManager.processLog(null as any);
				expect(mockTransport.log).not.toHaveBeenCalled();
			});

			it("should return for undefined entry", () => {
				LogManager.processLog(undefined as any);
				expect(mockTransport.log).not.toHaveBeenCalled();
			});

			it("should return for non-object entry", () => {
				LogManager.processLog("invalid" as any);
				expect(mockTransport.log).not.toHaveBeenCalled();
			});

			it("should return for entry missing loggerName", () => {
				const invalidEntry = { ...mockLogEntry, loggerName: undefined } as any;
				delete invalidEntry.loggerName;
				LogManager.processLog(invalidEntry);
				expect(mockTransport.log).not.toHaveBeenCalled();
			});

			it("should return for entry missing level", () => {
				const invalidEntry = { ...mockLogEntry, level: undefined } as any;
				delete invalidEntry.level;
				LogManager.processLog(invalidEntry);
				expect(mockTransport.log).not.toHaveBeenCalled();
			});
		});
	});

	describe("clearCaches", () => {
		it("should clear the level cache", () => {
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
				levels: { "test.cache": LogLevel.DEBUG },
			});
			// Populate the cache
			LogManager.isLevelEnabled(cacheLogger, LogLevel.DEBUG);
			// Verify that the level is enabled before clearing cache
			expect(LogManager.isLevelEnabled(cacheLogger, LogLevel.DEBUG)).toBe(true);

			LogManager.clearCaches();

			// After clearing cache, the effective level should revert to the root level (INFO),
			// so DEBUG for 'test.cache' should no longer be enabled.
			expect(LogManager.isLevelEnabled(cacheLogger, LogLevel.DEBUG)).toBe(
				false,
			);
		});
	});

	describe("getConfiguration", () => {
		it("should return the current configuration", () => {
			const config: LoggerConfiguration = {
				level: LogLevel.INFO,
				transports: [mockTransport],
			};
			LogManager.configure(config);
			expect(LogManager.getConfiguration()).toEqual(config);
		});

		it("should return null if not configured", () => {
			LogManager.reset();
			expect(LogManager.getConfiguration()).toBeNull();
		});
	});

	describe("isConfigured", () => {
		it("should return true if configured", () => {
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
			});
			expect(LogManager.isConfigured()).toBe(true);
		});

		it("should return false if not configured", () => {
			LogManager.reset();
			expect(LogManager.isConfigured()).toBe(false);
		});
	});

	describe("reset", () => {
		it("should reset all LogManager state", () => {
			LogManager.configure({
				level: LogLevel.INFO,
				transports: [mockTransport],
				levels: { "test.reset": LogLevel.DEBUG },
			});
			// Populate loggers and cache
			const logger = LogManager.getLogger("test.reset");
			LogManager.isLevelEnabled(testReset, LogLevel.DEBUG);

			expect(LogManager.isConfigured()).toBe(true);
			// Verify logger instance is returned (implies it's in the internal map)
			expect(LogManager.getLogger("test.reset")).toBe(logger);
			// Verify level is enabled (implies cache is populated)
			expect(LogManager.isLevelEnabled(testReset, LogLevel.DEBUG)).toBe(true);
			expect((globalThis as Record<string, unknown>).__LOGGER_MANAGER__).toBe(
				LogManager,
			);

			LogManager.reset();

			expect(LogManager.isConfigured()).toBe(false);
			// After reset, getting the same logger name should return a *new* instance
			expect(LogManager.getLogger("test.reset")).not.toBe(logger);
			// After reset, the level should revert to default (INFO), so DEBUG should not be enabled
			expect(LogManager.isLevelEnabled(testReset, LogLevel.DEBUG)).toBe(false);
			expect(
				(globalThis as Record<string, unknown>).__LOGGER_MANAGER__,
			).toBeUndefined();
		});
	});
});
