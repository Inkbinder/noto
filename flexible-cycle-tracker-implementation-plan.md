# Flexible Cycle Tracker Implementation Plan

Status: Active backlog  
Last updated: 2026-06-14  
Related docs:

- [flexible-cycle-tracker-design.md](C:/Users/gpsmi/projects/noto/flexible-cycle-tracker-design.md)
- [flexible-cycle-tracker-wireframes.svg](C:/Users/gpsmi/projects/noto/flexible-cycle-tracker-wireframes.svg)

## 1. Purpose

This document is the ordered implementation list for the Android app as it exists today.

It reflects two decisions:

- every feature item includes tests as part of the work
- because there are currently no automated tests in the repo, backfilling coverage for the existing scaffold is the first priority

## 2. Current App State

The current scaffold already includes:

- Android app shell with Jetpack Compose
- Room database and DataStore-backed settings
- seeded default tags
- month calendar with vertical color slices
- day detail tag toggling
- settings for period prediction, cycle length, and week start
- basic period prediction banner
- AGP 9.2.1 / Gradle 9.4.1 build setup

The current scaffold does not yet include:

- automated tests
- tag create/edit/archive flows
- stronger prediction rules and edge-case handling
- accessibility and UI hardening

## 3. Delivery Rule

No implementation item is done until its matching tests are added and passing.

For this project, that means:

- domain logic gets unit tests
- repository and persistence behavior gets integration-style tests
- user flows that matter visually or interaction-wise get UI tests
- `assembleDebug` stays green after every change

## 4. Ordered Implementation List

## Item 1: Build the test baseline and backfill current functionality

Goal: make the existing scaffold safe to build on.

Implementation:

- add the project test structure under `app/src/test` and `app/src/androidTest`
- add any missing test dependencies for coroutines, Room, and Compose UI testing
- add unit tests for `MonthGridBuilder`
- add unit tests for `PeriodPredictionEngine`
- add unit tests for day-slice and overflow behavior:
  - `0 tags`
  - `1 tag`
  - `2-6 tags`
  - `7+ tags`
- add repository tests for:
  - toggling tags on a day
  - loading month summaries
  - archived tags being hidden from active pickers
- add settings tests for:
  - prediction enabled/disabled
  - cycle length updates
  - week-start updates

Done when:

- current behavior is covered by automated tests
- tests document the expected rules for slice rendering and prediction behavior
- future feature work has a safe baseline

## Item 2: Finish tag management

Goal: let the user control the tracker vocabulary without code changes.

Implementation:

- add create-tag flow
- add edit-tag flow
- add archive/unarchive behavior
- add color selection
- add sort-order support
- add period-driving tag selection behavior

Tests:

- repository tests for create, update, archive, and reorder
- ViewModel tests for form state and validation
- UI tests for:
  - creating a tag
  - editing a tag
  - archiving a tag
  - seeing the new tag appear in day detail immediately

Done when:

- the placeholder note on the tags screen is gone
- custom tags are fully manageable in-app
- historical entries survive tag archiving correctly

## Item 3: Improve the day-detail logging flow

Goal: make daily logging fast enough to use casually.

Implementation:

- improve the selected-state presentation for tags
- add clearer feedback when toggling tags
- decide and implement autosave vs explicit done behavior
- make period-driving tags understandable in the day editor
- ensure many-tag days still feel manageable

Tests:

- unit or ViewModel tests for toggle behavior
- UI tests for:
  - opening a day
  - selecting multiple tags
  - deselecting tags
  - leaving and reopening with persisted state intact
  - overflow cases with many tags

Done when:

- a user can log a day quickly without confusion
- the month view updates reliably after edits

## Item 4: Harden period prediction

Goal: make the banner feel helpful and predictable without drifting into fertility tracking.

Implementation:

- define bleed-episode rules from consecutive bleeding days
- refine cycle interval calculations
- handle sparse history and inconsistent history explicitly
- improve the copy for:
  - no prediction
  - due soon
  - due today
  - delayed
- confirm prediction can be fully disabled in settings

Tests:

- unit tests for episode detection
- unit tests for interval calculation
- unit tests for banner-state wording triggers
- integration tests showing prediction changes after bleeding history updates

Done when:

- prediction behavior is simple, explainable, and well covered by tests
- no ovulation or fertility concepts leak into logic or UI

## Item 5: Accessibility and visual hardening

Goal: make the app readable and understandable in everyday use.

Implementation:

- improve day-cell contrast and text legibility
- add accessible descriptions for color-sliced day cells
- verify non-current-month and today states remain readable
- refine spacing, touch targets, and empty states
- handle long tag names gracefully

Tests:

- UI tests for accessible labels on day cells
- UI tests for day-detail content descriptions where useful
- snapshot or screenshot-style checks if we decide to add them

Done when:

- color is not the only way to understand a day
- the calendar remains legible across common edge cases

## Item 6: Reliability and data safety

Goal: reduce risk before the app starts storing real personal history long-term.

Implementation:

- review date normalization and timezone assumptions
- add migration strategy for schema changes
- consider backup/export format for future-proofing
- ensure seed data upgrades are idempotent

Tests:

- migration tests
- repository tests around seed/version behavior
- tests for date handling around month boundaries and leap years

Done when:

- app updates are less risky
- stored history can survive schema evolution cleanly

## 5. Suggested Build Sequence

The best order from here is:

1. test baseline and backfill
2. tag management
3. day-detail polish
4. prediction hardening
5. accessibility and visual hardening
6. reliability and migration work

## 6. Definition of Done

For each implementation item:

- feature behavior works on device or emulator
- automated tests are added for the new behavior
- existing tests still pass
- `assembleDebug` passes
- the code graph is re-indexed after the change set
