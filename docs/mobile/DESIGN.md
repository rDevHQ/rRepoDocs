---
name: rRepoDocs Design System
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#434655'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#737686'
  outline-variant: '#c3c6d7'
  surface-tint: '#0053db'
  primary: '#004ac6'
  on-primary: '#ffffff'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#b4c5ff'
  secondary: '#505f76'
  on-secondary: '#ffffff'
  secondary-container: '#d0e1fb'
  on-secondary-container: '#54647a'
  tertiary: '#943700'
  on-tertiary: '#ffffff'
  tertiary-container: '#bc4800'
  on-tertiary-container: '#ffede6'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#d3e4fe'
  secondary-fixed-dim: '#b7c8e1'
  on-secondary-fixed: '#0b1c30'
  on-secondary-fixed-variant: '#38485d'
  tertiary-fixed: '#ffdbcd'
  tertiary-fixed-dim: '#ffb596'
  on-tertiary-fixed: '#360f00'
  on-tertiary-fixed-variant: '#7d2d00'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
  app-bg: '#F8FAFC'
  sidebar-surface: '#F1F5F9'
  editor-surface: '#FFFFFF'
  context-pane-surface: '#F8FAFC'
  border-subtle: '#E2E8F0'
  text-primary: '#1E293B'
  text-secondary: '#64748B'
  accent-blue-subtle: '#EFF6FF'
  status-success: '#10B981'
  status-warning: '#F59E0B'
typography:
  editorial-display:
    fontFamily: Newsreader
    fontSize: 40px
    fontWeight: '600'
    lineHeight: '1.2'
  editorial-h1:
    fontFamily: Newsreader
    fontSize: 32px
    fontWeight: '600'
    lineHeight: '1.3'
  editorial-h2:
    fontFamily: Newsreader
    fontSize: 24px
    fontWeight: '500'
    lineHeight: '1.4'
  preview-body:
    fontFamily: Newsreader
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.7'
  ui-medium:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
  ui-small:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  code-editor:
    fontFamily: monospace
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.6'
  metadata:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.05em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  pane-sidebar-width: 260px
  pane-context-width: 320px
  container-max-width: 800px
  gutter: 1rem
  stack-xs: 0.25rem
  stack-sm: 0.5rem
  stack-md: 1rem
  stack-lg: 2rem
  inset-editor: 3rem
---

# Design System: rRepoDocs

## 1. Overview & Product Essence

rRepoDocs is a calm, repo-first Markdown workspace for people who store documents in GitHub.

It is not a general note-taking platform, not a CMS, and not a full Git client. Its purpose is narrower and more deliberate: to make browsing, editing, previewing, and saving Markdown files in a GitHub repository feel simple, clear, and quietly premium.

The product should feel like a cross between:
- a refined writing tool
- a repo-aware workspace
- a desktop-grade editor with restrained chrome

The emotional goal is confidence without noise.  
The functional goal is clarity without friction.

---

## 2. Creative North Star

The creative north star for rRepoDocs is:

## “The Quiet Repository”

A repository is usually treated as something technical, dense, and slightly intimidating.  
rRepoDocs reframes it as a readable, navigable, editorial workspace.

The app should feel:
- grounded
- minimal
- precise
- typographic
- spacious
- focused

Not:
- busy
- flashy
- dashboard-like
- IDE-heavy
- CMS-like
- playful for the sake of it

This is an app for working with real files in a real repo — but with more calm and elegance than a typical developer tool.

---

## 3. Product Identity

rRepoDocs is built around one simple mental model:

> One active repository. One file tree. One open Markdown file. One contextual side pane.

Everything in the app should reinforce that model.

### Core product pillars
- Repo-first — the repository is the workspace
- Markdown-native — Markdown is the primary document format
- Desktop-capable — the app should feel especially strong on desktop
- Calm by design — minimal chrome, low noise, clear hierarchy
- Contextual, not cluttered — the right information appears in the right place

---

## 4. Layout Philosophy

The layout is based on a three-pane workspace:

### Left pane — Repository rail
The left side is the grounded structural anchor of the app.  
It contains the repository context and file tree.

This pane should feel:
- stable
- quiet
- functional
- easy to scan

### Center pane — Editor surface
The center is the primary work surface.  
This is where Markdown is edited in raw form.

This pane should feel:
- precise
- focused
- calm
- efficient
- like the main working area

### Right pane — Contextual reading pane
The right side is a contextual pane for the currently open file.  
It supports:
- Preview
- History

This pane should feel:
- lighter than the editor
- more typographic in Preview mode
- more structured in History mode
- clearly secondary, but still important

---

## 5. Visual Language

### General tone
The visual language should be restrained and confident.

It should use:
- soft surface hierarchy
- subtle contrast
- typography as structure
- minimal accent color
- clean spacing
- clear visual rhythm

### Avoid
- heavy borders
- dashboard cards everywhere
- visual clutter
- strong ornamental effects
- repeated labels and metadata
- generic web-app styling

### The app should visually communicate:
- this is a workspace
- this is repo-aware
- this is calm
- this is built for reading and editing
- this is not trying to do too much

---

## 6. Color Philosophy

The palette should be based on soft neutral surfaces with a restrained blue accent.

### Base surfaces
Use light neutrals to create hierarchy without relying on hard borders:
- application background
- sidebar surface
- editor surface
- contextual side-pane surface

Hierarchy should come from:
- tonal shifts
- spacing
- containment
- typography

Not from:
- lots of outlined boxes
- thick separators
- strong card borders

### Accent color
Blue should be used sparingly and intentionally for:
- active file
- primary button
- selection
- active pane mode
- focus states
- moments of intent

The accent should not dominate the interface.

---

## 7. Typography

Typography is one of the primary interface tools in rRepoDocs.

There are three distinct typographic roles:

### 7.1 Editorial typography
Used in Preview mode and major headings.

This layer should feel:
- readable
- confident
- human
- slightly more expressive

It carries the reading experience.

### 7.2 Functional UI typography
Used for:
- labels
- repo info
- file tree text
- metadata
- buttons
- status text

This layer should feel:
- crisp
- clean
- quiet
- neutral

### 7.3 Code/editor typography
Used in raw Markdown editing.

This layer should feel:
- precise
- technical
- stable
- unobtrusive

The editor should not be over-styled.  
It should remain legible and calm, with syntax emphasis used carefully.

---

## 8. Pane Roles and UX Behavior

## 8.1 Left pane — Repository sidebar

### Purpose
To orient the user within the active repository.

### Contents
- repository name
- branch or repo context where useful
- new file action
- file tree
- bottom utility/account area if needed

### UX principles
- easy to scan
- stable hierarchy
- active file should be clearly visible
- context actions should be secondary and appear through right-click/context menu
- folders and files should have distinct but quiet treatment

### Must not feel like
- a CMS sidebar
- a notes app notebook list
- a settings menu
- a generic navigation rail

It is a repo tree, not a category system.

---

## 8.2 Center pane — Editor

### Purpose
To provide the main writing and editing surface for raw Markdown.

### Contents
- current file context
- commit/save area
- editor content

### UX principles
- file context should be shown clearly, but only once
- save state should be shown clearly, but only once
- commit message should feel integrated, not dominant
- editor should start quickly without too much header clutter
- unsaved changes should be obvious
- writing should feel immediate and friction-light

### The editor should feel like
- the primary workspace
- the most important pane
- a tool for careful writing/editing

### It should not feel like
- a form
- a stack of cards
- a control panel
- a dashboard widget

---

## 8.3 Right pane — Context pane

The right pane must always be tied to the currently open file.

It supports two modes:

### Preview
Rendered Markdown in a calm editorial presentation.

Preview should feel:
- typographic
- spacious
- more “reading mode” than “app panel”
- clearly different from the raw editor

### History
Git history for the currently open file.

History should feel:
- structured
- compact
- contextual
- readable

It should show:
- commit message
- author
- date/relative time
- short hash
- current/latest context if useful

### Important rule
The right pane is contextual.  
It should not become a general-purpose dashboard or Git client.

---

## 9. Header / Top Bar Philosophy

The top bar should function more like workspace chrome than a typical web-app header.

It should be:
- compact
- informative
- useful
- calm

It should communicate:
- active repository
- branch if relevant
- high-level workspace context
- global actions like switch repo, sync, account, overflow

It should not be:
- large
- decorative
- stuffed with icons
- a second navigation system
- visually heavier than the editor

The inspiration should be desktop workspace logic, not IDE clutter.

---

## 10. Save-State Philosophy

The app must feel trustworthy while editing.

### Rule
There should be one clear save-state model, not several repeated status indicators.

Good save states:
- Unsaved changes
- Saving…
- Saved to GitHub
- Save failed

Avoid showing multiple overlapping variations like:
- Saved
- Saved to GitHub
- No pending edits

all at once.

The user should always understand:
- whether there are changes
- whether the app is saving
- whether the save succeeded
- whether action is needed

---

## 11. File Actions

The app supports essential document operations:
- new file
- rename
- move
- save

These should feel:
- discoverable
- light
- file-native
- repo-consistent

They should not dominate the interface.

### Recommended behavior
File actions belong in:
- context menus
- selected-file actions
- focused contextual UI

Not as permanently visible noisy controls everywhere.

---

## 12. Interaction Design

Interactions should feel desktop-capable and polished.

### Priorities
- clear hover states
- clear focus states
- clear selected states
- good disabled states
- obvious active file state
- keyboard support where it matters

### Important keyboard behavior
At minimum:
- Cmd/Ctrl + S should save

### Motion
Motion should be:
- subtle
- quick
- calm
- never decorative for its own sake

---

## 13. Empty States and Failure States

rRepoDocs should feel designed even when nothing is selected or something goes wrong.

Good empty or edge states are needed for:
- no file selected
- no preview available
- no history available
- repo loading
- file loading
- save failed
- read-only file

These should feel:
- intentional
- quiet
- informative
- not broken

---

## 14. Design Tensions to Balance

rRepoDocs lives between a few important tensions:

### Technical vs editorial
The app must respect the repo and file structure, but still feel more refined than a raw developer tool.

### Minimal vs useful
The app should remain very focused, but still expose enough context and controls to feel trustworthy.

### Desktop-grade vs lightweight
The app should have desktop-quality interaction patterns without becoming IDE-heavy.

### Calm vs clear
The interface should be low-noise, but not vague.

These tensions must be resolved through:
- hierarchy
- spacing
- typography
- careful restraint

---

## 15. What Good Looks Like

A strong rRepoDocs experience feels like this:

- the user understands the active repo immediately
- the file tree is easy to scan
- the current file is obvious
- the editor starts quickly without repetitive metadata
- saving feels safe and clear
- preview feels meaningfully better for reading
- history feels contextual and useful
- the interface stays out of the way

The app should feel like a tool made by someone who respects:
- real files
- real writing
- real structure
- the user’s attention

---

## 16. Final Design Rule

When making design decisions, always ask:

> Does this make the app feel more like a calm, premium, repo-first Markdown workspace?

If yes, it probably belongs.

If it makes the app feel more like:
- a dashboard
- a CMS
- a note platform
- a generic web app
- a mini IDE

then it is probably the wrong direction.