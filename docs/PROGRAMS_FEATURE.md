# Programs Feature — Product, Architecture, and Implementation Specification

## Status and scope

This document defines the production-oriented implementation for the Programs tab. It is the source of truth for the first release and should be updated as the product decisions are finalized.

## Implementation status (2026-09-30)

The production-oriented Phase 1 implementation is committed in the repository:
- The Programs bottom tab replaces the old Progress tab; Home, History, and Profile remain.
- Bundled catalog: beginner movement, home strength, everyday stamina, sustainable wellness habits, grooming, skincare, mindfulness, and gratitude/reflection.
- Deterministic schedule generation creates a day-by-day snapshot, respecting the chosen time budget and number of available days per week.
- Room schema version 4 stores enrollments and scheduled activities. Migrations 2→3 add program tables and 3→4 add the daily-work activity preference without deleting existing habit data. The personalization screen asks about work activity, available time, days/week, experience, and equipment. Optional adult BMI is calculated on-device only; values are not saved or uploaded and do not set exercise intensity or calorie targets.
- Program enrollment and activity changes are enqueued transactionally and uploaded/pulled by the existing network-constrained WorkManager sync worker.
- Hilt-backed ViewModel/use cases manage catalog filtering, start/pause/resume, completion/skip, progress, deletion, and durable schedule revisions.
- Programs now use a dedicated full-screen flow: catalog → detail → personalization → plan review → active program. Android back navigation returns through the flow, and the bottom navigation is hidden while a program flow screen is open.
- Active program cards show each program's completion percentage and progress bar. Opening a program shows today's scheduled activity and the program timeline; marking an activity Done/Skip/Reset writes the activity and its outbox operation in one Room transaction.
- Program completion percentage counts scheduled activity days only. Zero-minute recovery/rest days remain visible in the plan but are excluded from the completion denominator.
- Program enrollment and activity records sync through the durable WorkManager outbox to the user's Firestore subcollections. Account-scoped realtime listeners merge remote program changes back into Room, so a completion made on another signed-in device updates the local progress without waiting for the periodic pull.
- The Programs repository waits for its initial enrollment/activity snapshots (with a bounded fallback) before exposing an empty catalogue state on a new device. Room remains the UI source of truth.
- Active programs expose an "Adjust remaining schedule" flow. Replanning atomically updates the Room enrollment and future schedule while preserving completed/skipped history and queueing the revised rows for cloud sync.
- All eight built-in templates are available free to every user; there is no purchase or subscription flow.

This is an implementation specification, not a release certification. The first public release deliberately makes all bundled programs free and excludes Google Play Billing, subscription products, and paid-user entitlements. Firebase Authentication and Firestore remain separate account and cloud-sync services. Fitness content is conservative starter content and still needs qualified review. Firestore rules must explicitly protect the `programEnrollments` and `programActivities` subcollections.

The first release should combine **curated, versioned templates** with **deterministic personalization rules**. Do not ask an AI model to invent a complete health or spiritual plan without reviewed constraints. AI can be added later to explain or rephrase a reviewed plan, but it must not bypass safety rules.

## Product goals

- Help a user choose a structured 14-, 30-, 60-, or 90-day program.
- Personalize schedule, difficulty, available time, equipment, and selected goal.
- Let users start, pause, resume, or finish a plan.
- Show one clear “Today” checklist and a phase/milestone view.
- Persist progress locally immediately and synchronize it in the background.
- Support offline use and process death.
- Make every bundled program available to every user without a subscription or paid account.
- Keep program progress separate from ordinary habits, while optionally offering a user-controlled “add activity as a habit” integration.

## Information architecture

1. **Programs landing**
   - Hero: “Build a better routine, one day at a time.”
   - Category filters: All, Fitness, Self-Grooming, Mindfulness/Spiritual.
   - Featured, beginner-friendly, and short-time-commitment collections.
   - Cards show goal, duration, difficulty, minutes/day, equipment, and a clear Free for everyone label.
   - Filters and categories must be accessible and scrollable.

2. **Program detail**
   - Purpose, intended audience, duration, time/day, equipment, difficulty, and phase outline.
   - “What you’ll do” list and realistic expectations.
   - Safety notes and exclusions for health-related programs.
   - Free access and “Personalize plan” action.

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
- `ProgramTemplate`: stable ID, version, title, category, description, duration, difficulty, minutes/day, free availability, equipment, phases, activities, safety metadata.
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

Each document should include schemaVersion, updatedAtMillis, and a stable ID. Do not copy full immutable template text into every completion document; enrollment stores the versioned snapshot needed to render a historic plan. Use Firestore rules to validate document ownership, allowed paths, fields, types, and sizes. Firestore rules must enforce `request.auth.uid == uid` and validate allowed document paths/fields. Deploy and test rules in Firebase Emulator Suite; a rules file in GitHub does not deploy itself.

## Access model

Every built-in program is free to start, personalize, pause, resume, and complete. There are no subscription products, in-app purchases, premium-only templates, or paid-account requirements in the first release.

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
- Verify that all catalog templates remain accessible without a network connection or purchase state.

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
- All program details and personalization actions are available without a purchase.
- TalkBack labels, minimum touch targets, scalable text, contrast, and loading/error/empty states.

Before release: run Gradle unit tests, Android lint, Room migration tests, Firebase Emulator rules tests, and a manual airplane-mode/account-switch test matrix. Do not mark a release build verified until CI actually passes.

## Recommended delivery phases

1. **Foundation:** approve template schema, safety policy, Room migration, repository/use cases, deterministic personalization, unit tests.
2. **Browse and start:** catalog, category filters, detail, personalization wizard, review, enrollment creation.
3. **Daily plan:** activity checklist, completion, pause/resume/skip/replan, local progress and reminders.
4. **Cloud sync:** outbox operations, per-user Firestore rules, migration/conflict handling, sync status.
5. **Content and release polish:** professionally review fitness content, accessibility, analytics minimization, privacy/delete/export, and release testing.

## Decisions to confirm before production content

1. Should version one remain curated templates + deterministic personalization, with no generative AI writing health plans?
2. Should spiritual programs be general mindfulness/reflection by default, with explicitly selected religious content as an optional category?
3. Which languages should the initial catalog support?
4. Will a qualified fitness/health reviewer approve exercise and weight-management content before release?

Until these decisions are finalized, use safe defaults: template-driven plans, no guaranteed outcomes, optional measurements, local-first persistence, and no billing or subscription logic in the first release.


## Recommended program roadmap (product proposal)

The bundled catalog currently contains eight templates. Keep the initial experience curated and reviewed rather than shipping dozens of shallow plans. Treat the following as a roadmap; a program is not implemented just because it appears in this proposal.

### Already in the bundled catalog
- **Beginner Movement Foundations** — 30 days; free.
- **Home Strength Foundations** — 60 days; free.
- **Everyday Stamina** — 60 days; free.
- **Sustainable Wellness Habits** — 90 days; free; no calorie prescriptions or promised weight change.
- **Everyday Grooming Routine** — 30 days; free.
- **Simple Skincare Consistency** — 30 days; free.
- **Mindfulness Foundations** — 30 days; free.
- **Gratitude and Reflection** — 30 days; free.

### Best next programs to add
Prioritize these after responsive UI and release-quality testing. Each should have a reviewed template, distinct phase content, and real schedule activities—not just a new title over the same generic steps.

1. **Sleep Routine Foundations (30 days):** consistent wind-down, regular wake-time reflection, screen-off cue and bedroom-preparation habits. Avoid diagnosing or treating sleep disorders.
2. **Digital Balance (14/30 days):** notification review, app-free blocks, intentional phone checks and evening device boundaries. Never shame users for setbacks.
3. **Focus and Study/Work Sessions (30 days):** task selection, short focus blocks, breaks, planning and end-of-day review. Let users choose study, work or personal projects.
4. **Confidence and Communication Practice (60 days):** low-pressure listening, clear requests, prepared conversations and reflection; do not promise personality transformation.
5. **Hair-Care Routine (30 days):** simple consistency and user-selected hair needs; avoid medical claims about hair loss or regrowth.
6. **Personal Organization and Weekly Reset (30 days):** small decluttering tasks, calendar review, preparing essentials and a sustainable weekly reset.
7. **Walking and Cardio Consistency (60 days):** a reviewed low-impact progression with easier days and options to substitute comfortable movement.
8. **Gym Consistency and Strength Foundations (90 days):** only after qualified review; ask about experience/equipment, use technique-first progression, recovery and safe alternatives.
9. **Purpose, Values and Journaling (60 days):** optional prompts for priorities, values and small weekly actions.
10. **User-selected spiritual reflection (30/90 days):** optional, explicitly chosen by the user. Never infer a religion or spiritual preference from name, location, language or other profile data.

### Personalization inputs by program category

- **All programs:** preferred goal, duration, minutes/day, days/week, start date, reminders, experience with the routine, and accessibility/preferences.
- **Fitness:** home/gym/outdoors, equipment, current comfort level and a brief safety screen. Work activity can inform scheduling/recovery, but must not alone prescribe exercise intensity.
- **Weight-related wellness:** make height/weight optional, adult-only where BMI interpretation is relevant, calculate locally unless the user explicitly consents to another use, and do not prescribe calories or promise weight loss/gain. For medical conditions, injury, pregnancy, eating-disorder concerns or other higher-risk cases, do not generate an intense or weight-loss plan; refer to qualified professional guidance.
- **Grooming/skincare/hair care:** routine preferences, time budget, sensitivities only if genuinely needed, and a clear skip option. Do not ask for BMI or weight.
- **Mindfulness/reflection:** preferred duration, prompt style and optional reminder. Avoid treating it as therapy or requiring disclosure of journal contents.

### Suggested release sequence

- **Release 1:** keep the current eight templates; ensure every template has distinct daily activities, accurate duration, reviewed safety notes and good empty/loading states.
- **Release 1.1:** Sleep Routine Foundations, Digital Balance, Focus and Study/Work Sessions, Personal Organization.
- **Release 1.2:** Confidence and Communication, Hair-Care Routine, and expanded walking/cardio options after content review.
- **Later:** 90-day gym/strength and user-selected spiritual paths only after appropriate review and user research.

The first release has no premium tier. Keep every user's saved plans, activities, and history available without payment.


## Built-in templates added for the next catalog revision

These two version-1 templates are bundled and deterministic; their content is original and informed by common patterns visible in public habit/workout repositories, rather than copied code or text.

### 1. Fitness & General Wellness — `fitness-wellness-60`
- **Category:** Fitness
- **Duration:** 60 days
- **Level:** Beginner
- **Default commitment:** 15 minutes/day
- **Equipment:** None
- **Access:** Free starter
- **Image:** `app/src/main/res/drawable/program_fitness_wellness.xml`
- **Phase 1 (Days 1–20), Build a baseline:** comfortable movement check-in; begin with familiar low-impact movement and rest as needed.
- **Phase 2 (Days 21–40), Create a steady rhythm:** movement and mobility; keep effort conversational and include easier days.
- **Phase 3 (Days 41–60), Make it sustainable:** recovery and routine review; add a cooldown and reflect on sleep/hydration habits without strict targets.
- **Safety:** general-wellness only; no diagnosis, calorie prescription, weight-loss promise, or intensity prescription based on BMI. Stop for pain, dizziness, chest discomfort, or unusual breathlessness; seek qualified advice where appropriate.

### 2. Personal Development — `personal-development-30`
- **Category:** Personal Development
- **Duration:** 30 days
- **Level:** Beginner
- **Default commitment:** 10 minutes/day
- **Equipment:** None
- **Access:** Free starter
- **Image:** `app/src/main/res/drawable/program_personal_development.xml`
- **Phase 1 (Days 1–10), Know your starting point:** a short daily check-in with one priority, one likely distraction, and one achievable action.
- **Phase 2 (Days 11–20), Practice intentionally:** one self-selected 10-minute focus or learning session, followed by a break.
- **Phase 3 (Days 21–30), Reflect and continue:** review what was learned, recognize a useful action, and choose one small adjustment for the following week.
- **Safety and scope:** educational habit-building only; no therapy claims or guaranteed personal/professional outcomes. Prompts are optional and adaptable.

The artwork is bundled Android VectorDrawable XML, so it renders locally/offline, scales cleanly on 720p and 1080p devices, and requires no image-network request or extra image-loading dependency. The domain model stores an artwork identifier; Android resource IDs are mapped only in the presentation layer.

### Public repository references reviewed for product patterns
- [pgrzel/Habit-Tracker](https://github.com/pgrzel/Habit-Tracker) — Compose, Room, Flow, Clean Architecture, and testing structure.
- [cr0sz/Workout-Tracker](https://github.com/cr0sz/Workout-Tracker) — workout programs, reusable templates, and structured progression concepts.
- [shayanmohd/habits-app](https://github.com/shayanmohd/habits-app) — minimalist offline-first habit tracking patterns.

These repositories are references for general product/architecture ideas only. Do not copy source code, visual assets, or wording without checking the relevant license and attribution requirements.
