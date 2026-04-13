rRepoDocs — MVP Plan

1. MVP Goal

Build the first usable version of rRepoDocs, a minimal cross-platform app for working with Markdown files in a single GitHub repository at a time.

The MVP must support the full basic document workflow:
•	sign in to GitHub
•	choose one repository
•	browse folder structure
•	open Markdown files
•	create Markdown files
•	edit Markdown files
•	preview rendered Markdown
•	rename files
•	move files
•	save changes back to GitHub

The app should stay intentionally small, calm, and easy to understand.

⸻

2. MVP Feature Scope

Included
•	GitHub authentication
•	Repository selection
•	One active repository at a time
•	Folder/file tree view
•	Open Markdown files
•	Create new Markdown files
•	Edit Markdown files
•	Preview Markdown
•	Rename files
•	Move files
•	Save file changes back to GitHub
•	Basic commit flow
•	Clear loading/saving/error states

Not included in MVP
•	Web target
•	Server backend
•	Multiple active repositories at once
•	Pull requests
•	Advanced branching workflows
•	Conflict resolution UI
•	Rich text editor
•	Plugin system
•	AI features inside the app
•	Complex offline sync

⸻

3. Important Product Clarification

This app is not only for editing existing files. It must also support basic repository document management.

That means the user should be able to:
•	create new Markdown files
•	rename files
•	move files between folders

These operations are part of the core product and should be treated as first-class functionality, not optional extras.

⸻

4. Recommended Build Order

Milestone 1 — Project foundation

Goal

Set up a clean Kotlin Multiplatform base that builds for Android, iOS, and Desktop.

Tasks
•	verify project builds on all selected targets
•	define basic package structure
•	create app theme and typography baseline
•	set up navigation shell
•	create placeholder screens:
•	sign-in screen
•	repository picker screen
•	file browser/editor screen

Done when
•	app launches successfully on Desktop
•	app launches successfully on Android
•	iOS target is configured and can build
•	placeholder navigation works

⸻

Milestone 2 — GitHub authentication

Goal

Allow the user to authenticate with GitHub.

Tasks
•	choose authentication approach suitable for mobile and desktop
•	implement sign-in flow
•	store auth state securely per platform where needed
•	verify authenticated requests work

Done when
•	user can sign in
•	app can call GitHub APIs using authenticated session
•	sign-in state survives app restart if appropriate

⸻

Milestone 3 — Repository selection

Goal

Let the user choose one repository to work with.

Tasks
•	fetch accessible repositories
•	display repository list simply
•	allow selecting one active repository
•	store current repository in app state
•	support reopening last selected repository later if practical

Done when
•	user can pick a repository
•	selected repo becomes the active workspace

⸻

Milestone 4 — Repository tree

Goal

Render the selected repository as a navigable folder/file tree.

Tasks
•	fetch repository contents
•	build folder/file tree model
•	show folders and files clearly
•	focus on .md files for opening/editing
•	decide how to treat non-Markdown files in MVP:
•	hide them completely, or
•	show them but only allow Markdown editing

Done when
•	user can browse folders
•	user can open supported Markdown files
•	tree feels understandable and stable

⸻

Milestone 5 — Open, edit, and preview Markdown

Goal

Make the core editor workflow feel good.

Tasks
•	load file contents
•	show raw Markdown editor
•	show rendered Markdown preview
•	support edit/preview toggle and desktop split view if practical
•	track dirty state
•	warn or handle navigation away from unsaved file

Done when
•	user can open a Markdown file
•	user can edit it
•	user can preview it
•	app clearly shows unsaved changes

⸻

Milestone 6 — Save and commit changes

Goal

Let the user save file edits back to GitHub.

Tasks
•	implement save flow for modified files
•	require or generate simple commit message
•	send update to GitHub
•	show success/failure states clearly
•	refresh local state after save

Done when
•	edited file can be saved to GitHub
•	commit completes successfully
•	UI reflects clean state after save

⸻

Milestone 7 — Create new files

Goal

Allow users to create new Markdown files inside the active repository.

Tasks
•	add “new file” action
•	let user choose folder location
•	enter file name
•	ensure .md extension handling is clear
•	create empty or starter Markdown content
•	save as new file through GitHub

Done when
•	user can create a new Markdown file in a selected folder
•	new file appears in the tree
•	file opens for editing immediately after creation if appropriate

⸻

Milestone 8 — Rename files

Goal

Allow users to rename Markdown files cleanly.

Tasks
•	add rename action from file context menu or equivalent
•	handle filename validation
•	preserve file content
•	update repository tree after rename
•	communicate this clearly in commit flow

Done when
•	user can rename a file
•	renamed file appears correctly in the tree
•	old path is no longer shown

⸻

Milestone 9 — Move files

Goal

Allow users to move Markdown files to another folder in the repo.

Tasks
•	define simple move UX
•	allow choosing destination folder
•	perform file move via GitHub-backed path change approach
•	refresh tree after move
•	ensure user feedback is clear

Done when
•	user can move a file to another folder
•	tree updates correctly
•	moved file can still be opened and edited

⸻

5. Suggested MVP UX Shape

Desktop

Main layout should be simple and document-focused:
•	left sidebar: repository tree
•	main pane: editor
•	preview: side-by-side or toggle

Possible top-level actions:
•	open repo
•	new file
•	rename
•	move
•	save

Avoid clutter.

Mobile

Keep navigation simpler:
•	screen 1: repository tree
•	screen 2: file editor
•	toggle between edit and preview
•	file actions available through a simple menu

Avoid dense toolbars.

⸻

6. File Operations UX Guidance

These operations should be easy to discover but not noisy.

Good candidates for file actions
•	toolbar button for New file
•	context menu / overflow menu for:
•	Rename
•	Move
•	Delete later, if added after MVP

UX rule

Do not bury essential repo-document actions so deeply that the app becomes annoying.
But do not turn the UI into a file manager either.

Keep actions:
•	visible when needed
•	hidden when not needed
•	consistent across platforms

⸻

7. Technical Notes for AI Agents

Repository operations to support

AI agents should treat these as core operations:
•	list repositories
•	load tree contents
•	fetch file contents
•	update file contents
•	create file
•	rename file
•	move file

Implementation caution

GitHub file operations are path-based, so rename and move may effectively behave like creating the file at a new path and removing the old path.
AI agents should account for that carefully.

Data model ideas

Useful shared models may include:
•	RepositoryRef
•	RepoTreeNode
•	MarkdownDocument
•	DocumentPath
•	DocumentEditState
•	SaveResult
•	RepoOperationResult

Keep these models small and clear.

⸻

8. Suggested Immediate Next Tasks

The next concrete implementation tasks should be:
1.	finalize package naming and project naming
2.	verify Android/Desktop/iOS targets build
3.	create basic screen structure
4.	decide auth approach for GitHub
5.	implement repository list fetch
6.	implement tree model for repo contents

Only after that should the agent move into editor and file management details.

⸻

9. Definition of a Good MVP

A good MVP is not feature-rich.
A good MVP feels dependable.

The app should let a user:
•	sign in
•	open a repo
•	browse files
•	create a markdown file
•	open a markdown file
•	edit and preview it
•	rename or move it
•	save changes cleanly

If that experience feels fast, calm, and reliable, the MVP is successful.

⸻

10. Final Direction for AI Agents

Do not bloat the first version.

The product is strongest when it stays focused on repository-based Markdown work.

When in doubt, prioritize:
•	fewer features
•	clearer UX
•	simpler architecture
•	better file workflow

The app should feel like a quiet, capable Markdown workspace for GitHub repos.