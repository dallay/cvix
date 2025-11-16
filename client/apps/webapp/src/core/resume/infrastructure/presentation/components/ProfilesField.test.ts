import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import ProfilesField from './ProfilesField.vue';
import type { Profile } from '@/core/resume/domain/Resume';

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  messages: {
    en: {
      resume: {
        actions: {
          descriptions: {
            profiles: 'Add your social media profiles',
          },
          labels: {
            profile: 'Profile #{number}',
          },
          empty: {
            profiles: 'No profiles added yet',
          },
          addProfile: 'Add Profile',
          addFirstProfile: 'Add your first profile',
        },
        fields: {
          network: 'Network',
          username: 'Username',
          profileUrl: 'Profile URL',
        },
        placeholders: {
          network: 'LinkedIn',
          username: 'johndoe',
          profileUrl: 'https://linkedin.com/in/johndoe',
        },
      },
    },
  },
});

describe('ProfilesField.vue', () => {
  const mountComponent = (profiles: Profile[] = []) => {
    return mount(ProfilesField, {
      props: {
        modelValue: profiles,
      },
      global: {
        plugins: [i18n],
      },
    });
  };

  describe('rendering', () => {
    it('should render add profile button', () => {
      const wrapper = mountComponent();
      expect(wrapper.text()).toContain('Add Profile');
    });

    it('should show empty state when no profiles', () => {
      const wrapper = mountComponent();
      expect(wrapper.text()).toContain('No profiles added yet');
    });

    it('should render profile entry when provided', () => {
      const profiles: Profile[] = [
        {
          network: '',
          username: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(profiles);
      expect(wrapper.find('[data-testid="profile-network-0"]').exists()).toBe(true);
    });

    it('should render multiple profile entries', () => {
      const profiles: Profile[] = [
        {
          network: 'LinkedIn',
          username: 'johndoe',
          url: 'https://linkedin.com/in/johndoe',
        },
        {
          network: 'GitHub',
          username: 'johndoe',
          url: 'https://github.com/johndoe',
        },
      ];

      const wrapper = mountComponent(profiles);
      expect(wrapper.find('[data-testid="profile-network-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="profile-network-1"]').exists()).toBe(true);
    });

    it('should display field labels', () => {
      const profiles: Profile[] = [
        {
          network: '',
          username: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(profiles);
      expect(wrapper.text()).toContain('Network');
      expect(wrapper.text()).toContain('Username');
      expect(wrapper.text()).toContain('Profile URL');
    });
  });

  describe('v-model binding', () => {
    it('should bind network field', async () => {
      const profiles: Profile[] = [
        {
          network: '',
          username: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(profiles);
      const networkInput = wrapper.find('[data-testid="profile-network-0"]');
      
      await networkInput.setValue('LinkedIn');
      expect((networkInput.element as HTMLInputElement).value).toBe('LinkedIn');
    });

    it('should bind username field', async () => {
      const profiles: Profile[] = [
        {
          network: '',
          username: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(profiles);
      const usernameInput = wrapper.find('[data-testid="profile-username-0"]');
      
      await usernameInput.setValue('johndoe');
      expect((usernameInput.element as HTMLInputElement).value).toBe('johndoe');
    });

    it('should bind URL field', async () => {
      const profiles: Profile[] = [
        {
          network: '',
          username: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(profiles);
      const urlInput = wrapper.find('[data-testid="profile-url-0"]');
      
      await urlInput.setValue('https://linkedin.com/in/johndoe');
      expect((urlInput.element as HTMLInputElement).value).toBe('https://linkedin.com/in/johndoe');
    });

    it('should display pre-filled data', () => {
      const profiles: Profile[] = [
        {
          network: 'LinkedIn',
          username: 'johndoe',
          url: 'https://linkedin.com/in/johndoe',
        },
        {
          network: 'GitHub',
          username: 'johndoe',
          url: 'https://github.com/johndoe',
        },
      ];

      const wrapper = mountComponent(profiles);
      const network0 = wrapper.find('[data-testid="profile-network-0"]');
      const username0 = wrapper.find('[data-testid="profile-username-0"]');
      const network1 = wrapper.find('[data-testid="profile-network-1"]');
      
      expect((network0.element as HTMLInputElement).value).toBe('LinkedIn');
      expect((username0.element as HTMLInputElement).value).toBe('johndoe');
      expect((network1.element as HTMLInputElement).value).toBe('GitHub');
    });
  });

  describe('form validation', () => {
    it('should have correct input types', () => {
      const profiles: Profile[] = [
        {
          network: '',
          username: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(profiles);
      expect(wrapper.find('[data-testid="profile-network-0"]').attributes('type')).toBe('text');
      expect(wrapper.find('[data-testid="profile-username-0"]').attributes('type')).toBe('text');
      expect(wrapper.find('[data-testid="profile-url-0"]').attributes('type')).toBe('url');
    });
  });

  describe('edge cases', () => {
    it('should handle single profile', () => {
      const profiles: Profile[] = [
        {
          network: 'Twitter',
          username: 'johndoe',
          url: 'https://twitter.com/johndoe',
        },
      ];

      const wrapper = mountComponent(profiles);
      expect(wrapper.find('[data-testid="profile-network-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="profile-network-1"]').exists()).toBe(false);
    });

    it('should handle multiple social networks', () => {
      const profiles: Profile[] = [
        {
          network: 'LinkedIn',
          username: 'johndoe',
          url: 'https://linkedin.com/in/johndoe',
        },
        {
          network: 'GitHub',
          username: 'johndoe',
          url: 'https://github.com/johndoe',
        },
        {
          network: 'Twitter',
          username: 'johndoe',
          url: 'https://twitter.com/johndoe',
        },
        {
          network: 'Stack Overflow',
          username: 'johndoe',
          url: 'https://stackoverflow.com/users/123/johndoe',
        },
      ];

      const wrapper = mountComponent(profiles);
      expect(wrapper.find('[data-testid="profile-network-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="profile-network-1"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="profile-network-2"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="profile-network-3"]').exists()).toBe(true);
    });

    it('should handle profiles with empty usernames', () => {
      const profiles: Profile[] = [
        {
          network: 'Personal Website',
          username: '',
          url: 'https://johndoe.com',
        },
      ];

      const wrapper = mountComponent(profiles);
      const usernameInput = wrapper.find('[data-testid="profile-username-0"]');
      expect((usernameInput.element as HTMLInputElement).value).toBe('');
    });

    it('should handle profiles with special characters in username', () => {
      const profiles: Profile[] = [
        {
          network: 'GitHub',
          username: 'john-doe_123',
          url: 'https://github.com/john-doe_123',
        },
      ];

      const wrapper = mountComponent(profiles);
      const usernameInput = wrapper.find('[data-testid="profile-username-0"]');
      expect((usernameInput.element as HTMLInputElement).value).toBe('john-doe_123');
    });

    it('should handle different URL formats', () => {
      const profiles: Profile[] = [
        {
          network: 'LinkedIn',
          username: 'johndoe',
          url: 'https://www.linkedin.com/in/johndoe',
        },
        {
          network: 'GitHub',
          username: 'johndoe',
          url: 'github.com/johndoe',
        },
        {
          network: 'Portfolio',
          username: 'johndoe',
          url: 'http://johndoe.dev',
        },
      ];

      const wrapper = mountComponent(profiles);
      const url0 = wrapper.find('[data-testid="profile-url-0"]');
      const url1 = wrapper.find('[data-testid="profile-url-1"]');
      const url2 = wrapper.find('[data-testid="profile-url-2"]');
      
      expect((url0.element as HTMLInputElement).value).toContain('linkedin.com');
      expect((url1.element as HTMLInputElement).value).toContain('github.com');
      expect((url2.element as HTMLInputElement).value).toContain('johndoe.dev');
    });

    it('should handle profiles without network specified', () => {
      const profiles: Profile[] = [
        {
          network: '',
          username: 'johndoe',
          url: 'https://example.com/johndoe',
        },
      ];

      const wrapper = mountComponent(profiles);
      const networkInput = wrapper.find('[data-testid="profile-network-0"]');
      expect((networkInput.element as HTMLInputElement).value).toBe('');
    });

    it('should handle professional vs personal profiles', () => {
      const profiles: Profile[] = [
        {
          network: 'LinkedIn',
          username: 'john-doe-professional',
          url: 'https://linkedin.com/in/john-doe-professional',
        },
        {
          network: 'Instagram',
          username: 'johndoe_personal',
          url: 'https://instagram.com/johndoe_personal',
        },
      ];

      const wrapper = mountComponent(profiles);
      const username0 = wrapper.find('[data-testid="profile-username-0"]');
      const username1 = wrapper.find('[data-testid="profile-username-1"]');
      
      expect((username0.element as HTMLInputElement).value).toContain('professional');
      expect((username1.element as HTMLInputElement).value).toContain('personal');
    });
  });
});
