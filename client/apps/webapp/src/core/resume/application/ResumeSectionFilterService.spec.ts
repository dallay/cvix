import { describe, it, expect } from "vitest";
import { ResumeSectionFilterService } from "./ResumeSectionFilterService";

// Minimal domain types to exercise behavior. We purposely model only what we need.
type Location = {
  address: string;
  postalCode: string;
  city: string;
  countryCode: string;
  region: string;
};

type Profile = { network: string; username: string; url: string };

type Basics = {
  name: string;
  label: string;
  image: string;
  email: string;
  phone: string;
  url: string;
  summary: string;
  location: Location;
  profiles: ReadonlyArray<Profile>;
};

type Resume = {
  basics: Basics;
  work: ReadonlyArray<{ company: string }>;
  education: ReadonlyArray<{ institution: string }>;
  skills: ReadonlyArray<{ name: string }>;
  projects: ReadonlyArray<{ name: string }>;
  certificates: ReadonlyArray<{ name: string }>;
  volunteer: ReadonlyArray<{ organization: string }>;
  awards: ReadonlyArray<{ title: string }>;
  publications: ReadonlyArray<{ name: string }>;
  languages: ReadonlyArray<{ language: string }>;
  interests: ReadonlyArray<{ name: string }>;
  references: ReadonlyArray<{ name: string }>;
};

// Visibility types modeled from service usage and comments
type PersonalDetailsVisibility = {
  fields: {
    image: boolean;
    email: boolean;
    phone: boolean;
    url: boolean;
    summary: boolean;
    location: {
      address: boolean;
      postalCode: boolean;
      city: boolean;
      countryCode: boolean;
      region: boolean;
    };
    profiles: Record<string, boolean | undefined>;
  };
};

type ArraySectionVisibility = {
  enabled: boolean;
  items: ReadonlyArray<boolean | undefined>;
};

type SectionVisibility = {
  personalDetails: PersonalDetailsVisibility;
  work: ArraySectionVisibility;
  education: ArraySectionVisibility;
  skills: ArraySectionVisibility;
  projects: ArraySectionVisibility;
  certificates: ArraySectionVisibility;
  volunteer: ArraySectionVisibility;
  awards: ArraySectionVisibility;
  publications: ArraySectionVisibility;
  languages: ArraySectionVisibility;
  interests: ArraySectionVisibility;
  references: ArraySectionVisibility;
};

function buildBasics(): Basics {
  return {
    name: "Jane Doe",
    label: "Engineer",
    image: "img.png",
    email: "jane@doe.test",
    phone: "+00",
    url: "https://site",
    summary: "Summary",
    location: {
      address: "addr",
      postalCode: "000",
      city: "City",
      countryCode: "CC",
      region: "Region",
    },
    profiles: [
      { network: "github", username: "jane", url: "https://gh" },
      { network: "twitter", username: "jane", url: "https://tw" },
    ],
  };
}

function onOffArray(n: number, offIdxs: number[] = []): boolean[] {
  return Array.from({ length: n }).map((_, i) => !offIdxs.includes(i));
}

function arrayVis(len: number, enabled = true, offIdxs: number[] = []): ArraySectionVisibility {
  return { enabled, items: onOffArray(len, offIdxs) };
}

function defaultVisibility(resume: Resume): SectionVisibility {
  return {
    personalDetails: {
      fields: {
        image: true,
        email: true,
        phone: true,
        url: true,
        summary: true,
        location: {
          address: true,
          postalCode: true,
          city: true,
          countryCode: true,
          region: true,
        },
        profiles: {}, // visible by default (undefined !== false)
      },
    },
    work: arrayVis(resume.work.length, true),
    education: arrayVis(resume.education.length, true),
    skills: arrayVis(resume.skills.length, true),
    projects: arrayVis(resume.projects.length, true),
    certificates: arrayVis(resume.certificates.length, true),
    volunteer: arrayVis(resume.volunteer.length, true),
    awards: arrayVis(resume.awards.length, true),
    publications: arrayVis(resume.publications.length, true),
    languages: arrayVis(resume.languages.length, true),
    interests: arrayVis(resume.interests.length, true),
    references: arrayVis(resume.references.length, true),
  };
}

function sampleResume(): Resume {
  return {
    basics: buildBasics(),
    work: [{ company: "A" }, { company: "B" }],
    education: [{ institution: "U1" }, { institution: "U2" }],
    skills: [{ name: "TS" }, { name: "Kotlin" }],
    projects: [{ name: "P1" }],
    certificates: [{ name: "C1" }],
    volunteer: [{ organization: "V1" }],
    awards: [{ title: "Best" }],
    publications: [{ name: "Paper" }],
    languages: [{ language: "EN" }, { language: "ES" }],
    interests: [{ name: "Chess" }],
    references: [{ name: "Ref" }],
  };
}

describe("ResumeSectionFilterService", () => {
  it("should preserve section order when filtering resume", () => {
    const svc = new ResumeSectionFilterService();
    const resume = sampleResume();
    const vis = defaultVisibility(resume);

    const filtered = svc.filterResume(resume as any, vis as any);

    // Ensures order: basics → work → education → skills → projects → certificates → volunteer →
    //                awards → publications → languages → interests → references
    expect(Object.keys(filtered)).toEqual([
      "basics",
      "work",
      "education",
      "skills",
      "projects",
      "certificates",
      "volunteer",
      "awards",
      "publications",
      "languages",
      "interests",
      "references",
    ]);
  });

  it("should return new arrays but identical content when all items are visible (performance path)", () => {
    const svc = new ResumeSectionFilterService();
    const resume = sampleResume();
    const vis = defaultVisibility(resume);

    const filtered1 = svc.filterResume(resume as any, vis as any);
    const filtered2 = svc.filterResume(resume as any, vis as any);

    // Each array section: new array instance due to [...items] branch
    expect(filtered1.work).not.toBe(resume.work);
    expect(filtered1.work).toEqual(resume.work);
    // calling again returns another new array
    expect(filtered2.work).not.toBe(filtered1.work);
    expect(filtered2.work).toEqual(resume.work);

    // Check a couple more sections
    expect(filtered1.languages).not.toBe(resume.languages);
    expect(filtered1.languages).toEqual(resume.languages);
  });

  it("should hide items explicitly marked false and keep undefined as visible by default", () => {
    const svc = new ResumeSectionFilterService();
    const resume = sampleResume();
    // Hide work[1]; leave languages visibility undefined (visible by default)
    const vis = defaultVisibility(resume);
    (vis.work as any) = { enabled: true, items: [true, false] };
    (vis.languages as any) = { enabled: true, items: [undefined, undefined] };

    const filtered = svc.filterResume(resume as any, vis as any);

    expect(filtered.work).toEqual([{ company: "A" }]);
    // languages unchanged; new array instance but same content
    expect(filtered.languages).toEqual(resume.languages);
    expect(filtered.languages).not.toBe(resume.languages);
  });

  it("should return empty array for a disabled section", () => {
    const svc = new ResumeSectionFilterService();
    const resume = sampleResume();
    const vis = defaultVisibility(resume);
    vis.education = { enabled: false, items: [] };

    const filtered = svc.filterResume(resume as any, vis as any);
    expect(filtered.education).toEqual([]);
  });

  it("should filter basics fields based on visibility flags", () => {
    const svc = new ResumeSectionFilterService();
    const resume = sampleResume();

    const vis = defaultVisibility(resume);
    vis.personalDetails.fields = {
      image: false,
      email: true,
      phone: false,
      url: true,
      summary: false,
      location: {
        address: false,
        postalCode: true,
        city: false,
        countryCode: true,
        region: false,
      },
      profiles: {
        github: true,
        twitter: false, // explicitly hidden
        // any other network would be undefined -> visible by default
      },
    };

    const filtered = svc.filterResume(resume as any, vis as any);

    expect(filtered.basics.image).toBe("");
    expect(filtered.basics.email).toBe(resume.basics.email);
    expect(filtered.basics.phone).toBe("");
    expect(filtered.basics.url).toBe(resume.basics.url);
    expect(filtered.basics.summary).toBe("");

    expect(filtered.basics.location.address).toBe("");
    expect(filtered.basics.location.postalCode).toBe(resume.basics.location.postalCode);
    expect(filtered.basics.location.city).toBe("");
    expect(filtered.basics.location.countryCode).toBe(resume.basics.location.countryCode);
    expect(filtered.basics.location.region).toBe("");

    // Only github remains
    expect(filtered.basics.profiles).toEqual([{ network: "github", username: "jane", url: "https://gh" }]);
  });

  it("should show newly added items without visibility entry (undefined !== false)", () => {
    const svc = new ResumeSectionFilterService();
    const resume = sampleResume();

    // Add a third work item but do not extend visibility items (undefined)
    const resume2: Resume = { ...resume, work: [...resume.work, { company: "C" }] };
    const vis = defaultVisibility(resume);
    // Keep original visibility length (2); index 2 is undefined => visible
    const filtered = svc.filterResume(resume2 as any, vis as any);

    expect(filtered.work).toEqual([{ company: "A" }, { company: "B" }, { company: "C" }]);
  });

  it("should correctly delegate countVisibleItems and hasVisibleItems helpers", () => {
    const svc = new ResumeSectionFilterService();
    const v1: ArraySectionVisibility = { enabled: true, items: [true, false, undefined] };
    const v2: ArraySectionVisibility = { enabled: false, items: [] };

    // We cannot import the internal implementations here, but we can validate behavioral contract:
    // - If disabled, the section is functionally empty; hasVisibleItems may still represent intent.
    expect(svc.countVisibleItems(v1 as any)).toBe(2); // true + undefined (defaults to visible) = 2
    expect(svc.hasVisibleItems(v1 as any)).toBe(true);

    // When disabled, visible count is 0; hasVisibleItems should be false.
    expect(svc.countVisibleItems(v2 as any)).toBe(0);
    expect(svc.hasVisibleItems(v2 as any)).toBe(false);
  });
});
