# AGENTS.md design for sc-trade-companion

## Problem

The repository has contributor guidance in `CONTRIBUTING.md`, but it does not yet have an `AGENTS.md` file that translates those expectations into concrete instructions for AI coding agents. The new file should start from the existing contributor policy, preserve the repo's quality bar, and add workflow and safety guidance that helps autonomous tools behave predictably in this codebase.

## Proposed approach

Create `AGENTS.md` as a hybrid operational guide for AI coding agents:

- Keep repository rules recognizable and policy-like where they are non-negotiable.
- Add direct execution guidance for common agent behavior such as reading context, making surgical changes, formatting, validation, and handoff.
- Treat `CONTRIBUTING.md` as the policy source of truth and position `AGENTS.md` as the agent-facing operating manual for applying that policy.

## Goals

- Give AI agents a single entry point for working safely in this repository.
- Preserve the important quality expectations already defined in `CONTRIBUTING.md`.
- Reduce avoidable agent mistakes by documenting workflow, validation, and file-boundary expectations.
- Keep the document practical and maintainable instead of duplicating the entire contributor guide verbatim.

## Non-goals

- Replacing `CONTRIBUTING.md` as the canonical contributor policy.
- Creating a generic, tool-agnostic manifesto that ignores repo specifics.
- Expanding project scope beyond contributor behavior and repository workflow.

## Audience

Primary audience: AI coding agents working in this repository.

Secondary audience: humans reviewing or maintaining the guidance, who need the file to stay aligned with existing repo conventions.

## Recommended AGENTS.md structure

### 1. Purpose and precedence

Open with a short section that explains what the file is for and how it should be interpreted. The section should clarify that:

- explicit user instructions take precedence;
- `AGENTS.md` provides agent-facing repo workflow and guardrails;
- `CONTRIBUTING.md` and other repository docs remain the source of truth for contributor policy.

This section should keep the document anchored to repo-specific guidance rather than generic agent behavior.

### 2. Source documents

List the repository documents and configuration files agents should consult before making changes:

- `CONTRIBUTING.md`
- `README.md`
- `build.gradle`
- files under `contributing\` that define formatting and import order

This gives agents a predictable starting point and helps keep behavior aligned with the actual project setup.

### 3. Required repository rules

Capture the core rules from `CONTRIBUTING.md` in a concise policy-style section. The section should preserve the meaning of the existing guidance, including:

- the branch must compile successfully;
- code must follow the configured code style and import order;
- files in `tools.sctrade.companion.domain` must not import outside the package except for `tools.sctrade.companion.utils`;
- public classes and methods must be documented;
- changes must be unit tested;
- changes must not reduce existing code coverage;
- changes should respect the project structure;
- changes should be self-explanatory;
- library integrations should be abstracted behind facades where appropriate;
- changes should not introduce new compiler or Checkstyle-style warnings.

The AGENTS version should stay shorter than `CONTRIBUTING.md`, but it should remain clearly traceable back to those rules.

### 4. Agent workflow

Translate repo expectations into direct instructions for autonomous agents. This section should guide agents to:

- read relevant docs and surrounding code before editing;
- prefer surgical changes over broad refactors;
- match existing naming, structure, and architectural patterns;
- use the Gradle wrapper rather than inventing alternate build tooling;
- use `gradlew spotlessApply` when formatting/import order changes are needed;
- use `gradlew clean build` as the primary validation gate;
- avoid claiming completion if repo constraints or local environment issues prevent full validation.

This section is where the document becomes an operational manual instead of a policy summary.

### 5. Safe boundaries

Add a repo-specific section that helps agents avoid noisy or unsafe edits. It should state that agents should not modify generated, runtime, or environment-specific areas unless the task explicitly requires it, including:

- `build\`
- `logs\`
- `my-data\`
- `my-images\`
- local native dependency contents under `bin\oneocr`

This section should also warn that missing local DLL dependencies are environment setup concerns, not something an agent should silently change repository code to work around.

### 6. Done and handoff expectations

Close with practical completion criteria for agents. This section should tell agents to:

- summarize the meaningful change;
- call out limitations or environment blockers plainly;
- mention relevant validation commands when useful;
- keep final handoff concise and reviewer-friendly.

This creates a consistent finish line for agent work in the repository.

## Content and tone

The document should use a hybrid tone:

- policy language for hard rules and repository invariants;
- direct action language for workflow, safety, and validation guidance.

That keeps the file aligned with the current contributor policy while making it easier for agents to follow.

## Example high-level outline

```md
# AGENTS.md

## Purpose and precedence
## Source documents
## Required repository rules
## Agent workflow
## Safe boundaries
## Done and handoff expectations
```

## Notes

- The file should reference `CONTRIBUTING.md` rather than restating large sections verbatim.
- It should remain specific to `sc-trade-companion`, especially around Gradle commands, formatting configuration, package-boundary rules, and local native dependencies.
- It should avoid speculative rules that are not supported by the current repository documentation or configuration.
