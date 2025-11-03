import { defineStore } from "pinia";
import { computed, ref } from "vue";
import type {
	ApiError,
	Education,
	Resume,
	ResumeData,
	SkillCategory,
	WorkExperience,
} from "@/resume/types/resume";
import { resumeSchema } from "@/resume/validation/resumeSchema";

export const useResumeStore = defineStore("resume", () => {
	// State
	const resume = ref<Resume>({
		basics: {
			name: "",
			email: "",
		},
		work: [
			{
				company: "",
				position: "",
				startDate: "",
			},
		],
		education: [],
		skills: [],
	});

	const isGenerating = ref(false);
	const generationError = ref<ApiError | null>(null);

	// Getters
	const isValid = computed(() => {
		try {
			resumeSchema.parse(resume.value);
			return true;
		} catch {
			return false;
		}
	});

	const hasContent = computed(() => {
		return (
			(resume.value.work && resume.value.work.length > 0) ||
			(resume.value.education && resume.value.education.length > 0) ||
			(resume.value.skills && resume.value.skills.length > 0)
		);
	});

	// Transform Resume to ResumeData format for preview component
	const resumeData = computed<ResumeData>(() => ({
		personalInfo: resume.value.basics,
		workExperience: resume.value.work,
		education: resume.value.education,
		skills: resume.value.skills,
		languages: resume.value.languages,
		projects: resume.value.projects,
	}));

	// Actions
	function updatePersonalInfo(info: Resume["basics"]) {
		resume.value.basics = info;
	}

	function addWorkExperience() {
		if (!resume.value.work) {
			resume.value.work = [];
		}
		const newEntry: WorkExperience = {
			company: "",
			position: "",
			startDate: "",
		};
		resume.value.work.push(newEntry);
	}

	function removeWorkExperience(index: number) {
		if (resume.value.work) {
			resume.value.work.splice(index, 1);
		}
	}

	function updateWorkExperience(index: number, data: WorkExperience) {
		if (resume.value.work?.[index]) {
			resume.value.work[index] = data;
		}
	}

	function addEducation() {
		if (!resume.value.education) {
			resume.value.education = [];
		}
		const newEntry: Education = {
			institution: "",
			area: "",
			studyType: "",
			startDate: "",
		};
		resume.value.education.push(newEntry);
	}

	function removeEducation(index: number) {
		if (resume.value.education) {
			resume.value.education.splice(index, 1);
		}
	}

	function updateEducation(index: number, data: Education) {
		if (resume.value.education?.[index]) {
			resume.value.education[index] = data;
		}
	}

	function addSkillCategory() {
		if (!resume.value.skills) {
			resume.value.skills = [];
		}
		const newEntry: SkillCategory = {
			category: "",
			keywords: [],
		};
		resume.value.skills.push(newEntry);
	}

	function removeSkillCategory(index: number) {
		if (resume.value.skills) {
			resume.value.skills.splice(index, 1);
		}
	}

	function updateSkillCategory(index: number, data: SkillCategory) {
		if (resume.value.skills?.[index]) {
			resume.value.skills[index] = data;
		}
	}

	function resetResume() {
		resume.value = {
			basics: {
				name: "",
				email: "",
			},
		};
		generationError.value = null;
	}

	function setGenerating(value: boolean) {
		isGenerating.value = value;
	}

	function setGenerationError(error: ApiError | null) {
		generationError.value = error;
	}

	return {
		// State
		resume,
		isGenerating,
		generationError,

		// Getters
		isValid,
		hasContent,
		resumeData,

		// Actions
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
		resetResume,
		setGenerating,
		setGenerationError,
	};
});
