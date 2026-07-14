import { describe, expect, it } from "vitest";
import { renderMarkdown, renderSharePage, shareRecordToSummary, validateCreateShareRequest, validateGitHubUser } from "../src/index";

const env = {
  MAX_MARKDOWN_BYTES: "32",
  GITHUB_API_BASE_URL: "https://example.test"
};

describe("share validation", () => {
  it("accepts a valid share payload", () => {
    const result = validateCreateShareRequest(
      {
        repoFullName: "owner/repo",
        documentPath: "docs/readme.md",
        sourceSha: "abc123",
        title: "Readme",
        markdown: "# Hello",
        expiresInDays: 30
      },
      env
    );

    expect(result.ok).toBe(true);
  });

  it("rejects oversized markdown", () => {
    const result = validateCreateShareRequest(
      {
        repoFullName: "owner/repo",
        documentPath: "docs/readme.md",
        title: "Readme",
        markdown: "x".repeat(40),
        expiresInDays: 30
      },
      env
    );

    expect(result.ok).toBe(false);
  });

  it("rejects invalid expiry", () => {
    const result = validateCreateShareRequest(
      {
        repoFullName: "owner/repo",
        documentPath: "docs/readme.md",
        title: "Readme",
        markdown: "# Hello",
        expiresInDays: 800
      },
      env
    );

    expect(result.ok).toBe(false);
  });
});

describe("markdown rendering", () => {
  it("escapes unsafe html", () => {
    expect(renderMarkdown("# <script>alert(1)</script>")).toContain("&lt;script&gt;");
  });

  it("renders basic headings and lists", () => {
    const html = renderMarkdown("# Title\n\n- One\n- Two");
    expect(html).toContain("<h1>Title</h1>");
    expect(html).toContain("<li>One</li>");
  });
});

describe("share page rendering", () => {
  it("renders document content first with attribution and expiry below", () => {
    const html = renderSharePage({
      id: "share-1",
      owner_github_user_id: "42",
      owner_login: "rDevHQ",
      repo_full_name: "owner/repo",
      document_path: "docs/chicken.md",
      source_sha: "abc123",
      title: "Hel kyckling i airfryer",
      markdown: "Start content",
      expires_at: "2026-06-02T10:00:00Z",
      created_at: "2026-05-03T10:00:00Z",
      revoked_at: null
    });

    expect(html).toContain("<article><p>Start content</p></article>");
    expect(html).toContain("<span>Shared by rDevHQ · Expires Jun 2, 2026</span>");
    expect(html).toContain('<a href="https://rdevhq.github.io">Shared with rRepoDocs</a>');
    expect(html.indexOf("<article>")).toBeLessThan(html.indexOf("<footer"));
    expect(html).not.toContain("owner/repo / docs/chicken.md");
    expect(html).not.toContain("<span>rRepoDocs</span>");
    expect(html).not.toContain("Hel kyckling i airfryer</h1>");
  });

  it("omits expiry details for a share that does not expire", () => {
    const html = renderSharePage({
      id: "share-2",
      owner_github_user_id: "42",
      owner_login: "rDevHQ",
      repo_full_name: "owner/repo",
      document_path: "docs/notes.md",
      source_sha: "abc123",
      title: "Notes",
      markdown: "Content",
      expires_at: null,
      created_at: "2026-05-03T10:00:00Z",
      revoked_at: null
    });

    expect(html).toContain("<span>Shared by rDevHQ</span>");
    expect(html).not.toContain("Expires");
  });
});

describe("GitHub validation", () => {
  it("returns null for failed GitHub responses", async () => {
    const user = await validateGitHubUser("bad-token", env, async () => new Response("no", { status: 401 }));
    expect(user).toBeNull();
  });

  it("returns user details for valid GitHub responses", async () => {
    const user = await validateGitHubUser(
      "token",
      env,
      async () => Response.json({ id: 42, login: "octo" })
    );
    expect(user).toEqual({ id: 42, login: "octo" });
  });
});

describe("share summaries", () => {
  it("includes metadata and configured share URL", () => {
    const summary = shareRecordToSummary(
      {
        id: "share-1",
        repo_full_name: "owner/repo",
        document_path: "docs/readme.md",
        source_sha: "abc123",
        title: "Readme",
        expires_at: null,
        created_at: "2026-04-26T10:00:00Z",
        revoked_at: null
      },
      new Request("https://request-origin.test/api/shares"),
      { PUBLIC_BASE_URL: "https://shares.example.test" }
    );

    expect(summary).toEqual({
      id: "share-1",
      shareUrl: "https://shares.example.test/s/share-1",
      repoFullName: "owner/repo",
      documentPath: "docs/readme.md",
      sourceSha: "abc123",
      title: "Readme",
      expiresAt: null,
      createdAt: "2026-04-26T10:00:00Z",
      revokedAt: null
    });
  });
});
