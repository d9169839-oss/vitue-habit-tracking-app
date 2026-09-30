# Local setup

## Requirements
- Android Studio with JDK 17 (Android Studio's embedded JBR is fine).
- Android SDK Platform 36 and a recent emulator or Android device.
- A Firebase project.

## Firebase setup
1. Create a project in the Firebase console.
2. Register an Android app with package name `com.virtue.habittracker`.
3. Download `google-services.json` into the `app/` directory. This file is ignored by Git and must not be committed.
4. In Firebase Authentication, enable **Email/Password**.
5. Enable **Google** in Firebase Authentication.
6. Create a Cloud Firestore database in the Firebase console.
7. Copy the repository's `firestore.rules` contents into Firestore → Rules and publish them. The rules file is not deployed automatically.
8. Configure your Android app's SHA-1 and SHA-256 fingerprints in Firebase project settings.
9. In `app/build.gradle.kts`, replace the `GOOGLE_WEB_CLIENT_ID` placeholder with your Web OAuth client ID, keeping the Kotlin/Gradle string quoting, e.g. `buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"123-your-client-id.apps.googleusercontent.com\"")`.
10. Confirm the OAuth consent screen, Android package name, signing fingerprints, and Web client ID are configured consistently.

## Open and run
1. Download or clone this repository.
2. Open the repository root in Android Studio.
3. Allow Gradle sync to finish and install SDK Platform 36 if prompted.
4. Add `app/google-services.json`, set the OAuth client ID, and publish the Firestore security rules before testing cloud sync.
5. Run the `app` configuration on an emulator/device with internet access.

## Important
- Never commit Firebase service-account credentials, signing keys, local SDK paths, or private configuration.
- Google sign-in cannot succeed while the placeholder Web client ID remains.
- This scaffold has not yet been built against an Android SDK in this environment. If Gradle sync or compilation reports errors, copy the first error and stack trace so it can be fixed directly.
