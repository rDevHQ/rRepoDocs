rRepoDocs — Architecture

1. Architecture Goal

The architecture for rRepoDocs must stay simple, understandable, and maintainable.

This is a focused product with a narrow workflow:
•	authenticate with GitHub
•	choose one repository
•	browse repository folders and Markdown files
•	create, open, edit, preview, rename, move, and save Markdown files

The architecture should support this workflow cleanly without introducing unnecessary layers, abstractions, or framework complexity.

⸻

2. Core Architectural Principles

2.1 Keep the architecture small

This project does not need enterprise layering or heavy abstractions.
Only introduce structure that clearly improves maintainability.

2.2 Prefer explicit flows

Important app state should be visible and easy to trace:
•	auth state
•	selected repository
•	repository tree
•	selected file
•	file content
•	unsaved changes
•	save/create/move/rename operation state

2.3 Shared logic first

Business logic, repository logic, and screen state logic should be shared across platforms whenever practical.

2.4 Platform-specific code only when necessary

Use platform-specific implementations only for things like:
•	secure token storage
•	platform auth handoff
•	platform window behavior
•	native integration edge cases

2.5 UI should not own data logic

The UI layer should render state and send user intents.
GitHub access, file operations, and state transitions should live outside pure composables.

⸻

3. Recommended Architecture Style

A lightweight layered approach is recommended.

Suggested layers:
•	presentation
•	domain
•	data
•	platform

This should not be treated as rigid ceremony.
It is simply a way to keep responsibilities clear.

⸻

4. Module Direction

For MVP, start with a single shared application module plus platform entry points.
Do not split into many Gradle modules too early.

A practical starting point is:
•	shared Compose/UI + application logic in one main shared module
•	platform-specific launcher modules for Android, iOS, and Desktop

Only introduce extra modules later if the codebase clearly needs them.

⸻

5. Suggested Source Structure

A clear package structure inside the shared code may look like this:

presentation/
app/
auth/
repo/
editor/
components/

domain/
model/
usecase/

data/
auth/
github/
documents/
repository/

platform/
securestorage/
browser/
logging/

This structure is a guide, not a rule.
The main goal is clear separation of concerns.

⸻

6. Responsibilities by Layer

6.1 Presentation layer

The presentation layer is responsible for:
•	screens
•	composables
•	UI state models
•	ViewModels or equivalent state holders
•	receiving user actions and triggering use cases

Examples:
•	sign-in screen
•	repository picker screen
•	repository tree sidebar
•	editor screen
•	preview rendering container
•	file action menus

Presentation should not directly call GitHub APIs.

Presentation sub-areas

presentation.app
App shell, navigation, and top-level app state routing.

presentation.auth
Auth screen and auth-related UI state.

presentation.repo
Repository selection and repository tree browsing.

presentation.editor
Document editing, preview, unsaved state, and file actions.

presentation.components
Reusable UI components shared by multiple screens.

⸻

6.2 Domain layer

The domain layer is responsible for:
•	core models
•	use cases
•	business rules
•	app-specific workflows

This layer should remain small and focused.

Examples of domain models:
•	RepositoryRef
•	RepoId
•	RepoTreeNode
•	DocumentPath
•	MarkdownDocument
•	DocumentDraft
•	OperationStatus
•	UserSession

Examples of domain use cases:
•	SignInWithGitHub
•	LoadRepositories
•	SelectRepository
•	LoadRepoTree
•	OpenDocument
•	CreateDocument
•	RenameDocument
•	MoveDocument
•	SaveDocument

Use cases should be simple orchestration units, not overdesigned classes with complex inheritance.

⸻

6.3 Data layer

The data layer is responsible for:
•	talking to GitHub
•	transforming API responses
•	storing or retrieving small local state if needed
•	implementing repository interfaces used by the domain layer

The data layer should hide GitHub API details from the rest of the app.

Suggested areas:

data.auth
•	auth session persistence
•	token retrieval
•	auth state restoration

data.github
•	GitHub API client
•	request/response mapping
•	HTTP configuration

data.repository
•	implementations of repository interfaces
•	repo listing
•	tree loading
•	file fetch/update operations

data.documents
•	document-focused operations
•	content loading
•	path-based file operations

Keep networking and mapping code contained here.

⸻

6.4 Platform layer

The platform layer is responsible for things that differ by platform.

Examples:
•	secure storage implementation
•	browser/auth redirect support
•	platform logging integration
•	platform file/share hooks later if needed

This layer should stay small.
The more logic that remains shared, the better.

⸻

7. State Management Direction

The app should use explicit state holders for key workflows.

Recommended state holders:
•	AppViewModel
•	AuthViewModel
•	RepoPickerViewModel
•	RepoBrowserViewModel
•	EditorViewModel

These can be adjusted depending on how the project evolves.

App-level state

App-level state should make it easy to determine:
•	is the user signed out?
•	is the user signed in but no repo selected?
•	is there an active repo?
•	is there an open document?

Editor state

Editor state should include at least:
•	current document path
•	original content
•	current edited content
•	dirty flag
•	preview mode / split mode
•	save status
•	rename/move/create operation status

Repo browser state

Repo browser state should include:
•	active repository
•	tree loading state
•	tree data
•	selected folder or file
•	operation refresh state after create/rename/move

⸻

8. Data Flow Direction

The intended flow should be simple:
1.	UI sends user intent.
2.	ViewModel handles intent.
3.	ViewModel calls use case.
4.	Use case calls repository/data interface.
5.	Data layer performs GitHub operation.
6.	Result is mapped back into domain/UI state.
7.	UI re-renders from updated state.

This should remain easy to follow.
Avoid event buses, hidden global mutable state, or overly abstract command systems.

⸻

9. GitHub Integration Design

GitHub is the system of record.

For MVP, the architecture should assume:
•	one authenticated user
•	one active repository at a time
•	path-based file operations
•	save actions go to GitHub directly

Important file operation note

Rename and move are conceptually simple in the product, but may be implemented as path changes under the hood.
The architecture should reflect this reality clearly.

Useful repository interfaces may include:
•	AuthRepository
•	GitHubRepositoryService
•	DocumentRepository
•	SessionRepository

Do not expose raw GitHub DTOs to presentation.
Use app-level models.

⸻

10. Error Handling

Errors should be handled in a user-friendly but technically clear way.

Principles
•	do not crash for expected operation failures
•	surface useful messages in UI
•	preserve editor state when possible
•	keep operation state explicit

Likely error areas
•	sign-in failure
•	expired session
•	repository load failure
•	file fetch failure
•	save failure
•	create/rename/move failure
•	network unavailable

AI agents should design for graceful recovery.

⸻

11. Navigation Structure

Navigation should stay simple.

Likely top-level flow:
•	sign-in
•	repository selection
•	workspace

Inside workspace:
•	repository tree
•	active document editor/preview

Avoid deep navigation complexity.
The product is a workspace tool, not a many-screen app.

⸻

12. Markdown Rendering Direction

Markdown rendering should be practical and reliable.

The architecture should separate:
•	raw text editing state
•	rendered preview generation

Preview rendering should not mutate source content.
Keep the editor content as the source of truth for the current document draft.

If rendering requires adapter code or a rendering service, place that outside the composables where practical.

⸻

13. File Operation Design

Create, rename, move, and save are core workflows.

These operations should be modeled explicitly rather than treated as ad hoc button actions.

Suggested operation modeling

A small operation result/state model may be useful, for example:
•	idle
•	loading
•	success
•	error

This can be reused for:
•	create file
•	rename file
•	move file
•	save file

Keep the pattern light and readable.

⸻

14. Local Persistence Guidance

For MVP, keep local persistence minimal.

Reasonable local persistence:
•	remembered session if secure and practical
•	last selected repository reference
•	lightweight UI preferences later if needed

Avoid creating a large local sync model or local document database in the first version.

⸻

15. Testing Direction

Tests should focus on logic that matters most.

Priority test areas:
•	path and file operation logic
•	tree mapping logic
•	rename/move behavior
•	editor dirty state logic
•	use case behavior
•	API mapping where practical

Do not overinvest in UI testing too early.
Focus on correctness in the shared logic.

⸻

16. Suggested Naming Guidelines

Use clear, boring, explicit names.

Good:
•	RepoBrowserViewModel
•	LoadRepoTreeUseCase
•	DocumentRepository
•	MoveDocumentUseCase
•	EditorUiState

Avoid:
•	vague names like Manager, Handler, Processor unless truly appropriate
•	overly generic names like DataModel
•	clever architecture branding

The code should read plainly.

⸻

17. Recommended Initial Project Structure

A practical initial structure could be:

composeApp/src/commonMain/kotlin/com/rdev/rrepodocs/
presentation/
domain/
data/
platform/

Possible early files:

presentation/app/AppViewModel.kt
presentation/app/AppUiState.kt
presentation/auth/AuthScreen.kt
presentation/auth/AuthViewModel.kt
presentation/repo/RepoPickerScreen.kt
presentation/repo/RepoBrowserScreen.kt
presentation/repo/RepoBrowserViewModel.kt
presentation/editor/EditorScreen.kt
presentation/editor/EditorViewModel.kt
presentation/editor/EditorUiState.kt

domain/model/RepositoryRef.kt
domain/model/RepoTreeNode.kt
domain/model/MarkdownDocument.kt
domain/model/DocumentPath.kt

domain/usecase/LoadRepositoriesUseCase.kt
domain/usecase/LoadRepoTreeUseCase.kt
domain/usecase/OpenDocumentUseCase.kt
domain/usecase/CreateDocumentUseCase.kt
domain/usecase/RenameDocumentUseCase.kt
domain/usecase/MoveDocumentUseCase.kt
domain/usecase/SaveDocumentUseCase.kt

data/github/GitHubApiClient.kt
data/repository/GitHubDocumentRepository.kt
data/repository/GitHubAuthRepository.kt

This is enough to start without overcommitting.

⸻

18. Future Evolution Guidance

If the app grows later, possible additions may include:
•	recent repositories
•	branch selection
•	conflict awareness
•	file deletion
•	better folder creation support
•	lightweight search

But these should only be added after the basic repository document workflow is genuinely solid.

Architecture should leave room for growth without prematurely building for everything.

⸻

19. Final Instruction to AI Agents

Do not build an elaborate architecture for a simple product.

The best architecture for rRepoDocs is one that:
•	is easy to understand
•	is easy to debug
•	keeps GitHub concerns contained
•	keeps UI state explicit
•	supports the core Markdown file workflow cleanly

If an architectural pattern adds complexity without clearly improving the core workflow, it should be rejected.