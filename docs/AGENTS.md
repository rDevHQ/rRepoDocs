AGENTS.md

Purpose

This file gives working instructions to AI agents contributing to rRepoDocs.

The goal is to keep the project focused, simple, and consistent while development is still in its early stages.

Agents should use this file together with:
•	README.md
•	RREPODOCS_AI_AGENT_BRIEF.md
•	MVP_PLAN.md
•	ARCHITECTURE.md
•	TODO.md

If there is any conflict, prefer the narrowest interpretation of the product scope.

⸻

What rRepoDocs is

rRepoDocs is a minimal cross-platform Markdown editor for one GitHub repository at a time.

The app is for users who want to:
•	browse a repository folder tree
•	open Markdown files
•	edit Markdown
•	preview Markdown
•	save changes back to GitHub
•	create files
•	rename files
•	move files

It is intentionally small in scope.

⸻

What rRepoDocs is not

Agents must not drift the project into any of the following unless explicitly asked:
•	a general note-taking platform
•	a PKM / second-brain tool
•	an Obsidian clone
•	a full Git client
•	a code editor for all file types
•	a collaboration platform
•	an AI-first product
•	a plugin-based platform
•	an IDE-like workspace

Do not introduce features just because they are common elsewhere.

⸻

Core product rule

Before proposing or implementing any feature, ask:

Does this directly improve browsing, editing, previewing, saving, creating, renaming, or moving Markdown files in a GitHub repository?

If the answer is not clearly yes, do not add it.

⸻

Development priorities

Agents should prioritize work in this general order:
1.	project foundation
2.	app shell and screen flow
3.	GitHub authentication
4.	repository selection
5.	repository tree
6.	open/edit/preview Markdown
7.	save flow
8.	create/rename/move file operations
9.	polish and stability

Do not skip ahead into side features before the core flow works.

⸻

Platform priorities

Initial target platforms are:
•	Android
•	iOS
•	Desktop

Desktop is important.
Do not treat Desktop as a secondary afterthought.

Web is not part of the initial MVP.
Server is not part of the initial MVP.

⸻

Architecture rules

1. Keep architecture small

Do not build elaborate abstractions for a simple product.
Avoid architecture for architecture’s sake.

2. Prefer readable code

Favor straightforward code and explicit naming.
The code should be easy for a human to follow.

3. Shared logic first

Put business logic, repo logic, and UI state logic in shared code whenever practical.

4. Platform-specific code only when necessary

Use platform-specific implementations only for truly platform-dependent concerns such as secure storage, auth callbacks, or system integration.

5. UI should not talk directly to GitHub

GitHub/network logic belongs outside composables.

6. Do not split into too many modules early

Keep the project structure simple until there is a strong reason to modularize further.

⸻

UI and UX rules

1. Keep the UI calm

The interface should be quiet, clean, and readable.

2. Avoid clutter

Do not add heavy toolbars, floating actions everywhere, formatting ribbons, or complex multi-panel UIs unless clearly necessary.

3. Respect the repo tree

The repository folder tree is a central part of the app.
Keep it easy to scan and understand.

4. Editing should feel file-based

The app should feel like editing real files in a repo, not interacting with a note database.

5. Save state must be obvious

Users should be able to tell whether a document has unsaved changes.

6. Mobile and Desktop can differ in layout

Do not force identical layout patterns where they hurt usability.
Shared product behavior matters more than visual sameness.

⸻

File operation rules

Create, rename, move, and save are core product features.
Treat them as first-class operations.

Agents should ensure these flows are:
•	discoverable
•	simple
•	stable
•	consistent

Do not bury them too deeply.
Do not overcomplicate them with advanced Git workflows.

⸻

Scope control rules

Agents must avoid adding the following without explicit instruction:
•	backlinks
•	graph view
•	custom markdown extensions beyond practical needs
•	real-time collaboration
•	comments/discussions
•	pull request tooling
•	advanced branch management
•	local-first sync engine
•	support for many non-Markdown file types
•	embedded AI assistance
•	plugin marketplace or extension systems

The product wins by restraint, not breadth.

⸻

Naming rules

Use clear, explicit, English names for:
•	code
•	comments
•	files
•	variables
•	functions
•	classes
•	models

Prefer names like:
•	RepoBrowserViewModel
•	LoadRepoTreeUseCase
•	DocumentRepository
•	EditorUiState

Avoid vague names like:
•	Manager
•	Handler
•	Processor
•	Util

unless they are truly the best fit.

⸻

Code style expectations

Agents should prefer code that is:
•	small
•	readable
•	explicit
•	maintainable
•	easy to debug

Agents should avoid:
•	overly clever patterns
•	unnecessary generics
•	deep inheritance structures
•	premature optimization
•	unnecessary frameworks

When in doubt, choose the simpler implementation.

⸻

State management expectations

Important app state should stay explicit and easy to trace.
This includes at least:
•	auth state
•	selected repository
•	repository tree
•	selected file
•	editor content
•	dirty state
•	operation status for save/create/rename/move

Avoid hidden global mutable state and overly magical event systems.

⸻

GitHub integration expectations

GitHub is the source of truth.
Agents should design accordingly.

Assume the app needs to:
•	authenticate with GitHub
•	list repositories
•	load repository tree contents
•	fetch file contents
•	update file contents
•	create files
•	rename files
•	move files

Keep raw GitHub API details contained in the data layer.
Do not leak GitHub DTOs into UI code.

⸻

Markdown expectations

This app is Markdown-focused.
Agents should prioritize reliable support for practical GitHub-style Markdown over advanced customization.

At minimum, the app should work well for common Markdown usage such as:
•	headings
•	lists
•	links
•	code fences
•	blockquotes
•	tables if practical

Do not overbuild Markdown features early.

⸻

Testing expectations

Favor tests for logic that matters most, especially shared logic.
High-value test areas include:
•	repo tree mapping
•	path handling
•	editor dirty state
•	create/rename/move/save behavior
•	use case logic

Do not spend early momentum on excessive UI testing.

⸻

How agents should make decisions

When several solutions are possible:
1.	choose the smallest solution that works
2.	choose the most readable solution
3.	choose the one that preserves product focus
4.	choose the one that is easiest to maintain across platforms

Do not choose a solution just because it is more advanced.

⸻

How agents should communicate in code changes

When making changes, agents should:
•	keep changes scoped
•	avoid unrelated refactors
•	preserve existing naming conventions
•	avoid speculative architectural rewrites
•	document only what is necessary

If a needed improvement is outside current scope, leave a small note rather than silently expanding the project.

⸻

Final instruction

rRepoDocs should remain a focused, minimal, GitHub-first Markdown workspace.

Agents must protect that clarity.

When in doubt:
•	simplify
•	reduce scope
•	keep the repo/document workflow central
•	avoid turning the app into something bigger than intended