# NewsApp — Vibe Team Orchestration Guide

> **How to work at maximum speed, precision, and immaculate vibes with Claude Code.**
> Three specialized agents acting as your autonomous Vibe Team. We own our domains, we never step on each other's toes, and we ship clean, secure code fast.

---

## Agent Roster

| Agent | File | Scope | When to Use |
|-------|------|-------|-------------|
| **Data Layer** | `.claude/agents/data-layer-agent.md` | `:data`, `:domain` | Room, Retrofit, Repositories, UseCases, Mappers |
| **UI Layer** | `.claude/agents/ui-layer-agent.md` | `:feature:*`, `:app`, `:core` | Fragments, ViewModels, Adapters, XML Layouts |
| **Review** | `.claude/agents/review-agent.md` | ALL modules (read + fix) | Audits, compliance checks, pre-submission review |

---


## Quick Reference Prompts

Copy-paste these directly into Claude Code:

### Start Data Layer Work
```
Read CLAUDE.md and .claude/agents/data-layer-agent.md.
After completion, run ./gradlew :data:compileDebugKotlin and report.
```

### Continue Data Layer
```
Read CLAUDE.md and .claude/agents/data-layer-agent.md.
```

### Start UI After Data Complete
```
Read CLAUDE.md and .claude/agents/ui-layer-agent.md.
```

### Run Review
```
Read CLAUDE.md, .claude/agents/review-agent.md, 
Run the full review checklist. Generate REVIEW_REPORT.md in project root.
Fix all critical issues automatically. Report what was fixed and what needs manual attention.
```

### Quick Edge Case Check
```
Read CLAUDE.md and GAP_ANALYSIS.md (Edge Cases section).
For each edge case in the table, verify it is handled in the current code.
Report any unhandled edge cases with the exact file and fix needed.
```

---

## Agent Interaction Rules

**Data Layer Agent ↔ UI Layer Agent**
- Data Layer Agent defines interfaces (`NewsRepository`, `BookmarkRepository`, `SearchRepository`) — UI Layer Agent ONLY injects UseCases, never repositories directly
- If UI Layer Agent needs a new UseCase, it specifies the requirement to the Data Layer Agent
- No cross-module imports: `:feature:*` never imports from `:data`

**Review Agent authority**
- Review Agent can READ all files and FIX minor issues autonomously
- Review Agent REPORTS major architectural changes before implementing
- Review Agent runs AFTER each phase completion, not during

---
