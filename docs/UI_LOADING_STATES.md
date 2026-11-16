# UI Loading States

Vitue uses Room as the local source of truth, so the UI waits for the first local database snapshot rather than waiting for a network request.

## Startup

- `AppNavigation` shows the branded Vitue splash while Firebase session state is being resolved.
- Once a signed-in session opens the app, `App` keeps the same splash over the app until `HomeViewModel` receives its first Room-backed habit emission.
- Home remains composed underneath the splash so its Room Flow can start and finish loading.
- The first emission is authoritative even when it is an empty list. Only then may Home show its empty state.
- Firestore synchronization continues in the background; startup does not require a network connection.

## Screen-level loading

- Home shows a small loader in the daily summary/habit section when the selected date changes. The app shell, date controls, and other screens remain visible.
- History shows a loader below its date controls until the selected day's history is available.
- Progress uses the same History ViewModel loading state and keeps its heading visible.
- Programs shows a loader only in the saved-program section while the local program cache is first read. The program catalogue remains usable.
- Opening a saved program while its data is still loading shows a loader in that content area.
- Static Profile content does not need a loading state. Authentication actions retain their existing button-level loading feedback.

## Manual verification

1. Launch with an account that already has saved habits. The splash should remain until the saved local snapshot is emitted; Home should then appear with correct counts, without an empty-state flash.
2. Launch with an account that has no habits. After the first Room emission, Home should show the genuine empty state.
3. Change the Home date and History date. Only the relevant data section should show its loader.
4. Open Programs. The catalogue should remain visible while saved programs load.
5. Repeat offline. Cached Room data should still render; no screen should wait for Firestore.

The startup gate intentionally waits for the first local Room snapshot, not for a remote Firestore round trip. This preserves offline-first startup and avoids making the entire app dependent on network availability.
