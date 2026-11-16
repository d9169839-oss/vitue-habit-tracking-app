# UI Loading States

Vitue uses Room as the local source of truth, so the UI waits for the first local database snapshot rather than waiting for a network request.

## Startup

- `AppNavigation` shows the branded Vitue splash while Firebase session state is being resolved.
- Once a signed-in session opens the app, `App` keeps the same splash over the app until the initial habit and check-in snapshots have been merged into Room and Home receives its first Room-backed emission.
- When online, startup waits for the initial server snapshots so an empty/partial local cache cannot flash before the user's cloud records arrive.
- When offline, the Firestore cached snapshot is used when available. A bounded 12-second fallback prevents a stalled listener/network from trapping the user on the splash.
- Home remains composed underneath the splash so its Room Flow can start and finish loading.
- The first Room emission is authoritative even when it is an empty list. Only then may Home show its empty state.
- WorkManager's initial full pull can also complete the hydration barrier; later uploads and periodic sync remain background work.

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

The startup gate uses Room as the rendering source of truth, but on a connected device it waits for initial cloud hydration before allowing the first Home snapshot through. Offline startup uses the best available local cache and remains bounded; normal habit actions still write to Room first.
