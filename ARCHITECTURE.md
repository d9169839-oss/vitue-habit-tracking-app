# Architecture

This follows the dependency direction and encapsulation conventions covered in the companion [enterprise architecture learning repository](https://github.com/d9169839-oss/android-enterprise_architecture).

```
Compose UI -> ViewModel -> Domain use cases -> Repository interfaces
                                                  ^                  ^
                                                  |                  |
                                  RoomHabitRepository          FirebaseAuthRepository
                                          |                           |
                                   Room + Firestore             Firebase Auth
```

- **Presentation:** Compose screens, immutable UI state, ViewModel, navigation.
- **Domain:** User model, repository contract, validation and use cases; no Android/Firebase types.
- **Data:** Room is the local-first source for habit screens; Firestore mirrors per-user profiles, habits, and dated check-ins. Firebase Authentication owns credentials.
- **DI:** Hilt binds the repository interface to Firebase implementation and provides FirebaseAuth.
- MutableStateFlow remains private; UI collects read-only StateFlow.
- SharedFlow represents one-time effects such as successful authentication.
- ViewModels orchestrate use cases; they do not call Firebase directly.
- Passwords are never logged or persisted by this app.

Authentication includes name/email/password registration, Google ID-token sign-in via Credential Manager, password reset, and sign-out. Habit creation, daily check-ins, selected-date history, derived day counts, archiving, and streak calculation are implemented. See `DATA_MODEL.md` for the current local-first sync policy and limitations.
