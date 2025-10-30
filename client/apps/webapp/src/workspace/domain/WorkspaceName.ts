/**
 * WorkspaceName value object
 * Encapsulates workspace name validation logic
 */
export class WorkspaceName {
	private readonly value: string;

	/**
	 * Creates a new WorkspaceName
	 * @param name - The workspace name (1-100 characters after trimming)
	 * @throws Error if the name is invalid
	 */
	constructor(name: string) {
		const trimmed = name.trim();
		if (trimmed.length < 1 || trimmed.length > 100) {
			throw new Error("Workspace name must be 1-100 characters");
		}
		this.value = trimmed;
	}

	/**
	 * Returns the string representation of the workspace name
	 */
	toString(): string {
		return this.value;
	}

	/**
	 * Checks equality with another WorkspaceName
	 */
	equals(other: WorkspaceName): boolean {
		return this.value === other.value;
	}
}
