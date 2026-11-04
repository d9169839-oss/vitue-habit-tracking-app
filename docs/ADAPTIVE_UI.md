# Adaptive UI and display-size support

## Goal

Vitue should adapt to available **window width in dp**, not a hard-coded 720p/1080p pixel resolution. Android density, font scale, split-screen, orientation and system display settings all affect usable space. Two devices with the same pixel resolution may have different dp widths.

## Changes in this phase

- Programs catalog uses one column on compact windows, two columns at medium widths, and three columns on wide windows.
- Program cards retain equal row widths and wrap naturally; long descriptions can grow vertically inside the scrollable catalog.
- Personalization choices use a wrapping FlowRow instead of a single horizontally scrolling row, so long labels and larger font scales remain reachable.
- Existing full-screen program steps continue to use vertical scrolling, so shorter displays can reach the bottom actions.
- No business logic, persistence schema, or sync behavior is changed by these UI adjustments.

## Layout breakpoints

| Available content width | Catalog layout |
|---|---|
| Under 600 dp | 1 column |
| 600–899 dp | 2 columns |
| 900 dp and above | 3 columns |

These are content-width breakpoints, not physical screen-resolution checks. A 1080p phone in portrait can correctly remain a one-column layout; a tablet or landscape window can expand into multiple columns.

## Required manual QA

Test on emulator/device configurations representing:

- 720 × 1280 and 1080 × 1920 pixel devices, both portrait and landscape.
- Compact-width Android phone (about 360 dp), medium window (600–899 dp), and wide/tablet window (900 dp or more).
- Split-screen or resizable emulator windows.
- Font scale 1.0 and 1.3–1.5.
- Long localized strings, long program titles/descriptions, empty catalog and premium dialog.
- Login/register, Home, History, Programs catalog, detail, personalization, review, active plan and Profile.
- Bottom navigation and dialog buttons remain reachable; no unintended horizontal scrolling or clipped text.

## Acceptance criteria

- No screen relies on a fixed pixel width or fixed screen resolution.
- Content scrolls vertically when the available height is small.
- Interactive elements remain reachable at larger font scales.
- Program catalog uses the correct number of columns for the available width.
- No layout regression is considered verified until the project builds and the manual matrix above has been run on emulators or devices.
