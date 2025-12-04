#!/usr/bin/env node

/**
 * update-gradle-version.mjs
 *
 * Updates the version property in gradle.properties with the semantic version.
 * Called by semantic-release during the prepare phase.
 *
 * Usage: node scripts/update-gradle-version.mjs <version>
 * Example: node scripts/update-gradle-version.mjs 1.2.3
 */

import { readFileSync, writeFileSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, "..");

const version = process.argv[2];

if (!version) {
	console.error("❌ Error: Version argument is required");
	console.error("Usage: node scripts/update-gradle-version.mjs <version>");
	process.exit(1);
}

// Validate semantic version format
const semverRegex = /^\d+\.\d+\.\d+(-[\w.]+)?(\+[\w.]+)?$/;
if (!semverRegex.test(version)) {
	console.error(`❌ Error: Invalid semantic version format: ${version}`);
	process.exit(1);
}

const gradlePropertiesPath = resolve(rootDir, "gradle.properties");

try {
	console.log(`📝 Updating gradle.properties with version: ${version}`);

	const content = readFileSync(gradlePropertiesPath, "utf8");

	// Replace the version line
	const updatedContent = content.replace(
		/^version\s*=\s*.+$/m,
		`version = ${version}`,
	);

	if (content === updatedContent) {
		console.warn(
			"⚠️ Warning: No version property found in gradle.properties, adding it",
		);
		// Prepend version if it doesn't exist
		writeFileSync(
			gradlePropertiesPath,
			`version = ${version}\n${content}`,
			"utf8",
		);
	} else {
		writeFileSync(gradlePropertiesPath, updatedContent, "utf8");
	}

	console.log(`✅ Successfully updated gradle.properties to version ${version}`);
} catch (error) {
	console.error(`❌ Error updating gradle.properties: ${error.message}`);
	process.exit(1);
}

// Also update workspace package.json files for frontend packages
const workspacePackages = [
	"client/apps/webapp/package.json",
	"client/apps/marketing/package.json",
	"client/packages/ui/package.json",
	"client/packages/utilities/package.json",
	"client/packages/assets/package.json",
];

for (const pkgPath of workspacePackages) {
	const fullPath = resolve(rootDir, pkgPath);
	try {
		const pkgContent = readFileSync(fullPath, "utf8");
		const pkg = JSON.parse(pkgContent);

		// Only update if this is not a private package with version 0.0.0
		// (which indicates it should inherit from root)
		if (pkg.version && pkg.version !== "0.0.0") {
			pkg.version = version;
			writeFileSync(fullPath, `${JSON.stringify(pkg, null, "\t")}\n`, "utf8");
			console.log(`✅ Updated ${pkgPath} to version ${version}`);
		}
	} catch {
		// Package file doesn't exist or can't be parsed, skip silently
		console.log(`⏭️ Skipped ${pkgPath} (not found or parse error)`);
	}
}

console.log("🎉 Version update complete!");
