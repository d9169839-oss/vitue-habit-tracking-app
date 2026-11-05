# SOLID architecture review — Vitue Habit Tracker

**Review scope:** Kotlin/Compose source currently on `main`, including presentation, domain, data, dependency-injection, authentication, persistence/sync, programs and billing. This is a source-level design review, not a claim that every SOLID concern has been eliminated or that the project has passed a build.

## What is already in good shape

- **Dependency inversion:** `AuthRepository`, `HabitRepository`, `ProgramRepository` and `PremiumEntitlementProvider` are domain-facing contracts. Hilt binds data implementations to those contracts.
- **Use-case boundaries:** sign-in, registration, password reset, habit creation/completion and program actions have dedicated use cases. Validation for email/password/name lives outside Compose.
- **ViewModel/UI separation:** authentication and the main habit/program screens expose state and user actions rather than making Firebase/Room calls directly from composables.
- **Local-first persistence:** Room repositories persist user actions and queue durable sync operations; WorkManager performs cloud synchronization separately from the immediate UI interaction.
- **Domain services:** BMI calculation and program schedule personalization are separate from Compose and Android framework classes.

## Findings and recommended remediation

| Principle | Current observation | Assessment / next action |
|---|---|---|
| SRP | `presentation/App.kt` is approximately 730 lines and owns app-shell navigation, connectivity monitoring, banners, Home, History and Profile UI/form behaviour. | **Needs refactoring.** Split app shell/connectivity, Home, History and Profile into focused files/composables. Keep the root shell responsible only for destination selection and shared chrome. |
| SRP | `FirebaseAuthRepository` performs Firebase Authentication and also prepares Firestore profile/cache state and schedules habit sync after sign-in/register. | **Some responsibility leakage.** Introduce a post-authentication bootstrap coordinator/use case so the repository only translates authentication operations and auth state. |
| SRP / ISP | `HabitCloudDataSource` handles several cloud record families (profile, habits, check-ins and program records) and their sync rules. | **Too broad for long-term growth.** Extract focused cloud data sources or sync handlers by aggregate while retaining one orchestration worker. Avoid splitting until contract tests cover conflict/tombstone behaviour. |
| SRP | `PremiumBillingManager` owns BillingClient lifecycle, product discovery, checkout, purchase updates, acknowledgement and cached entitlement state. | **Multiple reasons to change.** Separate Play Billing transport from entitlement policy/cache and add tests around pending, cancelled, expired and restored purchases. |
| DIP / testability | `GoogleButton` constructs `GoogleCredentialClient` inside the composable. | **Platform coupling at UI boundary.** Move credential acquisition behind a small interface/factory supplied by Hilt (or a UI launcher abstraction) so screen tests can provide a fake. |
| OCP | Some UI and data mapping use explicit `when`/enum/string branches for status/category handling. | **Acceptable for closed enums today.** Keep status-specific behaviour behind domain mapping/policy functions as variants grow; avoid a generic plugin framework prematurely. |
| LSP / ISP | The repository and entitlement contracts are relatively small and purpose-specific; no clear substitutability violation was identified in the reviewed interfaces. | **No confirmed violation.** Keep interfaces consumer-focused and avoid adding unrelated methods to them. |

## Changes made in this update

1. Added one reusable multicolour Google VectorDrawable and used it in the shared Google sign-in button used by both Login and Register.
2. Routed Google credential acquisition errors to the same visible error state on both screens; coroutine cancellation is rethrown instead of being displayed as a sign-in failure.
3. Hardened `AuthViewModel`: unexpected exceptions from authentication and password-reset operations now become UI errors rather than crashing the coroutine and leaving the loading state stuck. Cancellation is still propagated.
4. Kept validation in domain use cases and Firebase SDK calls in the data implementation.

## Refactoring order before production

1. Extract `App.kt` into app shell + separate feature screen files, preserving existing behaviour and adding UI tests.
2. Add a post-auth bootstrap coordinator; test auth success independently from profile bootstrap/sync scheduling.
3. Split the broad cloud sync implementation behind focused contracts and add tests for pending local edits, tombstones, retries and account switching.
4. Split Play Billing transport from entitlement verification. **Do not use the local cached premium boolean as an authorization decision**; validate purchase tokens on a trusted backend before granting paid features.
5. Add fake credential-provider tests for Login/Register and tests for ViewModel failure/cancellation/loading transitions.

## Verification status

The repository was inspected at source level. Gradle was not executed by this review workflow. Run `./gradlew testDebugUnitTest assembleDebug` after pulling the branch, then add targeted tests for the authentication error paths and the extracted boundaries before merging any larger structural refactor.
