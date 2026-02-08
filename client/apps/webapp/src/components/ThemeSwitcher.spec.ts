import { axe } from "vitest-axe";
import { render, cleanup } from "@testing-library/vue";
import ThemeSwitcher from "./ThemeSwitcher.vue";
import { expect, it, describe, vi, afterEach } from "vitest";

vi.mock("@/composables/useTheme", () => ({
  useTheme: () => ({
    theme: "light",
    resolvedTheme: "light",
    setTheme: vi.fn(),
  }),
}));

describe("ThemeSwitcher", () => {
  afterEach(() => {
    cleanup();
  });

  it("should have no accessibility violations", async () => {
    const { container } = render(ThemeSwitcher);
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });

  it("should render the component", () => {
    const { getByRole } = render(ThemeSwitcher);
    expect(getByRole("button", { name: /select theme/i })).toBeTruthy();
  });
});
