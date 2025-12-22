import { describe, expect, it } from "vitest";
import type { User } from "../models/auth.model.ts";
import { isValidUser } from "./user.validator.ts";

describe("user.validator", () => {
	describe("isValidUser", () => {
		it("should return true for a valid user with all required fields", () => {
			const validUser: User = {
				id: "123e4567-e89b-12d3-a456-426614174000",
				username: "testuser",
				email: "test@example.com",
				firstName: "Test",
				lastName: "User",
				roles: ["USER"],
			};

			expect(isValidUser(validUser)).toBe(true);
		});

		it("should return true for a valid user with minimal fields", () => {
			const minimalUser: User = {
				id: "abc123",
				username: "user",
				email: "user@test.com",
				firstName: null,
				lastName: null,
				roles: [],
			};

			expect(isValidUser(minimalUser)).toBe(true);
		});

		it("should return false for null user", () => {
			expect(isValidUser(null)).toBe(false);
		});

		it("should return false for user with empty string id", () => {
			const userWithEmptyId = {
				id: "",
				username: "testuser",
				email: "test@example.com",
				firstName: "Test",
				lastName: "User",
				roles: ["USER"],
			} as User;

			expect(isValidUser(userWithEmptyId)).toBe(false);
		});

		it("should return false for user with non-string id", () => {
			const userWithNumberId = {
				id: 123,
				username: "testuser",
				email: "test@example.com",
				firstName: "Test",
				lastName: "User",
				roles: ["USER"],
			} as unknown as User;

			expect(isValidUser(userWithNumberId)).toBe(false);
		});

		it("should return false for user with undefined id", () => {
			const userWithUndefinedId = {
				id: undefined,
				username: "testuser",
				email: "test@example.com",
				firstName: "Test",
				lastName: "User",
				roles: ["USER"],
			} as unknown as User;

			expect(isValidUser(userWithUndefinedId)).toBe(false);
		});

		it("should return false for user with null id", () => {
			const userWithNullId = {
				id: null,
				username: "testuser",
				email: "test@example.com",
				firstName: "Test",
				lastName: "User",
				roles: ["USER"],
			} as unknown as User;

			expect(isValidUser(userWithNullId)).toBe(false);
		});

		it("should return false for user object with missing id field", () => {
			const userWithoutId = {
				username: "testuser",
				email: "test@example.com",
				firstName: "Test",
				lastName: "User",
				roles: ["USER"],
			} as unknown as User;

			expect(isValidUser(userWithoutId)).toBe(false);
		});

		it("should return false for empty object", () => {
			const emptyObject = {} as User;

			expect(isValidUser(emptyObject)).toBe(false);
		});

		it("should return false for user with whitespace-only id", () => {
			const userWithWhitespaceId = {
				id: "   ",
				username: "testuser",
				email: "test@example.com",
				firstName: "Test",
				lastName: "User",
				roles: ["USER"],
			} as User;

			// Whitespace-only IDs are invalid
			expect(isValidUser(userWithWhitespaceId)).toBe(false);
		});

		it("should return true for user with UUID id", () => {
			const userWithUuid: User = {
				id: "550e8400-e29b-41d4-a716-446655440000",
				username: "uuid_user",
				email: "uuid@example.com",
				firstName: "UUID",
				lastName: "User",
				roles: ["ADMIN"],
			};

			expect(isValidUser(userWithUuid)).toBe(true);
		});

		it("should return true for user with very long id", () => {
			const userWithLongId: User = {
				id: "a".repeat(1000),
				username: "long_id_user",
				email: "long@example.com",
				firstName: "Long",
				lastName: "User",
				roles: [],
			};

			expect(isValidUser(userWithLongId)).toBe(true);
		});
	});
});
