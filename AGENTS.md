# AGENTS.md

## Purpose and precedence
- Explicit user instructions take precedence.
- This file is the operating guide for AI coding agents working in `sc-trade-companion`.
- `CONTRIBUTING.md` remains the source of truth for contributor policy.

## Source documents
- `CONTRIBUTING.md`
- `README.md`
- `build.gradle`
- `contributing/GoogleStyle.xml`
- `contributing/companion.importorder`

## Required repository rules
- Changes SHALL compile successfully with `gradlew clean build`.
- Code SHALL follow the repository's Google Style and import order.
- Files in `tools.sctrade.companion.domain` SHALL NOT import code outside that package, except `tools.sctrade.companion.utils` and `tools.sctrade.companion.exceptions`.
- Public classes and methods SHALL be documented.
- Changes SHALL be unit tested.
- Tests SHALL be named using `given X when Y then Z`; when setup is generic, `when Y then X` is acceptable.
- Tests SHALL assert one thing.
- Tests SHOULD be parameterized when possible.
- Changes SHALL NOT reduce existing coverage.
- Changes SHOULD respect the current project structure.
- Changes SHOULD be self-explanatory through naming and flow.
- External libraries SHOULD be wrapped behind facades where appropriate.
- Changes SHOULD NOT introduce new compiler warnings or Spotless failures.

## Agent workflow
- Read the relevant docs and surrounding code before editing.
- Prefer surgical changes over broad refactors.
- Match existing naming, structure, and architectural patterns.
- Use the Gradle wrapper instead of introducing alternate build tooling.
- Use `gradlew spotlessApply` when formatting or import order updates are needed.
- Use `gradlew clean build` as the primary validation command for code changes.
- Keep tests aligned with the repository's naming and structure rules.
- Do not claim completion if local environment constraints prevent full validation.

## Safe boundaries
- Do not modify `build/`, `logs/`, `my-data/`, or `my-images/` unless the task explicitly requires it.
- Treat `bin/oneocr` and local DLL setup as environment-specific dependencies, not places for speculative fixes.

## Done and handoff expectations
- Summarize the meaningful change.
- State blockers or environment limitations plainly.
- Mention the validation command used when relevant.
- Keep the final handoff concise and reviewer-friendly.
