# Programs Feature — Product, Architecture, and Implementation Specification

## Status and scope

This document defines the production-oriented implementation for the Programs tab. It is the source of truth for the first release and should be updated as the product decisions are finalized.

## Implementation status (2026-09-30)

The first implementation has been committed in the repository:
- The Programs bottom tab replaces the old Progress tab; Home, History, and Profile remain.
- Bundled catalog: beginner movement, home strength, everyday stamina, sustainable wellness habits, grooming, skincare, mindfulness, and gratitude/reflection.
- Deterministic schedule generation creates a day-by-day snapshot, respecting the chosen time budget and number of available days per week.
- Room schema version 4 stores enrollments and scheduled activities. Migrations 2→3 add program tables and 3→4 add the daily-work activity preference without deleting existing habit data. The personalization screen asks about work activity, available time, days/week, experience, and equipment. Optional adult BMI is calculated on-device only; values are not saved or uploaded and do not set exercise intensity or calorie targets.
- Program enrollment and activity changes are enqueued transactionally and uploaded/pulled by the existing network-constrained WorkManager sync worker.
- Hilt-backed ViewModel/use cases manage catalog filtering, start/pause/resume, completion/skip, progress, and deletion.
- Google Play Billing client loads subscription products, starts checkout, and restores owned subscriptions.

This is an initial implementation, not a release certification. Android compilation/tests have not yet been verified. The product IDs `vitue_premium_monthly` and `vitue_premium_yearly` are placeholders. Before production, configure real Play Console product IDs, validate purchase tokens on a trusted backend, and derive durable entitlements from server-verified state. Fitness content is conservative starter content and still needs qualified review. Firestore rules must explicitly protect the new `programEnrollments` and `programActivities` subcollections.

The first release should combine **curated, versioned templates** with **deterministic personalization rules**. Do not ask an AI model to invent a complete health or spiritual plan without reviewed constraints. AI can be added later to explain or rephrase a reviewed plan, but it must not bypass safety rules.

## Product goals

- Help a user choose a structured 14-, 30-, 60-, or 90-day program.
- Personalize schedule, difficulty, available time, equipment, and selected goal.
- Let users start, pause, resume, or finish a plan.
- Show one clear “Today” checklist and a phase/milestone view.
- Persist progress locally immediately and synchronize it in the background.
- Support offline use and process death.
- Gate premium-only programs using a real entitlement source, not a hard-coded UI boolean.
- Keep program progress separate from ordinary habits, while optionally offering a user-controlled “add activity as a habit” integration.

## Information architecture

1. **Programs landing**
   - Hero: “Build a better routine, one day at a time.”
   - Category filters: All, Fitness, Self-Grooming, Mindfulness/Spiritual.
   - Featured, beginner-friendly, and short-time-commitment collections.
   - Cards show goal, duration, difficulty, minutes/day, equipment, and Free/Premium label.
   - Filters and categories must be accessible and scrollable.

2. **Program detail**
   - Purpose, intended audience, duration, time/day, equipment, difficulty, and phase outline.
   - “What you’ll do” list and realistic expectations.
   - Safety notes and exclusions for health-related programs.
   - Free/Premium entitlement and “Personalize plan” action.

3. **Personalization wizard**
   - Step 1: Goal, duration, experience.
   - Step 2: Daily time, available days, preferred start date, home/gym/outdoors, equipment.
   - Step 3: Optional relevant profile details. Ask height/weight only when relevant to an adult weight-management program. Ask age range before any BMI-based adult interpretation. Ask a short, respectful safety-screening question for exercise programs.
   - Step 4: Review generated schedule and make the plan editable before starting.
   - Show why each answer is needed. Offer “Prefer not to say” for optional data.

4. **Active plan**
   - Day X of N, phase, progress, next milestone, and today's activities.
   - Each activity has instructions, estimated minutes, completion status, and an optional substitute.
   - Users may skip or reschedule; a missed day must not silently mark the program failed.
   - Provide Pause, Resume, Adjust schedule, and End program actions.

5. **Progress**
   - Program completion percentage, activities completed/remaining, weekly consistency, and milestones.
   - Keep adherence separate from outcome measures such as body weight.
   - Optional check-in metrics must be explicit opt-in, private, and editable/deletable.
   - Program completion is derived from enrollment snapshot and activity completion records.

6. **Program history**
   - Active, paused, completed, and ended plans.
   - Preserve finished plan history; starting another plan must not overwrite it.

## Initial curated catalog

Start with approximately 10–12 reviewed templates rather than an unmaintainable large library.

### Fitness
- Beginner movement foundations — 30 days, free starter.
- Walking and cardio consistency — 60 days.
- Home strength foundations — 60 days.
- Gym consistency and progressive strength — 90 days.
- Sustainable weight-management habits — 90 days.
- General stamina foundations — 60 days.

### Self-grooming and personal development
- Daily hygiene and grooming routine — 30 days.
- Basic skincare consistency — 30 days.
- Hair-care routine — 30 days.
- Confidence and communication practice — 60 days.
- Personal organization and style — 30 days.

### Mindfulness and spiritual growth
- Gratitude and daily reflection — 30 days.
- Mindfulness foundations — 30 days.
- Digital balance — 14 or 30 days.
- Purpose, values, and journaling — 60 days.
- User-selected spiritual reflection — 30 or 90 days.

Each template needs reviewed content, an owner/reviewer, a version, intended audience, excluded audiences, source/review date for health content, and a clear statement of what it does not promise. Spiritual/religious practice must be selected by the user, not inferred from identity.

## Personalization rules

The engine should be a pure Kotlin domain service. Same inputs + same template version should produce the same schedule, making it testable and reproducible.

Inputs:
- template ID and version;
- duration;
- experience level;
- available minutes/day and available days/week;
- preferred start date;
- activity location and equipment;
- goal-specific optional details;
- explicit safety-screen result.

Rules:
- Never increase activity intensity solely because a user has more free time.
- For beginners, choose the template's beginner path and increase duration/complexity only according to reviewed progression rules.
- When time is limited, reduce the number or duration of optional activities; do not compress a 90-day progression into a shorter period.
- Match equipment to a reviewed alternative. If no safe alternative exists, ask the user to choose another activity.
- A 60/90-day plan should contain phases and recovery/easier days where appropriate; do not merely repeat identical generic text 90 times.
- Generate a **plan snapshot** at enrollment. Later catalog edits must not mutate a user's active plan.
- Replanning should create a new schedule revision with an audit timestamp; preserve completed activity records.
- If a safety-screen response disqualifies a template, do not start it. Explain the reason and show a safer non-clinical alternative where appropriate.

### BMI and health safeguards

For an adult only, BMI = weight in kilograms / (height in metres squared). The initial implementation calculates an optional adult BMI estimate locally in the personalization dialog; the values are not persisted or uploaded. Treat BMI as a screening value, not a diagnosis, body-composition measure, or standalone prescription rule. Do not display adult BMI categories to children/teens. Do not derive calorie targets or exercise intensity from BMI. Avoid asking for weight on grooming/mindfulness plans. Offer “skip” for optional measurements.

For pregnancy, known relevant medical conditions, injury, eating-disorder concerns, or other high-risk responses, do not prescribe generic weight-loss or intense exercise programs. Direct the user toward a qualified professional and only offer general, low-risk habit content where appropriate. Content should be reviewed by a qualified fitness/health professional before launch. Do not represent the app as a medical device or substitute for care.

## Domain model

Keep the domain independent from Android, Room, Compose, and Firebase.

Suggested types:
- `ProgramTemplate`: stable ID, version, title, category, description, duration, difficulty, minutes/day, premium requirement, equipment, phases, activities, safety metadata.
- `ProgramPhaseTemplate`: phase number, title, start/end day, goal, activity references.
- `ProgramActivityTemplate`: stable ID, title, instructions, type, estimated minutes, optional equipment, safe substitutions, completion rule.
- `ProgramEnrollment`: stable ID, user ID, template ID/version, selected start date, planned duration, status, personalized settings, immutable content snapshot, created/updated timestamps.
- `ScheduledProgramActivity`: enrollment ID, day index/date, activity snapshot, planned minutes, optional schedule revision.
- `ProgramActivityCompletion`: enrollment ID, scheduled activity ID, completion status, completed timestamp, optional note.
- `ProgramCheckIn`: optional, consent-based user metric. Keep metric type/value/unit/date and explicit consent state; never require body measurements to use the program.
- `ProgramStatus`: ACTIVE, PAUSED, COMPLETED, ENDED.
- `ActivityStatus`: PENDING, COMPLETED, SKIPPED, RESCHEDULED.

Prefer stable IDs and explicit enums over magic strings. Use epoch day for calendar dates and epoch milliseconds for event timestamps. Define whether the end date is inclusive and test it. Use an explicit time-zone policy for reminders.

## Clean Architecture and package layout

```
domain/
  model/program/       ProgramTemplate, Enrollment, Activity, Profile, enums
  repository/          ProgramRepository interface
  usecase/program/     ObserveCatalog, StartProgram, CompleteActivity,
                       PauseProgram, ResumeProgram, ReplanProgram
  service/             ProgramPersonalizationEngine, BmiCalculator

data/
  local/               Room entities, DAO, migrations, mappers
  repository/          Room-first ProgramRepository implementation
  catalog/             Bundled, versioned, reviewed seed catalog
  sync/                Outbox operations and WorkManager worker integration

presentation/programs/
  ProgramsViewModel.kt
  ProgramsUiState.kt
  ProgramsScreen.kt
  ProgramDetailScreen.kt
  ProgramPersonalizationScreen.kt
  ActiveProgramScreen.kt
  ProgramProgressScreen.kt
  components/
```

Dependency direction: Compose -> ViewModel -> use cases/domain services -> repository interfaces <- data implementation. UI must not query Room or Firestore directly. Keep `MutableStateFlow` private and expose `StateFlow`. Inject dependencies with Hilt. Use immutable UI state and one-time events for navigation/snackbars.

## Local-first persistence and sync

Room is the immediate source of truth. Completing an activity or changing enrollment status must commit the local record and a durable outbox operation in the same transaction. UI observes Room; it never waits for Firestore.

- Bundle the curated catalog with the app initially, so browse/detail screens work offline and don't require catalog reads.
- Keep catalog templates immutable/versioned. A catalog update ships through an app release initially; remote catalog delivery can be introduced later with signature/version/review controls.
- Store enrollments and activity completions in Room, scoped to the authenticated UID.
- Extend the existing sync outbox with explicit PROGRAM_ENROLLMENT and PROGRAM_COMPLETION operation types. Never encode these as ordinary habit records.
- Worker uploads only queued changes and deletes queue rows only after confirmed writes and only if the queued revision is unchanged.
- Pull cloud updates incrementally and skip entities with pending local writes. Define deletion tombstones and conflict resolution before multi-device release.
- On account switch, clear the previous user's local program data and queued operations before exposing data for the new UID.
- Avoid a Firestore listener for every activity. Batch related writes and coalesce repeated edits.
- The app should show local save immediately, plus a subtle pending-sync/synced state. Sync failure must not discard the user's progress.
- Keep health-related optional profile fields out of analytics. Minimize collection and provide deletion/export controls.

## Firestore layout proposal

```
users/{uid}/programEnrollments/{enrollmentId}
users/{uid}/programCompletions/{completionId}
users/{uid}/programCheckIns/{checkInId}     # only with explicit opt-in
```

Each document should include schemaVersion, updatedAtMillis, and a stable ID. Do not copy full immutable template text into every completion document; enrollment stores the versioned snapshot needed to render a historic plan. Use server-side validation/entitlement verification for premium content and sensitive actions. Firestore rules must enforce `request.auth.uid == uid` and validate allowed document paths/fields. Deploy and test rules in Firebase Emulator Suite; a rules file in GitHub does not deploy itself.

## Premium access and billing

Do not treat a local boolean or a Firestore field writable by the client as proof of payment. Use Google Play Billing for Play-distributed Android subscriptions and verify purchases/entitlements using a trusted backend. The app may cache entitlement state for offline UX, but it must refresh/verify when online and must not grant durable premium access based only on a client edit. Define grace period, cancellation, refund, account restore, and offline behavior. Avoid hiding user data or deleting progress when a subscription expires; restrict starting/accessing premium-only content according to the product policy, but let users retain their history.

For the first implementation, use an injectable `PremiumEntitlementProvider` interface and a clearly labeled development implementation. Do not ship a fake provider as production billing.

## Privacy, consent, and trust

- Explain why each sensitive field is collected and make nonessential questions optional.
- Request only data needed for the selected program.
- Never infer religion, health status, body goals, or other sensitive traits.
- Let users edit/delete optional metrics and delete a plan.
- Do not log measurements, health-screen answers, personal journal text, or spiritual notes.
- Separate analytics events from sensitive content.
- Provide a privacy policy and retention/deletion behavior before release.

## Quality gates and tests

### Unit tests
- BMI calculation validates positive, finite inputs and uses adult interpretation only where eligible.
- Personalization respects duration, available days, time budget, equipment, and safety exclusions.
- Day indexing, start/end date, leap-year, and timezone boundary behavior.
- Progress percentage and milestone calculation, including zero activities.
- Pause/resume/replan does not erase completed work.
- Template version changes do not mutate active enrollment snapshots.
- Premium access states: active, expired, grace, unavailable/offline, and restored.

### Room and sync tests
- Local completion persists while offline and survives process death.
- Completion and outbox enqueue are atomic.
- Retry does not duplicate records.
- A newer local edit is not removed by an older sync acknowledgement.
- Account switching cannot expose or upload the previous user's program data.
- Remote deletion/tombstone and simultaneous edits follow the documented conflict policy.
- Migration from the current Room database version preserves existing habit data.

### UI/accessibility tests
- Browse categories, filter programs, open detail, complete the wizard, review and start.
- Activity completion updates progress immediately offline.
- Pause/resume, skip, replan, and completed-history flows.
- Premium lock and restore states.
- TalkBack labels, minimum touch targets, scalable text, contrast, and loading/error/empty states.

Before release: run Gradle unit tests, Android lint, Room migration tests, Firebase Emulator rules tests, and a manual airplane-mode/account-switch test matrix. Do not mark a release build verified until CI actually passes.

## Recommended delivery phases

1. **Foundation:** approve template schema, safety policy, entitlement contract, Room migration, repository/use cases, deterministic personalization, unit tests.
2. **Browse and start:** catalog, category filters, detail, personalization wizard, review, enrollment creation.
3. **Daily plan:** activity checklist, completion, pause/resume/skip/replan, local progress and reminders.
4. **Cloud sync:** outbox operations, per-user Firestore rules, migration/conflict handling, sync status.
5. **Premium:** Play Billing purchase/restore, backend verification, grace/cancellation behavior.
6. **Content and polish:** professionally review fitness content, accessibility, analytics minimization, privacy/delete/export, release testing.

## Decisions to confirm before production billing/content

1. Is Google Play Billing the intended premium entitlement source, and is any subscription implementation already in this repository?
2. Should version one be curated templates + deterministic personalization (recommended), with no generative AI writing health plans?
3. Should spiritual programs be general mindfulness/reflection by default, with explicitly selected religious content as an optional category?
4. Which languages should the initial catalog support?
5. Will a qualified fitness/health reviewer approve exercise and weight-management content before release?

Until these decisions are finalized, use safe defaults: template-driven plans, no guaranteed outcomes, optional measurements, local-first persistence, and no fake premium entitlement in production.
