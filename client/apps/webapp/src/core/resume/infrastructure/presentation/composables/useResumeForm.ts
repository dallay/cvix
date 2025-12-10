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
	const basics = computed<MutableBasics>({
		get: () => {
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
		},
		set: (value: MutableBasics) => {
			const currentResume = resumeStore.resume || {
				basics: value,
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
			resumeStore.setResume({
				...currentResume,
				basics: value,
			});
		},
	});
	const workExperiences = computed<Work[]>({
		get: () => (resumeStore.resume?.work ? [...resumeStore.resume.work] : []),
		set: (value: Work[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: value,
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
			resumeStore.setResume({
				...currentResume,
				work: value,
			});
		},
	});
	const volunteers = computed<Volunteer[]>({
		get: () =>
			resumeStore.resume?.volunteer ? [...resumeStore.resume.volunteer] : [],
		set: (value: Volunteer[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: workExperiences.value,
				volunteer: value,
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
			resumeStore.setResume({
				...currentResume,
				volunteer: value,
			});
		},
	});
	const education = computed<Education[]>({
		get: () =>
			resumeStore.resume?.education ? [...resumeStore.resume.education] : [],
		set: (value: Education[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: workExperiences.value,
				volunteer: volunteers.value,
				education: value,
				awards: [],
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};
			resumeStore.setResume({
				...currentResume,
				education: value,
			});
		},
	});
	const awards = computed<Award[]>({
		get: () =>
			resumeStore.resume?.awards ? [...resumeStore.resume.awards] : [],
		set: (value: Award[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: workExperiences.value,
				volunteer: volunteers.value,
				education: education.value,
				awards: value,
				certificates: [],
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};
			resumeStore.setResume({
				...currentResume,
				awards: value,
			});
		},
	});
	const certificates = computed<Certificate[]>({
		get: () =>
			resumeStore.resume?.certificates
				? [...resumeStore.resume.certificates]
				: [],
		set: (value: Certificate[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: workExperiences.value,
				volunteer: volunteers.value,
				education: education.value,
				awards: awards.value,
				certificates: value,
				publications: [],
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};
			resumeStore.setResume({
				...currentResume,
				certificates: value,
			});
		},
	});
	const publications = computed<Publication[]>({
		get: () =>
			resumeStore.resume?.publications
				? [...resumeStore.resume.publications]
				: [],
		set: (value: Publication[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: workExperiences.value,
				volunteer: volunteers.value,
				education: education.value,
				awards: awards.value,
				certificates: certificates.value,
				publications: value,
				skills: [],
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};
			resumeStore.setResume({
				...currentResume,
				publications: value,
			});
		},
	});
	const skills = computed<Skill[]>({
		get: () =>
			resumeStore.resume?.skills ? [...resumeStore.resume.skills] : [],
		set: (value: Skill[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: workExperiences.value,
				volunteer: volunteers.value,
				education: education.value,
				awards: awards.value,
				certificates: certificates.value,
				publications: publications.value,
				skills: value,
				languages: [],
				interests: [],
				references: [],
				projects: [],
			};
			resumeStore.setResume({
				...currentResume,
				skills: value,
			});
		},
	});
	const languages = computed<Language[]>({
		get: () =>
			resumeStore.resume?.languages ? [...resumeStore.resume.languages] : [],
		set: (value: Language[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: workExperiences.value,
				volunteer: volunteers.value,
				education: education.value,
				awards: awards.value,
				certificates: certificates.value,
				publications: publications.value,
				skills: skills.value,
				languages: value,
				interests: [],
				references: [],
				projects: [],
			};
			resumeStore.setResume({
				...currentResume,
				languages: value,
			});
		},
	});
	const interests = computed<Interest[]>({
		get: () =>
			resumeStore.resume?.interests ? [...resumeStore.resume.interests] : [],
		set: (value: Interest[]) => {
			const currentResume = resumeStore.resume || {
				basics: basics.value,
				work: workExperiences.value,
				volunteer: volunteers.value,
				education: education.value,
				awards: awards.value,
				certificates: certificates.value,
				publications: publications.value,
				skills: skills.value,
				languages: languages.value,
				interests: value,
				references: [],
				projects: [],
			};
			resumeStore.setResume({
				...currentResume,
				interests: value,
			});
		},
	});
	const references = computed<Reference[]>({
		get: () =>
			resumeStore.resume?.references ? [...resumeStore.resume.references] : [],
		set: (value: Reference[]) => {
			const currentResume = resumeStore.resume || {
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
				references: value,
				projects: [],
			};
			resumeStore.setResume({
				...currentResume,
				references: value,
			});
		},
	});
	const projects = computed<Project[]>({
		get: () =>
			resumeStore.resume?.projects ? [...resumeStore.resume.projects] : [],
		set: (value: Project[]) => {
			const currentResume = resumeStore.resume || {
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
				projects: value,
			};
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
