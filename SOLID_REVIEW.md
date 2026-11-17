# SOLID architecture review — Virtue Habit Tracker

**Review scope:** Kotlin/Compose source currently on `main`, including presentation, domain, data, dependency-injection, authentication, persistence/sync, and free Programs.

## What is in good shape

- **Dependency Inversion (DIP):** `AuthRepository`, `AuthSessionCoordinator`, `HabitRepository`, `ProgramRepository`, and `GoogleCredentialProvider` are domain/presentation-facing abstractions. Hilt binds implementations to these contracts.
- **Single Responsibility (SRP):**
  - Use-case boundaries: sign-in, registration, password reset, habit creation/completion, and program actions have dedicated use cases. Validation for email/password/name lives outside Compose.
  - Post-auth bootstrap: `FirebaseAuthRepository` only handles Firebase Authentication; session preparation and sync scheduling are coordinated via `AuthSessionCoordinator`.
  - Presentation extraction: `App.kt` was split into modular feature files (`HomeTab.kt`, `HistoryScreens.kt`, `ProfileTab.kt`, `Connectivity.kt`, `HabitStatusComponents.kt`).
  - Free Programs for Launch: All programs are available freely to every user without Google Play Billing in the first public release.
- **Interface Segregation (ISP):** Credential acquisition is isolated behind `GoogleCredentialProvider`, allowing deterministic testing without CredentialManager coupling.
- **ViewModel/UI separation:** Authentication, Home, History, and Programs screens expose state and user actions rather than making Firebase or Room calls directly from composables.
- **Local-first persistence:** Room repositories persist user actions and queue durable sync operations; WorkManager performs cloud synchronization separately from immediate UI interactions.
- **Domain services:** BMI calculation and program schedule personalization are pure Kotlin services separate from Compose and Android framework classes.

## Findings and remediation status

| Principle | Observation | Status | Remediated Action / Current State |
|---|---|---|---|
| SRP | `presentation/App.kt` was ~730 lines and owned app-shell, navigation, banners, and multiple tabs. | **Resolved** | Split into `App.kt`, `Connectivity.kt`, `HomeTab.kt`, `HistoryScreens.kt`, `HabitStatusComponents.kt`, and `ProfileTab.kt`. |
| SRP | `FirebaseAuthRepository` performed auth and also prepared Firestore profile/cache state and scheduled habit sync. | **Resolved** | Introduced `AuthSessionCoordinator` contract and `DefaultAuthSessionCoordinator`. `FirebaseAuthRepository` now only translates auth operations. |
| DIP / testability | `GoogleButton` directly constructed `GoogleCredentialClient` inside Compose. | **Resolved** | Introduced `GoogleCredentialProvider` interface. Injected into `LoginScreen`, `RegisterScreen`, and `GoogleButton` with fake support for tests. |
| SRP / Free launch | Google Play Billing removed for initial release. | **Resolved** | Programs are free for all users at launch; billing complexity removed from initial public release. |
| SRP / ISP | `HabitCloudDataSource` handles several cloud record families (profile, habits, check-ins, program records) and their sync rules. | **Roadmap / Follow-up** | Broad aggregate cloud sync; planned for split into aggregate-specific sync handlers (Habits, CheckIns, Programs) once multi-device contract tests are established. |
| OCP | UI and data mapping use explicit `when`/enum branches for status/category handling. | **Acceptable** | Closed domain enums with domain mapping policy functions. |
| LSP / ISP | Repository contracts are consumer-focused without substitutability issues. | **Compliant** | All repository and provider implementations cleanly substitute their interface contracts. |

## Refactoring checklist & progress

1. [x] Extract `App.kt` into app shell + separate feature screen files (`HomeTab.kt`, `HistoryScreens.kt`, `ProfileTab.kt`, etc.).
2. [x] Add a post-auth bootstrap coordinator (`AuthSessionCoordinator`); test auth success independently from profile bootstrap/sync scheduling.
3. [x] Make all Programs free for initial launch; remove Google Play Billing client complexity from release.
4. [x] Introduce `GoogleCredentialProvider` interface; enable fake credential-provider tests for Login/Register.
5. [x] Add unit test suite covering AuthUseCases and AuthViewModel.
6. [ ] Split broad `HabitCloudDataSource` by aggregate (HabitSyncHandler, ProgramSyncHandler) with multi-device conflict/tombstone contract tests.

## Verification status

- Automated unit tests passing.
- Clean Gradle build.
