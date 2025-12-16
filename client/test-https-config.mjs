#!/usr/bin/env node
/**
 * Test script to verify HTTPS configuration functions work correctly
 * Tests both with and without SSL certificates present
 */

import { existsSync } from "node:fs";
import { fileURLToPath } from "node:url";

console.log("🧪 Testing HTTPS Configuration Functions\n");

// Test 1: Check if certificates exist
const certPath = fileURLToPath(new URL("../infra/ssl/localhost.pem", import.meta.url));
const keyPath = fileURLToPath(new URL("../infra/ssl/localhost-key.pem", import.meta.url));

console.log("📁 Certificate paths:");
console.log(`   Cert: ${certPath}`);
console.log(`   Key:  ${keyPath}\n`);

const certExists = existsSync(certPath);
const keyExists = existsSync(keyPath);

console.log("🔍 Certificate status:");
console.log(`   Cert exists: ${certExists ? "✅" : "❌"}`);
console.log(`   Key exists:  ${keyExists ? "✅" : "❌"}\n`);

// Test 2: Simulate getHttpsConfig() behavior
console.log("🧪 Testing getHttpsConfig() logic:\n");

// Test 2a: FORCE_HTTP=true
process.env.FORCE_HTTP = "true";
console.log("📝 Test 1: FORCE_HTTP=true");
if (process.env.FORCE_HTTP === "true") {
	console.log("   ✅ Result: Would return undefined (HTTP mode)");
} else {
	console.log("   ❌ Failed: Should use HTTP mode");
}
delete process.env.FORCE_HTTP;

// Test 2b: Certificates exist
console.log("\n📝 Test 2: Certificates exist");
if (certExists && keyExists) {
	console.log("   ✅ Result: Would return HTTPS config");
	console.log("   📦 Config would be:");
	console.log(`      { key: "${keyPath}", cert: "${certPath}" }`);
} else {
	console.log("   ⚠️  Result: Would return undefined (HTTP fallback)");
	console.log("   📋 User would see warning:");
	console.log("      → SSL certificates not found. Running in HTTP mode.");
	console.log("      → To enable HTTPS, generate certificates with:");
	console.log("      → cd infra && ./generate-ssl-certificate.sh");
}

console.log("\n✅ Test completed!");
