# SOLID presentation refactor — Vitue Habit Tracker

**Date:** 2026-11-05  
**Scope:** Presentation-layer file organisation and maintainability only.

## Goal and compatibility promise

This change separates the previously 730-line `presentation/App.kt` into focused source files. The refactor is intended to preserve the existing user-visible flow and behaviour: authentication/navigation routes, tab order, tab state retention, connectivity banners, habit creation/completion/archive, date selection, history filtering, progress view, program full-screen navigation and sign-out callbacks.

No domain rules, repository contracts, Room schema, Firebase behaviour, billing logic, UI copy, styling, or navigation destinations were intentionally changed.

## Changes made

| File | Responsibility after refactor |
|---|---|
| `presentation/App.kt` | Root composition, selected tab, shared bottom navigation, program full-screen chrome, and reconnection-banner timing |
| `presentation/Connectivity.kt` | Validated internet observation and reusable connectivity banner |
| `presentation/HomeTab.kt` | Home UI, daily habit controls, date picker, create-habit dialog and habit card |
| `presentation/HistoryScreens.kt` | History filtering, date picker, history cards, day summaries and existing progress screen |
| `presentation/HabitStatusComponents.kt` | Shared habit-status summary card used by Home and Progress |
| `presentation/ProfileTab.kt` | Profile tab and sign-out UI |
| `docs/SOLID_REFACTORING_CHANGELOG.md` | This implementation record and validation checklist |

## SOLID impact

- **SRP:** app-shell orchestration is separated from feature-specific screen composition and connectivity observation.
- **OCP:** shared presentation pieces can be extended without growing the root shell.
- **ISP/DIP:** no repository or ViewModel interfaces were changed; existing dependency boundaries remain intact.
- **LSP:** no inheritance or subtype contracts were introduced or altered.

This is a focused first step, not a claim that every SOLID finding is now resolved. The broader findings in `docs/SOLID_REVIEW.md`—auth bootstrap, cloud sync responsibilities, billing responsibilities and Google credential injection—remain separate future work.

## Comments added or retained

Comments explain the intent of important non-obvious behaviour: destinations remain composed for fast tab switching and state retention; offline status remains visible until connectivity returns; and connectivity requires Android's validated internet capability. Routine statements were not given redundant comments.

## Validation

- Source-level extraction was prepared from the current `main` file.
- The first CI attempt exposed a pre-existing workflow setup failure: `setup-android` tried to install the retired SDK package `tools`, so compilation never started. The workflow now requests the required API 36 platform and build-tools packages explicitly.
- A successful Gradle build/test is still required; do not treat this document as proof of a successful build.
- Suggested local check:
  ```bash
  git checkout main
  git pull origin main
  ./gradlew testDebugUnitTest assembleDebug
  ```
- If a compile error appears, do not ship until it is fixed and the same command passes.

## Follow-up recommendations

1. Add Compose/UI tests around tab switching, date selection and sign-out before deeper changes.
2. Introduce a post-auth bootstrap use case to separate Firebase Auth from profile/sync setup.
3. Split cloud sync by aggregate with conflict/tombstone tests.
4. Split Play Billing transport from entitlement policy and verify purchases on a trusted backend.
5. Inject Google credential acquisition behind an interface for deterministic ViewModel/UI tests.
