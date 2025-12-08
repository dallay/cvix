import userEvent from "@testing-library/user-event";
import { render, screen, waitFor, within } from "@testing-library/vue";
import { describe, expect, it, vi } from "vitest";
import { createI18n } from "vue-i18n";
import type { TemplateMetadata } from "@/core/resume/domain/TemplateMetadata";
import PdfTemplateSelector from "./PdfTemplateSelector.vue";

const i18n = createI18n({
	legacy: false,
	locale: "en",
	messages: {
		en: {
			resume: {
				pdfSelector: {
					templateLabel: "Template",
					loading: "Loading templates…",
					noTemplates: "No templates available",
					coreOptions: "Appearance & Language",
					param: {
						locale: "Language",
						fontFamily: "Font Family",
						colorPalette: "Color Scheme",
					},
					selectLocale: "Select language",
					selectFont: "Select font",
					selectColor: "Select color",
				},
			},
		},
	},
});

const mockTemplates: TemplateMetadata[] = [
	{
		id: "classic",
		name: "Classic",
		version: "1.0.0",
		description: "A timeless professional template",
		supportedLocales: ["en", "es"],
		previewUrl: "/templates/classic.png",
		params: {
			colorPalette: "blue",
			fontFamily: "serif",
			spacing: "normal",
		},
	},
	{
		id: "modern",
		name: "Modern",
		version: "1.0.0",
		description: "A sleek contemporary design",
		supportedLocales: ["en", "es", "fr"],
		previewUrl: "/templates/modern.png",
		params: {
			colorPalette: "purple",
			fontFamily: "sans-serif",
			spacing: "compact",
		},
	},
	{
		id: "minimal",
		name: "Minimal",
		version: "1.0.0",
		description: "Clean and simple layout",
		supportedLocales: ["en"],
		params: {
			colorPalette: "gray",
			fontFamily: "sans-serif",
		},
	},
];

describe("PdfTemplateSelector", () => {
	describe("Template Card Selection", () => {
		it("should render template cards instead of dropdown", () => {
			const modelValue = { templateId: "", params: {} };
			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			// Verify cards are rendered
			expect(
				container.querySelector("button[aria-label='Select Classic template']"),
			).toBeInTheDocument();
			expect(
				container.querySelector("button[aria-label='Select Modern template']"),
			).toBeInTheDocument();
			expect(
				container.querySelector("button[aria-label='Select Minimal template']"),
			).toBeInTheDocument();

			// Verify descriptions are shown
			expect(
				screen.getByText("A timeless professional template"),
			).toBeInTheDocument();
			expect(
				screen.getByText("A sleek contemporary design"),
			).toBeInTheDocument();
			expect(screen.getByText("Clean and simple layout")).toBeInTheDocument();
		});

		it("should show active state for selected template", () => {
			const modelValue = { templateId: "modern", params: {} };
			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			const modernCard = container.querySelector(
				"button[aria-label='Select Modern template']",
			);
			expect(modernCard).toHaveClass("border-primary");
			expect(modernCard).toHaveAttribute("aria-pressed", "true");
		});

		it("should emit update:modelValue when template card is clicked", async () => {
			const user = userEvent.setup();
			const modelValue = { templateId: "", params: {} };
			const onUpdate = vi.fn();

			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
					"onUpdate:modelValue": onUpdate,
				},
				global: {
					plugins: [i18n],
				},
			});

			const classicCard = container.querySelector(
				"button[aria-label='Select Classic template']",
			);
			if (!classicCard) throw new Error("Classic card not found");
			await user.click(classicCard);

			await waitFor(() => {
				expect(onUpdate).toHaveBeenCalledWith(
					expect.objectContaining({
						templateId: "classic",
						params: expect.objectContaining({
							colorPalette: "blue",
							fontFamily: "serif",
							spacing: "normal",
						}),
					}),
				);
			});
		});

		it("should support keyboard navigation", async () => {
			const user = userEvent.setup();
			const modelValue = { templateId: "", params: {} };
			const onUpdate = vi.fn();

			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
					"onUpdate:modelValue": onUpdate,
				},
				global: {
					plugins: [i18n],
				},
			});

			const classicCard = container.querySelector(
				"button[aria-label='Select Classic template']",
			) as HTMLElement;
			if (!classicCard) throw new Error("Classic card not found");

			// Focus and activate with keyboard
			classicCard.focus();
			expect(classicCard).toHaveFocus();

			await user.keyboard("{Enter}");

			await waitFor(() => {
				expect(onUpdate).toHaveBeenCalled();
			});
		});

		it("should show checkmark icon for active template", () => {
			const modelValue = { templateId: "classic", params: {} };
			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			const classicCard = container.querySelector(
				"button[aria-label='Select Classic template']",
			);

			// Verify checkmark indicator is present for active template
			const checkmark = classicCard?.querySelector(
				'[data-testid="template-selected-indicator"]',
			);
			expect(checkmark).toBeInTheDocument();
		});
	});

	describe("Loading and Error States", () => {
		it("should show loading state", () => {
			const modelValue = { templateId: "", params: {} };
			render(PdfTemplateSelector, {
				props: {
					templates: [],
					modelValue,
					isLoading: true,
				},
				global: {
					plugins: [i18n],
				},
			});

			expect(screen.getByText("Loading templates…")).toBeInTheDocument();
		});

		it("should show error state", () => {
			const modelValue = { templateId: "", params: {} };
			const errorMessage = "Failed to load templates";
			render(PdfTemplateSelector, {
				props: {
					templates: [],
					modelValue,
					error: errorMessage,
				},
				global: {
					plugins: [i18n],
				},
			});

			expect(screen.getByText(errorMessage)).toBeInTheDocument();
		});

		it("should show empty state", () => {
			const modelValue = { templateId: "", params: {} };
			render(PdfTemplateSelector, {
				props: {
					templates: [],
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			expect(screen.getByText("No templates available")).toBeInTheDocument();
		});
	});

	describe("Appearance Controls (Dropdowns)", () => {
		it("should render appearance controls as dropdowns", async () => {
			const modelValue = {
				templateId: "classic",
				params: { locale: "en", fontFamily: "serif", colorPalette: "blue" },
			};

			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			// Verify the section title
			const sectionTitle = within(container as HTMLElement).getByText(
				"Appearance & Language",
			);
			expect(sectionTitle).toBeInTheDocument();

			// Verify dropdown labels using container scoping
			const labels = container.querySelectorAll("label");
			const labelTexts = Array.from(labels).map((label) =>
				label.textContent?.trim(),
			);
			expect(labelTexts).toContain("Language");
			expect(labelTexts).toContain("Font Family");
			expect(labelTexts).toContain("Color Scheme");
		});

		it("should maintain dropdown behavior for language/font/color", async () => {
			const modelValue = {
				templateId: "classic",
				params: { locale: "en" },
			};
			const onUpdate = vi.fn();

			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
					"onUpdate:modelValue": onUpdate,
				},
				global: {
					plugins: [i18n],
				},
			});

			// Language dropdown should exist
			const localeLabel = within(container as HTMLElement).getByText(
				"Language",
			);
			expect(localeLabel).toBeInTheDocument();
		});
	});

	describe("Accessibility", () => {
		it("should have proper ARIA attributes on template cards", () => {
			const modelValue = { templateId: "modern", params: {} };
			render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			const cards = screen.getAllByRole("button");

			cards.forEach((card) => {
				// Each card should have aria-label
				expect(card).toHaveAttribute("aria-label");
				// Each card should have aria-pressed state
				expect(card).toHaveAttribute("aria-pressed");
			});
		});

		it("should be focusable with keyboard", () => {
			const modelValue = { templateId: "", params: {} };
			render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			const cards = screen.getAllByRole("button");

			cards.forEach((card) => {
				card.focus();
				expect(card).toHaveFocus();
			});
		});

		it("should have visible focus indicators", () => {
			const modelValue = { templateId: "", params: {} };
			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			const cards = container.querySelectorAll("button");

			cards.forEach((card) => {
				// Should have focus styles
				expect(card.className).toContain("focus:outline-none");
				expect(card.className).toContain("focus:ring-2");
				expect(card.className).toContain("focus:ring-primary");
			});
		});
	});

	describe("Hover Interactions", () => {
		it("should have hover styles on template cards", () => {
			const modelValue = { templateId: "", params: {} };
			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			const cards = container.querySelectorAll("button");

			cards.forEach((card) => {
				expect(card.className).toContain("hover:shadow-md");
				expect(card.className).toContain("hover:border-primary/50");
			});
		});
	});

	describe("Responsive Behavior", () => {
		it("should render cards in a vertical stack", () => {
			const modelValue = { templateId: "", params: {} };
			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			const cardContainer = container.querySelector(".space-y-2");
			expect(cardContainer).toBeInTheDocument();
		});

		it("should use full width for cards", () => {
			const modelValue = { templateId: "", params: {} };
			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			const cards = container.querySelectorAll("button");

			cards.forEach((card) => {
				expect(card.className).toContain("w-full");
			});
		});
	});
});
