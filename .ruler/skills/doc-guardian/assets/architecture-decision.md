---
title: ADR-{NUMBER} - {Decision Title}
date: {YYYY-MM-DD}
status: {Proposed|Accepted|Deprecated|Superseded}
deciders: {List of people}
---

# ADR-{NUMBER}: {Decision Title}

## Status

**{Proposed|Accepted|Deprecated|Superseded}** - {Date}

{If superseded, link to new ADR}

## Context

{What is the issue we're facing? What factors are driving this decision?}

### Background

{Relevant background information}

### Problem Statement

{Clear, concise statement of the problem}

### Constraints

- Constraint 1: {Description}
- Constraint 2: {Description}

## Decision Drivers

- **Performance**: {Why it matters}
- **Maintainability**: {Why it matters}
- **Developer Experience**: {Why it matters}
- **Cost**: {Why it matters}
- **Security**: {Why it matters}

## Considered Options

### Option 1: {Name}

**Description:**
{What is this option?}

**Pros:**

- ✅ Pro 1
- ✅ Pro 2

**Cons:**

- ❌ Con 1
- ❌ Con 2

**Impact:**
{What needs to change to implement this?}

---

### Option 2: {Name}

{Same structure as Option 1}

---

### Option 3: {Name}

{Same structure as Option 1}

---

## Decision Outcome

**Chosen option:** "{Option X}"

### Rationale

{Why did we choose this option? What makes it better than the alternatives?}

1. **Reason 1**: {Explanation}
2. **Reason 2**: {Explanation}
3. **Reason 3**: {Explanation}

### Expected Consequences

**Positive:**

- Consequence 1
- Consequence 2

**Negative:**

- Trade-off 1: {How we'll mitigate}
- Trade-off 2: {How we'll mitigate}

**Neutral:**

- Change 1: {What needs to happen}

## Implementation Plan

### Phase 1: {Name}

1. Step 1
2. Step 2
3. Step 3

**Timeline:** {Duration}
**Responsible:** {Team/Person}

### Phase 2: {Name}

{Same structure}

### Phase 3: {Name}

{Same structure}

## Validation

{How will we know if this decision was correct?}

### Success Metrics

| Metric                 | Current | Target  | Measured By   |
|------------------------|---------|---------|---------------|
| Performance            | {value} | {value} | {tool/method} |
| Adoption               | 0%      | 100%    | {tool/method} |
| Developer Satisfaction | {value} | {value} | Survey        |

### Review Date

{When will we review this decision?} - {YYYY-MM-DD}

## Links

- **Related ADRs:**
    - [ADR-001: Previous Decision](./adr-001.md)
    - [ADR-003: Related Decision](./adr-003.md)

- **References:**
    - [Documentation](link)
    - [RFC/Proposal](link)
    - [Discussion Thread](link)
    - [Proof of Concept](link)

- **Implementation:**
    - [PR #123: Implementation](link)
    - [Issue #456: Tracking](link)

## Notes

{Additional context, discussions, or important points}

---

## ADR Template Usage

1. **Copy this template** to `/docs/adr/adr-{NUMBER}-{title}.md`
2. **Replace placeholders** with actual content
3. **Number sequentially** (001, 002, 003...)
4. **Update status** as decision evolves
5. **Link from main docs** in relevant sections
6. **Review periodically** and update if circumstances change

## Example ADR

See [ADR-001: Use Pinia for State Management](./adr-001-pinia-state-management.md)
