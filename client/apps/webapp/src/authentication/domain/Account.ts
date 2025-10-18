/**
 * Represents a user account with authentication and profile information.
 */
export interface Account {
	/** The unique username of the account */
	readonly username: string;

	/** The email address associated with the account */
	readonly email?: string;

	/** The full name of the user */
	readonly fullname?: string | null;

	/** The first name of the user */
	readonly firstname?: string | null;

	/** The last name of the user */
	readonly lastname?: string | null;

	/** A readonly set of authority strings assigned to the account */
	readonly authorities: ReadonlySet<string>;

	/** The language key for localization */
	readonly langKey?: string;

	/** Indicates whether the account is activated */
	readonly activated?: boolean;

	/** The URL of the user's profile image */
	readonly imageUrl?: string;

	/** The date the account was created */
	readonly createdDate?: Date | string;

	/** The date the account was last modified */
	readonly lastModifiedDate?: Date | string;
}

/**
 * Type guard to check if an account is activated
 */
export function isActivatedAccount(account: Account): boolean {
	return account.activated === true;
}

/**
 * Type guard to check if an account has a specific authority
 */
export function hasAuthority(account: Account, authority: string): boolean {
	return account.authorities.has(authority);
}

/**
 * Type guard to check if an account has any of the specified authorities
 */
export function hasAnyAuthority(
	account: Account,
	authorities: string[],
): boolean {
	return authorities.some((authority) => account.authorities.has(authority));
}

/**
 * Type guard to check if an account has all of the specified authorities
 */
export function hasAllAuthorities(
	account: Account,
	authorities: string[],
): boolean {
	return authorities.every((authority) => account.authorities.has(authority));
}
