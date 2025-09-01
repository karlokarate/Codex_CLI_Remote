# Changelog – Codex CLI Remote

All notable changes to this project are documented here. This file is maintained according to AGENTS.md (§6). Dates are in UTC.

## [Unreleased]

### Changed
- App name set to "Codex CLI Remote" globally for user-visible UI.
  - Manifest now uses `@string/app_name`.
  - `app_name` updated to "Codex CLI Remote".
  - Top bar title reads from `R.string.app_name` (Compose `stringResource`).
 - Gradle root project renamed to `Codex_CLI_Remote` to align with GitHub repo name.
- Move Android res backups to `app/src/main/res_backups` to avoid aapt merge errors.
- Bump `compileSdk` to 36 to satisfy androidx.core 1.17.0 AAR metadata.
- Remove deprecated `package` attribute from `AndroidManifest.xml` (namespace is defined in Gradle).
- Set manifest theme to app style `@style/Theme.CodexRemote` instead of a framework theme not present on all API levels.
- Add Gradle packaging exclusion for `META-INF/versions/9/OSGI-INF/MANIFEST.MF` to resolve MergeJavaRes duplicates from BouncyCastle/jspecify.

### Added
- First-run Setup Wizard tab with checklist steps, copyable PC setup commands, tests (TCP:22 reachability, SSH WSL status), and persistent checkboxes with reset.
- Help popups on Setup fields triggered on focus (toggleable).

### Fixed
- Kotlin compile: file-level opt-in for `ExperimentalMaterial3Api` to allow usage of Material3 components (TopAppBar/AssistChip) without warnings-as-errors.

### Why
- Align with canonical naming in AGENTS.md/ROADMAP and user request.

### How tested
- Grepped sources to remove old labels ("M3uSuite Remote", "Codex Remote").
- Local build recommended via `./gradlew :app:assembleDebug`.
