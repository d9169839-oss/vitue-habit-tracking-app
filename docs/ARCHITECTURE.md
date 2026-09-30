# Architecture

This follows the dependency direction and encapsulation conventions covered in the companion [enterprise architecture learning repository](https://github.com/d9169839-oss/android-enterprise_architecture).

```
Compose UI -> ViewModel -> Domain use cases -> Repository interfaces
                                                  ^                  ^
                                                  |                  |
                                  RoomHabitRepository          FirebaseAuthRepository
                                          |                           |
                           Room + sync outbox + Firestore        Firebase Auth
```

- **Presentation:** Compose screens, immutable UI state, ViewModel, navigation.
- **Domain:** User model, repository contract, validation and use cases; no Android/Firebase types.
- **Data:** Room is the immediate source of truth for habit screens. Each local mutation and a durable sync-outbox operation are committed in one Room transaction. Firestore mirrors per-user profiles, habits, and dated check-ins; Firebase Authentication owns credentials.
- **Background sync:** WorkManager requires network connectivity, uploads only queued entities in batches, retains failed operations for retry, and pulls changed cloud documents incrementally. Pulls are throttled to once every 15 minutes; a six-hour periodic job is a safety net.
- **Offline behavior:** Habit actions do not call Firestore and never wait for network access. A stable outbox key coalesces repeated edits to the same habit/day. Check-in deletions are represented as cloud tombstones so another device can observe them.
- **Account isolation:** The local cache is cleared when a different Firebase UID becomes the cache owner. Never upload one user's local cache under another UID.
- **DI:** Hilt binds the repository interface to Firebase implementation and provides FirebaseAuth.
- MutableStateFlow remains private; UI collects read-only StateFlow.
- SharedFlow represents one-time effects such as successful authentication.
- ViewModels orchestrate use cases; they do not call Firebase directly.
- Passwords are never logged or persisted by this app.

Authentication includes name/email/password registration, Google ID-token sign-in via Credential Manager, password reset, and sign-out. Habit creation, daily check-ins, selected-date history, derived day counts, archiving, and streak calculation are implemented. See `DATA_MODEL.md` for the current local-first sync policy and remaining multi-device conflict limitations.
