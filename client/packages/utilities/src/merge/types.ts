/**
 * Represents an object that can be merged, with string or symbol keys and unknown values.
 */
type MergeableObject = Record<string | symbol, unknown>;

/**
 * Options for deep merging objects.
 *
 * @property clone - If true, objects and arrays will be cloned during merge.
 * @property arrayMerge - Custom function to merge arrays.
 * @property isMergeableObject - Function to determine if a value is a mergeable object.
 * @property customMerge - Function to provide a custom merge function for a given key.
 * @property cloneUnlessOtherwiseSpecified - Function to clone a value unless specified otherwise.
 */
interface DeepMergeOptions {
	clone?: boolean;
	arrayMerge?: <T>(target: T[], source: T[], options: DeepMergeOptions) => T[];
	isMergeableObject?: (val: unknown) => val is MergeableObject;
	customMerge?: (key: string | symbol) => DeepMergeFn | undefined;
	cloneUnlessOtherwiseSpecified?: (
		value: unknown,
		options: DeepMergeOptions,
	) => unknown;
}

/**
 * Function type for deep merging two values with options.
 *
 * @param target - The target value to merge into.
 * @param source - The source value to merge from.
 * @param options - Options to control the merge behavior.
 * @returns The merged value.
 */
type DeepMergeFn = (
	target: unknown,
	source: unknown,
	options?: DeepMergeOptions,
) => unknown;
