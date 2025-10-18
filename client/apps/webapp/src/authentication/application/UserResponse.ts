/**
 * Data returned by the authentication subsystem representing a user.
 *
 * This interface contains the publicly safe fields for a user that are returned
 * from authentication or user-info endpoints. It is intended to be immutable
 * from the consumer's perspective.
 *
 * @remarks
 * - `username` is the unique identifier for the user within the system.
 * - `email` is the user's contact email address.
 * - `firstname` and `lastname` are optional and may be undefined or explicitly null
 *   when the information is not available.
 * - `authorities` is a read-only set of authority (role/permission) identifiers
 *   granted to the user.
 *
 * @example
 * // Example shape (conceptual)
 * // {
 * //   username: "jdoe",
 * //   email: "jdoe@example.com",
 * //   firstname: "John",
 * //   lastname: "Doe",
 * //   authorities: new Set(["ROLE_USER", "ROLE_ADMIN"])
 * // }
 */
export interface UserResponse {
	readonly username: string;
	readonly email: string;
	readonly firstname?: string | null;
	readonly lastname?: string | null;
	readonly authorities: ReadonlySet<string>;
}
