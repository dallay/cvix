import type Article from "@/models/article/article.model";

export const generateMockArticle = (
	overrides: Partial<Article> = {},
): Article => {
	return {
		id: "mock-article",
		title: "Mock Article",
		description: "This is a mock article.",
		cover: undefined, // Ajustar con ImageMetadata si es necesario
		category: { id: "mock-category", title: "Mock Category Title" }, // Añadida propiedad title
		tags: [{ id: "mock-tag", title: "Mock Tag Title" }], // Añadida propiedad title
		author: {
			id: "mock-author",
			name: "Mock Author",
			email: "mock@example.com",
			avatar: "",
			bio: "Mock bio",
			role: "Developer",
			location: "Mock location",
			socials: [],
		},
		draft: false,
		featured: false,
		date: new Date("2023-01-01T00:00:00.000Z"),
		lastModified: new Date("2023-01-01T00:00:00.000Z"),
		body: "This is the body of the mock article.",
		...overrides,
	};
};
