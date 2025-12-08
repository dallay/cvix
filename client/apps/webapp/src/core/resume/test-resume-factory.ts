import type { Resume } from "@/core/resume/domain/Resume.ts";

const DEFAULT_BASICS: Resume["basics"] = {
	name: "John Doe",
	label: "Software Engineer",
	image: "profile.jpg",
	email: "john@example.com",
	phone: "+1-555-0100",
	url: "https://johndoe.com",
	summary: "Experienced software engineer",
	location: {
		address: "123 Main St",
		postalCode: "12345",
		city: "San Francisco",
		countryCode: "US",
		region: "CA",
	},
	profiles: [
		{
			network: "GitHub",
			username: "johndoe",
			url: "https://github.com/johndoe",
		},
		{
			network: "LinkedIn",
			username: "johndoe",
			url: "https://linkedin.com/in/johndoe",
		},
	],
};

const DEFAULT_WORK: Resume["work"] = [
	{
		name: "Company A",
		position: "Senior Developer",
		url: "https://companya.com",
		startDate: "2020-01-01",
		endDate: "2022-12-31",
		summary: "Led development team",
		highlights: ["Achievement 1", "Achievement 2"],
	},
	{
		name: "Company B",
		position: "Developer",
		url: "https://companyb.com",
		startDate: "2018-01-01",
		endDate: "2019-12-31",
		summary: "Developed features",
		highlights: [],
	},
];

const DEFAULT_EDUCATION: Resume["education"] = [
	{
		institution: "University A",
		url: "https://university-a.edu",
		area: "Computer Science",
		studyType: "Bachelor",
		startDate: "2014-09-01",
		endDate: "2018-06-01",
		score: "3.8",
		courses: ["CS101", "CS201"],
	},
];

const DEFAULT_SKILLS: Resume["skills"] = [
	{ name: "JavaScript", level: "Expert", keywords: ["ES6", "Node.js"] },
	{
		name: "TypeScript",
		level: "Advanced",
		keywords: ["Types", "Interfaces"],
	},
	{ name: "Python", level: "Intermediate", keywords: ["Django", "Flask"] },
];

const DEFAULT_PROJECTS: Resume["projects"] = [
	{
		name: "Project Alpha",
		startDate: "2021-01-01",
		endDate: "2021-12-31",
		description: "A cool project",
		highlights: [],
		url: "https://project-alpha.com",
	},
];

const DEFAULT_CERTIFICATES: Resume["certificates"] = [
	{
		name: "AWS Certified",
		date: "2021-06-01",
		url: "https://aws.amazon.com",
		issuer: "Amazon",
	},
];

const DEFAULT_VOLUNTEER: Resume["volunteer"] = [
	{
		organization: "Code for Good",
		position: "Volunteer Developer",
		url: "https://codeforgood.org",
		startDate: "2020-01-01",
		endDate: "",
		summary: "Volunteering",
		highlights: [],
	},
];

const DEFAULT_AWARDS: Resume["awards"] = [
	{
		title: "Best Developer 2021",
		date: "2021-12-01",
		awarder: "Company A",
		summary: "Excellence in development",
	},
];

const DEFAULT_PUBLICATIONS: Resume["publications"] = [
	{
		name: "Article on TypeScript",
		publisher: "Medium",
		releaseDate: "2022-01-01",
		url: "https://medium.com/article",
		summary: "How to use TypeScript",
	},
];

const DEFAULT_LANGUAGES: Resume["languages"] = [
	{ language: "English", fluency: "Native" },
];

const DEFAULT_INTERESTS: Resume["interests"] = [
	{ name: "Photography", keywords: ["Landscape", "Portrait"] },
];

const DEFAULT_REFERENCES: Resume["references"] = [
	{
		name: "Jane Smith",
		reference: "John is an excellent developer",
	},
];

/**
 * Creates a test Resume object with default values, allowing overrides.
 * @param overrides Partial Resume object to override default values.
 * @returns Complete Resume object for testing.
 */
export const createTestResume = (overrides?: Partial<Resume>): Resume => ({
	basics: {
		...DEFAULT_BASICS,
		...overrides?.basics,
	},
	work: overrides?.work ?? DEFAULT_WORK,
	education: overrides?.education ?? DEFAULT_EDUCATION,
	skills: overrides?.skills ?? DEFAULT_SKILLS,
	projects: overrides?.projects ?? DEFAULT_PROJECTS,
	certificates: overrides?.certificates ?? DEFAULT_CERTIFICATES,
	volunteer: overrides?.volunteer ?? DEFAULT_VOLUNTEER,
	awards: overrides?.awards ?? DEFAULT_AWARDS,
	publications: overrides?.publications ?? DEFAULT_PUBLICATIONS,
	languages: overrides?.languages ?? DEFAULT_LANGUAGES,
	interests: overrides?.interests ?? DEFAULT_INTERESTS,
	references: overrides?.references ?? DEFAULT_REFERENCES,
});
