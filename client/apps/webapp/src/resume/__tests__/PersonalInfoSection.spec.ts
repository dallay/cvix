import { mount } from "@vue/test-utils";
import { createPinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { nextTick } from "vue";
import PersonalInfoSection from "../components/PersonalInfoSection.vue";

// Mock the i18n plugin
vi.mock("vue-i18n", () => ({
	useI18n: () => ({
		t: (key: string) => key,
		locale: { value: "en" },
	}),
}));

describe("PersonalInfoSection", () => {
	let pinia: ReturnType<typeof createPinia>;

	beforeEach(() => {
		pinia = createPinia();
		vi.clearAllMocks();
	});

	it("should render all personal info fields", () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		expect(wrapper.find('[data-testid="fullname-input"]').exists()).toBe(true);
		expect(wrapper.find('[data-testid="email-input"]').exists()).toBe(true);
		expect(wrapper.find('[data-testid="phone-input"]').exists()).toBe(true);
		expect(wrapper.find('[data-testid="location-input"]').exists()).toBe(true);
		expect(wrapper.find('[data-testid="summary-textarea"]').exists()).toBe(
			true,
		);
	});

	it("should render optional fields", () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		expect(wrapper.find('[data-testid="linkedin-input"]').exists()).toBe(true);
		expect(wrapper.find('[data-testid="github-input"]').exists()).toBe(true);
		expect(wrapper.find('[data-testid="website-input"]').exists()).toBe(true);
	});

	it("should show validation error when fullName is empty and blurred", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const nameInput = wrapper.find('[data-testid="fullname-input"]');
		await nameInput.setValue("");
		await nameInput.trigger("blur");
		await nextTick();

		expect(wrapper.text()).toContain("resume.validation.required");
	});

	it("should show validation error when email is invalid", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const emailInput = wrapper.find('[data-testid="email-input"]');
		await emailInput.setValue("invalid-email");
		await emailInput.trigger("blur");
		await nextTick();

		expect(wrapper.text()).toContain("resume.validation.email");
	});

	it("should show validation error when fullName exceeds 100 characters", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const nameInput = wrapper.find('[data-testid="fullname-input"]');
		const longName = "A".repeat(101);
		await nameInput.setValue(longName);
		await nameInput.trigger("blur");
		await nextTick();

		expect(wrapper.text()).toContain("resume.validation.max_length");
	});

	it("should show validation error when summary exceeds 500 characters", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const summaryTextarea = wrapper.find('[data-testid="summary-textarea"]');
		const longSummary = "A".repeat(501);
		await summaryTextarea.setValue(longSummary);
		await summaryTextarea.trigger("blur");
		await nextTick();

		expect(wrapper.text()).toContain("resume.validation.max_length");
	});

	it("should accept valid email format", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const emailInput = wrapper.find('[data-testid="email-input"]');
		await emailInput.setValue("valid.email@example.com");
		await emailInput.trigger("blur");
		await nextTick();

		// Should not show validation error
		expect(wrapper.find('[data-testid="email-error"]').exists()).toBe(false);
	});

	it("should accept valid phone number format", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const phoneInput = wrapper.find('[data-testid="phone-input"]');
		await phoneInput.setValue("+1234567890");
		await phoneInput.trigger("blur");
		await nextTick();

		// Should not show validation error
		expect(wrapper.find('[data-testid="phone-error"]').exists()).toBe(false);
	});

	it("should validate URL format for LinkedIn", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const linkedinInput = wrapper.find('[data-testid="linkedin-input"]');
		await linkedinInput.setValue("not-a-url");
		await linkedinInput.trigger("blur");
		await nextTick();

		expect(wrapper.text()).toContain("resume.validation.url");
	});

	it("should accept valid URL for LinkedIn", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const linkedinInput = wrapper.find('[data-testid="linkedin-input"]');
		await linkedinInput.setValue("https://linkedin.com/in/johndoe");
		await linkedinInput.trigger("blur");
		await nextTick();

		expect(wrapper.find('[data-testid="linkedin-error"]').exists()).toBe(false);
	});

	it("should validate URL format for GitHub", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const githubInput = wrapper.find('[data-testid="github-input"]');
		await githubInput.setValue("not-a-url");
		await githubInput.trigger("blur");
		await nextTick();

		expect(wrapper.text()).toContain("resume.validation.url");
	});

	it("should accept valid URL for GitHub", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const githubInput = wrapper.find('[data-testid="github-input"]');
		await githubInput.setValue("https://github.com/johndoe");
		await githubInput.trigger("blur");
		await nextTick();

		expect(wrapper.find('[data-testid="github-error"]').exists()).toBe(false);
	});

	it("should validate URL format for website", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const websiteInput = wrapper.find('[data-testid="website-input"]');
		await websiteInput.setValue("not-a-url");
		await websiteInput.trigger("blur");
		await nextTick();

		expect(wrapper.text()).toContain("resume.validation.url");
	});

	it("should accept valid URL for website", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const websiteInput = wrapper.find('[data-testid="website-input"]');
		await websiteInput.setValue("https://johndoe.com");
		await websiteInput.trigger("blur");
		await nextTick();

		expect(wrapper.find('[data-testid="website-error"]').exists()).toBe(false);
	});

	it("should display character count for summary", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const summaryTextarea = wrapper.find('[data-testid="summary-textarea"]');
		await summaryTextarea.setValue("Test summary");
		await nextTick();

		const charCount = wrapper.find('[data-testid="summary-char-count"]');
		expect(charCount.exists()).toBe(true);
		expect(charCount.text()).toContain("12");
		expect(charCount.text()).toContain("500");
	});

	it("should emit update event when field values change", async () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const nameInput = wrapper.find('[data-testid="fullname-input"]');
		await nameInput.setValue("John Doe");
		await nextTick();

		expect(wrapper.emitted("update:personalInfo")).toBeTruthy();
	});

	it("should show all fields are required except optional ones", () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		// Required fields should have asterisk or required indicator
		const nameLabel = wrapper.find('[data-testid="fullname-label"]');
		const emailLabel = wrapper.find('[data-testid="email-label"]');
		const phoneLabel = wrapper.find('[data-testid="phone-label"]');

		expect(nameLabel.text()).toContain("*");
		expect(emailLabel.text()).toContain("*");
		expect(phoneLabel.text()).toContain("*");
	});

	it("should show placeholders for all fields", () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		expect(
			wrapper.find('[data-testid="fullname-input"]').attributes("placeholder"),
		).toBeTruthy();
		expect(
			wrapper.find('[data-testid="email-input"]').attributes("placeholder"),
		).toBeTruthy();
		expect(
			wrapper.find('[data-testid="phone-input"]').attributes("placeholder"),
		).toBeTruthy();
	});

	it("should have proper accessibility labels", () => {
		const wrapper = mount(PersonalInfoSection, {
			global: {
				plugins: [pinia],
			},
		});

		const nameInput = wrapper.find('[data-testid="fullname-input"]');
		const emailInput = wrapper.find('[data-testid="email-input"]');

		expect(
			nameInput.attributes("aria-label") ||
				nameInput.attributes("aria-labelledby"),
		).toBeTruthy();
		expect(
			emailInput.attributes("aria-label") ||
				emailInput.attributes("aria-labelledby"),
		).toBeTruthy();
	});
});
