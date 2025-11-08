import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type {
	Education,
	Language,
	Project,
	Resume,
	ResumeData,
	SkillCategory,
	WorkExperience,
} from "@/core/resume-v1/types/resume.ts";
import { resumeSchema } from "@/core/resume-v1/validation/resumeSchema.ts";
import type { ProblemDetail } from "@/shared/BaseHttpClient.ts";

const INITIAL_WORK_ITEM: WorkExperience = {
	company: "",
	position: "",
	startDate: "",
};

const INITIAL_EDUCATION_ITEM: Education = {
	institution: "",
	area: "",
	studyType: "",
	startDate: "",
};

const INITIAL_SKILL_ITEM: SkillCategory = {
	category: "",
	keywords: [],
};

const INITIAL_LANGUAGE_ITEM: Language = {
	language: "",
	fluency: "",
};

const INITIAL_PROJECT_ITEM: Project = {
	name: "",
	description: "",
	url: "",
	startDate: "",
	endDate: "",
};

const getInitialResumeState = (): Resume => ({
	basics: {
		name: "",
		email: "",
	},
	work: [{ ...INITIAL_WORK_ITEM }],
	education: [],
	skills: [],
});

export const useResumeStore = defineStore("resume", () => {
	const resume = ref<Resume>(getInitialResumeState());
	const isGenerating = ref(false);
	const generationError = ref<ProblemDetail | null>(null);

	// Computed
	const isValid = computed(() => {
		try {
			resumeSchema.parse(resume.value);
			return true;
		} catch {
			return false;
		}
	});

	const hasContent = computed(
		() =>
			!!(
				resume.value.work?.length ||
				resume.value.education?.length ||
				resume.value.skills?.length
			),
	);

	const resumeData = computed<ResumeData>(() => ({
		personalInfo: resume.value.basics,
		workExperience: resume.value.work,
		education: resume.value.education,
		skills: resume.value.skills,
		languages: resume.value.languages,
		projects: resume.value.projects,
	}));

	// Generic CRUD operations factory
	const createArrayOperations = <T>(key: keyof Resume) => {
		const getArray = (): T[] => {
			const value = resume.value[key];
			return (Array.isArray(value) ? value : []) as T[];
		};

		return {
			add: (item: T) => {
				const current = getArray();
				resume.value = {
					...resume.value,
					[key]: [...current, item],
				};
			},
			remove: (index: number) => {
				const current = getArray();
				resume.value = {
					...resume.value,
					[key]: current.filter((_, i) => i !== index),
				};
			},
			update: (index: number, data: T) => {
				const current = getArray();
				if (current[index]) {
					resume.value = {
						...resume.value,
						[key]: current.map((item, i) => (i === index ? data : item)),
					};
				}
			},
		};
	};

	// Work Experience
	const workOps = createArrayOperations<WorkExperience>("work");
	const addWorkExperience = () => workOps.add({ ...INITIAL_WORK_ITEM });
	const removeWorkExperience = workOps.remove;
	const updateWorkExperience = workOps.update;

	// Education
	const educationOps = createArrayOperations<Education>("education");
	const addEducation = () => educationOps.add({ ...INITIAL_EDUCATION_ITEM });
	const removeEducation = educationOps.remove;
	const updateEducation = educationOps.update;

	// Skills
	const skillOps = createArrayOperations<SkillCategory>("skills");
	const addSkillCategory = () => skillOps.add({ ...INITIAL_SKILL_ITEM });
	const removeSkillCategory = skillOps.remove;
	const updateSkillCategory = skillOps.update;

	// Languages
	const languageOps = createArrayOperations<Language>("languages");
	const addLanguage = () => languageOps.add({ ...INITIAL_LANGUAGE_ITEM });
	const removeLanguage = languageOps.remove;
	const updateLanguage = languageOps.update;

	// Projects
	const projectOps = createArrayOperations<Project>("projects");
	const addProject = () => projectOps.add({ ...INITIAL_PROJECT_ITEM });
	const removeProject = projectOps.remove;
	const updateProject = projectOps.update;

	// Core actions
	const updatePersonalInfo = (info: Resume["basics"]) => {
		resume.value.basics = info;
	};

	const resetResume = () => {
		resume.value = getInitialResumeState();
		generationError.value = null;
	};

	const setGenerating = (value: boolean) => {
		isGenerating.value = value;
	};

	const setGenerationError = (error: ProblemDetail | null) => {
		generationError.value = error;
	};

	return {
		resume,
		isGenerating,
		generationError,
		isValid,
		hasContent,
		resumeData,
		updatePersonalInfo,
		addWorkExperience,
		removeWorkExperience,
		updateWorkExperience,
		addEducation,
		removeEducation,
		updateEducation,
		addSkillCategory,
		removeSkillCategory,
		updateSkillCategory,
		addLanguage,
		removeLanguage,
		updateLanguage,
		addProject,
		removeProject,
		updateProject,
		resetResume,
		setGenerating,
		setGenerationError,
	};
});
