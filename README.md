# rRepoDocs

rRepoDocs is a minimal GitHub-first Markdown editor built with Kotlin Multiplatform for Android, iOS, and Desktop.

MVP focus:
- authenticate with GitHub
- select one repository
- browse Markdown files in the repository tree
- open, edit, preview, and save Markdown files
- create, rename, and move Markdown files

## Project layout

- `composeApp/`: shared app code for Android, iOS, and Desktop targets
- `iosApp/`: Xcode entry point and iOS host app
- `docs/`: product brief, architecture, MVP plan, and implementation TODO
- `docs/MVP QA Checklist.md`: manual MVP validation checklist and smoke test flow

## Build commands

- Android debug:
  ```sh
  ./gradlew :composeApp:assembleDebug
  ```
- Desktop JVM artifact:
  ```sh
  ./gradlew :composeApp:jvmJar
  ```
- iOS framework link check:
  ```sh
  ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
  ```
- Full project verification:
  ```sh
  ./gradlew build
  ```

## Notes

- Kotlin/Native compiler daemon is disabled in `gradle.properties` to avoid an LLVM crash during iOS release linking in this environment.

## Authentication Foundation (Phase 3)

Current implementation:
- GitHub OAuth Device Flow (browser authorization with one-time code).
- OAuth app client ID is provided by app configuration (with env var fallback).
- Session token is validated via `GET /user` and persisted via `SecureSessionStorage`.

Planned follow-up:
- GitHub OAuth 2.0 Authorization Code with PKCE via system browser.
- Platform callback handling:
- Android: app link/custom scheme callback into app activity.
- iOS: URL scheme callback into app scene.
- Desktop: loopback redirect URI or custom scheme handler.

Required GitHub scopes (MVP):
- `read:user`
- `user:email`
- `repo` (required to read/write private repositories; includes public repo access)

OAuth app setup (for Device Flow):
1. Create a GitHub OAuth App in your GitHub developer settings.
2. Set `GitHubAuthConfig.defaultClientId` in [composeApp/src/commonMain/kotlin/com/rdev/rrepodocs/platform/GitHubAuthConfig.kt](/Users/robert.gustavsson/Documents/GitHub/RAG/rRepoDocs/composeApp/src/commonMain/kotlin/com/rdev/rrepodocs/platform/GitHubAuthConfig.kt).
3. Click `Connect with GitHub`, then `Open GitHub Authorization`. Approve the request in GitHub; rRepoDocs detects the approval and continues automatically.

Optional fallback:
- You can still use `RREPODOCS_GITHUB_CLIENT_ID` if you prefer not to commit a client ID in code.

Session persistence strategy:
- Android: `EncryptedSharedPreferences` backed by Android Keystore.
- iOS: `NSUserDefaults` persistence via the shared `SecureSessionStorage` abstraction.
- Desktop: JVM `Preferences` persistence via the shared `SecureSessionStorage` abstraction.
