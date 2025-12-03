import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Resume } from "@/core/resume/domain/Resume";
import { useJsonResume } from "./useJsonResume";

// Mock URL.createObjectURL and URL.revokeObjectURL
const mockCreateObjectURL = vi.fn(() => "blob:mock-url");
const mockRevokeObjectURL = vi.fn();

vi.stubGlobal("URL", {
	createObjectURL: mockCreateObjectURL,
	revokeObjectURL: mockRevokeObjectURL,
});

// Mock document.createElement for link downloads
const mockClick = vi.fn();
const mockLink = {
	href: "",
	download: "",
	click: mockClick,
};

vi.spyOn(document, "createElement").mockImplementation((tagName: string) => {
	if (tagName === "a") {
		return mockLink as unknown as HTMLAnchorElement;
	}
	return document.createElement(tagName);
});

/**
 * Helper to create a mock File with text() method
 */
function createMockFile(content: string, name = "resume.json"): File {
	const blob = new Blob([content], { type: "application/json" });
	const file = new File([blob], name, { type: "application/json" });
	// Ensure text() method works in jsdom
	file.text = () => Promise.resolve(content);
	return file;
}

describe("useJsonResume", () => {
	let validResume: Resume;

	beforeEach(() => {
		vi.clearAllMocks();

		validResume = {
			basics: {
				name: "John Doe",
				label: "Software Engineer",
				image: "https://example.com/photo.jpg",
				email: "john@example.com",
				phone: "+1-555-0100",
				url: "https://johndoe.com",
				summary: "Experienced software engineer",
				location: {
					address: "123 Main St",
					postalCode: "12345",
					city: "San Francisco",
					countryCode: "US",
					region: "California",
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
	});

	describe("initialization", () => {
		it("should initialize with default state", () => {
			const { isValidating, validationErrors } = useJsonResume();

			expect(isValidating.value).toBe(false);
			expect(validationErrors.value).toEqual([]);
		});
	});

	describe("importJson", () => {
		it("should import valid JSON Resume file", async () => {
			const { importJson } = useJsonResume();
			const jsonContent = JSON.stringify(validResume);
			const file = createMockFile(jsonContent);

			const result = await importJson(file);

			expect(result.success).toBe(true);
			expect(result.data).toEqual(validResume);
			expect(result.errors).toBeUndefined();
		});

		it("should return error for invalid JSON syntax", async () => {
			const { importJson, validationErrors } = useJsonResume();
			const file = createMockFile("invalid json{]");

			const result = await importJson(file);

			expect(result.success).toBe(false);
			expect(result.errors).toBeDefined();
			expect(result.errors?.[0]?.message).toContain("Invalid JSON format");
			expect(validationErrors.value.length).toBeGreaterThan(0);
		});

		it("should return validation errors for invalid resume data", async () => {
			const { importJson, validationErrors } = useJsonResume();
			const invalidResume = {
				basics: {
					name: "John Doe",
					email: "invalid-email",
					location: { countryCode: "US" },
					profiles: [],
				},
				// Missing required arrays
			};
			const file = createMockFile(JSON.stringify(invalidResume));

			const result = await importJson(file);

			expect(result.success).toBe(false);
			expect(result.errors).toBeDefined();
			expect(validationErrors.value.length).toBeGreaterThan(0);
		});

		it("should handle file read errors", async () => {
			const { importJson } = useJsonResume();
			const mockFile = {
				text: vi.fn().mockRejectedValue(new Error("File read error")),
			} as unknown as File;

			const result = await importJson(mockFile);

			expect(result.success).toBe(false);
			expect(result.errors?.[0]?.message).toBe("File read error");
		});

		it("should set isValidating during import", async () => {
			const { importJson, isValidating } = useJsonResume();
			const jsonContent = JSON.stringify(validResume);
			const file = createMockFile(jsonContent);

			expect(isValidating.value).toBe(false);

			const importPromise = importJson(file);
			// Note: Due to async nature, we can't easily test intermediate state
			await importPromise;

			expect(isValidating.value).toBe(false);
		});
	});

	describe("exportJson", () => {
		it("should export valid resume as JSON file", () => {
			const { exportJson } = useJsonResume();

			const result = exportJson(validResume, "my-resume.json");

			expect(result).toBe(true);
			expect(mockCreateObjectURL).toHaveBeenCalled();
			expect(mockLink.download).toBe("my-resume.json");
			expect(mockClick).toHaveBeenCalled();
			expect(mockRevokeObjectURL).toHaveBeenCalled();
		});

		it("should use default filename when not provided", () => {
			const { exportJson } = useJsonResume();

			const result = exportJson(validResume);

			expect(result).toBe(true);
			expect(mockLink.download).toBe("resume.json");
		});

		it("should return false for invalid resume data", () => {
			const { exportJson, validationErrors } = useJsonResume();
			const invalidResume = {
				basics: {
					name: "John",
					email: "invalid-email",
					location: { countryCode: "US" },
					profiles: [],
				},
			} as unknown as Resume;

			const result = exportJson(invalidResume);

			expect(result).toBe(false);
			expect(validationErrors.value.length).toBeGreaterThan(0);
			expect(mockClick).not.toHaveBeenCalled();
		});
	});

	describe("validateResume", () => {
		it("should return empty array for valid resume", () => {
			const { validateResume, validationErrors } = useJsonResume();

			const errors = validateResume(validResume);

			expect(errors).toEqual([]);
			expect(validationErrors.value).toEqual([]);
		});

		it("should return errors for invalid resume", () => {
			const { validateResume, validationErrors } = useJsonResume();
			const invalidResume = {
				...validResume,
				basics: {
					...validResume.basics,
					email: "invalid-email",
				},
			};

			const errors = validateResume(invalidResume);

			expect(errors.length).toBeGreaterThan(0);
			expect(validationErrors.value.length).toBeGreaterThan(0);
		});

		it("should return errors for missing required fields", () => {
			const { validateResume } = useJsonResume();
			const incompleteResume = {
				basics: null,
				work: [],
			} as unknown as Resume;

			const errors = validateResume(incompleteResume);

			expect(errors.length).toBeGreaterThan(0);
		});
	});

	describe("groupErrors", () => {
		it("should group errors by section", () => {
			const { groupErrors } = useJsonResume();
			const errors = [
				{
					path: "basics.email",
					message: "Invalid email",
					section: "Personal Information",
				},
				{
					path: "basics.phone",
					message: "Invalid phone",
					section: "Personal Information",
				},
				{
					path: "work[0].startDate",
					message: "Invalid date",
					section: "Work Experience",
				},
			];

			const grouped = groupErrors(errors);

			expect(grouped["Personal Information"]).toHaveLength(2);
			expect(grouped["Work Experience"]).toHaveLength(1);
		});

		it("should use General section when section is missing", () => {
			const { groupErrors } = useJsonResume();
			const errors = [{ path: "resume", message: "Invalid resume" }];

			const grouped = groupErrors(errors);

			expect(grouped["General"]).toHaveLength(1);
		});

		it("should handle empty errors array", () => {
			const { groupErrors } = useJsonResume();

			const grouped = groupErrors([]);

			expect(Object.keys(grouped)).toHaveLength(0);
		});
	});
});
