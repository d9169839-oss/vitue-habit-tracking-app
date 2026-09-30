# Architecture

This follows the dependency direction and encapsulation conventions covered in the companion [enterprise architecture learning repository](https://github.com/d9169839-oss/android-enterprise_architecture).

```
Compose UI -> AuthViewModel -> Domain use cases -> AuthRepository interface
                                                    ^
                                                    |
                                       FirebaseAuthRepository -> Firebase SDK
```

- **Presentation:** Compose screens, immutable UI state, ViewModel, navigation.
- **Domain:** User model, repository contract, validation and use cases; no Android/Firebase types.
- **Data:** Firebase implementation and mapping to domain outcomes.
- **DI:** Hilt binds the repository interface to Firebase implementation and provides FirebaseAuth.
- MutableStateFlow remains private; UI collects read-only StateFlow.
- SharedFlow represents one-time effects such as successful authentication.
- ViewModels orchestrate use cases; they do not call Firebase directly.
- Passwords are never logged or persisted by this app.

Authentication scaffold includes email/password registration and sign-in, Google ID-token sign-in via Credential Manager, password reset, and sign-out. Habit creation, daily check-ins, date history/calendar, and streak logic are planned next.
