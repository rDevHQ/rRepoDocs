rRepoDocs — TODO

Current goal

Set up the project cleanly and get the first end-to-end foundation in place for rRepoDocs.

This TODO is intentionally practical and ordered. It should help an AI agent or developer work through the project step by step without adding unnecessary scope.

⸻

Phase 1 — Clean project foundation

1.1 Rename and verify project identity
•	Set project name to rRepoDocs
•	Set package/base namespace to com.rdev.rrepodocs
•	Set Android application ID appropriately
•	Check generated iOS and Desktop names for consistency
•	Update any placeholder com.example values
•	Update README project title and summary

1.2 Clean generated boilerplate
•	Remove demo/sample UI code from generated project
•	Remove placeholder screens and sample composables
•	Remove unused resources created by template
•	Remove unused strings, icons, colors, and sample preview code
•	Verify project still builds after cleanup

1.3 Set up shared package structure
•	Create base package folders under shared/common code:
•	presentation/app
•	presentation/auth
•	presentation/repo
•	presentation/editor
•	presentation/components
•	domain/model
•	domain/usecase
•	data/auth
•	data/github
•	data/repository
•	platform
•	Verify package naming stays consistent and simple

1.4 Confirm targets build
•	Build Desktop target
•	Build Android target
•	Verify iOS target is configured correctly
•	Fix any generated template issues before continuing

⸻

Phase 2 — Basic app shell

2.1 Create top-level app state
•	Create AppUiState
•	Create AppViewModel
•	Define simple top-level app modes:
•	signed out
•	repository selection
•	workspace

2.2 Create top-level screens
•	Create AuthScreen
•	Create RepoPickerScreen
•	Create WorkspaceScreen
•	Route between them from one central app entry point

2.3 Add minimal app theme
•	Define basic typography
•	Define basic spacing rules
•	Define minimal color scheme
•	Keep styling clean and restrained

2.4 First workspace layout
•	Desktop: left sidebar + main area layout shell
•	Mobile: simple file-list-first layout shell
•	Add placeholders for:
•	repo tree
•	editor area
•	preview area

⸻

Phase 3 — GitHub authentication foundation

3.1 Decide auth approach
•	Choose the simplest viable GitHub auth flow for Android, iOS, and Desktop
•	Document the chosen approach briefly in README or notes
•	Identify required scopes/permissions

3.2 Build auth abstraction
•	Define auth-related models:
•	UserSession
•	AuthState
•	Define AuthRepository interface
•	Create placeholder implementation first

3.3 Implement sign-in flow skeleton
•	Add sign-in button to AuthScreen
•	Wire sign-in intent to AuthViewModel
•	Mock successful sign-in temporarily if needed
•	Flow into repository picker after successful sign-in

3.4 Session persistence basics
•	Decide where session/token should be stored per platform
•	Add minimal secure persistence abstraction
•	Restore session on app startup if possible

⸻

Phase 4 — Repository listing

4.1 Create GitHub API client foundation
•	Create GitHubApiClient
•	Add authenticated request support
•	Add basic error handling
•	Keep API code isolated from UI

4.2 Load repositories
•	Define RepositoryRef model
•	Create LoadRepositoriesUseCase
•	Implement GitHubRepositoryService or equivalent
•	Fetch accessible repositories from GitHub
•	Map raw API responses to app models

4.3 Repository picker UI
•	Show repositories in simple list form
•	Allow one repository to be selected
•	Move to workspace after selection
•	Store active repository in app state

⸻

Phase 5 — Repository tree

5.1 Define tree model
•	Create RepoTreeNode
•	Support folder and file node types
•	Store relevant path information cleanly

5.2 Load tree contents
•	Create LoadRepoTreeUseCase
•	Implement tree loading from selected repository
•	Decide MVP handling of non-Markdown files:
•	hide them, or
•	show them but disable editing

5.3 Render tree UI
•	Desktop: render tree in left sidebar
•	Mobile: render navigable list/tree view
•	Allow folder expansion/collapse where needed
•	Allow tapping/clicking Markdown file to open

5.4 Refresh behavior
•	Define when tree reloads
•	Refresh tree after create/rename/move operations

⸻

Phase 6 — Open and edit Markdown documents

6.1 Define document models
•	Create DocumentPath
•	Create MarkdownDocument
•	Create DocumentEditState

6.2 Open document flow
•	Create OpenDocumentUseCase
•	Load file contents from GitHub
•	Show current file in editor state

6.3 Editor UI
•	Build raw Markdown text editor view
•	Ensure typing/editing works smoothly
•	Show active file name/path clearly

6.4 Dirty state tracking
•	Compare original vs current content
•	Show unsaved/dirty state clearly
•	Prevent accidental silent loss of edits

⸻

Phase 7 — Markdown preview

7.1 Preview rendering strategy
•	Choose practical Markdown rendering approach
•	Keep rendering logic separate from editor state

7.2 Preview UI
•	Desktop: support split view or toggle view
•	Mobile: support edit/preview toggle
•	Ensure rendered content is readable and calm

7.3 Preview correctness
•	Verify common Markdown features render acceptably:
•	headings
•	lists
•	links
•	code fences
•	blockquotes
•	tables if supported in MVP

⸻

Phase 8 — Save changes

8.1 Save use case
•	Create SaveDocumentUseCase
•	Implement GitHub file update flow
•	Keep GitHub path/content details out of UI layer

8.2 Save UI
•	Add Save action
•	Add commit message input or lightweight dialog
•	Show saving state
•	Show success/error feedback

8.3 Post-save behavior
•	Refresh editor original content after save
•	Clear dirty state after successful save
•	Handle save failure without losing edits

⸻

Phase 9 — Create new Markdown files

9.1 Define create flow
•	Create CreateDocumentUseCase
•	Decide UX for new file creation
•	Allow choosing target folder
•	Allow entering file name
•	Handle .md extension clearly

9.2 Create UI
•	Add New file action
•	Add simple create-file dialog/sheet
•	Validate file name

9.3 Post-create behavior
•	Refresh tree
•	Open newly created file automatically if appropriate
•	Put cursor in editor ready for writing

⸻

Phase 10 — Rename files

10.1 Define rename flow
•	Create RenameDocumentUseCase
•	Decide rename interaction model
•	Validate new file name

10.2 Rename UI
•	Add rename action in file menu/context menu
•	Add rename dialog/sheet
•	Make rename easy but not noisy

10.3 Post-rename behavior
•	Refresh tree
•	Update active editor path if renamed file is open
•	Preserve content and editing state where possible

⸻

Phase 11 — Move files

11.1 Define move flow
•	Create MoveDocumentUseCase
•	Decide how destination folder selection should work
•	Keep move UX simple

11.2 Move UI
•	Add move action in file menu/context menu
•	Add destination folder picker
•	Confirm move clearly

11.3 Post-move behavior
•	Refresh tree
•	Update active editor path if moved file is open
•	Preserve content and editing state where possible

⸻

Phase 12 — Cleanup and polish for first usable version

12.1 Error handling polish
•	Improve user-facing errors for auth/repo/file/save failures
•	Avoid technical noise where not needed
•	Keep enough clarity to debug issues

12.2 Loading state polish
•	Add loading indicators where necessary
•	Avoid overusing spinners
•	Make transitions feel stable

12.3 Basic persistence polish
•	Restore last selected repository if appropriate
•	Restore session if appropriate

12.4 UI cleanup
•	Remove any leftover debug text
•	Tighten spacing and hierarchy
•	Keep the UI minimal and calm

⸻

Nice-to-have after MVP
•	Delete file
•	Create folders
•	Recent repositories
•	Search by file name
•	Branch selection
•	Better conflict awareness
•	Keyboard shortcuts on Desktop

⸻

Rules while implementing
•	Do not add unnecessary features
•	Do not turn the app into a general IDE
•	Do not overengineer the architecture
•	Keep repo/document workflow central
•	Prefer explicit, readable code
•	Keep UI clean and low-noise

⸻

Definition of success for the first usable version
•	User can sign in
•	User can pick a repository
•	User can browse Markdown files
•	User can open a file
•	User can edit and preview it
•	User can create a new Markdown file
•	User can rename a file
•	User can move a file
•	User can save changes back to GitHub
•	The app feels simple, calm, and dependable