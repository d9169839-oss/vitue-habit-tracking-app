# Vitue Habit Tracker

Native Android habit tracker built with Kotlin, Jetpack Compose, MVVM, Clean Architecture, Hilt, Firebase Authentication, and Room.

## Implemented
- Email/password registration and sign-in with domain validation
- Google ID-token sign-in using Android Credential Manager and Firebase
- Password reset and sign-out
- Restored Firebase session routing on app launch
- Room-backed habit creation and date-specific check-ins
- Historical date navigation with completed / not completed / unrecorded states
- Active habit lifecycle based on creation and inactive dates
- Dark purple premium-style Compose theme
- Unit tests for registration validation

## Setup before running
Follow [docs/SETUP.md](docs/SETUP.md). You must add your own `app/google-services.json`, enable Email/Password and Google providers in Firebase, and set `GOOGLE_WEB_CLIENT_ID` in `app/build.gradle.kts`. These private/project-specific files are intentionally not committed.

## Architecture
See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and the companion [enterprise architecture learning repository](https://github.com/d9169839-oss/android-enterprise_architecture).

## Current scope / next
Local Room persistence is in place for habits and check-ins. Calendar visualization, streak calculations, habit editing/archive UI, and cloud sync are follow-up work. This scaffold has not yet been built on a local Android SDK; run Gradle sync and tests on your machine and share any errors.
