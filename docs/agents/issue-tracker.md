# Issue tracker: GitHub

Issues and specs for this repo live in GitHub Issues for `rDevHQ/rRepoDocs`.
Use the `gh` CLI for all operations.

## Conventions

Run commands inside this clone so `gh` infers the repository from its remote.
When operating elsewhere, pass `--repo rDevHQ/rRepoDocs`.

- Create: `gh issue create --title "..." --body-file <path>`.
  Use a Markdown file or `--body-file -` with a quoted heredoc for multiline bodies.
- Read: `gh issue view <number> --json number,title,body,labels,comments`.
- List: `gh issue list --state open --json number,title,body,labels`.
  Add `--label` and `--state` filters as appropriate.
- Comment: `gh issue comment <number> --body "..."`.
- Apply/remove labels: `gh issue edit <number> --add-label "..."`
  or `--remove-label "..."`.
- Close: `gh issue close <number> --comment "..."`.

Use the label vocabulary in `docs/agents/triage-labels.md`.

## Parent tickets and dependencies

Prefer GitHub's native sub-issues and issue dependencies when available.
The REST API uses numeric database IDs, not issue numbers or node IDs.

- Get an issue's database ID:
  `gh api repos/rDevHQ/rRepoDocs/issues/<number> --jq .id`.
- Attach a child:
  `gh api --method POST repos/rDevHQ/rRepoDocs/issues/<parent>/sub_issues -F sub_issue_id=<child-db-id>`.
- Add a blocker:
  `gh api --method POST repos/rDevHQ/rRepoDocs/issues/<child>/dependencies/blocked_by -F issue_id=<blocker-db-id>`.

If these features are unavailable, put `Part of #<parent>` and
`Blocked by: #<number>, ...` at the top of the child body, and link children
in a task list in the parent. A ticket is unblocked when every blocker is closed.

## Pull requests as a triage surface

**PRs as a request surface: no.**

## Skill terminology

- "Publish to the issue tracker": create a GitHub issue.
- "Fetch the relevant ticket": read the GitHub issue.
