/**
 * Determines if a value is a mergeable object (plain object, not array).
 * @param val Value to check
 * @returns True if value is a mergeable object
 */
const defaultIsMergeableObject = (val: unknown): val is MergeableObject => {
	if (!val || typeof val !== "object" || Array.isArray(val)) return false;
	// Exclude special objects
	if (
		val instanceof Date ||
		val instanceof RegExp ||
		val instanceof Set ||
		val instanceof Map
	) {
		return false;
	}
	// Only plain objects are mergeable
	return Object.getPrototypeOf(val) === Object.prototype;
};

/**
 * Returns an empty target of the same type as the input (array or object).
 * @param val Value to check
 * @returns Empty array or object
 */
const emptyTarget = (val: unknown): unknown[] | Record<string, unknown> =>
	Array.isArray(val) ? [] : {};

/**
 * Clones a value unless the options specify otherwise.
 * If the value is mergeable, performs a deep merge with an empty target.
 * @param value Value to clone
 * @param options Deep merge options
 * @returns Cloned value or original value
 */
const cloneUnlessOtherwiseSpecified = (
	value: unknown,
	options: DeepMergeOptions,
): unknown => {
	return options.clone !== false && options.isMergeableObject?.(value)
		? deepmerge(emptyTarget(value), value, options)
		: value;
};

/**
 * Default strategy for merging arrays: concatenates and clones elements.
 * @param target Target array
 * @param source Source array
 * @param options Deep merge options
 * @returns Merged array
 */
const defaultArrayMerge = <T>(
	target: T[],
	source: T[],
	options: DeepMergeOptions,
): T[] =>
	[...target, ...source].map(
		(element) => cloneUnlessOtherwiseSpecified(element, options) as T,
	);

/**
 * Gets the merge function for a given key, using customMerge if provided.
 * @param key Property key
 * @param options Deep merge options
 * @returns Merge function
 */
const getMergeFunction = (
	key: string | symbol,
	options: DeepMergeOptions,
): DeepMergeFn => options.customMerge?.(key) ?? deepmerge;

/**
 * Gets enumerable own property symbols of an object.
 * @param target Object to inspect
 * @returns Array of enumerable symbols
 */
const getEnumerableOwnPropertySymbols = (target: object): symbol[] =>
	Object.getOwnPropertySymbols
		? Object.getOwnPropertySymbols(target).filter((s) =>
				Object.prototype.propertyIsEnumerable.call(target, s),
			)
		: [];

/**
 * Gets all enumerable keys (string and symbol) of an object.
 * @param target Object to inspect
 * @returns Array of keys
 */
const getKeys = (target: object): (string | symbol)[] => [
	...Object.keys(target),
	...getEnumerableOwnPropertySymbols(target),
];

/**
 * Checks if a property exists on an object.
 * @param object Object to check
 * @param property Property key
 * @returns True if property exists
 */
const propertyIsOnObject = (
	object: object,
	property: string | symbol,
): boolean => {
	try {
		return property in object;
	} catch {
		return false;
	}
};

/**
 * Determines if a property is unsafe to merge (not own or not enumerable).
 * @param key Property key
 * @returns True if property is unsafe
 */
const propertyIsUnsafe = (key: string | symbol): boolean =>
	key === "__proto__" || key === "constructor" || key === "prototype";

/**
 * Deeply merges two mergeable objects according to options.
 * @param target Target object
 * @param source Source object
 * @param options Deep merge options
 * @returns Merged object
 */
const mergeObject = (
	target: MergeableObject,
	source: MergeableObject,
	options: DeepMergeOptions,
): MergeableObject => {
	const destination: MergeableObject = {};

	if (options.isMergeableObject?.(target)) {
		for (const key of getKeys(target)) {
			destination[key] = cloneUnlessOtherwiseSpecified(target[key], options);
		}
	}

	for (const key of getKeys(source)) {
		if (propertyIsUnsafe(key)) continue;

		if (
			propertyIsOnObject(target, key) &&
			Array.isArray(target[key]) &&
			Array.isArray(source[key])
		) {
			if (options.arrayMerge) {
				destination[key] = options.arrayMerge(
					target[key] as unknown[],
					source[key] as unknown[],
					options,
				);
			}
		} else if (
			propertyIsOnObject(target, key) &&
			options.isMergeableObject?.(source[key])
		) {
			const mergeFn = getMergeFunction(key, options);
			destination[key] = mergeFn(target[key], source[key], options);
		} else {
			destination[key] = cloneUnlessOtherwiseSpecified(source[key], options);
		}
	}

	return destination;
};

/**
 * Deeply merges two values (objects or arrays) according to options.
 * Supports custom array/object merge strategies and cloning.
 *
 * @param target Target value (object or array)
 * @param source Source value (object or array)
 * @param options Deep merge options
 * @returns Merged value
 */
export const deepmerge: DeepMergeFn & {
	all: (array: unknown[], options?: DeepMergeOptions) => unknown;
} = (target, source, options = {}) => {
	options.arrayMerge = options.arrayMerge || defaultArrayMerge;
	options.isMergeableObject =
		options.isMergeableObject || defaultIsMergeableObject;
	options.cloneUnlessOtherwiseSpecified = cloneUnlessOtherwiseSpecified;

	const sourceIsArray = Array.isArray(source);
	const targetIsArray = Array.isArray(target);

	if (sourceIsArray !== targetIsArray) {
		return cloneUnlessOtherwiseSpecified(source, options);
	}

	return sourceIsArray
		? options.arrayMerge(target as unknown[], source as unknown[], options)
		: mergeObject(
				target as MergeableObject,
				source as MergeableObject,
				options,
			);
};

/**
 * Deeply merges all items in an array into a single object or array.
 * @param array Array of objects/arrays to merge
 * @param options Deep merge options
 * @returns Merged result
 * @throws Error if first argument is not an array
 */
deepmerge.all = (array: unknown[], options?: DeepMergeOptions): unknown => {
	if (!Array.isArray(array)) {
		throw new Error("first argument should be an array");
	}

	return array.reduce((prev, next) => deepmerge(prev, next, options), {});
};
