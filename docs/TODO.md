# Implementation TODO

## Multi-account GitHub support

Allow a person to keep multiple GitHub accounts on one device and switch between
them without completing the sign-in flow again.

### Scope

- Replace the single-session `SecureSessionStorage` contract with an account
  store that persists multiple accounts and one active account.
- Migrate an existing saved session into the account store on first launch.
- Keep the active account's last selected repository separately for each account.
- Update the Account menu and mobile account screen to provide:
  - a list of saved accounts with avatar and username;
  - **Add GitHub account...**;
  - **Switch GitHub account...**;
  - **Remove this account**; and
  - **Sign out of all accounts**.
- Validate a selected account's token before loading its repositories and guide
  the user to sign in again if GitHub has revoked or expired it.

### Security requirements

- Store each GitHub access token in platform-protected storage: Android Keystore,
  iOS Keychain, and the desktop operating system's credential store.
- Do not retain tokens in iOS `NSUserDefaults` or desktop JVM `Preferences`.
- Do not use the unencrypted Android fallback for access tokens; surface an error
  if protected storage cannot be initialized.

### Acceptance criteria

- A current single-account installation upgrades without requiring a new login.
- Adding a second account leaves the first account usable.
- Switching accounts restores that account and its most recently used repository.
- Removing one account does not affect other saved accounts.
- Signing out of all accounts removes all persisted tokens and account metadata.
- Android, iOS, and desktop implementations pass targeted storage, migration, and
  account-switching tests.
