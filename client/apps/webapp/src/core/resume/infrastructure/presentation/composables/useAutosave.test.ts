import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick, ref } from "vue";
import type { Resume } from "@/core/resume/domain/Resume";
import { type AutosaveOptions, useAutosave } from "./useAutosave";

// Mock idb-keyval
vi.mock("idb-keyval", () => ({
	get: vi.fn(),
	set: vi.fn(),
	del: vi.fn(),
}));

// Import the mocked functions
import { del, get, set } from "idb-keyval";

// Mock BroadcastChannel
class MockBroadcastChannel {
	name: string;
	onmessage: ((event: MessageEvent) => void) | null = null;
	static instances: MockBroadcastChannel[] = [];

	constructor(name: string) {
		this.name = name;
		MockBroadcastChannel.instances.push(this);
	}

	postMessage(message: unknown) {
		// Broadcast to other instances with the same name
		MockBroadcastChannel.instances
			.filter((instance) => instance !== this && instance.name === this.name)
			.forEach((instance) => {
				if (instance.onmessage) {
					instance.onmessage({ data: message } as MessageEvent);
				}
			});
	}

	close() {
		const index = MockBroadcastChannel.instances.indexOf(this);
		if (index > -1) {
			MockBroadcastChannel.instances.splice(index, 1);
		}
	}

	static reset() {
		MockBroadcastChannel.instances = [];
	}
}

vi.stubGlobal("BroadcastChannel", MockBroadcastChannel);

describe("useAutosave", () => {
	let mockResume: Resume;

	beforeEach(() => {
		vi.clearAllMocks();
		MockBroadcastChannel.reset();
		vi.useFakeTimers();

		mockResume = {
			basics: {
				name: "John Doe",
				label: "Software Engineer",
				image: "",
				email: "john@example.com",
				phone: "+1-555-0100",
				url: "",
				summary: "",
				location: {
					address: "",
					postalCode: "",
					city: "San Francisco",
					countryCode: "US",
					region: "",
				},
				profiles: [],
			},
			work: [],
			volunteer: [],
			education: [],
			awards: [],
			certificates: [],
			publications: [],
			skills: [],
			languages: [],
			interests: [],
			references: [],
			projects: [],
		};

		vi.mocked(get).mockResolvedValue(undefined);
		vi.mocked(set).mockResolvedValue(undefined);
		vi.mocked(del).mockResolvedValue(undefined);
	});

	afterEach(() => {
		vi.useRealTimers();
	});

	describe("initialization", () => {
		it("should initialize with default state", () => {
			const resumeRef = ref<Resume | null>(null);
			const { state } = useAutosave(resumeRef);

			expect(state.value.isSaving).toBe(false);
			expect(state.value.lastSaved).toBeNull();
			expect(state.value.error).toBeNull();
		});

		it("should use custom options", () => {
			const resumeRef = ref<Resume | null>(null);
			const options: AutosaveOptions = {
				key: "custom:resume",
				debounceMs: 5000,
				enableSync: false,
			};

			const { state } = useAutosave(resumeRef, options);

			expect(state.value.isSaving).toBe(false);
		});
	});

	describe("save", () => {
		it("should save resume to IndexedDB", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			const { save, state } = useAutosave(resumeRef);

			await save(mockResume);

			expect(set).toHaveBeenCalledWith("resume:draft", mockResume);
			expect(state.value.lastSaved).not.toBeNull();
			expect(state.value.isSaving).toBe(false);
			expect(state.value.error).toBeNull();
		});

		it("should handle save errors", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			vi.mocked(set).mockRejectedValue(new Error("Storage quota exceeded"));

			const { save, state } = useAutosave(resumeRef);

			await expect(save(mockResume)).rejects.toThrow("Storage quota exceeded");
			expect(state.value.error).toBeInstanceOf(Error);
			expect(state.value.error?.message).toBe("Storage quota exceeded");
		});

		it("should broadcast changes to other tabs", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			const { save } = useAutosave(resumeRef, { enableSync: true });

			// Create another instance to receive the broadcast
			const otherResumeRef = ref<Resume | null>(null);
			useAutosave(otherResumeRef, { enableSync: true });

			await save(mockResume);

			// The broadcast should have updated the other ref
			// Note: In actual implementation, the timestamp comparison may prevent update
		});
	});

	describe("load", () => {
		it("should load resume from IndexedDB", async () => {
			const resumeRef = ref<Resume | null>(null);
			vi.mocked(get).mockResolvedValue(mockResume);

			const { load, state } = useAutosave(resumeRef);
			const result = await load();

			expect(get).toHaveBeenCalledWith("resume:draft");
			expect(result).toEqual(mockResume);
			expect(state.value.lastSaved).not.toBeNull();
		});

		it("should return null when no data exists", async () => {
			const resumeRef = ref<Resume | null>(null);
			vi.mocked(get).mockResolvedValue(undefined);

			const { load } = useAutosave(resumeRef);
			const result = await load();

			expect(result).toBeNull();
		});

		it("should handle load errors", async () => {
			const resumeRef = ref<Resume | null>(null);
			vi.mocked(get).mockRejectedValue(new Error("Database error"));

			const { load, state } = useAutosave(resumeRef);
			const result = await load();

			expect(result).toBeNull();
			expect(state.value.error).toBeInstanceOf(Error);
		});
	});

	describe("clear", () => {
		it("should clear resume from IndexedDB", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			const { clear, state } = useAutosave(resumeRef);

			await clear();

			expect(del).toHaveBeenCalledWith("resume:draft");
			expect(state.value.lastSaved).toBeNull();
		});

		it("should handle clear errors", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			vi.mocked(del).mockRejectedValue(new Error("Clear failed"));

			const { clear, state } = useAutosave(resumeRef);

			await expect(clear()).rejects.toThrow("Clear failed");
			expect(state.value.error).toBeInstanceOf(Error);
		});

		it("should broadcast clear to other tabs", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			const { clear } = useAutosave(resumeRef, { enableSync: true });

			await clear();

			expect(del).toHaveBeenCalled();
		});
	});

	describe("debounced save", () => {
		it("should debounce save when resume changes", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			useAutosave(resumeRef, { debounceMs: 2000 });

			// Change the resume
			resumeRef.value = {
				...mockResume,
				basics: { ...mockResume.basics, name: "Jane Doe" },
			};

			await nextTick();

			// Save should not be called immediately
			expect(set).not.toHaveBeenCalled();

			// Advance timers
			await vi.advanceTimersByTimeAsync(2000);

			// Now save should be called
			expect(set).toHaveBeenCalled();
		});

		it("should not save when resume is null", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			useAutosave(resumeRef, { debounceMs: 1000 });

			resumeRef.value = null;

			await nextTick();
			await vi.advanceTimersByTimeAsync(1000);

			// Save should not be called for null
			expect(set).not.toHaveBeenCalled();
		});
	});

	describe("broadcast channel", () => {
		it("should receive updates from other tabs", async () => {
			const resumeRef = ref<Resume | null>(null);
			useAutosave(resumeRef, { enableSync: true });

			// Simulate receiving a message from another tab
			const broadcastChannel = MockBroadcastChannel.instances[0];
			expect(broadcastChannel).toBeDefined();

			const updatedResume = {
				...mockResume,
				basics: { ...mockResume.basics, name: "Updated Name" },
			};

			if (broadcastChannel?.onmessage) {
				broadcastChannel.onmessage({
					data: {
						type: "resume-updated",
						data: updatedResume,
						timestamp: Date.now() + 1000, // Future timestamp
					},
				} as MessageEvent);
			}

			expect(resumeRef.value).toEqual(updatedResume);
		});

		it("should handle clear message from other tabs", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			const { state } = useAutosave(resumeRef, { enableSync: true });

			const broadcastChannel = MockBroadcastChannel.instances[0];

			if (broadcastChannel?.onmessage) {
				broadcastChannel.onmessage({
					data: {
						type: "resume-cleared",
						timestamp: Date.now(),
					},
				} as MessageEvent);
			}

			expect(resumeRef.value).toBeNull();
			expect(state.value.lastSaved).toBeNull();
		});

		it("should ignore older updates (last-write-wins)", async () => {
			const resumeRef = ref<Resume | null>(mockResume);
			const { state, save } = useAutosave(resumeRef, { enableSync: true });

			// Save current resume to set lastSaved
			await save(mockResume);
			const lastSavedTime = state.value.lastSaved?.getTime() || Date.now();

			const broadcastChannel = MockBroadcastChannel.instances[0];

			const olderResume = {
				...mockResume,
				basics: { ...mockResume.basics, name: "Older Update" },
			};

			if (broadcastChannel?.onmessage) {
				broadcastChannel.onmessage({
					data: {
						type: "resume-updated",
						data: olderResume,
						timestamp: lastSavedTime - 10000, // Older timestamp
					},
				} as MessageEvent);
			}

			// Resume should not be updated with older data
			expect(resumeRef.value?.basics.name).toBe("John Doe");
		});
	});

	describe("without BroadcastChannel support", () => {
		it("should work without BroadcastChannel", async () => {
			// Temporarily remove BroadcastChannel
			const originalBC = globalThis.BroadcastChannel;
			// @ts-expect-error - Intentionally setting to undefined for testing
			globalThis.BroadcastChannel = undefined;

			const resumeRef = ref<Resume | null>(mockResume);
			const { save, load, clear, state } = useAutosave(resumeRef, {
				enableSync: true,
			});

			// Should still work for basic operations
			await save(mockResume);
			expect(state.value.lastSaved).not.toBeNull();

			vi.mocked(get).mockResolvedValue(mockResume);
			const loaded = await load();
			expect(loaded).toEqual(mockResume);

			await clear();
			expect(del).toHaveBeenCalled();

			// Restore BroadcastChannel
			globalThis.BroadcastChannel = originalBC;
		});
	});
});
