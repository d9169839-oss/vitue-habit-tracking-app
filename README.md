# Vitue Habit Tracker

Native Android habit tracker built with Kotlin, Jetpack Compose, MVVM, Clean Architecture, Hilt, Firebase Authentication, and Room.

## Implemented in this scaffold
- Email/password registration and sign-in with domain validation
- Google ID-token sign-in using Android Credential Manager and Firebase
- Password reset and sign-out
- Restored Firebase session routing when the app launches
- Room-backed habit creation and daily check-ins
- Calendar/date navigation for historical habit review
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
This first scaffold uses local Room storage for habits and check-ins. Cloud sync/backup, dedicated history tab, richer streak analytics, edit/delete flows, and UI polish remain follow-up milestones. The project has not been built on a local Android SDK in this environment; run Gradle sync and tests locally and share any errors for correction.
