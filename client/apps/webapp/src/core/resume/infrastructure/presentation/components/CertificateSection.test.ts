import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import CertificateSection from './CertificateSection.vue';
import type { Certificate } from '@/core/resume/domain/Resume';

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  messages: {
    en: {
      resume: {
        actions: {
          descriptions: {
            certificates: 'Add your certifications',
          },
          labels: {
            certificate: 'Certificate #{number}',
          },
          empty: {
            certificates: 'No certificates added yet',
          },
          addFirstCertificate: 'Add your first certificate',
        },
        buttons: {
          addCertificate: 'Add Certificate',
        },
        fields: {
          certificateName: 'Certificate Name',
          date: 'Date',
          issuer: 'Issuer',
          url: 'URL',
        },
        placeholders: {
          certificateName: 'AWS Certified Solutions Architect',
          issuer: 'Amazon Web Services',
          certificateUrl: 'https://aws.amazon.com/certification/',
        },
      },
    },
  },
});

describe('CertificateSection.vue', () => {
  const mountComponent = (certificates: Certificate[] = []) => {
    return mount(CertificateSection, {
      props: {
        modelValue: certificates,
      },
      global: {
        plugins: [i18n],
      },
    });
  };

  describe('rendering', () => {
    it('should render add certificate button', () => {
      const wrapper = mountComponent();
      expect(wrapper.text()).toContain('Add Certificate');
    });

    it('should show empty state when no certificates', () => {
      const wrapper = mountComponent();
      expect(wrapper.text()).toContain('No certificates added yet');
    });

    it('should render certificate entry when provided', () => {
      const certificates: Certificate[] = [
        {
          name: '',
          date: '',
          issuer: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      expect(wrapper.find('[data-testid="certificate-name-0"]').exists()).toBe(true);
    });

    it('should render multiple certificate entries', () => {
      const certificates: Certificate[] = [
        {
          name: 'AWS Certified',
          date: '2021-01-01',
          issuer: 'Amazon',
          url: '',
        },
        {
          name: 'Azure Certified',
          date: '2021-06-01',
          issuer: 'Microsoft',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      expect(wrapper.find('[data-testid="certificate-name-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="certificate-name-1"]').exists()).toBe(true);
    });

    it('should display field labels', () => {
      const certificates: Certificate[] = [
        {
          name: '',
          date: '',
          issuer: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      expect(wrapper.text()).toContain('Certificate Name');
      expect(wrapper.text()).toContain('Date');
      expect(wrapper.text()).toContain('Issuer');
      expect(wrapper.text()).toContain('URL');
    });
  });

  describe('v-model binding', () => {
    it('should bind certificate name field', async () => {
      const certificates: Certificate[] = [
        {
          name: '',
          date: '',
          issuer: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      const nameInput = wrapper.find('[data-testid="certificate-name-0"]');
      
      await nameInput.setValue('AWS Solutions Architect');
      expect((nameInput.element as HTMLInputElement).value).toBe('AWS Solutions Architect');
    });

    it('should bind date field', async () => {
      const certificates: Certificate[] = [
        {
          name: '',
          date: '',
          issuer: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      const dateInput = wrapper.find('[data-testid="certificate-date-0"]');
      
      await dateInput.setValue('2021-03-15');
      expect((dateInput.element as HTMLInputElement).value).toBe('2021-03-15');
    });

    it('should bind issuer field', async () => {
      const certificates: Certificate[] = [
        {
          name: '',
          date: '',
          issuer: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      const issuerInput = wrapper.find('[data-testid="certificate-issuer-0"]');
      
      await issuerInput.setValue('Amazon Web Services');
      expect((issuerInput.element as HTMLInputElement).value).toBe('Amazon Web Services');
    });

    it('should bind URL field', async () => {
      const certificates: Certificate[] = [
        {
          name: '',
          date: '',
          issuer: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      const urlInput = wrapper.find('[data-testid="certificate-url-0"]');
      
      await urlInput.setValue('https://aws.amazon.com/certification/');
      expect((urlInput.element as HTMLInputElement).value).toBe('https://aws.amazon.com/certification/');
    });

    it('should display pre-filled data', () => {
      const certificates: Certificate[] = [
        {
          name: 'AWS Solutions Architect',
          date: '2021-03-15',
          issuer: 'Amazon Web Services',
          url: 'https://aws.amazon.com',
        },
      ];

      const wrapper = mountComponent(certificates);
      const nameInput = wrapper.find('[data-testid="certificate-name-0"]');
      const issuerInput = wrapper.find('[data-testid="certificate-issuer-0"]');
      
      expect((nameInput.element as HTMLInputElement).value).toBe('AWS Solutions Architect');
      expect((issuerInput.element as HTMLInputElement).value).toBe('Amazon Web Services');
    });
  });

  describe('form validation', () => {
    it('should mark required fields', () => {
      const certificates: Certificate[] = [
        {
          name: '',
          date: '',
          issuer: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      expect(wrapper.find('[data-testid="certificate-name-0"]').attributes('required')).toBeDefined();
      expect(wrapper.find('[data-testid="certificate-date-0"]').attributes('required')).toBeDefined();
      expect(wrapper.find('[data-testid="certificate-issuer-0"]').attributes('required')).toBeDefined();
    });

    it('should have correct input types', () => {
      const certificates: Certificate[] = [
        {
          name: '',
          date: '',
          issuer: '',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      expect(wrapper.find('[data-testid="certificate-name-0"]').attributes('type')).toBe('text');
      expect(wrapper.find('[data-testid="certificate-date-0"]').attributes('type')).toBe('date');
      expect(wrapper.find('[data-testid="certificate-issuer-0"]').attributes('type')).toBe('text');
      expect(wrapper.find('[data-testid="certificate-url-0"]').attributes('type')).toBe('url');
    });
  });

  describe('edge cases', () => {
    it('should handle single certificate', () => {
      const certificates: Certificate[] = [
        {
          name: 'Scrum Master',
          date: '2021-01-01',
          issuer: 'Scrum Alliance',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      expect(wrapper.find('[data-testid="certificate-name-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="certificate-name-1"]').exists()).toBe(false);
    });

    it('should handle certificate without URL', () => {
      const certificates: Certificate[] = [
        {
          name: 'Internal Certification',
          date: '2021-01-01',
          issuer: 'Company',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      const urlInput = wrapper.find('[data-testid="certificate-url-0"]');
      expect((urlInput.element as HTMLInputElement).value).toBe('');
    });

    it('should handle multiple certificates from different issuers', () => {
      const certificates: Certificate[] = [
        {
          name: 'AWS Solutions Architect',
          date: '2020-01-01',
          issuer: 'Amazon',
          url: 'https://aws.amazon.com',
        },
        {
          name: 'Azure Developer',
          date: '2020-06-01',
          issuer: 'Microsoft',
          url: 'https://azure.microsoft.com',
        },
        {
          name: 'GCP Professional',
          date: '2021-01-01',
          issuer: 'Google',
          url: 'https://cloud.google.com',
        },
      ];

      const wrapper = mountComponent(certificates);
      expect(wrapper.find('[data-testid="certificate-name-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="certificate-name-1"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="certificate-name-2"]').exists()).toBe(true);
    });

    it('should handle certificates with varying date formats', () => {
      const certificates: Certificate[] = [
        {
          name: 'Cert 1',
          date: '2021-01-15',
          issuer: 'Issuer 1',
          url: '',
        },
        {
          name: 'Cert 2',
          date: '2021-12-31',
          issuer: 'Issuer 2',
          url: '',
        },
      ];

      const wrapper = mountComponent(certificates);
      const date0 = wrapper.find('[data-testid="certificate-date-0"]');
      const date1 = wrapper.find('[data-testid="certificate-date-1"]');
      
      expect((date0.element as HTMLInputElement).value).toBe('2021-01-15');
      expect((date1.element as HTMLInputElement).value).toBe('2021-12-31');
    });

    it('should handle long certificate names', () => {
      const certificates: Certificate[] = [
        {
          name: 'Professional Scrum Master I (PSM I) - Advanced Agile Methodologies and Leadership Certification',
          date: '2021-01-01',
          issuer: 'Scrum.org',
          url: 'https://scrum.org',
        },
      ];

      const wrapper = mountComponent(certificates);
      const nameInput = wrapper.find('[data-testid="certificate-name-0"]');
      expect((nameInput.element as HTMLInputElement).value.length).toBeGreaterThan(50);
    });
  });
});
