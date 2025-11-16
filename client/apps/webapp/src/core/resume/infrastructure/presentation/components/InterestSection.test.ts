import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import InterestSection from './InterestSection.vue';
import type { Interest } from '@/core/resume/domain/Resume';

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  messages: {
    en: {
      resume: {
        actions: {
          descriptions: {
            interests: 'Add your interests and hobbies',
          },
          labels: {
            interest: 'Interest #{number}',
          },
          empty: {
            interests: 'No interests added yet',
            keywords: 'No keywords added yet',
          },
          addFirstInterest: 'Add your first interest',
        },
        buttons: {
          addInterest: 'Add Interest',
          addKeyword: 'Add Keyword',
        },
        fields: {
          interestName: 'Interest Name',
          keywords: 'Keywords',
        },
        placeholders: {
          interestName: 'Photography',
          keyword: 'Landscape',
        },
      },
    },
  },
});

describe('InterestSection.vue', () => {
  const mountComponent = (interests: Interest[] = []) => {
    return mount(InterestSection, {
      props: {
        modelValue: interests,
      },
      global: {
        plugins: [i18n],
      },
    });
  };

  describe('rendering', () => {
    it('should render add interest button', () => {
      const wrapper = mountComponent();
      expect(wrapper.text()).toContain('Add Interest');
    });

    it('should show empty state when no interests', () => {
      const wrapper = mountComponent();
      expect(wrapper.text()).toContain('No interests added yet');
    });

    it('should render interest entry when provided', () => {
      const interests: Interest[] = [
        {
          name: '',
          keywords: [],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.find('[data-testid="interest-name-0"]').exists()).toBe(true);
    });

    it('should render multiple interest entries', () => {
      const interests: Interest[] = [
        {
          name: 'Photography',
          keywords: [],
        },
        {
          name: 'Travel',
          keywords: [],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.find('[data-testid="interest-name-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="interest-name-1"]').exists()).toBe(true);
    });

    it('should display field labels', () => {
      const interests: Interest[] = [
        {
          name: '',
          keywords: [],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.text()).toContain('Interest Name');
      expect(wrapper.text()).toContain('Keywords');
    });
  });

  describe('v-model binding', () => {
    it('should bind interest name field', async () => {
      const interests: Interest[] = [
        {
          name: '',
          keywords: [],
        },
      ];

      const wrapper = mountComponent(interests);
      const nameInput = wrapper.find('[data-testid="interest-name-0"]');
      
      await nameInput.setValue('Photography');
      expect((nameInput.element as HTMLInputElement).value).toBe('Photography');
    });

    it('should display pre-filled data', () => {
      const interests: Interest[] = [
        {
          name: 'Photography',
          keywords: ['Landscape', 'Portrait'],
        },
      ];

      const wrapper = mountComponent(interests);
      const nameInput = wrapper.find('[data-testid="interest-name-0"]');
      expect((nameInput.element as HTMLInputElement).value).toBe('Photography');
    });
  });

  describe('keywords management', () => {
    it('should render keywords when provided', () => {
      const interests: Interest[] = [
        {
          name: 'Photography',
          keywords: ['Landscape', 'Portrait', 'Wildlife'],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.find('[data-testid="interest-keyword-0-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="interest-keyword-0-1"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="interest-keyword-0-2"]').exists()).toBe(true);
    });

    it('should show empty state when no keywords', () => {
      const interests: Interest[] = [
        {
          name: 'Photography',
          keywords: [],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.text()).toContain('No keywords added yet');
    });

    it('should display keyword values correctly', () => {
      const interests: Interest[] = [
        {
          name: 'Photography',
          keywords: ['Landscape', 'Portrait'],
        },
      ];

      const wrapper = mountComponent(interests);
      const keyword1 = wrapper.find('[data-testid="interest-keyword-0-0"]');
      const keyword2 = wrapper.find('[data-testid="interest-keyword-0-1"]');
      
      expect((keyword1.element as HTMLInputElement).value).toBe('Landscape');
      expect((keyword2.element as HTMLInputElement).value).toBe('Portrait');
    });
  });

  describe('form validation', () => {
    it('should mark name field as required', () => {
      const interests: Interest[] = [
        {
          name: '',
          keywords: [],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.find('[data-testid="interest-name-0"]').attributes('required')).toBeDefined();
    });

    it('should have correct input type', () => {
      const interests: Interest[] = [
        {
          name: '',
          keywords: [],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.find('[data-testid="interest-name-0"]').attributes('type')).toBe('text');
    });
  });

  describe('edge cases', () => {
    it('should handle empty keywords array', () => {
      const interests: Interest[] = [
        {
          name: 'Photography',
          keywords: [],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.find('[data-testid="interest-keyword-0-0"]').exists()).toBe(false);
    });

    it('should render multiple interests with different keyword counts', () => {
      const interests: Interest[] = [
        {
          name: 'Photography',
          keywords: ['Landscape'],
        },
        {
          name: 'Travel',
          keywords: ['Europe', 'Asia', 'Backpacking'],
        },
      ];

      const wrapper = mountComponent(interests);
      expect(wrapper.find('[data-testid="interest-keyword-0-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="interest-keyword-1-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="interest-keyword-1-2"]').exists()).toBe(true);
    });

    it('should handle interest with single keyword', () => {
      const interests: Interest[] = [
        {
          name: 'Reading',
          keywords: ['Fiction'],
        },
      ];

      const wrapper = mountComponent(interests);
      const keyword = wrapper.find('[data-testid="interest-keyword-0-0"]');
      expect(keyword.exists()).toBe(true);
      expect((keyword.element as HTMLInputElement).value).toBe('Fiction');
    });
  });
});
