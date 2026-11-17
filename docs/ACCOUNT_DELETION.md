# Vitue Habit Tracker — Account deletion

## What the feature does

The Profile screen contains a permanent **Delete account** action. The user must review an irreversible-action warning and type `DELETE` before the app sends the request.

The Android client does not attempt to recursively delete Firestore data itself. It calls the trusted Firebase callable function `deleteMyAccount`, which:

1. Requires a valid Firebase Authentication session.
2. Requires a real sign-in within the previous five minutes (`auth_time`; refreshing an old ID token is not sufficient).
3. Requires a valid Firebase App Check token.
4. Takes the UID only from the verified callable request context. A client cannot submit a different UID.
5. Uses the Admin SDK `recursiveDelete` on `users/{uid}`, deleting that document and all nested collections/documents.
6. Deletes the Firebase Authentication user only after the Firestore purge succeeds. If Auth deletion fails, the still-existing account can retry the idempotent purge.

The current app's cloud user data is stored under `users/{uid}`: `habits`, `habitCheckIns`, `programEnrollments`, `programActivities`, and `dailySummaries`. Recursive deletion covers current and future nested collections under that user document.

After the callable succeeds, the Android client signs out, clears UID-specific sync metadata, cancels WorkManager jobs, and clears every Room table (including the durable sync outbox). Firestore disk persistence is disabled because Room is the app's offline source of truth; this avoids retaining a persistent Firestore disk cache on the device after account deletion.

## Important platform limitation

Uninstalling an Android app does **not** run app code and cannot reliably notify Firebase. Android does not provide an uninstall callback for this purpose. Users must delete their account from **Profile → Delete account before uninstalling**. Users who cannot access the app can use the public web request page below.

## Required Firebase setup

### 1. Firebase plan and project

Cloud Functions deployment requires the Firebase project to be on the **Blaze** plan (a billing account). The function is small and invoked only when an account is deleted, but Firebase billing must be enabled before deployment.

### 2. Install and test Functions

From the repository root:

```bash
cd functions
npm install
npm test
npm run build
cd ..
firebase login
firebase use --add
firebase deploy --only firestore:rules
firebase deploy --only functions,hosting
```

Select the same Firebase project used by the Android app. Do not deploy to a different project from the one whose `google-services.json` is bundled in the Android build. Deploy Firestore rules first: the callable function relies on the deletion tombstone rule to prevent stale clients from recreating data. Enable the `accountDeletionTombstones.expireAt` TTL policy before allowing real account deletion requests.

### 3. Configure App Check

- In Firebase Console, open **App Check** and register the Android app using the production package name and Play Integrity.
- Add the Play signing SHA-256 certificate fingerprint for the Play-distributed app.
- The debug build uses the Firebase App Check debug provider. Run the debug app, copy the debug token from Logcat, and register that token under the Android app's App Check debug tokens in Firebase Console.
- Do not use a debug token in a production build.
- The callable function enforces App Check. If App Check is not configured correctly, deletion requests will be rejected rather than silently bypassing the protection.

### 4. Publish the external deletion-request page

Before deployment, replace `REPLACE_WITH_SUPPORT_EMAIL` in `public/delete-account.html` with the real monitored support email address.

After Firebase Hosting deployment, verify:

`https://<your-firebase-project-id>.web.app/delete-account.html`

Use this exact public URL in the Google Play Console account-deletion URL field. Verify it opens in a browser without requiring sign-in. The email address must be monitored and the process must actually complete deletion requests.

## Verification checklist

- [ ] Deploy the callable function to the same Firebase project as the Android app.
- [ ] Register App Check for Android and register the debug token for debug testing.
- [ ] Replace the support-email placeholder and deploy Firebase Hosting.
- [ ] Verify the public deletion URL works without authentication.
- [ ] Create a test account with records in habits, check-ins, program enrollments, and program activities.
- [ ] Sign in recently, delete from Profile, and verify the Firebase Authentication user no longer exists.
- [ ] Verify `users/{uid}` and every nested Firestore collection/document are gone.
- [ ] Verify the app returns to the signed-out state and Room tables/outbox are empty.
- [ ] Verify a stale session is rejected with a sign-out/sign-in instruction and does not delete data.
- [ ] Verify an offline request fails safely and does not clear local data.
- [ ] Test both email/password and Google-authenticated accounts.
- [ ] Test on a physical device using the Play-distributed build and production App Check configuration.

## Release gate / limitations

The temporary `accountDeletionTombstones/{uid}` record contains no profile or habit content. Enable a Firestore TTL policy for collection group `accountDeletionTombstones` using field `expireAt`. Do this in Firebase Console → Firestore Database → Time-to-live. The function sets expiry to 24 hours; TTL cleanup is asynchronous and may take additional time. Until this policy is enabled, the marker will remain and continue blocking stale sessions. The repository implementation is not a claim that production Firebase resources have been configured or deployed. The project owner must deploy Functions and Hosting, register App Check, provide the real support email, and run the verification checklist against the production Firebase project before Play Store submission.

No Firebase Storage deletion is included because the current repository does not upload user files to Firebase Storage. If user-owned Storage paths are introduced later, add their recursive deletion to the callable function before release.
