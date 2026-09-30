# Vitue Habit Tracker

Native Android habit tracker built with Kotlin, Jetpack Compose, MVVM, Clean Architecture, Hilt, Firebase Authentication, and Room.

## Implemented in this scaffold
- Email/password registration and sign-in with domain validation
- Google ID-token sign-in using Android Credential Manager and Firebase
- Password reset and sign-out
- Restored Firebase session routing when the app launches
- Room-backed habit creation and daily check-ins with local-first Firestore cloud backup
- User-scoped Firestore profile, habit, and check-in documents (passwords remain in Firebase Authentication)
- Retry pass that uploads local records after the device reconnects
- Derived daily history summaries for active/completed/not-completed/unrecorded counts
- Dedicated History tab with calendar-based date selection\n- Date-specific history includes habits created by that date, with archived/inactive habits dimmed\n- Summary counts for completed, not completed, unrecorded, active, and inactive habits\n- All/Active/Inactive history filters
- Three daily states: unrecorded, completed, and not completed
- Habit active/inactive lifecycle that preserves historical visibility after archiving
- Consecutive-day streak calculation as of the selected date
- Dark purple Compose theme
- Registration validation unit tests

## Configure before running
Follow [docs/SETUP.md](docs/SETUP.md). Add your own `app/google-services.json`, enable Email/Password and Google sign-in in Firebase, and replace the Google Web OAuth client ID placeholder in `app/build.gradle.kts`. These project-specific values are not committed.

## Architecture
See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and the companion [enterprise architecture learning repository](https://github.com/d9169839-oss/android-enterprise_architecture).

## Remaining work
The History tab is a date-specific snapshot: it shows habits created by the selected date and their check-in status for that day. Archived habits remain visible but dimmed when inactive on that date. The current Firestore sync is an initial best-effort implementation, not a durable multi-device conflict-resolution system. Richer analytics, habit editing, and production-grade sync remain follow-up milestones. The project has not been built on a local Android SDK in this environment; run Gradle sync and tests locally and share any errors for correction. The project has not been built on a local Android SDK in this environment; run Gradle sync and tests locally and share any errors for correction.
