import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { LocalStorageResumeStorage } from './LocalStorageResumeStorage';
import type { Resume } from '@/core/resume/domain/Resume';

describe('LocalStorageResumeStorage', () => {
  let storage: LocalStorageResumeStorage;
  let mockResume: Resume;

  beforeEach(() => {
    storage = new LocalStorageResumeStorage();
    localStorage.clear();

    mockResume = {
      basics: {
        name: 'John Doe',
        label: 'Software Engineer',
        image: 'https://example.com/photo.jpg',
        email: 'john@example.com',
        phone: '+1-555-0100',
        url: 'https://johndoe.com',
        summary: 'Experienced software engineer',
        location: {
          address: '123 Main St',
          postalCode: '12345',
          city: 'San Francisco',
          countryCode: 'US',
          region: 'California',
        },
        profiles: [],
      },
      work: [],
      volunteer: [],
      education: [],
      awards: [],
      certificates: [],
      publications: [],
      skills: [],
      languages: [],
      interests: [],
      references: [],
      projects: [],
    };
  });

  afterEach(() => {
    localStorage.clear();
  });

  describe('save', () => {
    it('should save resume to local storage', async () => {
      const result = await storage.save(mockResume);

      expect(result.data).toEqual(mockResume);
      expect(result.storageType).toBe('local');
      expect(result.timestamp).toBeDefined();

      const saved = localStorage.getItem('cvix:resume');
      expect(saved).toBeTruthy();
      expect(JSON.parse(saved!)).toEqual(mockResume);
    });

    it('should save partial resume to local storage', async () => {
      const partialResume = {
        basics: {
          name: 'Jane Doe',
          email: 'jane@example.com',
        },
      };

      const result = await storage.save(partialResume);

      expect(result.data).toEqual(partialResume);
      expect(result.storageType).toBe('local');

      const saved = localStorage.getItem('cvix:resume');
      expect(JSON.parse(saved!)).toEqual(partialResume);
    });

    it('should overwrite existing data', async () => {
      await storage.save(mockResume);

      const updatedResume = {
        ...mockResume,
        basics: {
          ...mockResume.basics,
          name: 'Jane Smith',
        },
      };

      await storage.save(updatedResume);

      const saved = localStorage.getItem('cvix:resume');
      const parsed = JSON.parse(saved!);
      expect(parsed.basics.name).toBe('Jane Smith');
    });

    it('should handle storage errors gracefully', async () => {
      const setItemSpy = vi.spyOn(Storage.prototype, 'setItem');
      setItemSpy.mockImplementation(() => {
        throw new Error('QuotaExceededError');
      });

      await expect(storage.save(mockResume)).rejects.toThrow(
        'Failed to save resume to local storage'
      );

      setItemSpy.mockRestore();
    });
  });

  describe('load', () => {
    it('should load resume from local storage', async () => {
      localStorage.setItem('cvix:resume', JSON.stringify(mockResume));

      const result = await storage.load();

      expect(result.data).toEqual(mockResume);
      expect(result.storageType).toBe('local');
      expect(result.timestamp).toBeDefined();
    });

    it('should return null when no data exists', async () => {
      const result = await storage.load();

      expect(result.data).toBeNull();
      expect(result.storageType).toBe('local');
    });

    it('should handle invalid JSON gracefully', async () => {
      localStorage.setItem('cvix:resume', 'invalid json{]');

      await expect(storage.load()).rejects.toThrow(
        'Failed to load resume from local storage'
      );
    });

    it('should handle storage errors gracefully', async () => {
      const getItemSpy = vi.spyOn(Storage.prototype, 'getItem');
      getItemSpy.mockImplementation(() => {
        throw new Error('Storage error');
      });

      await expect(storage.load()).rejects.toThrow(
        'Failed to load resume from local storage'
      );

      getItemSpy.mockRestore();
    });
  });

  describe('clear', () => {
    it('should clear resume from local storage', async () => {
      localStorage.setItem('cvix:resume', JSON.stringify(mockResume));

      await storage.clear();

      const saved = localStorage.getItem('cvix:resume');
      expect(saved).toBeNull();
    });

    it('should not throw when clearing empty storage', async () => {
      await expect(storage.clear()).resolves.not.toThrow();
    });

    it('should handle storage errors gracefully', async () => {
      const removeItemSpy = vi.spyOn(Storage.prototype, 'removeItem');
      removeItemSpy.mockImplementation(() => {
        throw new Error('Storage error');
      });

      await expect(storage.clear()).rejects.toThrow(
        'Failed to clear resume from local storage'
      );

      removeItemSpy.mockRestore();
    });
  });

  describe('type', () => {
    it('should return local as storage type', () => {
      expect(storage.type()).toBe('local');
    });
  });

  describe('integration', () => {
    it('should persist data across storage instances', async () => {
      const storage1 = new LocalStorageResumeStorage();
      await storage1.save(mockResume);

      const storage2 = new LocalStorageResumeStorage();
      const result = await storage2.load();

      expect(result.data).toEqual(mockResume);
    });

    it('should handle save-load-clear workflow', async () => {
      // Save
      await storage.save(mockResume);

      // Load
      const loadResult = await storage.load();
      expect(loadResult.data).toEqual(mockResume);

      // Clear
      await storage.clear();

      // Load again (should be empty)
      const emptyResult = await storage.load();
      expect(emptyResult.data).toBeNull();
    });
  });
});
