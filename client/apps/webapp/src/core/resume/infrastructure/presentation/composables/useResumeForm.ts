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
 * Derive MutableBasics from the domain Basics type.
 * Omit the readonly profiles array and replace it with a mutable version.
 * This ensures the type stays synchronized with the domain definition
 * and automatically reflects any domain changes.
 */
type MutableBasics = Omit<Basics, "profiles"> & {
	profiles: Profile[];
};

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
 * Composable for managing the resume form state and validation.
 * Returns a shared singleton accessor to Pinia store-backed computed refs for bi-directional reactivity.
 * All callers receive the same reactive state, ensuring a single source of truth.
 */
export function useResumeForm(): UseResumeFormReturn {
	const resumeStore = useResumeStore();

	/**
	 * Ensures resumeStore.resume is always initialized with a fully-populated, mutable resume object.
	 * Returns the resume object (never null/undefined after this call).
	 */
	function ensureResume(): Resume {
		if (!resumeStore.resume) {
			resumeStore.setResume({
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
			});
		}
		if (!resumeStore.resume) {
			throw new Error("Failed to initialize resume");
		}
		return resumeStore.resume;
	}

	// Computed refs backed by Pinia store state (no cloning, always store-backed)
	const basics = computed<MutableBasics>({
		get: () => ensureResume().basics as MutableBasics,
		set: (value: MutableBasics) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				basics: value,
			});
		},
	});
	const workExperiences = computed<Work[]>({
		get: () => ensureResume().work as Work[],
		set: (value: Work[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				work: value,
			});
		},
	});
	const volunteers = computed<Volunteer[]>({
		get: () => ensureResume().volunteer as Volunteer[],
		set: (value: Volunteer[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				volunteer: value,
			});
		},
	});
	const education = computed<Education[]>({
		get: () => ensureResume().education as Education[],
		set: (value: Education[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				education: value,
			});
		},
	});
	const awards = computed<Award[]>({
		get: () => ensureResume().awards as Award[],
		set: (value: Award[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				awards: value,
			});
		},
	});
	const certificates = computed<Certificate[]>({
		get: () => ensureResume().certificates as Certificate[],
		set: (value: Certificate[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				certificates: value,
			});
		},
	});
	const publications = computed<Publication[]>({
		get: () => ensureResume().publications as Publication[],
		set: (value: Publication[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				publications: value,
			});
		},
	});
	const skills = computed<Skill[]>({
		get: () => ensureResume().skills as Skill[],
		set: (value: Skill[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				skills: value,
			});
		},
	});
	const languages = computed<Language[]>({
		get: () => ensureResume().languages as Language[],
		set: (value: Language[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				languages: value,
			});
		},
	});
	const interests = computed<Interest[]>({
		get: () => ensureResume().interests as Interest[],
		set: (value: Interest[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				interests: value,
			});
		},
	});
	const references = computed<Reference[]>({
		get: () => ensureResume().references as Reference[],
		set: (value: Reference[]) => {
			const currentResume = ensureResume();
			resumeStore.setResume({
				...currentResume,
				references: value,
			});
		},
	});
	const projects = computed<Project[]>({
		get: () => ensureResume().projects as Project[],
		set: (value: Project[]) => {
			const currentResume = ensureResume();
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
