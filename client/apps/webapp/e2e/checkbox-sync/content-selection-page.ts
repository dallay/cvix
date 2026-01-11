import type { Locator, Page } from "@playwright/test";
import { expect } from "@playwright/test";
import { BasePage } from "../base-page";

/**
 * Page Object for Content Selection Panel (SectionTogglePanel)
 *
 * This panel appears on the PDF generation page and allows users to:
 * - Toggle entire sections on/off
 * - Toggle individual items within sections
 * - See indeterminate state when some items are selected
 */
export class ContentSelectionPage extends BasePage {
	// Panel container
	readonly sectionPanel: Locator;

	// Section rows (clickable items in the accordion)
	readonly sectionRows: Locator;

	constructor(page: Page) {
		super(page);

		// The panel is a div with role="list" containing the sections
		this.sectionPanel = page.locator(
			'[role="list"][aria-label="Resume sections"]',
		);
		this.sectionRows = this.sectionPanel.locator('[role="button"]');
	}

	/**
	 * Navigate to PDF page where content selection panel is visible
	 */
	async gotoPdfPage(): Promise<void> {
		await super.goto("/resume/pdf");
		await this.waitForPageLoad();
	}

	/**
	 * Wait for the content selection panel to be visible
	 */
	override async waitForPageLoad(): Promise<void> {
		await this.sectionPanel.waitFor({ state: "visible", timeout: 10000 });
	}

	/**
	 * Get a section row by its label text
	 */
	getSectionByLabel(label: string | RegExp): Locator {
		return this.sectionPanel
			.locator('[role="button"]')
			.filter({ hasText: label });
	}

	/**
	 * Get the checkbox within a section row
	 */
	getSectionCheckbox(sectionLabel: string | RegExp): Locator {
		return this.getSectionByLabel(sectionLabel).locator(
			'[data-slot="checkbox"]',
		);
	}

	/**
	 * Get the checkbox state for a section
	 * Returns: 'checked' | 'unchecked' | 'indeterminate'
	 */
	async getSectionCheckboxState(
		sectionLabel: string | RegExp,
	): Promise<string> {
		const checkbox = this.getSectionCheckbox(sectionLabel);
		const state = await checkbox.getAttribute("data-state");
		return state ?? "unchecked";
	}

	/**
	 * Click a section checkbox to toggle it
	 */
	async toggleSectionCheckbox(sectionLabel: string | RegExp): Promise<void> {
		const checkbox = this.getSectionCheckbox(sectionLabel);
		const initialState = await checkbox.getAttribute("data-state");
		await checkbox.click();
		// Wait for state to flip (deterministic, not timeout-based)
		await expect(checkbox).not.toHaveAttribute(
			"data-state",
			initialState ?? "",
		);
	}

	/**
	 * Expand a section to show its items (click on the row, not the checkbox)
	 */
	async expandSection(sectionLabel: string | RegExp): Promise<void> {
		const section = this.getSectionByLabel(sectionLabel);
		// Click on the section row (not the checkbox) to expand
		await section.click();
		// Wait for expansion to complete (web-first assertion)
		await expect(section).toHaveAttribute("aria-expanded", "true");
	}

	/**
	 * Check if a section is expanded (has visible items)
	 */
	async isSectionExpanded(sectionLabel: string | RegExp): Promise<boolean> {
		const section = this.getSectionByLabel(sectionLabel);
		const expanded = await section.getAttribute("aria-expanded");
		return expanded === "true";
	}

	/**
	 * Get all item checkboxes within an expanded section
	 */
	getItemCheckboxes(sectionLabel: string | RegExp): Locator {
		const section = this.getSectionByLabel(sectionLabel);
		// Items are in a sibling div after the section button
		return section.locator("..").locator('[id^="item-"]').locator("..");
	}

	/**
	 * Get a specific item checkbox by index within an expanded section.
	 * The checkbox has id="item-{index}" and data-slot="checkbox" on the same element.
	 */
	getItemCheckbox(sectionLabel: string | RegExp, itemIndex: number): Locator {
		const section = this.getSectionByLabel(sectionLabel);
		// The checkbox element itself has both the id and data-slot="checkbox"
		return section
			.locator("..")
			.locator(`#item-${itemIndex}[data-slot="checkbox"]`);
	}

	/**
	 * Get item checkbox by its label text
	 */
	getItemCheckboxByLabel(
		sectionLabel: string | RegExp,
		itemLabel: string | RegExp,
	): Locator {
		const section = this.getSectionByLabel(sectionLabel);
		// Find the item row by label, then get its checkbox
		// Use a more semantic approach instead of brittle CSS selectors
		return section
			.locator("..")
			.locator('[id^="item-"]')
			.locator("..")
			.filter({ hasText: itemLabel })
			.locator('[data-slot="checkbox"]');
	}

	/**
	 * Toggle an item checkbox within an expanded section
	 */
	async toggleItemCheckbox(
		sectionLabel: string | RegExp,
		itemIndex: number,
	): Promise<void> {
		const checkbox = this.getItemCheckbox(sectionLabel, itemIndex);
		const initialState = await checkbox.getAttribute("data-state");
		await checkbox.click();
		// Wait for state to flip (deterministic, not timeout-based)
		await expect(checkbox).not.toHaveAttribute(
			"data-state",
			initialState ?? "",
		);
	}

	/**
	 * Toggle an item checkbox by its label text
	 */
	async toggleItemCheckboxByLabel(
		sectionLabel: string | RegExp,
		itemLabel: string | RegExp,
	): Promise<void> {
		const checkbox = this.getItemCheckboxByLabel(sectionLabel, itemLabel);
		const initialState = await checkbox.getAttribute("data-state");
		await checkbox.click();
		// Wait for state to flip (deterministic, not timeout-based)
		await expect(checkbox).not.toHaveAttribute(
			"data-state",
			initialState ?? "",
		);
	}

	/**
	 * Get the state of an item checkbox
	 */
	async getItemCheckboxState(
		sectionLabel: string | RegExp,
		itemIndex: number,
	): Promise<string> {
		const checkbox = this.getItemCheckbox(sectionLabel, itemIndex);
		const state = await checkbox.getAttribute("data-state");
		return state ?? "unchecked";
	}

	/**
	 * Get the counter text (e.g., "2/3") for a section
	 */
	async getSectionCounter(
		sectionLabel: string | RegExp,
	): Promise<string | null> {
		const section = this.getSectionByLabel(sectionLabel);
		const counter = section.locator("span").filter({ hasText: /\d+\/\d+/ });
		if (await counter.isVisible()) {
			return await counter.textContent();
		}
		return null;
	}

	/**
	 * Assert that a section checkbox is in a specific state
	 */
	async expectSectionState(
		sectionLabel: string | RegExp,
		expectedState: "checked" | "unchecked" | "indeterminate",
	): Promise<void> {
		const checkbox = this.getSectionCheckbox(sectionLabel);
		await expect(checkbox).toHaveAttribute("data-state", expectedState);
	}

	/**
	 * Assert that an item checkbox is in a specific state
	 */
	async expectItemState(
		sectionLabel: string | RegExp,
		itemIndex: number,
		expectedState: "checked" | "unchecked",
	): Promise<void> {
		const checkbox = this.getItemCheckbox(sectionLabel, itemIndex);
		await expect(checkbox).toHaveAttribute("data-state", expectedState);
	}

	/**
	 * Get the number of checked items in a section
	 */
	async getCheckedItemCount(sectionLabel: string | RegExp): Promise<number> {
		const section = this.getSectionByLabel(sectionLabel);
		// Only count item checkboxes (exclude section checkbox)
		const checkedItems = section
			.locator("..")
			.locator('[id^="item-"][data-state="checked"]');
		return await checkedItems.count();
	}

	/**
	 * Get the total number of items in a section
	 */
	async getTotalItemCount(sectionLabel: string | RegExp): Promise<number> {
		const section = this.getSectionByLabel(sectionLabel);
		const items = section.locator("..").locator('[id^="item-"]');
		return await items.count();
	}
}
