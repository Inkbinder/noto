# Flexible Cycle Tracker Implementation Plan

Status: Draft  
Last updated: 2026-06-14  
Related docs:

- [flexible-cycle-tracker-design.md](C:/Users/gpsmi/projects/noto/flexible-cycle-tracker-design.md)
- [flexible-cycle-tracker-wireframes.svg](C:/Users/gpsmi/projects/noto/flexible-cycle-tracker-wireframes.svg)

## 1. Purpose

This document turns the current product direction into a practical MVP build plan.

The goal is to build a calm, mobile-first tracker that:

- stores custom tags with a label and color
- lets the user assign multiple tags to a day
- renders up to 6 vertical color slices inside each calendar day cell
- predicts the next period from the `Bleeding` tag history
- works without any fertility or ovulation feature set

## 2. Working Assumptions

This plan assumes:

- v1 is a single-user mobile app
- v1 is local-first, with no account system or sync
- the first build targets one platform-friendly codebase rather than separate native apps
- the data model in the design doc is still valid
- the current agreed month-view rule is `1 = full fill`, `2-6 = equal vertical slices`, `7+ = slices plus overflow badge`

If any of those change, the plan is still usable, but the sequencing may shift.

## 3. Decisions To Lock Before Coding

These do not need perfect answers, but they should be decided before implementation starts:

1. Framework choice
   Recommended: one cross-platform mobile stack.
2. Local persistence choice
   Recommended: SQLite-backed storage or an equivalent structured local database.
3. `Bleeding` tag behavior
   Decide whether it is non-removable or simply a default starter tag.
4. Day edit behavior
   Decide between autosave on toggle or a `Done` action.
5. Overflow rule
   Confirm how `7+` tags are shown in the day cell.
6. Notes in v1
   Decide whether `note` exists in the data model now or stays dormant until later.

## 4. Recommended Technical Shape

Even if the exact stack changes, the app should be split into these layers:

### 4.1 Presentation Layer

- Month calendar screen
- Day detail sheet/modal
- Tag management screen
- Create/edit tag screen
- Settings screen

### 4.2 Domain Layer

- calendar composition logic
- day-to-tag assignment logic
- tag sorting and archive behavior
- period episode detection
- next-period prediction logic
- accessibility label generation for each day

### 4.3 Data Layer

- `tags`
- `day_entries`
- `settings`
- derived selectors for month cells, day detail state, and period summary banner

### 4.4 Suggested Internal Modules

- `calendar/`
- `tags/`
- `entries/`
- `prediction/`
- `settings/`
- `storage/`
- `ui/`

The exact folder names can change, but the logic should stay separated this way.

## 5. MVP Delivery Plan

## Phase 0: Foundation

Goal: create the app shell and development baseline.

Deliverables:

- project scaffold
- navigation shell
- theme tokens for colors, spacing, typography
- date utilities
- local storage setup
- seed mechanism for default tags and default settings

Acceptance criteria:

- app boots to a placeholder home screen
- local database or structured local persistence is working
- a first-run seed inserts default tags and settings exactly once

Notes:

- keep the visual system intentionally simple at this stage
- do not build prediction or full editing yet

## Phase 1: Data Model and Storage

Goal: make tag and day-entry storage real before UI complexity grows.

Deliverables:

- `Tag` storage model
- `DayEntry` storage model
- `UserSettings` storage model
- repository/service methods for:
  - list tags
  - create tag
  - update tag
  - archive tag
  - get day entry by date
  - toggle tag on date
  - load month data

Acceptance criteria:

- tags persist across app restarts
- multiple tags can be attached to one day
- archived tags no longer appear in normal tag pickers
- date lookups are stable and timezone-safe

Primary risk:

- date normalization bugs

Recommendation:

- normalize stored dates to local calendar dates in `YYYY-MM-DD` form and keep that consistent everywhere

## Phase 2: Calendar Home Screen

Goal: deliver a usable read-only month view backed by real data.

Deliverables:

- month navigation
- month grid
- current-day styling
- selected-day styling
- top banner placeholder with simple prediction text states
- segmented cell renderer:
  - 1 tag = full fill
  - 2-6 tags = equal vertical slices
  - 7+ tags = first 6 slices plus overflow badge

Acceptance criteria:

- month renders correctly for varying month lengths
- day numbers remain readable on tinted fills
- cells render stable segment ordering
- empty days look clean and uncluttered

Primary risk:

- visual density and legibility in narrow cells

Recommendation:

- keep fills soft and tag order deterministic

## Phase 3: Day Detail Logging Flow

Goal: make the core user action fast and reliable.

Deliverables:

- tap day to open day detail sheet
- selected tags section
- available tags list
- toggle tag on/off
- optional autosave or explicit save behavior
- accessible day summary string

Acceptance criteria:

- user can mark a day in one or two taps
- toggling updates the month view immediately
- reopening a day shows the correct persisted tags
- day detail screen always names the tags represented by color in the month grid

Primary risk:

- interaction ambiguity if autosave is used without clear feedback

Recommendation:

- if autosave is chosen, show subtle instant confirmation and update the calendar underneath immediately

## Phase 4: Tag Management

Goal: let the user adapt the tracker to real life without product intervention.

Deliverables:

- tag list screen
- add tag flow
- edit tag flow
- color picker
- sort order management
- archive/unarchive behavior
- period-driving tag toggle or selector

Acceptance criteria:

- user can create a new tag in under a minute
- a new tag becomes available in day logging immediately
- archiving hides the tag without deleting historical entries
- exactly one period-driving tag rule is enforced if that is the chosen behavior

Primary risk:

- making tag setup feel heavier than day logging

Recommendation:

- prioritize speed over customization depth

## Phase 5: Period Prediction

Goal: add the small amount of cycle intelligence that is actually useful.

Deliverables:

- episode detection from consecutive `Bleeding` days
- cycle interval calculation
- fallback logic for sparse history
- top-banner states:
  - `No prediction yet`
  - `Period due in X days`
  - `Period due today`
  - `Delayed by X days`
- optional faint prediction hint on calendar if still wanted after testing

Acceptance criteria:

- prediction updates automatically after bleeding history changes
- consecutive bleeding days count as one period episode
- no fertility or ovulation concepts appear anywhere in logic or UI

Primary risk:

- edge cases around partial or inconsistent bleeding history

Recommendation:

- ship simple and explainable rules rather than “smart” hidden heuristics

## Phase 6: Polish, Accessibility, and Hardening

Goal: make the MVP feel stable, readable, and respectful.

Deliverables:

- accessibility labels for every day cell
- color contrast pass
- empty-state copy
- first-run experience cleanup
- performance cleanup for calendar rendering
- basic analytics hooks only if explicitly wanted

Acceptance criteria:

- each day can be described in text without relying on color alone
- navigation and logging feel fast on a real phone
- no broken states when there are 0 tags, 1 tag, or many tags

## 6. Suggested Build Order

If one person is building this, the simplest order is:

1. scaffold app + storage
2. implement tags and day entries
3. render the calendar home screen
4. add the day detail logging flow
5. add tag management
6. add period prediction
7. polish accessibility and edge cases

This keeps the highest-value workflow moving first.

## 7. Testing Plan

### Unit Tests

- period episode detection
- cycle length calculation
- due/delayed banner state logic
- date normalization
- segment layout helper for `0-7+` tags

### Integration Tests

- create tag -> tag appears in picker
- toggle tag on a day -> month cell updates
- archive tag -> hidden from picker, retained in history
- bleeding history change -> prediction banner updates

### Manual QA

- month boundaries
- timezone changes
- leap years
- daylight saving transitions
- busy day with 6 tags
- overflow day with 7+ tags
- long tag names
- first launch with no history

## 8. Risks and Mitigations

### Risk: calendar rendering gets fiddly early

Mitigation:

- implement the cell renderer as its own isolated component with snapshot coverage

### Risk: date logic becomes messy

Mitigation:

- standardize local date handling from day one and avoid mixing timestamps with day keys in UI logic

### Risk: prediction feels too “medical” or overconfident

Mitigation:

- keep messaging lightweight and explicitly estimated

### Risk: tag management becomes overengineered

Mitigation:

- hold the line on v1: label, color, archive, prediction role, sort order

## 9. Rough Effort

Very rough solo-developer estimate for MVP:

- Foundation + storage: 2-3 days
- Calendar home screen: 2-4 days
- Day logging flow: 2-3 days
- Tag management: 2-3 days
- Period prediction: 1-2 days
- Polish + QA: 2-4 days

Rough total:

- about 2 to 4 weeks for a solid MVP, depending on stack choice and visual polish

## 10. Recommended First Sprint

The best first sprint is:

1. scaffold the app
2. implement local data models and repositories
3. seed default tags
4. build the month grid with mock data first
5. swap mock data for real persisted month data

That gets the hardest structural work done before we pile on more screens.

## 11. Next Step

Before implementation starts, I recommend we lock exactly these four things:

1. framework
2. storage approach
3. autosave vs `Done`
4. overflow treatment for `7+` tags

Once those are decided, this is ready to turn into tickets.

