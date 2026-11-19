# Account & Data Deletion — Virtue Habit Tracker

**Application:** Virtue Habit Tracker (`com.ytron.virtuhabit`)

Users of **Virtue Habit Tracker** can permanently delete their account and all associated personal data at any time using either of the methods below.

---

## Option 1: Instant In-App Account Deletion (Recommended)

1. Open **Virtue Habit Tracker** on your Android device.
2. Go to the **Profile** tab (bottom-right navigation item).
3. Scroll to the bottom and tap **Delete Account Permanently**.
4. Confirm the deletion dialog.

> **Note:** For security, if you have been signed in for a long period, Firebase Authentication may ask you to sign out and sign back in once before completing permanent deletion.

---

## Option 2: Request Deletion Without the App

If you no longer have access to the Android app, you can request permanent deletion of your account and cloud data by emailing the developer support contact listed on our Google Play Store page with:
- **Subject:** `Account Deletion Request — Virtue Habit Tracker`
- **Body:** The email address associated with your Virtue Habit Tracker account.

Requests submitted via email are processed within **7 business days**.

---

## What Data Is Deleted?

When your account is deleted (in-app or via web request), the following data is **permanently and irreversibly deleted** with no retention period:
- Your Firebase Authentication account (email, display name, UID, credentials).
- Your user profile document (`users/{uid}`).
- All habits (`users/{uid}/habits`).
- All habit check-in history (`users/{uid}/habitCheckIns`).
- All program enrollments and daily program activities (`users/{uid}/programEnrollments` and `users/{uid}/programActivities`).
- All local Room SQLite tables and cached preferences on your device.
