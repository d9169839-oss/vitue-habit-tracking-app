# Habit business data model

This document explains the source of truth, local persistence, and Firestore layout.

## 1. User profile

`UserProfile` contains the Firebase UID, display name, email, optional photo URL, and timestamps.
The UID is the stable identity key. Do **not** store a password, password hash, or ID token in
Room or Firestore; Firebase Authentication owns credentials and session tokens.

Firestore document: `users/{uid}`.

## 2. Habit definition

Each habit has a stable random ID, title, description, creation day, and optional
`inactiveFromEpochDay`. The inactive day is the **first day the habit is no longer active**.
Archive a habit instead of deleting it, because historical calendar pages still need its identity.

Firestore document: `users/{uid}/habits/{habitId}`.

## 3. Daily check-in

A check-in records whether one habit was completed on one calendar day. Its logical key is
`(habitId, epochDay)`; Firestore uses the deterministic document ID
`{habitId}_{epochDay}`. The value `completed = false` means explicitly marked “not completed”.
A missing check-in means “unrecorded”, which is different from an explicit failure.

Firestore document: `users/{uid}/habitCheckIns/{habitId}_{epochDay}`. A cleared check-in remains as a tombstone with `deleted = true` and `updatedAtMillis`; clients remove it from Room when they pull the tombstone.

Epoch days use `LocalDate.toEpochDay()`, avoiding timezone-dependent date keys. Convert to and
from `LocalDate` at the UI boundary.

## 4. Calendar/history summaries

The History tab is a date-specific snapshot. It lists every habit created on or before the selected date, including archived habits. A habit is visually dimmed when the selected date is on or after its `inactiveFromEpochDay`; choosing a date before its archive boundary correctly shows it as active then. Status counts and completion rate use only habits active on the selected date, while the total and inactive counts provide context. The All/Active/Inactive filters only change presentation, not the stored data.

The active habit list for a date is derived from the habit creation/archive boundaries. Counts are
calculated from that list and its check-ins:

- active = all habits active on the selected date;
- completed = active habits with a completed check-in;
- not completed = active habits with an explicit false check-in;
- unrecorded = active minus completed minus not-completed;
- streak = consecutive completed dates ending on the selected date.

A missing/unrecorded date breaks the streak. This is deliberate: a streak means completion on
every consecutive calendar day. `HabitDaySummary` is a derived domain read model; it is not
persisted as authoritative data. If history becomes large, a daily summary can be cached later,
but it must be rebuildable from habits and check-ins.

## 5. Local-first persistence and sync

Room is the immediate source of truth. A habit action is committed locally together with a durable `sync_operations` outbox row in the same Room transaction; the UI observes Room and does not wait for Firestore. The queue uses a stable key per habit or habit/day, so rapid edits coalesce to the latest pending operation. A queued row is removed only after the corresponding cloud batch succeeds and only if that queue revision has not changed while uploading.

WorkManager runs only when Android reports network connectivity. It uploads queued records in batches of at most 350 writes, retries failures with exponential backoff, and retains pending work across app closure/process death. A six-hour periodic job provides a safety sync. Profile metadata sync is deferred to background work and does not block authentication.

The first pull imports the user's cloud history. Later pulls query `updatedAtMillis` changes since the account's last successful pull cursor; pulls are throttled to at most once every 15 minutes, reducing empty query reads after frequent local edits. Local check-in deletions are represented by Firestore tombstones (`deleted = true`) so other devices can receive deletions rather than accidentally resurrecting a cleared record. The cache owner is checked before local data is exposed; switching Firebase UID clears the previous account's local habits, check-ins, and outbox.

This is a durable local-first sync foundation, but it is not yet a full multi-device conflict-resolution engine. Concurrent edits to the same habit/day still need an explicit policy (for example, server version/updated-at conflict resolution), and large histories need pagination. `updatedAtMillis` relies on device clocks, so clock skew is another limitation to address before production.

## 6. Security

Deploy `firestore.rules` in the Firebase console (Firestore Database → Rules). Rules scope every
document to the authenticated UID. The rules file in the repository does not deploy itself.


## 5. Programs

Room database version 4 adds `program_enrollments` and `program_activities` (version 3) and a `workActivityLevel` preference (version 4). A program enrollment stores a snapshot of the chosen template version, title, duration, start epoch day, status, and personalization settings. Each scheduled day is stored as an activity row with a stable ID, day index, date, phase, instructions, estimated time, status, and update timestamp.

Program mutations and corresponding `PROGRAM_ENROLLMENT` / `PROGRAM_ACTIVITY` outbox operations are committed in the same Room transaction. The existing WorkManager worker uploads the outbox to:

- `users/{uid}/programEnrollments/{enrollmentId}`
- `users/{uid}/programActivities/{activityId}`

Cloud deletion is represented by a `deleted = true` tombstone. Pulling cloud data skips local entities with pending writes and ignores activity documents whose enrollment is absent. Account-owner changes clear the previous account's local program tables together with the habit cache.

Catalog templates are bundled and versioned. The active enrollment retains the title, template version, schedule, and instructions so later catalog edits do not silently rewrite an existing plan. Optional adult BMI can be calculated on-device in the personalization dialog; height/weight values are not stored in Room or uploaded. BMI is informational only and does not determine exercise intensity or calorie targets.

Before multi-device production, test clock skew and concurrent edits. The existing sync cursor is based on client-side `updatedAtMillis`, so it is not a conflict-free synchronization protocol. Firestore security rules must validate the new subcollections and enforce that a signed-in user can only access their own UID path.
