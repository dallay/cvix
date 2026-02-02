import { existsSync } from "node:fs";
import { SSL_CERT_PATH, SSL_KEY_PATH } from "../consts/ssl-paths.js";

/**
 * Check if SSL certificates exist for HTTPS development
 * @returns true if both cert and key files exist
 */
export function hasSSLCertificates(): boolean {
	return existsSync(SSL_CERT_PATH) && existsSync(SSL_KEY_PATH);
}

/**
 * HTTPS configuration object for Vite dev server
 */
export interface HttpsConfig {
	key: string;
	cert: string;
}

/**
 * Get HTTPS configuration for Vite dev server.
 * Falls back to HTTP if certificates are not available.
 *
 * @returns HTTPS config object or undefined for HTTP mode
 */
export function getHttpsConfig(): HttpsConfig | undefined {
	// Check for explicit HTTP-only mode via environment variable
	if (process.env.FORCE_HTTP === "true") {
		console.log("ℹ️  FORCE_HTTP=true detected, running in HTTP mode");
		return undefined;
	}

	// Check if certificates exist
	if (!hasSSLCertificates()) {
		console.warn("⚠️  SSL certificates not found. Running in HTTP mode.");
		console.warn("   To enable HTTPS, generate certificates with:");
		console.warn("   → cd infra && ./generate-ssl-certificate.sh");
		console.warn("   → OR run: make ssl-cert");
		console.warn("   → See: client/HTTPS_DEVELOPMENT.md for details");
		return undefined;
	}

	// Certificates exist, use HTTPS
	console.log("✅ SSL certificates found, running in HTTPS mode");
	return {
		key: SSL_KEY_PATH,
		cert: SSL_CERT_PATH,
	};
}
