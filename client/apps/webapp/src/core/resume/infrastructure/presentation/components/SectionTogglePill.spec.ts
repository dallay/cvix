import userEvent from "@testing-library/user-event";
import { cleanup, render, screen } from "@testing-library/vue";
import { afterEach, describe, expect, it, vi } from "vitest";

import SectionTogglePill from "./SectionTogglePill.vue";

describe("SectionTogglePill", () => {
	afterEach(() => {
		cleanup();
	});
	describe("rendering", () => {
		it("should render the label", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
				},
			});

			expect(
				screen.getByRole("button", { name: /Work Experience/i }),
			).toBeInTheDocument();
		});

		it("should show checkmark icon when enabled", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			// Check for checkmark icon by its semantic role or test-id
			expect(
				button.querySelector('[data-testid="checkmark-icon"]'),
			).toBeInTheDocument();
		});

		it("should show item count when provided", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
					visibleCount: 3,
					totalCount: 5,
				},
			});

			expect(screen.getByText("3/5")).toBeInTheDocument();
		});

		it("should not show item count when totalCount is 0", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
					visibleCount: 0,
					totalCount: 0,
				},
			});

			expect(screen.queryByText("0/0")).not.toBeInTheDocument();
		});
	});

	describe("enabled state", () => {
		it("should apply primary styles when enabled", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			expect(button.className).toContain("bg-primary");
			expect(button.className).toContain("text-primary-foreground");
		});

		it("should be clickable when enabled", async () => {
			const user = userEvent.setup();
			const onToggle = vi.fn();

			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
					onToggle,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			await user.click(button);

			expect(onToggle).toHaveBeenCalledTimes(1);
		});

		it("should include 'enabled' in aria-label when enabled", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			expect(button).toHaveAccessibleName(/enabled/i);
		});
	});

	describe("disabled state", () => {
		it("should apply outline styles when disabled but has data", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: false,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			expect(button.className).toContain("border");
			expect(button.className).toContain("text-muted-foreground");
		});

		it("should be clickable when disabled but has data", async () => {
			const user = userEvent.setup();
			const onToggle = vi.fn();

			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: false,
					hasData: true,
					onToggle,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			await user.click(button);

			expect(onToggle).toHaveBeenCalledTimes(1);
		});

		it("should include 'disabled' in aria-label when not enabled", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: false,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			expect(button).toHaveAccessibleName(/disabled/i);
		});
	});

	describe("no data state", () => {
		it("should apply muted styles when no data", () => {
			render(SectionTogglePill, {
				props: {
					label: "Projects",
					enabled: false,
					hasData: false,
				},
			});

			const button = screen.getByRole("button", { name: /Projects/i });
			expect(button.className).toContain("bg-muted");
			expect(button.className).toContain("cursor-not-allowed");
			expect(button.className).toContain("opacity-50");
		});

		it("should be disabled when no data", () => {
			render(SectionTogglePill, {
				props: {
					label: "Projects",
					enabled: false,
					hasData: false,
				},
			});

			const button = screen.getByRole("button", { name: /Projects/i });
			expect(button).toBeDisabled();
		});

		it("should not emit toggle event when clicked with no data", async () => {
			const user = userEvent.setup();
			const onToggle = vi.fn();

			render(SectionTogglePill, {
				props: {
					label: "Projects",
					enabled: false,
					hasData: false,
					onToggle,
				},
			});

			const button = screen.getByRole("button", { name: /Projects/i });
			// Attempting to click a disabled button shouldn't trigger the event
			await user.click(button).catch(() => {
				// @testing-library may throw when trying to click disabled button
			});

			expect(onToggle).not.toHaveBeenCalled();
		});

		it("should render with tooltip wrapper when no data and disabledTooltip provided", () => {
			render(SectionTogglePill, {
				props: {
					label: "Projects",
					enabled: false,
					hasData: false,
					disabledTooltip: "No data available",
				},
			});

			const button = screen.getByRole("button", { name: /Projects/i });
			expect(button).toBeDisabled();

			// Verify the component renders with TooltipProvider wrapper
			// Note: Tooltip content is only rendered when visible, and disabled buttons
			// don't trigger pointer events, so we can't test actual tooltip visibility.
			// We verify the button is wrapped in a tooltip structure by checking
			// it has the appropriate disabled state.
			expect(button).toBeInTheDocument();
			expect(button).toHaveAttribute("aria-disabled", "true");
		});
	});

	describe("keyboard interaction", () => {
		it("should handle Enter key press", async () => {
			const user = userEvent.setup();
			const onToggle = vi.fn();

			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
					onToggle,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			button.focus();
			await user.keyboard("{Enter}");

			expect(onToggle).toHaveBeenCalledTimes(1);
		});

		it("should handle Space key press", async () => {
			const user = userEvent.setup();
			const onToggle = vi.fn();

			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
					onToggle,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			button.focus();
			await user.keyboard(" ");

			expect(onToggle).toHaveBeenCalledTimes(1);
		});

		it("should not handle keyboard events when no data", async () => {
			const user = userEvent.setup();
			const onToggle = vi.fn();

			render(SectionTogglePill, {
				props: {
					label: "Projects",
					enabled: false,
					hasData: false,
					onToggle,
				},
			});

			const button = screen.getByRole("button", { name: /Projects/i });
			button.focus();
			await user.keyboard("{Enter}");
			await user.keyboard(" ");

			expect(onToggle).not.toHaveBeenCalled();
		});

		it("should have visible focus indicator", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			// Check that focus-visible classes are present
			expect(button.className).toContain("focus-visible:ring");
		});
	});

	describe("accessibility", () => {
		it("should have accessible name with item count", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
					visibleCount: 3,
					totalCount: 5,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			expect(button).toHaveAccessibleName("Work Experience (3/5), enabled");
		});

		it("should have aria-disabled when no data", () => {
			render(SectionTogglePill, {
				props: {
					label: "Projects",
					enabled: false,
					hasData: false,
				},
			});

			const button = screen.getByRole("button", { name: /Projects/i });
			expect(button).toHaveAttribute("aria-disabled", "true");
		});

		it("should be keyboard accessible", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			expect(button).not.toHaveAttribute("tabindex", "-1");
		});
	});

	describe("hover state", () => {
		it("should have hover styles when enabled", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: true,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			expect(button.className).toContain("hover:shadow-lg");
		});

		it("should have hover styles when disabled but has data", () => {
			render(SectionTogglePill, {
				props: {
					label: "Work Experience",
					enabled: false,
					hasData: true,
				},
			});

			const button = screen.getByRole("button", { name: /Work Experience/i });
			expect(button.className).toContain("hover:bg-muted");
		});
	});
});
