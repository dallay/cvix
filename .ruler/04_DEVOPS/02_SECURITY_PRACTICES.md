# Security Best Practices

> This document outlines critical secure coding guidelines based on the OWASP Top 10 to be followed by all developers and AI assistants.

## A01: Broken Access Control

- **Enforce Principle of Least Privilege**: Default to the most restrictive permissions. Authorization logic must verify user permissions against the specific resource being accessed on every request.
- **Backend Enforcement**: Access control checks must be performed on the backend. Never rely on the UI to enforce access control.

## A02: Cryptographic Failures

- **Use Strong Algorithms**: Use modern, strong algorithms. For password hashing, use **Argon2** or **bcrypt**. For data in transit, use **TLS 1.2+**. For data at rest, use authenticated encryption like **AES-GCM**.
- **No Hardcoded Secrets**: Never hardcode API keys, passwords, or other secrets in the source code. Load them from environment variables or a secure secret management service (e.g., HashiCorp Vault, AWS Secrets Manager).

## A03: Injection

- **Parameterized Queries**: To prevent SQL injection, all database queries must be constructed using parameterized queries (prepared statements). Never use string concatenation to build queries with user input.
- **Output Encoding**: To prevent Cross-Site Scripting (XSS), encode all user-supplied data before rendering it in the UI. Use modern frontend frameworks' built-in encoding mechanisms (e.g., Vue's `{{ }}`). When you must insert HTML, sanitize it first with a library like DOMPurify.

## A05: Security Misconfiguration

- **Secure Headers**: Set security headers on all web responses: `Content-Security-Policy`, `Strict-Transport-Security`, `X-Content-Type-Options`, `X-Frame-Options`.
- **Disable Debug Features**: Ensure verbose error messages, stack traces, and other debug features are disabled in production environments.

## A06: Vulnerable and Outdated Components

- **Regularly Scan Dependencies**: Use tools like `npm audit`, `pnpm audit`, or OWASP Dependency-Check to scan for vulnerabilities in third-party libraries. Update dependencies promptly.

## A08: Software and Data Integrity Failures

- **Prevent Insecure Deserialization**: Do not deserialize data from untrusted sources without strict validation. Prefer safe data formats like JSON over more complex formats that can be abused.

## General Guidelines

- **Validate All Input**: Treat all input from users, third-party services, and even other parts of your own system as untrusted. Validate for type, length, format, and range.
- **Secure Session Management**: Generate a new session identifier upon login. Use `HttpOnly`, `Secure`, and `SameSite=Strict` attributes for session cookies.
