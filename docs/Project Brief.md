rRepoDocs — AI Agent Project Brief

1. Project Summary

rRepoDocs is a minimal, GitHub-first Markdown editor for a single selected repository at a time.

The app is intended for users who store Markdown files in GitHub and want a simple way to:
•	browse folders in the repository
•	open Markdown files
•	edit Markdown in raw form
•	preview rendered Markdown
•	save changes back to GitHub

This is not a general note-taking app, not a PKM tool, not a wiki platform, and not a full Git client.

The product should feel calm, focused, and technically simple.

⸻

2. Product Vision

Create the simplest good cross-platform app for working with Markdown files stored in GitHub.

Core idea:

Open a GitHub repository, browse its folder tree, open Markdown files, edit them, preview them, and save changes.

The app should remove complexity found in many existing Markdown and note apps.

⸻

3. Target Use Cases

Primary use cases:
•	personal Markdown repositories
•	recipe collections stored as .md
•	technical instructions and documentation
•	README-style docs
•	simple private or public document repositories in GitHub

The app is especially aimed at users who:
•	prefer plain files over proprietary formats
•	want GitHub as the source of truth
•	do not want a complicated knowledge-management system
•	want the same core workflow on mobile and desktop

⸻

4. Product Principles

These principles are mandatory and should guide all design and implementation decisions.

4.1 Keep it minimal

Do not add features unless they directly improve the core workflow:
•	browse repo
•	open file
•	edit Markdown
•	preview Markdown
•	save changes

4.2 Repo-first, not note-first

The repository is the workspace.
The folder structure in the repo is important and should be respected.

4.3 Markdown-first

This app is specifically for Markdown files.
It should not try to become a general-purpose code editor.

4.4 One active repo at a time

The app should focus on one currently open repository.
Support for switching repos later is acceptable, but the core UX should revolve around one active repo.

4.5 No unnecessary abstraction

Avoid overengineering.
Use straightforward architecture and naming.

4.6 Calm UI

The interface should be clean, readable, and low-noise.
No visual gimmicks, no clutter, and no feature-heavy chrome.

⸻

5. Non-Goals

The following are explicitly out of scope for the MVP unless added later by deliberate product decision:
•	plugin system
•	PKM / “second brain” features
•	graph view
•	backlinks / wiki links as a core concept
•	rich-text/WYSIWYG editor as primary mode
•	full Git client behavior
•	pull request management
•	advanced branch workflows
•	support for all file types
•	collaboration features
•	comments / discussions
•	AI features inside the app
•	sync engines beyond GitHub-based save/load
•	local database as source of truth

⸻

6. Target Platforms

This project is built with Kotlin Multiplatform.

Initial target platforms:
•	Android
•	iOS
•	Desktop

Web is not part of the initial MVP.
Server is not part of the initial MVP.

UI should be shared where practical using Compose Multiplatform.

⸻

7. Core MVP Scope

The first usable version should include only the essentials.

7.1 Authentication
•	Sign in with GitHub
•	Support private and public repositories if possible
•	Keep auth implementation as simple and secure as practical

7.2 Repository selection
•	Let the user select a repository
•	One active repository at a time

7.3 Repository tree
•	Show folder and file structure from the selected repository
•	Focus on Markdown files (.md)
•	Tree should feel fast and easy to understand

7.4 File viewing and editing
•	Open Markdown file
•	Show raw Markdown editor
•	Show Markdown preview
•	Support toggling between edit and preview, or split view where suitable

7.5 Saving changes
•	Detect unsaved changes
•	Save back to GitHub
•	Require a simple commit message
•	Show clear success/error feedback

⸻

8. User Experience Goals

Desktop

Desktop is a key platform.
Preferred layout:
•	left sidebar: repository folder tree
•	main editor area: raw Markdown editing
•	preview available either side-by-side or via toggle

The desktop UI should feel like a focused document tool.

Mobile

Mobile should preserve the same core workflow with simpler navigation:
•	file list / folder navigation first
•	tap file to open
•	switch between edit and preview cleanly

General UX requirements
•	fast startup
•	low visual noise
•	clear typography
•	simple navigation
•	obvious save state
•	no feature clutter

⸻

9. UX Rules for AI Agents

When proposing UI or UX changes, follow these rules:
1.	Prefer simplicity over cleverness.
2.	Prefer clarity over density.
3.	Do not introduce floating toolbars, heavy formatting ribbons, or complex panels unless clearly justified.
4.	The repository tree must remain easy to scan.
5.	Editing must feel like editing a file, not interacting with a note system.
6.	Preview must be readable and trustworthy.
7.	Save state must always be obvious.
8.	Avoid turning the app into an IDE.

⸻

10. Technical Direction

10.1 Architecture style

Use a simple, maintainable architecture.

Recommended direction:
•	shared domain layer
•	shared data layer
•	shared ViewModels / presentation logic where appropriate
•	platform-specific implementations only when necessary

Avoid unnecessary indirection.

10.2 Suggested layers

A reasonable structure could be:
•	domain/
•	entities
•	use cases
•	data/
•	GitHub API client
•	auth handling
•	repository tree loading
•	file content loading/saving
•	presentation/
•	screens
•	ui state
•	view models
•	platform/
•	platform-specific integrations

This structure can be adapted, but should stay clear and minimal.

10.3 State management

The app should have explicit state for:
•	signed-in status
•	selected repository
•	repository tree
•	selected file
•	file content
•	dirty/unsaved state
•	save progress and result

10.4 Offline behavior

For MVP, GitHub remains the source of truth.
A small temporary unsaved draft cache may be acceptable later, but should not complicate the first version unnecessarily.

⸻

11. Markdown Support

The MVP should support standard GitHub-flavored Markdown as well as practical preview rendering.

At minimum, support:
•	headings
•	lists
•	links
•	code fences
•	blockquotes
•	tables if practical
•	inline formatting
•	images if practical

Do not overbuild Markdown customization early.
The goal is reliable everyday Markdown editing for GitHub docs.

⸻

12. GitHub Integration Expectations

AI agents should assume GitHub integration is central.

Expected GitHub-related capabilities over time:
•	authenticate user
•	list repositories
•	load repository tree
•	fetch file contents
•	save updated file contents
•	create commits

Nice-to-have later:
•	branch selection
•	conflict awareness
•	recent repositories

But these are secondary to the main editing flow.

⸻

13. What Good Looks Like

A successful MVP should feel like this:
•	The user opens the app and signs in.
•	The user picks a repository.
•	The user sees a familiar folder tree.
•	The user opens a Markdown file.
•	The user edits without friction.
•	The user previews the result.
•	The user saves with a simple commit.
•	The app feels quiet, dependable, and useful.

If a proposed feature does not improve that flow, it should probably not be added.

⸻

14. Design Direction

Visual direction should be:
•	minimal
•	modern
•	calm
•	typography-led
•	productivity-focused

Avoid:
•	flashy gradients
•	unnecessary animation
•	decorative UI chrome
•	visual metaphors borrowed from note-taking apps

The app should feel more like a clean document workspace than a social/productivity platform.

⸻

15. Naming and Product Identity

Product name: rRepoDocs

Working description:

A minimal Markdown editor for your GitHub repository.

Alternative short internal description:

Browse, edit and preview Markdown documents in a GitHub repo.

⸻

16. Constraints for AI Agents

When generating code, architecture, UX ideas, or implementation plans, follow these constraints:
•	Use English for code, comments, variables, and technical naming.
•	Keep solutions small and understandable.
•	Do not invent extra product scope.
•	Do not propose large frameworks or abstractions unless they clearly solve a real problem.
•	Prefer pragmatic implementation over theoretically perfect architecture.
•	Favor maintainability and readability.
•	Assume the user wants step-by-step progress.
•	Treat desktop as an important platform, not an afterthought.

⸻

17. Recommended First Milestones

Milestone 1 — Project foundation
•	create clean Kotlin Multiplatform project
•	verify Android, iOS, and Desktop targets build
•	set up basic navigation and theme

Milestone 2 — GitHub authentication
•	implement sign-in flow
•	verify authenticated API access

Milestone 3 — Repository selection
•	list user repositories
•	select one repository as active

Milestone 4 — Repository tree
•	load and render folder/file tree
•	allow opening Markdown files

Milestone 5 — Editor and preview
•	raw Markdown editing
•	rendered Markdown preview
•	unsaved state handling

Milestone 6 — Save flow
•	commit message input
•	save changes back to GitHub
•	success/error states

Only after these are working well should broader features be considered.

⸻

18. Final Instruction to AI Agents

Do not try to turn rRepoDocs into a bigger product than intended.

The strength of this app is its restraint.

Every proposed feature, architectural choice, or UI change should be evaluated against this question:

Does this make it easier to browse, edit, preview, and save Markdown files in a GitHub repository?

If not, it likely does not belong in the product.