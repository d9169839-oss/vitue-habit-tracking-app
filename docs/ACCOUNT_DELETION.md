# Account & Data Deletion Policy — Virtue Habit Tracker

**Effective Date:** November 20, 2026  
**Application Name:** Virtue Habit Tracker  
**Package Identifier:** `com.ytron.virtuhabit`  
**Support & Data Protection Contact:** [d9169839@gmail.com](mailto:d9169839@gmail.com)  
**Official HTTPS Deletion Page:** [https://d9169839-oss.github.io/vitue-habit-tracking-app/account-deletion.html](https://d9169839-oss.github.io/vitue-habit-tracking-app/account-deletion.html)

In compliance with Google Play's **User Data and Account Deletion Policy**, users of **Virtue Habit Tracker** (`com.ytron.virtuhabit`) can permanently delete their account and all associated personal, habit, and program data at any time using either of the two options below.

---

## Option 1: Instant In-App Account Deletion (Recommended — Immediate)

You can permanently erase your account and all cloud/local data directly inside the Android app in seconds:

1. Open **Virtue Habit Tracker** on your Android device and ensure you are signed in.
2. Tap the **Profile** tab (bottom-right navigation bar icon).
3. Scroll to the bottom of the screen and tap **Delete Account Permanently**.
4. Tap **Delete Permanently** in the confirmation dialog.

> **Security Note:** If your current sign-in session is old, Firebase Authentication may require you to sign out and sign back in once to verify account ownership before executing permanent deletion.

---

## Option 2: Web / Email Deletion Request (If You Uninstalled the App)

If you no longer have the app installed on your device, you can request full account and cloud data deletion via email:

1. Send an email to **[d9169839@gmail.com](mailto:d9169839@gmail.com?subject=Account%20Deletion%20Request%20%E2%80%94%20Virtue%20Habit%20Tracker)** from the email address associated with your Virtue Habit Tracker account.
2. Include the subject line: `Account Deletion Request — Virtue Habit Tracker`
3. State your registered account email address in the message body.

**Processing Time:** All email deletion requests are processed within **7 business days**, and a confirmation email is sent once your account and cloud records have been purged (at which point your email address is also deleted).

---

## Complete List of Data Deleted & Retention Policy

When your account is deleted (whether in-app or via web/email request), **100% of your data is permanently and irreversibly erased with zero post-deletion retention period**:

| Data Source | Records Permanently Deleted | Retention Period After Deletion |
| :--- | :--- | :--- |
| **Firebase Authentication** | Email address, Display Name, User UID, OAuth/password credentials | **0 days** (Immediate permanent erasure) |
| **Cloud Firestore (`users/{uid}`)** | Root user profile document | **0 days** (Immediate permanent erasure) |
| **Cloud Firestore (`users/{uid}/habits`)** | All custom and starter habits, descriptions, and archive states | **0 days** (Immediate permanent erasure) |
| **Cloud Firestore (`users/{uid}/habitCheckIns`)** | All historical daily habit check-in timestamps and streak history | **0 days** (Immediate permanent erasure) |
| **Cloud Firestore (`users/{uid}/programEnrollments`)** | All active/completed program enrollments and progress checkpoints (`completedDays`, `skippedDays`) | **0 days** (Immediate permanent erasure) |
| **Local Device Storage (Room & SharedPreferences)** | All cached SQLite tables, on-device biometric calibration inputs, and notification settings | **0 days** (Wiped immediately on in-app deletion or app uninstall) |

---

## Related Policy Links

- **Full Privacy & Health Policy (HTTPS):** [https://d9169839-oss.github.io/vitue-habit-tracking-app/privacy-policy.html](https://d9169839-oss.github.io/vitue-habit-tracking-app/privacy-policy.html)
- **Markdown Privacy Policy:** [PRIVACY_POLICY.md](PRIVACY_POLICY.md)
