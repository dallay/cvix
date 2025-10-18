# Code Review Guidelines

> This document outlines the process and expectations for code reviews to ensure code quality, knowledge sharing, and a collaborative team environment.

## The Golden Rule

Treat every code review as a learning opportunity. Provide constructive, respectful, and clear feedback. The goal is to improve the code, not to criticize the author.

## For the Author (Submitting a Pull Request)

1. **Self-Review First**: Before requesting a review, perform a self-review of your own Pull Request (PR). Check for typos, debug code, and ensure it meets all requirements.
2. **Write a Clear PR Description**: The PR description should be clear and concise. It should explain:
    - **What** the change is.
    - **Why** the change is needed (link to the issue/ticket).
    - **How** the changes were implemented (if complex).
3. **Keep PRs Small and Focused**: A PR should ideally address a single concern. Small PRs are easier and faster to review.
4. **Ensure CI is Green**: All automated checks (tests, linting, builds) must pass before you request a review.

## For the Reviewer

1. **Understand the Context**: Read the PR description and the related issue to understand the purpose of the change.
2. **Provide Constructive Feedback**:
    - Be specific. Instead of "this is confusing," say "Can we rename this variable to `userProfile` for clarity?"
    - Offer suggestions for improvement.
    - Use comments to ask clarifying questions.
3. **Review for Key Areas**:
    - **Correctness**: Does the code do what it's supposed to do? Does it handle edge cases?
    - **Readability & Maintainability**: Is the code easy to understand? Does it follow our established conventions (see other `.ruler` docs)?
    - **Security**: Does the change introduce any security vulnerabilities (e.g., injection, XSS)?
    - **Performance**: Are there any obvious performance bottlenecks?
    - **Tests**: Are there enough tests? Do they cover the changes effectively?
4. **Approve or Request Changes**:
    - If the PR is good to go, approve it.
    - If changes are needed, leave specific comments and select "Request changes."

## Approval Process

- A PR must be approved by at least **one** other team member before it can be merged.
- The author is responsible for merging the PR after approval.
- Address all comments and resolve conversations before merging.
