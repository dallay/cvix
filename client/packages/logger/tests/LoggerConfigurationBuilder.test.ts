import { describe, expect, it } from "vitest";
import { ConsoleTransport, LoggerConfigurationBuilder, LogLevel } from "../src";

describe("LoggerConfigurationBuilder", () => {
	it("should start with development defaults", () => {
		const config = LoggerConfigurationBuilder.development()
			.withTransport(new ConsoleTransport())
			.build();
		expect(config.level).toBe(LogLevel.DEBUG);
	});

	it("should start with production defaults", () => {
		const config = LoggerConfigurationBuilder.production()
			.withTransport(new ConsoleTransport())
			.build();
		expect(config.level).toBe(LogLevel.INFO);
	});

	it("should set a custom default log level", () => {
		const config = new LoggerConfigurationBuilder()
			.withTransport(new ConsoleTransport())
			.build();
		expect(config.level).toBe(LogLevel.INFO);
	});

	it("should add a transport", () => {
		const transport = new ConsoleTransport();
		const config = LoggerConfigurationBuilder.development()
			.withTransport(transport)
			.build();
		expect(config.transports).toContain(transport);
	});

	it("should set logger-specific levels", () => {
		const config = LoggerConfigurationBuilder.development()
			.withLoggerLevel("api", LogLevel.TRACE)
			.withLoggerLevel("ui", LogLevel.ERROR)
			.withTransport(new ConsoleTransport())
			.build();
		expect(config.levels).toEqual({
			api: LogLevel.TRACE,
			ui: LogLevel.ERROR,
		});
	});

	it("should set multiple logger-specific levels at once", () => {
		const levels = {
			"api.database": LogLevel.TRACE,
			"ui.animation": LogLevel.WARN,
		};
		const config = LoggerConfigurationBuilder.development()
			.withLoggerLevels(levels)
			.withTransport(new ConsoleTransport())
			.build();
		expect(config.levels).toEqual(levels);
	});

	it("should overwrite existing logger levels when setting multiple", () => {
		const initialLevels = { "api.database": LogLevel.TRACE };
		const newLevels = { "ui.animation": LogLevel.WARN };
		const config = LoggerConfigurationBuilder.development()
			.withLoggerLevels(initialLevels)
			.withLoggerLevels(newLevels)
			.withTransport(new ConsoleTransport())
			.build();
		expect(config.levels).toEqual({ ...initialLevels, ...newLevels });
	});

	it("should chain methods correctly", () => {
		const transport = new ConsoleTransport();
		const config = LoggerConfigurationBuilder.production()
			.withLoggerLevel("api", LogLevel.DEBUG)
			.withTransport(transport)
			.build();

		expect(config.level).toBe(LogLevel.INFO);
		expect(config.levels).toEqual({ api: LogLevel.DEBUG });
		expect(config.transports).toContain(transport);
	});
});
