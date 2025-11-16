import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import { createI18n } from 'vue-i18n';
import ProjectSection from './ProjectSection.vue';
import type { Project } from '@/core/resume/domain/Resume';

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  messages: {
    en: {
      resume: {
        actions: {
          descriptions: {
            projects: 'Add your notable projects',
          },
          labels: {
            project: 'Project #{number}',
          },
          empty: {
            projects: 'No projects added yet',
            highlights: 'No highlights added yet',
          },
          addFirstProject: 'Add your first project',
        },
        buttons: {
          addProject: 'Add Project',
          addHighlight: 'Add Highlight',
        },
        fields: {
          projectName: 'Project Name',
          url: 'URL',
          startDate: 'Start Date',
          endDate: 'End Date',
          currentProject: 'This is an ongoing project',
          description: 'Description',
          highlights: 'Highlights',
        },
        placeholders: {
          projectName: 'E-commerce Platform',
          projectUrl: 'https://github.com/user/project',
          projectDescription: 'Built a full-stack e-commerce solution',
          projectHighlight: 'Key feature or achievement',
        },
      },
    },
  },
});

describe('ProjectSection.vue', () => {
  const mountComponent = (projects: Project[] = []) => {
    return mount(ProjectSection, {
      props: {
        modelValue: projects,
      },
      global: {
        plugins: [i18n],
      },
    });
  };

  describe('rendering', () => {
    it('should render add project button', () => {
      const wrapper = mountComponent();
      expect(wrapper.text()).toContain('Add Project');
    });

    it('should show empty state when no projects', () => {
      const wrapper = mountComponent();
      expect(wrapper.text()).toContain('No projects added yet');
    });

    it('should render project entry when provided', () => {
      const projects: Project[] = [
        {
          name: '',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.find('[data-testid="project-name-0"]').exists()).toBe(true);
    });

    it('should render multiple project entries', () => {
      const projects: Project[] = [
        {
          name: 'Project A',
          startDate: '2020-01-01',
          endDate: '2020-12-31',
          description: 'Description A',
          highlights: [],
          url: '',
        },
        {
          name: 'Project B',
          startDate: '2021-01-01',
          endDate: '',
          description: 'Description B',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.find('[data-testid="project-name-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="project-name-1"]').exists()).toBe(true);
    });

    it('should display field labels', () => {
      const projects: Project[] = [
        {
          name: '',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.text()).toContain('Project Name');
      expect(wrapper.text()).toContain('URL');
      expect(wrapper.text()).toContain('Start Date');
      expect(wrapper.text()).toContain('End Date');
      expect(wrapper.text()).toContain('Description');
    });
  });

  describe('v-model binding', () => {
    it('should bind project name field', async () => {
      const projects: Project[] = [
        {
          name: '',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      const nameInput = wrapper.find('[data-testid="project-name-0"]');
      
      await nameInput.setValue('E-commerce Platform');
      expect((nameInput.element as HTMLInputElement).value).toBe('E-commerce Platform');
    });

    it('should bind URL field', async () => {
      const projects: Project[] = [
        {
          name: '',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      const urlInput = wrapper.find('[data-testid="project-url-0"]');
      
      await urlInput.setValue('https://github.com/user/project');
      expect((urlInput.element as HTMLInputElement).value).toBe('https://github.com/user/project');
    });

    it('should bind date fields', async () => {
      const projects: Project[] = [
        {
          name: '',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      const startDate = wrapper.find('[data-testid="project-start-date-0"]');
      const endDate = wrapper.find('[data-testid="project-end-date-0"]');

      await startDate.setValue('2020-01-01');
      await endDate.setValue('2020-12-31');

      expect((startDate.element as HTMLInputElement).value).toBe('2020-01-01');
      expect((endDate.element as HTMLInputElement).value).toBe('2020-12-31');
    });

    it('should bind description field', async () => {
      const projects: Project[] = [
        {
          name: '',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      const descInput = wrapper.find('[data-testid="project-description-0"]');
      
      await descInput.setValue('Built a full-stack application');
      expect((descInput.element as HTMLTextAreaElement).value).toBe('Built a full-stack application');
    });

    it('should display pre-filled data', () => {
      const projects: Project[] = [
        {
          name: 'E-commerce Platform',
          startDate: '2020-01-01',
          endDate: '2020-12-31',
          description: 'Built using React and Node.js',
          highlights: [],
          url: 'https://example.com',
        },
      ];

      const wrapper = mountComponent(projects);
      const nameInput = wrapper.find('[data-testid="project-name-0"]');
      expect((nameInput.element as HTMLInputElement).value).toBe('E-commerce Platform');
    });
  });

  describe('highlights management', () => {
    it('should render highlights when provided', () => {
      const projects: Project[] = [
        {
          name: 'Project A',
          startDate: '',
          endDate: '',
          description: '',
          highlights: ['Feature 1', 'Feature 2', 'Feature 3'],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.find('[data-testid="project-highlight-0-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="project-highlight-0-1"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="project-highlight-0-2"]').exists()).toBe(true);
    });

    it('should show empty state when no highlights', () => {
      const projects: Project[] = [
        {
          name: 'Project A',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.text()).toContain('No highlights added yet');
    });

    it('should display highlight values correctly', () => {
      const projects: Project[] = [
        {
          name: 'Project A',
          startDate: '',
          endDate: '',
          description: '',
          highlights: ['Implemented authentication', 'Added payment gateway'],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      const h1 = wrapper.find('[data-testid="project-highlight-0-0"]');
      const h2 = wrapper.find('[data-testid="project-highlight-0-1"]');
      
      expect((h1.element as HTMLInputElement).value).toBe('Implemented authentication');
      expect((h2.element as HTMLInputElement).value).toBe('Added payment gateway');
    });
  });

  describe('form validation', () => {
    it('should mark required fields', () => {
      const projects: Project[] = [
        {
          name: '',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.find('[data-testid="project-name-0"]').attributes('required')).toBeDefined();
      expect(wrapper.find('[data-testid="project-start-date-0"]').attributes('required')).toBeDefined();
      expect(wrapper.find('[data-testid="project-description-0"]').attributes('required')).toBeDefined();
    });

    it('should have correct input types', () => {
      const projects: Project[] = [
        {
          name: '',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.find('[data-testid="project-url-0"]').attributes('type')).toBe('url');
      expect(wrapper.find('[data-testid="project-start-date-0"]').attributes('type')).toBe('date');
      expect(wrapper.find('[data-testid="project-end-date-0"]').attributes('type')).toBe('date');
    });
  });

  describe('edge cases', () => {
    it('should handle empty highlights array', () => {
      const projects: Project[] = [
        {
          name: 'Project A',
          startDate: '',
          endDate: '',
          description: '',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.find('[data-testid="project-highlight-0-0"]').exists()).toBe(false);
    });

    it('should render multiple projects with different highlight counts', () => {
      const projects: Project[] = [
        {
          name: 'Project A',
          startDate: '',
          endDate: '',
          description: '',
          highlights: ['H1'],
          url: '',
        },
        {
          name: 'Project B',
          startDate: '',
          endDate: '',
          description: '',
          highlights: ['H1', 'H2', 'H3', 'H4'],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      expect(wrapper.find('[data-testid="project-highlight-0-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="project-highlight-1-0"]').exists()).toBe(true);
      expect(wrapper.find('[data-testid="project-highlight-1-3"]').exists()).toBe(true);
    });

    it('should handle project without URL', () => {
      const projects: Project[] = [
        {
          name: 'Internal Project',
          startDate: '2020-01-01',
          endDate: '2020-12-31',
          description: 'No public URL',
          highlights: [],
          url: '',
        },
      ];

      const wrapper = mountComponent(projects);
      const urlInput = wrapper.find('[data-testid="project-url-0"]');
      expect((urlInput.element as HTMLInputElement).value).toBe('');
    });
  });
});
