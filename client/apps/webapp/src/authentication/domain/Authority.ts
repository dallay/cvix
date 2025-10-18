/**
 * Enum representing application security authorities.
 *
 * Each value maps to a role string used for access control in the backend (e.g., "ROLE_ADMIN").
 * Use this enum to assign or check user permissions consistently throughout the frontend codebase.
 */
export enum Authority {
	ADMIN = "ROLE_ADMIN",
	USER = "ROLE_USER",
}
