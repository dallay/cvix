import { type ComputedRef, computed, onMounted, type Ref } from "vue";
import type {
	Award,
	Basics,
	Certificate,
	Education,
	Interest,
	Language,
	Location,
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

	// Computed refs backed by Pinia store state
	const basics = computed<MutableBasics>(() => {
		const b = resumeStore.resume?.basics;
		return b
			? { ...b, profiles: [...b.profiles] }
			: {
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
					} as Location,
					profiles: [],
				};
	});
	const workExperiences = computed<Work[]>(() =>
		resumeStore.resume?.work ? [...resumeStore.resume.work] : [],
	);
	const volunteers = computed<Volunteer[]>(() =>
		resumeStore.resume?.volunteer ? [...resumeStore.resume.volunteer] : [],
	);
	const education = computed<Education[]>(() =>
		resumeStore.resume?.education ? [...resumeStore.resume.education] : [],
	);
	const awards = computed<Award[]>(() =>
		resumeStore.resume?.awards ? [...resumeStore.resume.awards] : [],
	);
	const certificates = computed<Certificate[]>(() =>
		resumeStore.resume?.certificates
			? [...resumeStore.resume.certificates]
			: [],
	);
	const publications = computed<Publication[]>(() =>
		resumeStore.resume?.publications
			? [...resumeStore.resume.publications]
			: [],
	);
	const skills = computed<Skill[]>(() =>
		resumeStore.resume?.skills ? [...resumeStore.resume.skills] : [],
	);
	const languages = computed<Language[]>(() =>
		resumeStore.resume?.languages ? [...resumeStore.resume.languages] : [],
	);
	const interests = computed<Interest[]>(() =>
		resumeStore.resume?.interests ? [...resumeStore.resume.interests] : [],
	);
	const references = computed<Reference[]>(() =>
		resumeStore.resume?.references ? [...resumeStore.resume.references] : [],
	);
	const projects = computed<Project[]>(() =>
		resumeStore.resume?.projects ? [...resumeStore.resume.projects] : [],
	);

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
