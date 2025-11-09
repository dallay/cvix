import { describe, expect, it } from "vitest";
import { authRoutes } from "../index.ts";

describe("index", () => {
	it("includes profile and settings routes", () => {
		const paths = authRoutes.map((r) => r.path);
		expect(paths).toContain("/profile");
		expect(paths).toContain("/settings");
	});
});
