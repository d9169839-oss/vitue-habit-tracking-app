# Vitue Habit Tracker — Release Readiness Audit

**Audit date:** 17 November 2026  
**Repository:** `d9169839-oss/vitue-habit-tracking-app`  
**Branch audited:** `main` after merge of PR #6  
**Verdict:** **Not ready for public Play Store release yet.** The core Android app and local-first habit workflow are a useful MVP foundation, but account deletion, subscription security/compliance, data-sync safety, and release verification remain launch gates.

This is a source-level audit. It does not claim that Firebase rules are deployed, Play Console products are configured, or the app has passed physical-device testing. The audit cannot inspect the user's Firebase/Play Console account state.

## What is in good shape

- Native Kotlin + Jetpack Compose app with Hilt, MVVM, Room, Firebase Authentication and Firestore.
- Room-first habit writes with a durable outbox; network work is moved to WorkManager.
- User-scoped Firestore paths and UID checks in the rules.
- Auth session restoration, email/password and Google sign-in flows.
- History and date-based habit status model, with completed / not completed / unrecorded states.
- Program catalog and deterministic schedule generation are separated from Android UI code.
- The recent startup hydration and section-loading work is merged to `main`; its GitHub Actions run passed the debug unit-test and debug-assembly job.
- The app targets API 36, which meets the Google Play target API requirement for new mobile apps and updates from 31 August 2026.

## Release blockers — fix before public launch

### P0 — Account deletion is missing

The app supports account creation, but the inspected app has sign-out and no account-deletion flow or external deletion-request webpage. Google Play requires both an in-app path and an external web resource for apps that allow account creation. The deletion operation must remove the account's associated data, not merely sign the user out.

**Required work**
- Add a discoverable Delete account action with a clear irreversible-action confirmation and recent-authentication handling.
- Delete the user's Firestore profile and all nested habit, check-in, program-enrollment and program-activity documents.
- Remove local Room data, per-user sync state, pending work and cached entitlement data for that UID.
- Delete the Firebase Authentication account after cloud data deletion succeeds; handle partial failures and retry safely.
- Publish an external deletion-request page that names Vitue Habit Tracker and can be used without reinstalling the app.
- Add retention disclosures for any data that cannot legally or operationally be deleted immediately.

### P0 — Google Play Billing Library is too old

The version catalog currently pins `com.android.billingclient:billing-ktx:6.2.1`. Google Play's current deprecation schedule gives Billing Library 6 a submission deadline of 31 August 2025 and Billing Library 7 a deadline of 31 August 2026. As of this audit date, new apps and updates need Billing Library 8 or newer.

**Required work**
- Migrate to a supported release (prefer the current 9.x line after reviewing its migration guide) and update removed/deprecated API calls.
- Run purchase, restore, pending, cancellation, expiry, grace-period, refund and reconnect tests using Play license testers.
- Do not treat a successful debug build as proof that Play Billing is compliant.

### P0 — Premium entitlement is not securely verified

`PremiumBillingManager` stores a `premiumCached` boolean in SharedPreferences and sets it from the client-side purchase query. There is no trusted purchase-token verification endpoint in the repository. The client also acknowledges owned purchases without first receiving a verified backend entitlement.

**Required work**
- Verify purchase tokens with the Google Play Developer API on a trusted backend.
- Bind verified subscription entitlements to the authenticated Firebase UID; never accept a client-writable boolean as proof of payment.
- Handle token replay, account changes, expiry, grace period, cancellation, refunds, revocation and restore.
- Keep offline entitlement caching as a temporary UX convenience only, with a bounded and explicit stale-entitlement policy.
- If the backend is not ready, launch the first public version without paid program access rather than shipping insecure billing.

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

- Habit/check-in real-time listeners are present; program documents currently rely on WorkManager pulls/uploads rather than their own live listeners. This is acceptable for an MVP if eventual consistency is clearly communicated, but test the actual delay and recovery behavior.
- The sync implementation uses client-generated `updatedAtMillis` values as incremental cursors. Device clock skew can cause missed or repeatedly fetched updates. Prefer server-authoritative timestamps or a versioning strategy, and define a deterministic conflict policy.
- Same-entity concurrent edits across devices still need a documented policy (for example, server version + last-write-wins with explicit local pending-write protection, or a conflict record).
- Add a user-visible last-synced state and a recoverable sync-error state. The offline banner alone does not tell a user whether their latest change is safely backed up.
- Add tests for outbox atomicity, retries, tombstones, stale acknowledgements, account changes, clock skew and simultaneous edits.

### Program progress accuracy and first-sync state

- `ProgramProgress.totalCount` currently counts every generated activity, including zero-minute "Rest and reset" days, while `progressPercent` counts only completed activities. A user can follow every planned workout and still see an artificially low completion percentage. Define whether planned rest days count as completed adherence or should be excluded from the denominator, then add unit tests.
- The Programs ViewModel considers its first Room emission to mean programs have loaded. On a fresh device, that emission can be empty while the WorkManager cloud import is still pending, so saved programs may appear late without a dedicated syncing state. Test a second-device first launch and add a program-hydration indicator/gate if the empty flash is visible.
- Program activity/enrollment cloud synchronization currently uses WorkManager rather than real-time listeners. Set a clear expected consistency window and test process-death/reconnect recovery.

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

- The manifest does not declare `android:icon` or `android:roundIcon`. Add a production adaptive launcher icon and verify it on light/dark launchers.
- The current app version is `versionCode 1`, `versionName 0.1.0`; increment these for each Play release.
- The inspected Gradle configuration does not show a dedicated release verification pipeline or a documented signed AAB generation step. Configure a protected upload key / Play App Signing workflow, build a release AAB, and verify the merged release manifest.
- Add release shrinking/obfuscation only with keep rules and a tested mapping-file retention process.
- The Google web OAuth client ID is hard-coded in Gradle. It is not a private secret, but it is environment-specific; move it to a documented build configuration property and validate production Firebase fingerprints.
- Confirm the app has a stable package name, app icon, screenshots, feature graphic, store description, support contact and content rating before listing.

### Quality gates and automated tests

The current GitHub Actions workflow runs `testDebugUnitTest assembleDebug`. It does not run Android Lint, build a release bundle, run instrumentation/UI tests, validate Firestore rules in an emulator, or exercise a real Firebase/Play Billing environment.

Add CI gates for:
1. `testDebugUnitTest`
2. `lint` (or `lintDebug`)
3. `assembleDebug`
4. `bundleRelease` using CI-safe configuration
5. Room migration tests
6. Firestore Emulator rules tests
7. Selected Compose UI / navigation / accessibility tests

The visible unit-test suite focuses mainly on domain calculations, registration validation and program scheduling. Add repository, ViewModel, database migration, sync, billing-state and reminder tests. Run TalkBack, large font, narrow-device and dark-theme checks on physical devices.

## Play Store policy and product readiness

- **Target API:** `targetSdk = 36` is aligned with Google's requirement for new mobile apps and updates starting 31 August 2026.
- **Data Safety:** complete the Play Console Data safety form accurately for Firebase Authentication, profile fields, habit records, cloud sync and any future analytics/ads. Keep it consistent with the privacy policy.
- **Privacy policy:** publish a public, accessible policy that describes data collected, purposes, storage, retention, deletion, security and support contact.
- **Account deletion:** provide both the in-app path and external request page described above.
- **Subscriptions:** clearly show localized price, billing period, auto-renewal terms, trial/introductory offer terms, cancellation and restore behavior before purchase.
- **Health/wellness content:** keep claims conservative, clearly distinguish general wellness from medical advice, and have fitness content reviewed by a qualified professional before public distribution.
- **Accessibility:** verify touch targets, content descriptions, contrast, keyboard/switch access, TalkBack order and font scaling.
- **Localization:** validate English copy and date/time behavior; consider Urdu only after the first language is polished and tested.

## Recommended launch sequence

1. **Close P0 gates:** account deletion, privacy policy/web deletion resource, Billing Library upgrade, trusted purchase verification (or disable paid access), deploy/test Firestore rules, and protect unsynced local changes.
2. **Stabilize data:** server-authoritative sync timestamps, conflict policy, visible sync status, Room schema snapshots and migration tests.
3. **Harden the release:** launcher icon, release AAB/signing, lint + release CI, emulator rules tests, UI/accessibility/device matrix.
4. **Internal testing:** install from a Play internal testing track on multiple Android versions and at least two accounts/devices; test fresh install, upgrade, offline, reconnect, account switch, restore purchases, notification denial and data deletion.
5. **Closed testing / staged rollout:** monitor crash-free sessions, sync failures, sign-in failures, billing errors, uninstall feedback and support requests before broad rollout.

## Audit limitations

- No physical device or emulator was available for this source audit.
- Production Firebase project settings, deployed Firestore rules, OAuth fingerprints and account data were not inspected.
- Play Console product configuration, developer account status, signing setup, Data Safety answers and store listing were not inspected.
- The source-level CI pass only confirms the workflow's debug unit tests and debug assembly for the merged loading-state branch; it is not a production release sign-off.
