# Changelog – Codex CLI Remote

All notable changes to this project are documented here. This file is maintained according to AGENTS.md (§6). Dates are in UTC.

## [Unreleased]

### Changed
- App name set to "Codex CLI Remote" globally for user-visible UI.
  - Manifest now uses `@string/app_name`.
  - `app_name` updated to "Codex CLI Remote".
  - Top bar title reads from `R.string.app_name` (Compose `stringResource`).

### Why
- Align with canonical naming in AGENTS.md/ROADMAP and user request.

### How tested
- Grepped sources to remove old labels ("M3uSuite Remote", "Codex Remote").
- Local build recommended via `./gradlew :app:assembleDebug`.

