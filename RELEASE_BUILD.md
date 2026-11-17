# Building a signed Play Store release

The app uses an Android App Bundle (AAB) and Google Play App Signing. Never commit the upload keystore or its passwords.

## 1. Create an upload key (only if you do not already have one)

Run this in a terminal on a secure computer:

```bash
keytool -genkeypair -v \
  -keystore vitue-upload-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias vitue-upload
```

Keep the generated `vitue-upload-key.jks` outside the repository and store a secure backup. If you already enrolled this package in Play App Signing, use the existing upload key rather than generating a replacement.

## 2. Configure Gradle properties

Add these values to your user-level Gradle properties file (normally `~/.gradle/gradle.properties`). Do not add them to the repository's tracked `gradle.properties`.

```properties
VITUE_UPLOAD_STORE_FILE=/absolute/path/to/vitue-upload-key.jks
VITUE_UPLOAD_STORE_PASSWORD=your-keystore-password
VITUE_UPLOAD_KEY_ALIAS=vitue-upload
VITUE_UPLOAD_KEY_PASSWORD=your-key-password
```

The build reads these values only when creating the release signing configuration. If they are absent, CI can still compile an unsigned release bundle, but that artifact is not ready to upload to Play Console.

## 3. Build the release bundle

From the repository root:

```bash
./gradlew clean bundleRelease
```

Expected output:

`app/build/outputs/bundle/release/app-release.aab`

Upload the signed AAB to the Play Console internal testing track first. Complete Play App Signing enrollment when prompted and keep the upload key safe for future updates.

## 4. Before uploading

- Confirm the package name is `com.virtue.habittracker`.
- Confirm `versionCode` is greater than the last version uploaded to Play Console.
- Verify the Firebase SHA-1/SHA-256 fingerprints include the upload/app-signing certificates required by your Firebase sign-in setup.
- Test the AAB installed from Play internal testing, not only a locally installed debug APK.
- Keep the Play Console app-signing certificate and the upload certificate distinct; Firebase may need both fingerprints.
