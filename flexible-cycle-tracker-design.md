# Flexible Cycle Tracker Design Doc

Status: Draft  
Last updated: 2026-06-14  
Reference: calendar-first period tracker UI from the attached screenshot, used as layout inspiration only

## 1. Summary

We want a mobile-first calendar app that helps track periods and arbitrary day-level symptoms/events without turning into a fertility or ovulation product.

The core idea is:

- the app is centered on a monthly calendar
- the user can create custom tags with a label and color
- any day can have multiple tags
- the calendar shows those tags by splitting the day cell into colored slices
- the app can optionally estimate when the next period is due based on recent bleeding history

This should feel lightweight, flexible, and easy to glance at.

## 2. Problem Statement

Most period tracking apps assume the user wants fertility, ovulation, or pregnancy-related insight. That is not the goal here.

The actual need is simpler:

- track whether bleeding happened
- track other day-level states such as hangover, bad poops, lightheadedness, or future custom symptoms
- allow multiple things to be visible on the same day
- still provide a simple "period due soon / delayed" hint because that is genuinely useful

## 3. Product Goals

- Make it fast to mark one or more tags on a specific day.
- Support an open-ended set of custom tags rather than a fixed symptom list.
- Keep the month view useful at a glance, even when several tags land on the same day.
- Offer a lightweight next-period estimate without requiring fertility tracking.
- Make the product feel calm and non-medicalized.

## 4. Non-Goals

- Ovulation prediction
- Fertility windows
- Pregnancy planning features
- Partner sharing or social features
- Deep medical analytics in v1
- Clinically validated cycle prediction

## 5. User Stories

- As a user, I can create a tag like `Bleeding`, `Bad hangover`, `No hangover`, `Dodgy poops`, or `Lightheaded`.
- As a user, I can assign multiple tags to the same day.
- As a user, I can glance at the month view and see which days had which events.
- As a user, I can add new tags later without needing a product update.
- As a user, I can get a rough idea of when my next period is due from past bleeding entries.
- As a user, I never have to interact with fertility or ovulation concepts if I do not want them.

## 6. Design Principles

- Calendar first: the month view is the home screen, not a secondary screen.
- Flexible over prescriptive: users define what matters to them.
- Fast logging: a day should be editable in one or two taps.
- Color in the grid, text in the detail view: the month view can be color-led as long as expanded views clearly name the tags.
- Private by default: health-adjacent data should feel local and personal.
- Graceful density: multiple tags on one day should remain readable.

## 7. Proposed User Experience

### 7.1 Home Screen

The main screen should be a monthly calendar modeled after the reference image, but simplified:

- a top status banner shows period guidance only
- month navigation sits above the calendar grid
- the calendar occupies most of the screen
- tapping a day opens a day detail sheet
- settings or tag management are available from a top-right action

Example banner states:

- `Period due in 4 days`
- `Period due today`
- `Delayed by 3 days`
- `No prediction yet`

There should be no fertility, ovulation, or intimacy legend on this screen.

### 7.2 Calendar Day Cell

Each day cell should contain:

- day number
- selected state styling for the currently focused day
- a segmented background that represents the selected tags for that day

Proposed day fill behavior:

- 1 tag: the cell is filled with one color
- 2 tags: the cell is split into two equal vertical slices
- 3 tags: the cell is split into three equal vertical slices
- 4 tags: the cell is split into four equal vertical slices
- 5 tags: the cell is split into five equal vertical slices
- 6 tags: the cell is split into six equal vertical slices
- 7+ tags: show the first 6 slices plus a small `+N` overflow badge

Recommendation for v1:

- use soft tinted fills rather than fully saturated fills so the date number stays readable
- do not use icons in the month grid
- keep the source of truth for meaning in the day detail sheet and accessible text summary

This makes the month view feel cleaner and removes the need for iconography in the densest part of the UI.
It also gives the grid one consistent visual rule instead of changing layout patterns by tag count.

### 7.3 Day Detail / Logging Flow

Tapping a day should open a bottom sheet or full-screen modal with:

- the selected date
- the tags already selected for that date
- a grid/list of available tags as toggleable chips
- a clear `Create new tag` action

Suggested interaction:

1. Tap a day.
2. Tap one or more tags.
3. Save automatically or with a clear confirm action.

The best default is probably autosave on toggle, because this is lightweight day logging rather than form entry.

### 7.4 Tag Management

Users need a dedicated place to manage tags.

Each tag should support:

- label
- color
- active / archived state
- sort order
- optional flag for period prediction participation

Suggested starter tags:

- `Bleeding`
- `Bad hangover`
- `No hangover`
- `Dodgy poops`
- `Lightheaded`

These should be defaults, not hardcoded constraints.

### 7.5 Period Prediction Experience

Period prediction should exist, but as a small utility layer rather than the main event.

Proposed UX:

- one tag is designated as the period-driving tag
- by default this is `Bleeding`
- the home banner uses this tag history to estimate the next due date
- predicted period days may be lightly hinted on the calendar, but this is optional for v1

Important: prediction should be framed as an estimate, not a promise.

## 8. Period Prediction Logic

### 8.1 Proposed v1 Rule

Use a simple heuristic:

1. Find all dates marked with the period-driving tag.
2. Collapse consecutive tagged days into a single period episode.
3. Use the start date of each episode as the cycle anchor.
4. If enough history exists, estimate cycle length from recent episode starts.
5. If not enough history exists, fall back to a default cycle length.

### 8.2 Recommended Defaults

- default cycle length: 28 days
- history window: last 3 to 6 period starts
- minimum history for averaging: 2 period episodes

Fallback behavior:

- if there is only one known period start, predict the next one as `last start + 28 days`
- if there are at least two starts, use the rolling average interval
- if there is no bleeding history, show `No prediction yet`

### 8.3 Important Constraints

- no ovulation calculation
- no fertility window calculation
- no attempt to infer anything beyond a rough next-period estimate

This keeps the feature aligned with the user's actual need.

## 9. Proposed Data Model

### 9.1 Tag

```json
{
  "id": "uuid",
  "label": "Bleeding",
  "color": "#D95C5C",
  "isPeriodTag": true,
  "isArchived": false,
  "sortOrder": 0
}
```

### 9.2 Day Entry

```json
{
  "date": "2026-06-14",
  "tagIds": ["tag-bleeding", "tag-lightheaded"],
  "note": null,
  "updatedAt": "2026-06-14T10:15:00Z"
}
```

### 9.3 User Settings

```json
{
  "periodPredictionEnabled": true,
  "defaultCycleLengthDays": 28,
  "weekStartsOn": "monday"
}
```

### 9.4 Derived Data

These likely do not need to be stored directly in v1:

- period episodes
- predicted next period date
- delayed / due-soon status

They can be derived from tag history plus settings.

## 10. Calendar Rendering Rules

Recommended month cell rules:

- date number at top
- segmented background fill based on tag count
- overflow badge after 6 visible segments
- subtle outline for today
- stronger fill or border for selected date

Proposed fill layouts:

- 1 tag: full-cell fill
- 2 tags: vertical split
- 3 tags: vertical thirds
- 4 tags: vertical quarters
- 5 tags: vertical fifths
- 6 tags: vertical sixths

Current recommendation:

- keep the slice direction consistent from 2 through 6 tags
- use softened tag colors in the calendar so black text remains legible
- use a small corner badge for `+N` when more than 6 tags exist

This deserves visual testing because it directly affects scanability.

## 11. Accessibility and Readability

- Do not rely on color alone; every colored day cell needs a clear text representation in the day detail view and accessibility layer.
- Keep contrast high enough that segmented fills remain visible while the date number stays readable.
- Provide an accessible text summary for each day, such as `June 14: Bleeding, Lightheaded`.
- Let the user rename or archive tags rather than forcing a rigid vocabulary.

## 12. Privacy and Trust

Because this is personal health-adjacent data, the product should feel respectful:

- local-first storage by default
- no social feed or sharing assumptions
- no fertility-oriented nudges
- plain language around predictions

If backup or sync is added later, it should be clearly opt-in.

## 13. MVP Scope

### In

- month calendar home screen
- custom tag creation
- color selection for tags
- multiple tags per day
- day detail logging flow
- default `Bleeding` tag
- simple next-period estimate
- settings for basic prediction preferences

### Out

- trends dashboard
- charts
- reminders
- export
- account system
- fertility features
- icons in the calendar grid
- advanced note-taking

## 14. Open Decisions

These should stay explicitly open for now:

- Should `Bleeding` be a built-in non-removable tag, or just a default starter tag the user can edit?
- Should the slices always stay vertical for every count from 2 through 6?
- What is the best overflow treatment when a day has more than 6 tags?
- Should the fills use soft tints only, or should the user be able to choose stronger saturation?
- Should day edits autosave immediately, or should there be an explicit `Done` action?
- Should the app support free-text notes in v1, or only structured tags?
- Should users be able to mark more than one tag as period-related?
- Should predicted period days be shown directly on the calendar, or only in the top banner?
- Should the fallback cycle length be fixed at 28 days or user-editable from day one?

## 15. Recommendation

For the first build, the cleanest version is:

- a calendar-first app
- user-defined tags with label and color
- multiple tags per day
- segmented color fills on the month view
- a default `Bleeding` tag used for simple period prediction
- a single banner that says when the next period is likely due

That gets the core value into the product quickly without importing the complexity of mainstream fertility apps.

## 16. Success Criteria

We should consider the first version successful if:

- a user can create a new tag in under a minute
- a user can mark a day with multiple tags in a couple of taps
- the month view remains readable even on busy days
- the period estimate updates automatically from bleeding history
- the product feels useful without ever asking the user to care about ovulation or fertility
