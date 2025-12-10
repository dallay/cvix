import {
	computed,
	getCurrentInstance,
	nextTick,
	onMounted,
	ref,
	watch,
} from "vue";
import type {
	Award,
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

// Define a mutable basics type explicitly to avoid depending on `Basics` import
type MutableBasics = {
	name: string;
	label: string;
	image: string;
	email: string;
	phone: string;
	url: string;
	summary: string;
	location: Location;
	profiles: Profile[];
};

// Define the return type for clarity and reuse
export interface UseResumeFormReturn {
	// form fields
	basics: import("vue").Ref<MutableBasics>;
	workExperiences: import("vue").Ref<Work[]>;
	volunteers: import("vue").Ref<Volunteer[]>;
	education: import("vue").Ref<Education[]>;
	awards: import("vue").Ref<Award[]>;
	certificates: import("vue").Ref<Certificate[]>;
	publications: import("vue").Ref<Publication[]>;
	skills: import("vue").Ref<Skill[]>;
	languages: import("vue").Ref<Language[]>;
	interests: import("vue").Ref<Interest[]>;
	references: import("vue").Ref<Reference[]>;
	projects: import("vue").Ref<Project[]>;

	// computed
	resume: import("vue").ComputedRef<Resume>;
	isValid: import("vue").ComputedRef<boolean>;
	hasResume: import("vue").ComputedRef<boolean>;
	isGenerating: import("vue").ComputedRef<boolean>;
	generationError: import("vue").ComputedRef<ProblemDetail | null>;

	// actions
	submitResume: () => boolean;
	saveToStorage: () => Promise<void>;
	generatePdf: (locale?: string) => Promise<Blob>;
	clearForm: () => void;
	loadResume: (r: Resume) => void;
}

// Module-scoped shared instance
let sharedInstance: UseResumeFormReturn | null = null;

/**
 * Composable for managing the resume form state and validation.
 * Returns a shared instance so different callers (pages/components)
 * operate on the same reactive state.
 */
export function useResumeForm(): UseResumeFormReturn {
	if (sharedInstance) return sharedInstance;

	const resumeStore = useResumeStore();

	const hasComponentInstance = Boolean(getCurrentInstance());
	// Flag para evitar que el watch sobrescriba datos durante la carga inicial
	const isInitializing = ref(hasComponentInstance);

	// Form state - cada sección del resume
	const basics = ref<MutableBasics>({
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
		profiles: [] as Profile[],
	});

	const workExperiences = ref<Work[]>([]);
	const volunteers = ref<Volunteer[]>([]);
	const education = ref<Education[]>([]);
	const awards = ref<Award[]>([]);
	const certificates = ref<Certificate[]>([]);
	const publications = ref<Publication[]>([]);
	const skills = ref<Skill[]>([]);
	const languages = ref<Language[]>([]);
	const interests = ref<Interest[]>([]);
	const references = ref<Reference[]>([]);
	const projects = ref<Project[]>([]);

	// Computed para construir el resume completo
	const resume = computed<Resume>(() => ({
		basics: basics.value,
		work: workExperiences.value,
		volunteer: volunteers.value,
		education: education.value,
		awards: awards.value,
		certificates: certificates.value,
		publications: publications.value,
		skills: skills.value,
		languages: languages.value,
		interests: interests.value,
		references: references.value,
		projects: projects.value,
	}));

	// Validación automática cuando cambia el resume
	// Solo sincronizar después de la carga inicial
	watch(
		resume,
		(newResume) => {
			if (!isInitializing.value) {
				resumeStore.setResume(newResume);
			}
		},
		{ deep: true },
	); // Estados del store
	const isValid = computed(() => resumeStore.isValid);
	const hasResume = computed(() => resumeStore.hasResume);
	const isGenerating = computed(() => resumeStore.isGenerating);
	const generationError = computed(
		() => resumeStore.generationError as ProblemDetail | null,
	);

	/**
	 * Validate the current resume form and save it to the resume store.
	 *
	 * @returns `true` if the resume is valid, `false` otherwise.
	 */
	function submitResume(): boolean {
		resumeStore.setResume(resume.value);
		return resumeStore.validateResume();
	}

	/**
	 * Persist the current resume to the configured storage.
	 */
	async function saveToStorage(): Promise<void> {
		return resumeStore.saveToStorage();
	}

	/**
	 * Generate a PDF from the current resume data.
	 *
	 * @param locale - Optional locale code (e.g., "en", "es") to localize generated content
	 * @returns The generated PDF as a `Blob`
	 */
	async function generatePdf(locale?: string): Promise<Blob> {
		return resumeStore.generatePdf(locale);
	}

	/**
	 * Reset all form sections to their empty defaults and clear the resume in the store.
	 *
	 * This resets `basics` (including `location` and `profiles`) and clears every section array
	 * (workExperiences, volunteers, education, awards, certificates, publications, skills,
	 * languages, interests, references, projects), then delegates to the resume store to clear persisted state.
	 */
	function clearForm(): void {
		basics.value = {
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
			profiles: [] as Profile[],
		};
		workExperiences.value = [];
		volunteers.value = [];
		education.value = [];
		awards.value = [];
		certificates.value = [];
		publications.value = [];
		skills.value = [];
		languages.value = [];
		interests.value = [];
		references.value = [];
		projects.value = [];

		resumeStore.clearResume();
	}

	/**
	 * Populate the form state with values from a Resume object.
	 *
	 * Clones the resume's arrays and the `basics.profiles` array before assigning to avoid retaining references to the source object.
	 *
	 * @param resumeData - The Resume object whose data should be loaded into the form
	 */
	function loadResume(resumeData: Resume): void {
		basics.value = {
			...resumeData.basics,
			profiles: [...resumeData.basics.profiles],
		};
		workExperiences.value = [...(resumeData.work || [])];
		volunteers.value = [...(resumeData.volunteer || [])];
		education.value = [...(resumeData.education || [])];
		awards.value = [...(resumeData.awards || [])];
		certificates.value = [...(resumeData.certificates || [])];
		publications.value = [...(resumeData.publications || [])];
		skills.value = [...(resumeData.skills || [])];
		languages.value = [...(resumeData.languages || [])];
		interests.value = [...(resumeData.interests || [])];
		references.value = [...(resumeData.references || [])];
		projects.value = [...(resumeData.projects || [])];
	} // Initialize: load from storage on mount
	onMounted(async () => {
		try {
			await resumeStore.loadFromStorage();
			if (resumeStore.resume) {
				loadResume(resumeStore.resume);
				// Esperar dos ticks: uno para que Vue procese los cambios, otro para que los componentes se actualicen
				await nextTick();
				await nextTick();
			}
		} catch (error) {
			console.error("Failed to load resume from storage:", error);
		} finally {
			// Permitir que el watch sincronice cambios después de la carga inicial
			isInitializing.value = false;
		}
	});

	sharedInstance = {
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
	return sharedInstance;
}
