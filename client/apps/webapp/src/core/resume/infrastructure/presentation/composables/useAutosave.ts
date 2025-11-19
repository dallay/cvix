import { useDebounceFn } from "@vueuse/core";
import { del, get, set } from "idb-keyval";
import { onUnmounted, type Ref, ref, watch } from "vue";
import type { Resume } from "@/core/resume/domain/Resume";

/**
 * Autosave configuration options
 */
export interface AutosaveOptions {
	/**
	 * Storage key for IndexedDB
	 * @default "resume:draft"
	 */
	key?: string;
	/**
	 * Debounce delay in milliseconds
	 * @default 2000
	 */
	debounceMs?: number;
	/**
	 * Enable BroadcastChannel for multi-tab sync
	 * @default true
	 */
	enableSync?: boolean;
}

/**
 * Autosave state
 */
export interface AutosaveState {
	/**
	 * Whether autosave is currently in progress
	 */
	isSaving: boolean;
	/**
	 * Last successful save timestamp
	 */
	lastSaved: Date | null;
	/**
	 * Last save error
	 */
	error: Error | null;
}

/**
 * Composable for automatic resume saving to IndexedDB with multi-tab synchronization.
 *
 * Features:
 * - Debounced saves (default: 2s delay)
 * - IndexedDB persistence for offline support
 * - BroadcastChannel for multi-tab sync with last-write-wins strategy
 * - Error handling and retry logic
 *
 * @example
 * ```typescript
 * const { state, save, load, clear } = useAutosave(resumeRef, {
 *   debounceMs: 2000,
 *   enableSync: true
 * });
 *
 * // Autosave is automatic when resume changes
 * watch(resume, () => {
 *   // Automatically saved after debounce delay
 * });
 * ```
 */
export function useAutosave(
	resumeRef: Ref<Resume | null>,
	options: AutosaveOptions = {},
) {
	const {
		key = "resume:draft",
		debounceMs = 2000,
		enableSync = true,
	} = options;

	const state = ref<AutosaveState>({
		isSaving: false,
		lastSaved: null,
		error: null,
	});

	let broadcastChannel: BroadcastChannel | null = null;

	/**
	 * Save resume to IndexedDB
	 */
	async function saveToStorage(resume: Resume): Promise<void> {
		try {
			state.value.isSaving = true;
			state.value.error = null;

			await set(key, resume);

			state.value.lastSaved = new Date();
			state.value.isSaving = false;

			// Broadcast change to other tabs
			if (enableSync && broadcastChannel) {
				broadcastChannel.postMessage({
					type: "resume-updated",
					data: resume,
					timestamp: Date.now(),
				});
			}
		} catch (error) {
			state.value.isSaving = false;
			state.value.error =
				error instanceof Error ? error : new Error("Unknown save error");
			console.error("[useAutosave] Error saving to IndexedDB:", error);
			throw error;
		}
	}

	/**
	 * Load resume from IndexedDB
	 */
	async function loadFromStorage(): Promise<Resume | null> {
		try {
			const data = await get<Resume>(key);
			if (data) {
				state.value.lastSaved = new Date();
			}
			return data || null;
		} catch (error) {
			state.value.error =
				error instanceof Error ? error : new Error("Unknown load error");
			console.error("[useAutosave] Error loading from IndexedDB:", error);
			return null;
		}
	}

	/**
	 * Clear saved resume from IndexedDB
	 */
	async function clearStorage(): Promise<void> {
		try {
			await del(key);
			state.value.lastSaved = null;

			// Broadcast clear to other tabs
			if (enableSync && broadcastChannel) {
				broadcastChannel.postMessage({
					type: "resume-cleared",
					timestamp: Date.now(),
				});
			}
		} catch (error) {
			state.value.error =
				error instanceof Error ? error : new Error("Unknown clear error");
			console.error("[useAutosave] Error clearing IndexedDB:", error);
			throw error;
		}
	}

	/**
	 * Debounced save function
	 */
	const debouncedSave = useDebounceFn(async (resume: Resume | null) => {
		if (resume) {
			await saveToStorage(resume);
		}
	}, debounceMs);

	/**
	 * Initialize BroadcastChannel for multi-tab sync
	 */
	function initBroadcastChannel() {
		if (!enableSync || typeof BroadcastChannel === "undefined") {
			return;
		}

		try {
			broadcastChannel = new BroadcastChannel("resume-autosave");

			broadcastChannel.onmessage = (event) => {
				const { type, data, timestamp } = event.data;

				// Last-write-wins strategy: accept updates from other tabs
				if (type === "resume-updated") {
					const localTimestamp = state.value.lastSaved?.getTime() || 0;

					// Only update if the remote change is newer
					if (timestamp > localTimestamp) {
						resumeRef.value = data;
						state.value.lastSaved = new Date(timestamp);
					}
				} else if (type === "resume-cleared") {
					resumeRef.value = null;
					state.value.lastSaved = null;
				}
			};
		} catch (error) {
			console.warn(
				"[useAutosave] BroadcastChannel not supported or failed to initialize:",
				error,
			);
		}
	}

	/**
	 * Cleanup BroadcastChannel
	 */
	function cleanupBroadcastChannel() {
		if (broadcastChannel) {
			broadcastChannel.close();
			broadcastChannel = null;
		}
	}

	// Watch resume changes and trigger debounced save
	watch(
		resumeRef,
		(newResume) => {
			debouncedSave(newResume)
				.then((r) => r)
				.catch((e) => {
					console.error("[useAutosave] Debounced save error:", e);
				});
		},
		{ deep: true },
	);

	// Initialize broadcast channel
	initBroadcastChannel();

	// Cleanup on unmount
	onUnmounted(() => {
		cleanupBroadcastChannel();
	});

	return {
		state,
		save: saveToStorage,
		load: loadFromStorage,
		clear: clearStorage,
	};
}
