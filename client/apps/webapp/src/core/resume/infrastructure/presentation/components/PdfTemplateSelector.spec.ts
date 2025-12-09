import userEvent from "@testing-library/user-event";
import { render, screen, waitFor, within } from "@testing-library/vue";
import { describe, expect, it, vi } from "vitest";
import { createI18n } from "vue-i18n";
import type { TemplateMetadata } from "@/core/resume/domain/TemplateMetadata";
import PdfTemplateSelector from "./PdfTemplateSelector.vue";

// Mock ResizeObserver and IntersectionObserver for Embla Carousel
vi.stubGlobal(
	"ResizeObserver",
	class ResizeObserver {
		observe() {}
		unobserve() {}
		disconnect() {}
	},
);

vi.stubGlobal(
	"IntersectionObserver",
	class IntersectionObserver {
		observe() {}
		unobserve() {}
		disconnect() {}
	},
);

vi.stubGlobal("matchMedia", () => ({
	matches: false,
	addListener: () => {},
	removeListener: () => {},
}));

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
					selectTemplateAria: "Select {name} template",
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
			spacing: "compact",
		},
	},
];

describe("PdfTemplateSelector", () => {
	describe("Template Card Selection", () => {
		it("should render template cards in a carousel", () => {
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
			// Check if carousel wrapper exists (optional, simply checking cards exist might be enough but good to check structure)
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

			// Use testing-library query to get a strong, descriptive failure if the element is missing
			const classicCard = within(container as HTMLElement).getByRole("button", {
				name: "Select Classic template",
			});
			expect(classicCard).toBeInTheDocument();
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

			// Prefer a testing-library query so missing elements fail with a clear message
			const classicCardKeyboard = within(container as HTMLElement).getByRole(
				"button",
				{
					name: "Select Classic template",
				},
			);
			expect(classicCardKeyboard).toBeInTheDocument();

			// Focus and activate with keyboard
			classicCardKeyboard.focus();
			expect(classicCardKeyboard).toHaveFocus();

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

		it("should render language dropdown label", async () => {
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
			const { container } = render(PdfTemplateSelector, {
				props: {
					templates: mockTemplates,
					modelValue,
				},
				global: {
					plugins: [i18n],
				},
			});

			// Use specific selector for template cards
			const cards = container.querySelectorAll(
				"button[aria-label^='Select'][aria-label$='template']",
			);

			cards.forEach((card) => {
				// Each card should have aria-label
				expect(card).toHaveAttribute("aria-label");
				// Each card should have aria-pressed state
				expect(card).toHaveAttribute("aria-pressed");
			});
		});

		it("should be focusable with keyboard", () => {
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

			const cards = container.querySelectorAll(
				"button[aria-label^='Select'][aria-label$='template']",
			) as NodeListOf<HTMLElement>;

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

			const cards = container.querySelectorAll(
				"button[aria-label^='Select'][aria-label$='template']",
			);

			// Prefer to avoid brittle assertions tied to exact Tailwind class names (implementation detail).
			// Instead assert that the elements expose a focus-related class token (e.g., contains 'focus', 'ring', or 'outline').
			// This keeps tests robust to minor refactors while still ensuring a contract that focus styles exist.
			const hasFocusStyleClass = Array.from(cards).some((card) =>
				Array.from(card.classList).some(
					(c) =>
						c.includes("focus") || c.includes("ring") || c.includes("outline"),
				),
			);

			expect(hasFocusStyleClass).toBe(true);
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

			const cards = container.querySelectorAll(
				"button[aria-label^='Select'][aria-label$='template']",
			);

			// Avoid asserting exact Tailwind utility names. Instead check that at least one card
			// contains a hover-related class token ('hover') indicating hover styles are present.
			const hasHoverClass = Array.from(cards).some((card) =>
				Array.from(card.classList).some((c) => c.includes("hover")),
			);

			expect(hasHoverClass).toBe(true);
		});
	});

	describe("Responsive Behavior", () => {
		it("should render cards within a carousel item wrapper", () => {
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

			// Check if carousel structure exists
			const carousel = container.querySelector(".pl-4");
			expect(carousel).toBeInTheDocument();
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

			const cards = container.querySelectorAll(
				"button[aria-label^='Select'][aria-label$='template']",
			);

			cards.forEach((card) => {
				expect(card.className).toContain("w-full");
			});
		});
	});
});
