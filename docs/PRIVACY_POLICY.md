# Privacy Policy & Health Apps Disclosure — Virtue Habit Tracker

**Effective Date:** November 20, 2026  
**Last Updated:** November 20, 2026  
**Application Name:** Virtue Habit Tracker  
**Package Identifier:** `com.ytron.virtuhabit`  
**Developer / Data Controller:** Virtue Habit Tracker Team (`d9169839@gmail.com`)  
**Official HTTPS Privacy Policy URL:** [https://d9169839-oss.github.io/vitue-habit-tracking-app/privacy-policy.html](https://d9169839-oss.github.io/vitue-habit-tracking-app/privacy-policy.html)  
**Official HTTPS Account Deletion URL:** [https://d9169839-oss.github.io/vitue-habit-tracking-app/account-deletion.html](https://d9169839-oss.github.io/vitue-habit-tracking-app/account-deletion.html)

Virtue Habit Tracker ("Virtue", "we", "our", or "the App") is a habit-building, physical fitness, and behavioral wellness application for Android. This Privacy Policy and Health Policy Disclosure explains in detail what personal, behavioral, and health/fitness data we collect, how it is processed and stored, how it is protected, your legal rights under GDPR, CCPA/CPRA, and Google Play Developer Policies, and how you can permanently delete your account and all associated data at any time.

---

## 1. Summary of Our Privacy Commitments

- **100% Ad-Free & Zero Data Selling:** We do **not** embed third-party advertising SDKs, behavioral ad trackers, or data broker pixels. We never sell, rent, or trade your personal or health data.
- **On-Device Biometric Processing:** Biological sex, age, height, and weight entered during fitness or program personalization are stored strictly locally on your device (`SharedPreferences`) to calculate estimated Basal Metabolic Rate (BMR) and Total Daily Energy Expenditure (TDEE) using the Mifflin-St Jeor equation. These biometric attributes are **never** uploaded to Cloud Firestore or external servers.
- **Minimal Cloud Sync:** Only your habit definitions, daily check-in dates, and lightweight program progress checkpoints (`completedDays` and `skippedDays` indices) are synchronized over encrypted HTTPS/TLS to your private Google Cloud Firestore workspace so you can switch devices without losing progress.
- **Instant Self-Service Deletion:** You can permanently erase your entire account, cloud database records, and local device cache at any time via **Profile → Delete Account Permanently** inside the App, or via our [HTTPS Account Deletion Portal](https://d9169839-oss.github.io/vitue-habit-tracking-app/account-deletion.html).

---

## 2. Information We Collect & Google Play Data Safety Disclosure

In full compliance with Google Play's **Data Safety** requirements, the table below lists every category of data handled by `com.ytron.virtuhabit`:

| Data Category | Specific Data Elements | Where Stored | Purpose | Shared with Third Parties? |
| :--- | :--- | :--- | :--- | :--- |
| **Personal Info (Account Identity)** | Email address, Display Name, Firebase User ID (`UID`) | Device & Google Firebase Authentication / Cloud Firestore (`users/{uid}`) | Account creation, session authentication, and cross-device cloud backup isolation | **No** (Processed solely by Google Firebase as infrastructure sub-processor) |
| **Health & Fitness / Behavioral Wellness Progress** | Custom habit titles & notes, daily check-in completion status, enrolled program IDs, schedule preferences, and completed/skipped day numbers | Local Room SQLite Database & Google Cloud Firestore (`users/{uid}/habits`, `habitCheckIns`, `programEnrollments`) | Core app functionality: tracking daily habit streaks, rendering consistency charts, and restoring program progress across logins | **No** |
| **On-Device Biometric & Intake Calibration** | Biological sex, age, height (cm), weight (kg), domain intake focus, and daily time budget | **Local Device Only** (`SharedPreferences` in private app sandbox) | Computing on-device BMR/TDEE calorie estimates, training volume, and personalized 4-step program blueprints | **Never leaves your device** |
| **Device Permissions** | `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `INTERNET` | Local Android OS | Scheduling optional local daily habit reminder notifications via `WorkManager` and syncing with Firebase | **No** |

### Data We Do NOT Collect
Virtue Habit Tracker does **not** collect, access, or request:
- Precise or coarse GPS location (`ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION`)
- Contacts, call logs, SMS, microphone, or camera/photos
- Health Connect / Android sensor background telemetry (heart rate, blood pressure, medical records)
- Advertising ID (`AAID`) or cross-app tracking identifiers
- Payment card or financial billing details (all 12 guided programs are 100% free)

---

## 3. Google Play Health Policy & Medical / Clinical Disclaimer

Virtue Habit Tracker provides structured self-improvement curricula across **Fitness & Metabolic Conditioning**, **CBT-Inspired Mindfulness & Emotional Regulation**, **Circadian Sleep Hygiene (CBT-I principles)**, and **Cognitive Focus & Dopamine Reset**.

### a. Not a Medical Device or Healthcare Provider
Virtue Habit Tracker is a general wellness, fitness, and behavioral habit-tracking application. **It is NOT a medical device, clinical diagnostic tool, psychotherapy service, or healthcare provider**, and has not been evaluated by the U.S. Food and Drug Administration (FDA) or any international medical regulatory authority.

### b. No Medical, Nutritional, or Psychiatric Advice
All content in the App—including metabolic calculators (Mifflin-St Jeor BMR/TDEE equations, caloric deficit/surplus projections, macronutrient guidelines), physical training workloads, and psychology/CBT-informed behavioral exercises (such as box breathing, cognitive reframing, stimulus control, and somatic grounding)—is provided strictly for **general educational, informational, and self-help wellness purposes**. Nothing in the App constitutes:
1. Medical diagnosis, physical therapy, or clinical treatment of any disease or physical condition; or
2. Licensed psychotherapy, psychiatric care, or crisis intervention for clinical depression, anxiety disorders, ADHD, insomnia, or other mental health conditions.

### c. Consult a Qualified Professional & Emergency Notice
- Always consult a qualified physician or certified healthcare provider before starting any new exercise program, caloric modification, or physical training protocol, especially if you have pre-existing cardiovascular, orthopedic, or metabolic conditions.
- If you are experiencing severe psychological distress, clinical mental health symptoms, or thoughts of self-harm, stop using the App immediately and contact your local emergency services (e.g., **911** or **988** Suicide & Crisis Lifeline in the US/Canada, **112** in Europe, or your nearest emergency department).

### d. Protection of Health & Wellness Data
We treat your habit and program progress data with the highest level of confidentiality:
- Health, fitness, and behavioral program progress is **never** used for advertising, marketing profiling, creditworthiness, or insurance underwriting.
- All cloud-synced habit and program progress records are logically isolated per user via strict cryptographic Firebase Authentication UID rules (`request.auth != null && request.auth.uid == userId`).

---

## 4. How We Use & Process Your Data

We process your data solely to deliver the features you explicitly initiate:
1. **Authentication & Security:** Verifying your identity via Email/Password or Google Sign-In (`androidx.credentials.CredentialManager` and Firebase Authentication).
2. **Offline-First Habit & Program Tracking:** Storing your daily habits, check-ins, and 28–56 day curriculum schedules in a local Room SQLite database so the App works seamlessly offline.
3. **Lightweight Cloud Backup & Multi-Device Sync:** Syncing your habits, check-in records, and compact program progress checkpoints (`completedDays` and `skippedDays`) to Google Cloud Firestore so your progress is automatically restored when you sign in on a new device.
4. **Local Smart Reminders:** Running a local Android `WorkManager` task on your device to display a daily reminder notification at your chosen hour (only if you grant notification permission).

---

## 5. Third-Party Service Providers (Sub-Processors)

We rely exclusively on Google LLC's enterprise cloud infrastructure to authenticate users and synchronize data:
- **Google Firebase Authentication:** Manages secure sign-in, password hashing, password reset emails, and OAuth tokens ([Firebase Privacy & Security](https://firebase.google.com/support/privacy)).
- **Google Cloud Firestore:** Stores your encrypted per-user habit documents and program progress checkpoints ([Google Cloud Privacy Notice](https://cloud.google.com/terms/cloud-privacy-notice)).
- **Google Play Services (Credential Manager):** Facilitates one-tap "Continue with Google" authentication on Android devices ([Google Privacy Policy](https://policies.google.com/privacy)).

These service providers act strictly as data processors under binding data protection agreements and are prohibited from using your personal data for independent purposes.

---

## 6. Data Encryption, Security & Retention

- **Encryption in Transit:** 100% of network traffic between the App and Google Firebase endpoints is encrypted using industry-standard **HTTPS / TLS 1.3**.
- **Encryption at Rest:** All data stored in Google Cloud Firestore is automatically encrypted at rest using **AES-256** server-side encryption. Local data on modern Android devices is stored within the app's private internal storage directory (`/data/data/com.ytron.virtuhabit/`) protected by Android File-Based Encryption (FBE).
- **Strict Access Controls:** Cloud Firestore Security Rules enforce that a user can only read, write, or delete documents located under their own authenticated path (`/users/{userId}/**`).
- **Data Retention:** We retain your account and synced habit/program data only for as long as your account remains active. When you delete your account, all cloud and local records are permanently erased immediately.

---

## 7. Account & Data Deletion (In-App and Web HTTPS)

In compliance with Google Play's **User Data & Account Deletion Policy**, you can permanently delete your account and all associated data at any time through two convenient methods:

### Method A: Instant In-App Deletion (Immediate Execution)
1. Open **Virtue Habit Tracker** and sign in to your account.
2. Tap the **Profile** tab in the bottom navigation bar.
3. Scroll down and tap **Delete Account Permanently**.
4. Confirm the prompt. The App will immediately:
   - Delete all of your subcollections (`habits`, `habitCheckIns`, `programEnrollments`, `programActivities`) and user profile document (`users/{uid}`) from Google Cloud Firestore;
   - Delete your account credentials from Firebase Authentication; and
   - Wipe all local Room SQLite database tables and `SharedPreferences` on your device.

### Method B: Web / Email Account Deletion Request (Without the App Installed)
If you have already uninstalled the App or cannot access your device:
1. Visit our HTTPS Account Deletion page at: [https://d9169839-oss.github.io/vitue-habit-tracking-app/account-deletion.html](https://d9169839-oss.github.io/vitue-habit-tracking-app/account-deletion.html) (or view [ACCOUNT_DELETION.md](https://github.com/d9169839-oss/vitue-habit-tracking-app/blob/main/docs/ACCOUNT_DELETION.md)).
2. Send an email to **`d9169839@gmail.com`** with the subject line `"Account Deletion Request — Virtue Habit Tracker"` from the email address registered with your account.
3. We will permanently delete your Firebase Authentication account and all Cloud Firestore records within **7 business days** and send you a final confirmation email.

---

## 8. Children's Privacy (COPPA & GDPR-K)

Virtue Habit Tracker is intended for general audiences aged **13 and older** (or **16 and older** in the European Economic Area / UK where applicable). We do not knowingly collect personal information from children under the age of 13. If a parent or guardian believes that a child under 13 has created an account, please contact us at **`d9169839@gmail.com`** and we will immediately purge the account and all associated data.

---

## 9. Your Legal Privacy Rights (GDPR, UK GDPR, CCPA/CPRA, LGPD)

Depending on your jurisdiction, you have the following statutory rights regarding your personal data:
- **Right of Access & Portability:** You can inspect all of your habits, check-in history, and active programs directly within the App or request a copy of your cloud records by emailing us.
- **Right to Rectification:** You can edit, rename, archive, or update your habits and program calibration settings at any time inside the App.
- **Right to Erasure ("Right to be Forgotten"):** You can permanently delete your account and all data immediately in-app or via email request.
- **Right to Withdraw Consent:** You can disable daily reminder notifications at any time via **Profile → Daily Smart Reminders** or Android OS settings, and you can sign out or delete your account whenever you choose.
- **Non-Discrimination & Opt-Out of Sale:** We do not sell or share personal information as defined by the California Consumer Privacy Act (CCPA/CPRA).

---

## 10. Changes to This Policy & Contact Us

We may update this Privacy Policy and Health Policy Disclosure periodically to reflect new app capabilities or regulatory requirements. Any updates will be published at our official HTTPS URL with a revised "Last Updated" date.

If you have any questions, privacy inquiries, or data requests, please contact us at:
- **Email:** [d9169839@gmail.com](mailto:d9169839@gmail.com)
- **GitHub Repository:** [https://github.com/d9169839-oss/vitue-habit-tracking-app](https://github.com/d9169839-oss/vitue-habit-tracking-app)
- **Web Policy Portal:** [https://d9169839-oss.github.io/vitue-habit-tracking-app/privacy-policy.html](https://d9169839-oss.github.io/vitue-habit-tracking-app/privacy-policy.html)
