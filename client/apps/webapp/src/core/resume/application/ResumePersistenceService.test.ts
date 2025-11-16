import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ResumePersistenceService } from './ResumePersistenceService';
import type { ResumeStorage, StorageType, Resume, PartialResume } from '@/core/resume/domain/ResumeStorage';

describe('ResumePersistenceService', () => {
  let mockStorage: ResumeStorage;
  let service: ResumePersistenceService;
  let mockResume: Resume;

  beforeEach(() => {
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

    mockStorage = {
      save: vi.fn(),
      load: vi.fn(),
      clear: vi.fn(),
      type: vi.fn().mockReturnValue('session' as StorageType),
    };

    service = new ResumePersistenceService(mockStorage);
  });

  describe('save', () => {
    it('should save complete resume using storage strategy', async () => {
      const mockResult = {
        success: true,
        data: mockResume,
        timestamp: Date.now(),
      };
      vi.mocked(mockStorage.save).mockResolvedValue(mockResult);

      const result = await service.save(mockResume);

      expect(mockStorage.save).toHaveBeenCalledWith(mockResume);
      expect(result).toEqual(mockResult);
    });

    it('should save partial resume using storage strategy', async () => {
      const partialResume: PartialResume = {
        basics: {
          name: 'Jane Doe',
          email: 'jane@example.com',
        },
      };

      const mockResult = {
        success: true,
        data: partialResume,
        timestamp: Date.now(),
      };
      vi.mocked(mockStorage.save).mockResolvedValue(mockResult);

      const result = await service.save(partialResume);

      expect(mockStorage.save).toHaveBeenCalledWith(partialResume);
      expect(result).toEqual(mockResult);
    });

    it('should propagate error from storage', async () => {
      vi.mocked(mockStorage.save).mockRejectedValue(new Error('Storage full'));

      await expect(service.save(mockResume)).rejects.toThrow('Storage full');
    });
  });

  describe('load', () => {
    it('should load resume using storage strategy', async () => {
      const mockResult = {
        success: true,
        data: mockResume,
        timestamp: Date.now(),
      };
      vi.mocked(mockStorage.load).mockResolvedValue(mockResult);

      const result = await service.load();

      expect(mockStorage.load).toHaveBeenCalled();
      expect(result).toEqual(mockResult);
    });

    it('should return null when no resume is stored', async () => {
      const mockResult = {
        success: true,
        data: null,
        timestamp: Date.now(),
      };
      vi.mocked(mockStorage.load).mockResolvedValue(mockResult);

      const result = await service.load();

      expect(result.data).toBeNull();
    });

    it('should propagate error from storage', async () => {
      vi.mocked(mockStorage.load).mockRejectedValue(new Error('Storage error'));

      await expect(service.load()).rejects.toThrow('Storage error');
    });
  });

  describe('clear', () => {
    it('should clear resume using storage strategy', async () => {
      vi.mocked(mockStorage.clear).mockResolvedValue(undefined);

      await service.clear();

      expect(mockStorage.clear).toHaveBeenCalled();
    });

    it('should propagate error from storage', async () => {
      vi.mocked(mockStorage.clear).mockRejectedValue(new Error('Clear failed'));

      await expect(service.clear()).rejects.toThrow('Clear failed');
    });
  });

  describe('getStorageType', () => {
    it('should return storage type from strategy', () => {
      const type = service.getStorageType();

      expect(mockStorage.type).toHaveBeenCalled();
      expect(type).toBe('session');
    });

    it('should return correct type for different storage strategies', () => {
      const localMockStorage: ResumeStorage = {
        ...mockStorage,
        type: vi.fn().mockReturnValue('local' as StorageType),
      };

      const localService = new ResumePersistenceService(localMockStorage);
      const type = localService.getStorageType();

      expect(type).toBe('local');
    });
  });

  describe('setStrategy', () => {
    it('should change storage strategy', () => {
      const newMockStorage: ResumeStorage = {
        save: vi.fn(),
        load: vi.fn(),
        clear: vi.fn(),
        type: vi.fn().mockReturnValue('local' as StorageType),
      };

      service.setStrategy(newMockStorage);

      const type = service.getStorageType();
      expect(type).toBe('local');
      expect(newMockStorage.type).toHaveBeenCalled();
    });

    it('should use new strategy for subsequent operations', async () => {
      const newMockStorage: ResumeStorage = {
        save: vi.fn().mockResolvedValue({ success: true, data: mockResume, timestamp: Date.now() }),
        load: vi.fn(),
        clear: vi.fn(),
        type: vi.fn().mockReturnValue('local' as StorageType),
      };

      service.setStrategy(newMockStorage);
      await service.save(mockResume);

      expect(newMockStorage.save).toHaveBeenCalledWith(mockResume);
      expect(mockStorage.save).not.toHaveBeenCalled();
    });
  });
});
