# Local setup

## Requirements
- Android Studio with JDK 17 (Android Studio's embedded JBR is fine).
- Android SDK Platform 36 and a recent emulator or Android device.
- A Firebase project.

## Firebase setup
1. Create a project in the Firebase console.
2. Add an Android app with package name `com.virtue.habittracker`.
3. Download `google-services.json` into the `app/` directory. This file is ignored by Git and must not be committed.
4. Enable **Email/Password** in Firebase Authentication.
5. Enable **Google** in Firebase Authentication and configure your app SHA-1/SHA-256 fingerprints.
6. Set the Web OAuth client ID in `GOOGLE_WEB_CLIENT_ID` in `app/build.gradle.kts`; the placeholder intentionally requires replacement.
7. Confirm the OAuth consent screen and package/SHA fingerprints are correct.

## Build
Open the repository root in Android Studio, let Gradle sync, install SDK Platform 36 if prompted, and run the `app` configuration.

## Important
Firebase credentials and signing keys are not committed. The scaffold has not yet been built against an Android SDK in this environment; perform a Gradle sync/build locally and share any errors for correction.
