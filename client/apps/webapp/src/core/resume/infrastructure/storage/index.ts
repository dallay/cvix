/**
 * Infrastructure layer exports for resume storage implementations.
 *
 * This module provides concrete implementations of the ResumeStorage interface
 * for different persistence mechanisms.
 */

export {
	createResumeStorage,
	getDefaultStorageType,
	getStorageMetadata,
	getStorageMetadataByType,
	type StorageMetadata,
} from "./factory";
export { IndexedDBResumeStorage } from "./IndexedDBResumeStorage";
export { LocalStorageResumeStorage } from "./LocalStorageResumeStorage";
export { SessionStorageResumeStorage } from "./SessionStorageResumeStorage";
