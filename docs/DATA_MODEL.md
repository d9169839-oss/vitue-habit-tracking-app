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

Firestore document: `users/{uid}/habitCheckIns/{habitId}_{epochDay}`.

Epoch days use `LocalDate.toEpochDay()`, avoiding timezone-dependent date keys. Convert to and
from `LocalDate` at the UI boundary.

## 4. Calendar/history summaries

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

Room is the fast UI source. User actions write to Room first, then attempt a Firestore write.
When a day stream is first observed, the app pulls cloud records that are missing locally and
retries uploading local records. This is a starter sync strategy, not a complete multi-device
conflict-resolution engine: when two devices edit the same record concurrently, local-first
merge semantics may retain stale values. Before production, add a durable sync outbox, explicit
sync status, version/updated-at conflict rules, account-switch isolation tests, and pagination for
large histories.

## 6. Security

Deploy `firestore.rules` in the Firebase console (Firestore Database → Rules). Rules scope every
document to the authenticated UID. The rules file in the repository does not deploy itself.
