/**
 * WorkspaceId value object
 * Represents a type-safe workspace identifier with UUID v4 validation
 */
export class WorkspaceId {
	private readonly value: string;

	/**
	 * Creates a new WorkspaceId
	 * @param id - UUID v4 format string
	 * @throws Error if the ID is not a valid UUID v4
	 */
	constructor(id: string) {
		if (!WorkspaceId.isValid(id)) {
			throw new Error(`Invalid workspace ID: ${id}`);
		}
		this.value = id;
	}

	/**
	 * Validates if a string is a valid UUID v4
	 * @param id - The string to validate
	 * @returns true if valid UUID v4, false otherwise
	 */
	static isValid(id: string): boolean {
		const uuidRegex =
			/^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
		return uuidRegex.test(id);
	}

	/**
	 * Returns the string representation of the workspace ID
	 */
	toString(): string {
		return this.value;
	}

	/**
	 * Compares this WorkspaceId with another for equality
	 * @param other - Another WorkspaceId to compare
	 * @returns true if both IDs are equal
	 */
	equals(other: WorkspaceId): boolean {
		return this.value === other.value;
	}
}
