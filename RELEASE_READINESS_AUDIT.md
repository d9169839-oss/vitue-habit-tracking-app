# Vitue Habit Tracker — Release Readiness Audit

**Audit date:** 18 November 2026  
**Repository:** `d9169839-oss/vitue-habit-tracking-app`  
**Baseline audited:** `main` after merge of PR #8; PR #9 adds free Programs and initial release hardening.  
**Verdict:** **Not ready for public Play Store release yet.** The core Android app and local-first habit workflow are a useful MVP foundation, but account deletion, data-sync safety, Firebase verification, and release verification remain launch gates. The first public release now includes all Programs free of charge and has no in-app subscription or purchase flow.

This is a source-level audit. It does not claim that Firebase rules are deployed or that the app has passed physical-device testing. The audit cannot inspect the user's Firebase/Play Console account state.

## What is in good shape

- Native Kotlin + Jetpack Compose app with Hilt, MVVM, Room, Firebase Authentication and Firestore.
- Room-first habit writes with a durable outbox; network work is moved to WorkManager.
- User-scoped Firestore paths and UID checks in the rules.
- Auth session restoration, email/password and Google sign-in flows.
- History and date-based habit status model, with completed / not completed / unrecorded states.
- Program catalog and deterministic schedule generation are separated from Android UI code.
- The startup hydration, program progress, realtime program sync, and weekly History changes are merged to `main`; GitHub Actions passed unit tests and debug compilation/assembly for PR #8.
- The app targets API 36, which meets the Google Play target API requirement for new mobile apps and updates from 31 August 2026.

## Release blockers — fix before public launch

### P0 — Account deletion is missing

The app supports account creation, but the inspected app has sign-out and no account-deletion flow or external deletion-request webpage. Google Play requires both an in-app path and an external web resource for apps that allow account creation. The deletion operation must remove the account's associated data, not merely sign the user out.

**Required work**
- Add a discoverable Delete account action with a clear irreversible-action confirmation and recent-authentication handling.
- Delete the user's Firestore profile and all nested habit, check-in, program-enrollment and program-activity documents.
- Remove local Room data, per-user sync state, pending work for that UID.
- Delete the Firebase Authentication account after cloud data deletion succeeds; handle partial failures and retry safely.
- Publish an external deletion-request page that names Vitue Habit Tracker and can be used without reinstalling the app.
- Add retention disclosures for any data that cannot legally or operationally be deleted immediately.

### Removed from launch scope — subscriptions and premium access

The first public release makes every built-in program free. Google Play Billing, premium-only program restrictions, subscription products, and trusted purchase verification are not part of this release. This removes the need for a separate subscription-verification backend. Firebase Authentication and Firestore are separate existing services used for account sign-in and cloud sync; their launch status must be decided and verified independently.

### P0 — Firestore rules must be deployed and tested

The app's domain enum includes `PERSONAL_DEVELOPMENT`, but the original Firestore allow-list only accepted `FITNESS`, `SELF_GROOMING` and `MINDFULNESS`. That would reject cloud writes for personal-development enrollments. This audit branch adds the missing category to `firestore.rules`; the change is not deployed automatically.

**Required work**
- Publish the corrected rules to the production Firebase project.
- Add Firebase Emulator Suite rules tests for signed-out access, cross-user access, malformed documents, allowed program categories and tombstones.
- Review the broad owner-write rules on profile, habit and check-in documents. UID scoping is good, but field/type/size validation should be added to prevent malformed or unexpectedly large records.
- Verify the production Firebase project, Android package, OAuth client ID and signing fingerprints match.

### P0 — Offline account switching can discard unsynced work

`HabitCloudDataSource.ensureLocalOwner()` clears the single local Room cache and the entire sync outbox when a different UID signs in. This prevents one account's data being exposed to another, but it also discards legitimate offline edits that have not reached Firestore.

**Required work**
- Prefer per-account local storage/outboxes, or prevent account switching until pending changes are uploaded.
- If the user chooses to discard pending changes, explain exactly what will be lost and require explicit confirmation.
- Add tests for sign-out, sign-in as another user, offline edits, process death and retry.
- Never silently delete pending user work.

## High-priority production hardening

### Sync correctness and observability

- Habit, check-in, program-enrollment, and program-activity realtime listeners are now present. The durable WorkManager outbox remains responsible for uploads and retry/catch-up. Test live updates, offline recovery, and process-death behaviour on two devices.
- The sync implementation uses client-generated `updatedAtMillis` values as incremental cursors. Device clock skew can cause missed or repeatedly fetched updates. Prefer server-authoritative timestamps or a versioning strategy, and define a deterministic conflict policy.
- Same-entity concurrent edits across devices still need a documented policy (for example, server version + last-write-wins with explicit local pending-write protection, or a conflict record).
- Add a user-visible last-synced state and a recoverable sync-error state. The offline banner alone does not tell a user whether their latest change is safely backed up.
- Add tests for outbox atomicity, retries, tombstones, stale acknowledgements, account changes, clock skew and simultaneous edits.

### Programs progress and History — updated in PR #8

- Program progress is integrated into Programs: active program cards show completion percentage and a progress bar; the program detail screen shows scheduled activities and their status.
- Marking an activity Done/Skip/Reset updates Room and queues the matching Firestore operation. Account-scoped realtime listeners merge remote program changes into Room.
- The Programs repository waits for initial program enrollment/activity snapshots, with a bounded fallback, before exposing an empty state on a new device.
- Zero-minute rest/recovery days remain visible in a program but are excluded from the completion denominator.
- History remains habit-only and now navigates by complete Monday–Sunday weeks. The seven-day chart is not clickable, includes zero-completion days, and calculates completion against all habits active that day.
- PR #8 unit tests and debug compilation/assembly passed. The actual two-device sync path and production Firestore rules still require physical-device verification.

### Local database and history scale

- Room database version is 4 and explicit migrations are registered, which is a good foundation.
- Room schema export is disabled (`exportSchema = false`). Enable schema export, commit schema snapshots and add migration tests before production data accumulates.
- History/progress reads all check-ins through the selected date and renders habit rows in a regular Column. This is simple for early users, but long histories will increase query, memory and composition cost. Add indexed, paginated date-range queries / LazyColumn when history volume grows, and benchmark realistic multi-year data.
- Test migrations from every released schema version using real fixture databases; never rely on destructive migration for user data.

### Notifications and permission UX

- Notification permission is requested automatically during the first app launch, before the user has chosen to use reminders. Ask contextually after the user enables reminders or chooses a reminder time, explain the value, and provide an in-app route to Android notification settings after denial.
- The current one-time `requested` flag means a denied first prompt is not naturally retried. Make the preference model reflect the user's reminder choice rather than just whether the prompt was shown.
- WorkManager reminders are intentionally inexact and can be delayed by battery restrictions; communicate this honestly in settings.

### App identity and release configuration

- PR #9 adds adaptive launcher icon resources and declares `android:icon` / `android:roundIcon`. Verify the icon visually on light/dark launchers and multiple device shapes before release.
- PR #9 sets `versionName 1.0.0` and keeps `versionCode 1` for the first release. Confirm no higher versionCode has already been uploaded to Play Console; increment it for every subsequent upload.
- PR #9 adds a `bundleRelease` CI task, Gradle-property-based upload-key signing, and `docs/RELEASE_BUILD.md`. A signed AAB is not verified until the owner configures the upload keystore properties and builds locally; CI intentionally has no signing secrets.
- Add release shrinking/obfuscation only with keep rules and a tested mapping-file retention process.
- The Google web OAuth client ID is hard-coded in Gradle. It is not a private secret, but it is environment-specific; move it to a documented build configuration property and validate production Firebase fingerprints.
- Confirm the app has a stable package name, app icon, screenshots, feature graphic, store description, support contact and content rating before listing.

### Quality gates and automated tests

PR #9 configures GitHub Actions to run `testDebugUnitTest lintDebug assembleDebug bundleRelease`. The latest run must pass before merge. CI still does not run instrumentation/UI tests, validate Firestore rules in an emulator, or exercise production Firebase authentication/sync.

Add CI gates for:
1. `testDebugUnitTest`
2. `lint` (or `lintDebug`)
3. `assembleDebug`
4. `bundleRelease` using CI-safe configuration
5. Room migration tests
6. Firestore Emulator rules tests
7. Selected Compose UI / navigation / accessibility tests

The visible unit-test suite focuses mainly on domain calculations, registration validation and program scheduling. Add repository, ViewModel, database migration, sync, account-deletion and reminder tests. Run TalkBack, large font, narrow-device and dark-theme checks on physical devices.

## Conditional Google Play production-access gate

- Personal developer accounts created after 13 November 2023 must complete a closed test with at least 12 testers opted in continuously for 14 days before applying for production access.
- Internal testing can still be used while preparing the release, but it does not replace this closed-testing requirement.
- Confirm the developer account type/creation date and whether the 14-day tester period has already been completed before promising a public production date.

## Play Store policy and product readiness

- **Target API:** `targetSdk = 36` is aligned with Google's requirement for new mobile apps and updates starting 31 August 2026.
- **Data Safety:** complete the Play Console Data safety form accurately for Firebase Authentication, profile fields, habit records, cloud sync and any future analytics/ads. Keep it consistent with the privacy policy.
- **Privacy policy:** publish a public, accessible policy that describes data collected, purposes, storage, retention, deletion, security and support contact.
- **Account deletion:** provide both the in-app path and external request page described above.
- **Health/wellness content:** keep claims conservative, clearly distinguish general wellness from medical advice, and have fitness content reviewed by a qualified professional before public distribution.
- **Accessibility:** verify touch targets, content descriptions, contrast, keyboard/switch access, TalkBack order and font scaling.
- **Localization:** validate English copy and date/time behavior; consider Urdu only after the first language is polished and tested.

## Recommended launch sequence

1. **Close P0 gates:** account deletion, privacy policy/web deletion resource, deploy/test Firestore rules, and protect unsynced local changes.
2. **Stabilize data:** server-authoritative sync timestamps, conflict policy, visible sync status, Room schema snapshots and migration tests.
3. **Harden the release:** launcher icon, release AAB/signing, lint + release CI, emulator rules tests, UI/accessibility/device matrix.
4. **Internal testing:** install from a Play internal testing track on multiple Android versions and at least two accounts/devices; test fresh install, upgrade, offline, reconnect, account switch, notification denial and data deletion.
5. **Closed testing / staged rollout:** monitor crash-free sessions, sync failures, sign-in failures, sync errors, uninstall feedback and support requests before broad rollout.

## Audit limitations

- No physical device or emulator was available for this source audit.
- Production Firebase project settings, deployed Firestore rules, OAuth fingerprints and account data were not inspected.
- Play Console developer account status, signing setup, Data Safety answers and store listing were not inspected. No subscription products or billing configuration are required for the free first release.
- The source-level CI pass only confirms the workflow's debug unit tests and debug assembly for the merged loading-state branch; it is not a production release sign-off.
