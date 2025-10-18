/**
 * Transforms a UserResponse DTO into an Account domain object.
 *
 * @param data - The UserResponse object returned by the API.
 * @returns An Account object created from the provided data. The returned object:
 *  - spreads all properties from the source `data`,
 *  - sets `fullname` to the concatenation of `firstname` and `lastname` (or `undefined` if both are missing),
 *  - sets `langKey` to the default value `"en"` (TODO: make dynamic from user settings),
 *  - sets `activated` to `true`,
 *  - sets `imageUrl` using `avatar(data.email, 100)`.
 *
 * @remarks
 * - This function is a pure mapper and does not perform side effects.
 * - If `data.email` is missing, the generated `imageUrl` may be a fallback provided by the `avatar` helper.
 *
 * @example
 * const account = transformUserResponseToAccount(apiUserResponse);
 */
import type { Account } from "@/authentication/domain/Account";
import { avatar } from "@loomify/utilities";
import type { UserResponse } from "../UserResponse";

export function transformUserResponseToAccount(data: UserResponse): Account {
	return {
		...data,
		fullname:
			[data.firstname, data.lastname].filter(Boolean).join(" ") || undefined,
		langKey: "en", // TODO: Make dynamic from user settings
		activated: true,
		imageUrl: avatar(data.email, 100),
	};
}
