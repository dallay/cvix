import type { User } from "../models/auth.model.ts";

/**
 * Validates that a user object has the minimum required fields to be considered valid.
 * This prevents false positives when the API returns malformed data (e.g., HTML instead of JSON).
 *
 * @param u - The user object to validate
 * @returns true if the user is valid (has a non-empty string id after trimming), false otherwise
 *
 * @example
 * ```typescript
 * const user = { id: "123", email: "user@example.com", ... };
 * if (isValidUser(user)) {
 *   // user.id is guaranteed to be a non-empty string
 * }
 * ```
 */
export function isValidUser(u: User | null): u is User {
	return u !== null && typeof u.id === "string" && u.id.trim().length > 0;
}
