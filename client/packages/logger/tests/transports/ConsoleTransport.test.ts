import {
	beforeEach,
	describe,
	expect,
	it,
	type MockInstance,
	vi,
} from "vitest";
import type { LogEntry } from "../../src";
import { ConsoleTransport, type LoggerName, LogLevel } from "../../src";

// biome-ignore lint/suspicious/noExplicitAny: In this context, globalThis is used for browser compatibility
let consoleSpy: Record<string, MockInstance<any>>;
const testLogger = "test.logger" as LoggerName;
const noArgs = "no.args" as LoggerName;
const complexArgs = "complex.args" as LoggerName;
const unknownLevel = "unknown.level" as LoggerName;
describe("ConsoleTransport", () => {
	let transport: ConsoleTransport;

	beforeEach(() => {
		transport = new ConsoleTransport();
		// Mock isBrowser to false to force Node.js logging path
		Object.defineProperty(transport, "isBrowser", {
			value: false,
			writable: false,
			configurable: true,
		});

		consoleSpy = {
			trace: vi.spyOn(console, "trace").mockImplementation(() => {}),
			debug: vi.spyOn(console, "debug").mockImplementation(() => {}),
			info: vi.spyOn(console, "info").mockImplementation(() => {}),
			warn: vi.spyOn(console, "warn").mockImplementation(() => {}),
			error: vi.spyOn(console, "error").mockImplementation(() => {}),
		};
	});

	it("should log TRACE level messages to console.trace", () => {
		const timestamp = new Date();
		const entry: LogEntry = {
			timestamp: timestamp,
			level: LogLevel.TRACE,
			loggerName: testLogger,
			message: "Trace message",
			args: ["arg1", { key: "value" }],
		};
		transport.log(entry);
		const expectedMessage = `\x1b[90m${timestamp.toISOString()}\x1b[0m \x1b[37m[TRACE]\x1b[0m \x1b[94mtest.logger\x1b[0m Trace message`;
		expect(consoleSpy.debug).toHaveBeenCalledWith(
			expectedMessage,
			"arg1",
			JSON.stringify({ key: "value" }, null, 2),
		);
	});

	it("should log DEBUG level messages to console.debug", () => {
		const timestamp = new Date();
		const entry: LogEntry = {
			timestamp: timestamp,
			level: LogLevel.DEBUG,
			loggerName: testLogger,
			message: "Debug message",
			args: [123],
		};
		transport.log(entry);
		const expectedMessage = `\x1b[90m${timestamp.toISOString()}\x1b[0m \x1b[90m[DEBUG]\x1b[0m \x1b[94mtest.logger\x1b[0m Debug message`;
		expect(consoleSpy.debug).toHaveBeenCalledWith(expectedMessage, 123);
	});

	it("should log INFO level messages to console.info", () => {
		const timestamp = new Date();
		const entry: LogEntry = {
			timestamp: timestamp,
			level: LogLevel.INFO,
			loggerName: testLogger,
			message: "Info message",
			args: [],
		};
		transport.log(entry);
		const expectedMessage = `\x1b[90m${timestamp.toISOString()}\x1b[0m \x1b[94m[INFO]\x1b[0m \x1b[94mtest.logger\x1b[0m Info message`;
		expect(consoleSpy.info).toHaveBeenCalledWith(expectedMessage);
	});

	it("should log WARN level messages to console.warn", () => {
		const timestamp = new Date();
		const entry: LogEntry = {
			timestamp: timestamp,
			level: LogLevel.WARN,
			loggerName: testLogger,
			message: "Warn message",
			args: ["warning"],
		};
		transport.log(entry);
		const expectedMessage = `\x1b[90m${timestamp.toISOString()}\x1b[0m \x1b[93m[WARN]\x1b[0m \x1b[94mtest.logger\x1b[0m Warn message`;
		expect(consoleSpy.warn).toHaveBeenCalledWith(expectedMessage, "warning");
	});

	it("should log ERROR level messages to console.error", () => {
		const timestamp = new Date();
		const error = new Error("Something went wrong");
		const entry: LogEntry = {
			timestamp: timestamp,
			level: LogLevel.ERROR,
			loggerName: testLogger,
			message: "Error message",
			args: [error, { context: "data" }],
		};
		transport.log(entry);
		const expectedMessage = `\x1b[90m${timestamp.toISOString()}\x1b[0m \x1b[91m[ERROR]\x1b[0m \x1b[94mtest.logger\x1b[0m Error message`;
		expect(consoleSpy.error).toHaveBeenCalledWith(
			expectedMessage,
			"{}",
			JSON.stringify({ context: "data" }, null, 2),
		);
	});

	it("should log FATAL level messages to console.error", () => {
		const timestamp = new Date();
		const entry: LogEntry = {
			timestamp: timestamp,
			level: LogLevel.FATAL,
			loggerName: testLogger,
			message: "Fatal message",
			args: ["critical failure"],
		};
		transport.log(entry);
		const expectedMessage = `\x1b[90m${timestamp.toISOString()}\x1b[0m \x1b[97m\x1b[41m[FATAL]\x1b[0m \x1b[94mtest.logger\x1b[0m Fatal message`;
		expect(consoleSpy.error).toHaveBeenCalledWith(
			expectedMessage,
			"critical failure",
		);
	});

	it("should handle log entries with no arguments", () => {
		const timestamp = new Date();
		const entry: LogEntry = {
			timestamp: timestamp,
			level: LogLevel.INFO,
			loggerName: noArgs,
			message: "Message without args",
			args: [],
		};
		transport.log(entry);
		const expectedMessage = `\x1b[90m${timestamp.toISOString()}\x1b[0m \x1b[94m[INFO]\x1b[0m \x1b[94mno.args\x1b[0m Message without args`;
		expect(consoleSpy.info).toHaveBeenCalledWith(expectedMessage);
	});

	it("should handle log entries with complex arguments", () => {
		const timestamp = new Date();
		const complexObject = { a: 1, b: [2, 3], c: { d: "e" } };
		const entry: LogEntry = {
			timestamp: timestamp,
			level: LogLevel.DEBUG,
			loggerName: complexArgs,
			message: "Complex message",
			args: [complexObject, null, undefined, 123, "string"],
		};
		transport.log(entry);
		const expectedMessage = `\x1b[90m${timestamp.toISOString()}\x1b[0m \x1b[90m[DEBUG]\x1b[0m \x1b[94mcomplex.args\x1b[0m Complex message`;
		expect(consoleSpy.debug).toHaveBeenCalledWith(
			expectedMessage,
			JSON.stringify(complexObject, null, 2),
			null,
			undefined,
			123,
			"string",
		);
	});

	it("should not log for unknown log levels", () => {
		const entry: LogEntry = {
			timestamp: new Date(),
			level: 999 as LogLevel, // Unknown level
			loggerName: unknownLevel,
			message: "Unknown level message",
			args: [],
		};
		transport.log(entry);
		expect(consoleSpy.trace).not.toHaveBeenCalled();
		expect(consoleSpy.debug).not.toHaveBeenCalled();
		expect(consoleSpy.info).not.toHaveBeenCalled();
		expect(consoleSpy.warn).not.toHaveBeenCalled();
		expect(consoleSpy.error).not.toHaveBeenCalled();
	});
});
