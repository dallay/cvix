import { type ComputedRef, computed, onMounted, type Ref } from "vue";
import type {
	Award,
	Basics,
	Certificate,
	Education,
	Interest,
	Language,
	Profile,
	Project,
	Publication,
	Reference,
	Resume,
	Skill,
	Volunteer,
	Work,
} from "@/core/resume/domain/Resume";
import { useResumeStore } from "@/core/resume/infrastructure/store/resume.store.ts";
import type { ProblemDetail } from "@/shared/BaseHttpClient";

/**
 * Utility type to strip readonly modifiers from deeply nested types.
 * Transforms ReadonlyArray<T> → Array<T> and readonly properties → mutable.
 * This enables bi-directional form binding without runtime type assertions.
 */
type DeepWritable<T> =
	T extends ReadonlyArray<infer U>
		? Array<DeepWritable<U>>
		: T extends object
			? { -readonly [K in keyof T]: DeepWritable<T[K]> }
			: T;

/**
 * Derive MutableBasics from the domain Basics type.
 * Omit the readonly profiles array and replace it with a mutable version.
 * This ensures the type stays synchronized with the domain definition
 * and automatically reflects any domain changes.
 */
type MutableBasics = Omit<Basics, "profiles"> & {
	profiles: Profile[];
};

/**
 * Form-specific mutable version of the entire Resume.
 * Strips all readonly modifiers to enable bi-directional form binding.
 */
type WritableResume = DeepWritable<Resume>;

// Define the return type for clarity and reuse
export interface UseResumeFormReturn {
	// form fields
	basics: Ref<MutableBasics>;
	workExperiences: Ref<Work[]>;
	volunteers: Ref<Volunteer[]>;
	education: Ref<Education[]>;
	awards: Ref<Award[]>;
	certificates: Ref<Certificate[]>;
	publications: Ref<Publication[]>;
	skills: Ref<Skill[]>;
	languages: Ref<Language[]>;
	interests: Ref<Interest[]>;
	references: Ref<Reference[]>;
	projects: Ref<Project[]>;

	// computed
	resume: ComputedRef<Resume | null>;
	isValid: ComputedRef<boolean>;
	hasResume: ComputedRef<boolean>;
	isGenerating: ComputedRef<boolean>;
	generationError: ComputedRef<ProblemDetail | null>;

	// actions
	submitResume: () => boolean;
	saveToStorage: () => Promise<void>;
	generatePdf: (locale?: string) => Promise<Blob>;
	clearForm: () => void;
	loadResume: (r: Resume) => void;
}

/**
 * Factory function to create an empty resume with all sections initialized.
 * Used as a safe default for getters when no resume exists yet.
 */
function createEmptyResume(): WritableResume {
	return {
		basics: {
			name: "",
			label: "",
			image: "",
			email: "",
			phone: "",
			url: "",
			summary: "",
			location: {
				address: "",
				postalCode: "",
				city: "",
				countryCode: "",
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
}

/**
 * Composable for managing the resume form state and validation.
 * Returns a shared singleton accessor to Pinia store-backed computed refs for bi-directional reactivity.
 * All callers receive the same reactive state, ensuring a single source of truth.
 *
 * Key design decisions:
 * - Resume is NOT initialized automatically; callers must explicitly create or load one.
 * - Getters return safe defaults without side effects (preserves "no resume" state).
 * - Setters only write when explicitly called (no accidental state creation).
 * - Type casting is centralized in the WritableResume utility type.
 */
export function useResumeForm(): UseResumeFormReturn {
	const resumeStore = useResumeStore();

	// Computed refs backed by Pinia store state with safe defaults (no side effects)
	const basics = computed<MutableBasics>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.basics ??
				createEmptyResume().basics) as MutableBasics,
		set: (value: MutableBasics) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				basics: value,
			});
		},
	});
	const workExperiences = computed<Work[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.work ??
				createEmptyResume().work) as Work[],
		set: (value: Work[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				work: value,
			});
		},
	});
	const volunteers = computed<Volunteer[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.volunteer ??
				createEmptyResume().volunteer) as Volunteer[],
		set: (value: Volunteer[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				volunteer: value,
			});
		},
	});
	const education = computed<Education[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.education ??
				createEmptyResume().education) as Education[],
		set: (value: Education[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				education: value,
			});
		},
	});
	const awards = computed<Award[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.awards ??
				createEmptyResume().awards) as Award[],
		set: (value: Award[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				awards: value,
			});
		},
	});
	const certificates = computed<Certificate[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.certificates ??
				createEmptyResume().certificates) as Certificate[],
		set: (value: Certificate[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				certificates: value,
			});
		},
	});
	const publications = computed<Publication[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.publications ??
				createEmptyResume().publications) as Publication[],
		set: (value: Publication[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				publications: value,
			});
		},
	});
	const skills = computed<Skill[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.skills ??
				createEmptyResume().skills) as Skill[],
		set: (value: Skill[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				skills: value,
			});
		},
	});
	const languages = computed<Language[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.languages ??
				createEmptyResume().languages) as Language[],
		set: (value: Language[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				languages: value,
			});
		},
	});
	const interests = computed<Interest[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.interests ??
				createEmptyResume().interests) as Interest[],
		set: (value: Interest[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				interests: value,
			});
		},
	});
	const references = computed<Reference[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.references ??
				createEmptyResume().references) as Reference[],
		set: (value: Reference[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				references: value,
			});
		},
	});
	const projects = computed<Project[]>({
		get: () =>
			((resumeStore.resume as WritableResume | null)?.projects ??
				createEmptyResume().projects) as Project[],
		set: (value: Project[]) => {
			const currentResume =
				(resumeStore.resume as WritableResume) ?? createEmptyResume();
			resumeStore.setResume({
				...currentResume,
				projects: value,
			});
		},
	});

	const resume = computed<Resume | null>(() => resumeStore.resume);
	const isValid = computed(() => resumeStore.isValid);
	const hasResume = computed(() => resumeStore.hasResume);
	const isGenerating = computed(() => resumeStore.isGenerating);
	const generationError = computed(() => resumeStore.generationError);

	function submitResume(): boolean {
		return resumeStore.validateResume();
	}

	async function saveToStorage(): Promise<void> {
		return resumeStore.saveToStorage();
	}

	async function generatePdf(locale?: string): Promise<Blob> {
		return resumeStore.generatePdf(locale);
	}

	function clearForm(): void {
		resumeStore.clearResume();
	}

	function loadResume(r: Resume): void {
		resumeStore.setResume(r);
	}

	// Initialize: load from storage on mount
	onMounted(async () => {
		try {
			await resumeStore.loadFromStorage();
		} catch (error) {
			console.error("Failed to load resume from storage:", error);
		}
	});

	return {
		basics,
		workExperiences,
		volunteers,
		education,
		awards,
		certificates,
		publications,
		skills,
		languages,
		interests,
		references,
		projects,
		resume,
		isValid,
		hasResume,
		isGenerating,
		generationError,
		submitResume,
		saveToStorage,
		generatePdf,
		clearForm,
		loadResume,
	};
}
