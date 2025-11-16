import { describe, it, expect, beforeEach } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import BasicsSection from './BasicsSection.vue';
import type { Basics } from '@/core/resume/domain/Resume';

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  messages: {
    en: {
      resume: {
        actions: {
          descriptions: {
            personalInfo: 'Enter your personal information',
          },
        },
        fields: {
          fullName: 'Full Name',
          label: 'Professional Title',
          image: 'Profile Picture',
          email: 'Email',
          phone: 'Phone',
          url: 'Website',
          summary: 'Professional Summary',
          location: {
            address: 'Street Address',
            city: 'City',
            postalCode: 'Postal Code',
            countryCode: 'Country Code',
            region: 'Region',
          },
        },
        placeholders: {
          name: 'John Doe',
          label: 'Software Engineer',
          image: 'https://example.com/photo.jpg',
          imageHint: 'Enter a URL to your profile picture',
          email: 'john@example.com',
          phone: '+1-555-0100',
          url: 'https://johndoe.com',
          summary: 'Brief professional summary...',
          location: {
            address: '123 Main St',
            city: 'San Francisco',
            postalCode: '94102',
            countryCode: 'US',
            region: 'California',
          },
        },
      },
    },
  },
});

describe('BasicsSection.vue', () => {
  let mockBasics: Basics;

  beforeEach(() => {
    mockBasics = {
      name: '',
      label: '',
      image: '',
      email: '',
      phone: '',
      url: '',
      summary: '',
      location: {
        address: '',
        postalCode: '',
        city: '',
        countryCode: '',
        region: '',
      },
      profiles: [],
    };
  });

  const mountComponent = (basics: Basics = mockBasics) => {
    return mount(BasicsSection, {
      props: {
        modelValue: basics,
      },
      global: {
        plugins: [i18n],
      },
    });
  };

  describe('rendering', () => {
    it('should render all form fields', () => {
      const wrapper = mountComponent();

      expect(wrapper.find('[data-testid="fullname-input"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="label-short-description-input"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="profile-picture-input"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="email-input"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="phone-input"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="url-input"]').exists()).toBe(true);
    });

    it('should render location fields', () => {
      const wrapper = mountComponent();

      expect(wrapper.find('#street').exists()).toBe(true);
      expect(wrapper.find('#city').exists()).toBe(true);
      expect(wrapper.find('#zip').exists()).toBe(true);
      expect(wrapper.find('#countryCode').exists()).toBe(true);
      expect(wrapper.find('#region').exists()).toBe(true);
    });

    it('should display field labels', () => {
      const wrapper = mountComponent();

      expect(wrapper.text()).toContain('Full Name');
      expect(wrapper.text()).toContain('Professional Title');
      expect(wrapper.text()).toContain('Email');
      expect(wrapper.text()).toContain('Phone');
      expect(wrapper.text()).toContain('Website');
      expect(wrapper.text()).toContain('Professional Summary');
    });

    it('should display placeholders', () => {
      const wrapper = mountComponent();

      const nameInput = wrapper.find('[data-testid="fullname-input"]');
      expect(nameInput.attributes('placeholder')).toBe('John Doe');

      const emailInput = wrapper.find('[data-testid="email-input"]');
      expect(emailInput.attributes('placeholder')).toBe('john@example.com');
    });
  });

  describe('v-model binding', () => {
    it('should bind name field to model', async () => {
      const wrapper = mountComponent();
      const nameInput = wrapper.find('[data-testid="fullname-input"]');

      await nameInput.setValue('Jane Doe');

      // Check that input value changed
      expect((nameInput.element as HTMLInputElement).value).toBe('Jane Doe');
    });

    it('should bind email field to model', async () => {
      const wrapper = mountComponent();
      const emailInput = wrapper.find('[data-testid="email-input"]');

      await emailInput.setValue('jane@example.com');

      expect((emailInput.element as HTMLInputElement).value).toBe('jane@example.com');
    });

    it('should bind phone field to model', async () => {
      const wrapper = mountComponent();
      const phoneInput = wrapper.find('[data-testid="phone-input"]');

      await phoneInput.setValue('+1-555-9999');

      expect((phoneInput.element as HTMLInputElement).value).toBe('+1-555-9999');
    });

    it('should bind url field to model', async () => {
      const wrapper = mountComponent();
      const urlInput = wrapper.find('[data-testid="url-input"]');

      await urlInput.setValue('https://janedoe.com');

      expect((urlInput.element as HTMLInputElement).value).toBe('https://janedoe.com');
    });

    it('should bind location fields to model', async () => {
      const wrapper = mountComponent();
      const cityInput = wrapper.find('#city');

      await cityInput.setValue('New York');

      expect((cityInput.element as HTMLInputElement).value).toBe('New York');
    });

    it('should display pre-filled data', () => {
      const prefilledBasics: Basics = {
        name: 'John Smith',
        label: 'Developer',
        image: 'https://example.com/pic.jpg',
        email: 'john@example.com',
        phone: '+1-555-1234',
        url: 'https://johnsmith.com',
        summary: 'Experienced developer',
        location: {
          address: '456 Oak St',
          postalCode: '90210',
          city: 'Los Angeles',
          countryCode: 'US',
          region: 'CA',
        },
        profiles: [],
      };

      const wrapper = mountComponent(prefilledBasics);

      const nameInput = wrapper.find('[data-testid="fullname-input"]');
      expect((nameInput.element as HTMLInputElement).value).toBe('John Smith');

      const emailInput = wrapper.find('[data-testid="email-input"]');
      expect((emailInput.element as HTMLInputElement).value).toBe('john@example.com');
    });
  });

  describe('form validation attributes', () => {
    it('should mark required fields', () => {
      const wrapper = mountComponent();

      const nameInput = wrapper.find('[data-testid="fullname-input"]');
      expect(nameInput.attributes('required')).toBeDefined();

      const emailInput = wrapper.find('[data-testid="email-input"]');
      expect(emailInput.attributes('required')).toBeDefined();

      const urlInput = wrapper.find('[data-testid="url-input"]');
      expect(urlInput.attributes('required')).toBeDefined();
    });

    it('should have correct input types', () => {
      const wrapper = mountComponent();

      expect(wrapper.find('[data-testid="email-input"]').attributes('type')).toBe('email');
      expect(wrapper.find('[data-testid="phone-input"]').attributes('type')).toBe('tel');
      expect(wrapper.find('[data-testid="url-input"]').attributes('type')).toBe('url');
    });

    it('should have maxlength on name and label fields', () => {
      const wrapper = mountComponent();

      expect(wrapper.find('[data-testid="fullname-input"]').attributes('maxlength')).toBe('100');
      expect(wrapper.find('[data-testid="label-short-description-input"]').attributes('maxlength')).toBe('100');
    });
  });

  describe('user interactions', () => {
    it('should update multiple fields', async () => {
      const wrapper = mountComponent();

      await wrapper.find('[data-testid="fullname-input"]').setValue('Jane Doe');
      await wrapper.find('[data-testid="email-input"]').setValue('jane@test.com');
      await wrapper.find('#city').setValue('Boston');

      expect((wrapper.find('[data-testid="fullname-input"]').element as HTMLInputElement).value).toBe('Jane Doe');
      expect((wrapper.find('[data-testid="email-input"]').element as HTMLInputElement).value).toBe('jane@test.com');
      expect((wrapper.find('#city').element as HTMLInputElement).value).toBe('Boston');
    });

    it('should clear field values', async () => {
      const prefilledBasics: Basics = {
        ...mockBasics,
        name: 'John Doe',
        email: 'john@example.com',
      };

      const wrapper = mountComponent(prefilledBasics);
      const nameInput = wrapper.find('[data-testid="fullname-input"]');

      await nameInput.setValue('');

      expect((nameInput.element as HTMLInputElement).value).toBe('');
    });
  });

  describe('edge cases', () => {
    it('should handle empty location object', () => {
      const basics = { ...mockBasics };
      const wrapper = mountComponent(basics);

      expect(wrapper.find('#city').exists()).toBe(true);
      expect((wrapper.find('#city').element as HTMLInputElement).value).toBe('');
    });

    it('should handle long text input', async () => {
      const wrapper = mountComponent();
      const summaryTextarea = wrapper.find('#summary');
      const longText = 'A'.repeat(500);

      await summaryTextarea.setValue(longText);

      expect((summaryTextarea.element as HTMLTextAreaElement).value).toBe(longText);
    });

    it('should handle special characters in fields', async () => {
      const wrapper = mountComponent();
      const nameInput = wrapper.find('[data-testid="fullname-input"]');

      await nameInput.setValue('José María O\'Connor');

      expect((nameInput.element as HTMLInputElement).value).toBe('José María O\'Connor');
    });
  });
});
