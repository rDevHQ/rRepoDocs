const DefaultExpiryDays = 30;
const MaxExpiryDays = 365;
const DefaultMaxMarkdownBytes = 256 * 1024;

type GitHubUser = {
  id: number;
  login: string;
};

type CreateShareRequest = {
  repoFullName?: unknown;
  documentPath?: unknown;
  sourceSha?: unknown;
  title?: unknown;
  markdown?: unknown;
  expiresInDays?: unknown;
};

type ShareRecord = {
  id: string;
  owner_github_user_id: string;
  owner_login: string;
  repo_full_name: string;
  document_path: string;
  source_sha: string | null;
  title: string;
  markdown: string;
  expires_at: string | null;
  created_at: string;
  revoked_at: string | null;
};

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    return handleRequest(request, env, ctx);
  }
};

export async function handleRequest(
  request: Request,
  env: Env,
  _ctx?: ExecutionContext,
  fetcher: typeof fetch = fetch
): Promise<Response> {
  const url = new URL(request.url);

  try {
    if (request.method === "GET" && url.pathname === "/api/shares") {
      return listShares(request, env, fetcher);
    }

    if (request.method === "POST" && url.pathname === "/api/shares") {
      return createShare(request, env, fetcher);
    }

    const deleteMatch = url.pathname.match(/^\/api\/shares\/([A-Za-z0-9_-]+)$/);
    if (request.method === "DELETE" && deleteMatch?.[1]) {
      return revokeShare(request, env, fetcher, deleteMatch[1]);
    }

    const shareMatch = url.pathname.match(/^\/s\/([A-Za-z0-9_-]+)$/);
    if (request.method === "GET" && shareMatch?.[1]) {
      return renderShare(env, shareMatch[1]);
    }

    return jsonResponse({ error: "not_found", message: "Route not found." }, 404);
  } catch (error) {
    console.error(JSON.stringify({ level: "error", message: "Unhandled Worker error", error: errorMessage(error) }));
    return jsonResponse({ error: "internal_error", message: "Unable to handle this request." }, 500);
  }
}

async function listShares(
  request: Request,
  env: Env,
  fetcher: typeof fetch
): Promise<Response> {
  const accessToken = bearerToken(request);
  if (accessToken == null) {
    return jsonResponse({ error: "unauthorized", message: "Missing bearer token." }, 401);
  }

  const user = await validateGitHubUser(accessToken, env, fetcher);
  if (user == null) {
    return jsonResponse({ error: "unauthorized", message: "GitHub token could not be validated." }, 401);
  }

  const now = new Date().toISOString();
  const result = await env.DB.prepare(
    `SELECT
      id,
      owner_github_user_id,
      owner_login,
      repo_full_name,
      document_path,
      source_sha,
      title,
      markdown,
      expires_at,
      created_at,
      revoked_at
    FROM document_shares
    WHERE owner_github_user_id = ?
      AND revoked_at IS NULL
      AND (expires_at IS NULL OR expires_at > ?)
    ORDER BY created_at DESC
    LIMIT 100`
  )
    .bind(String(user.id), now)
    .all<ShareRecord>();

  return jsonResponse(
    {
      shares: (result.results ?? []).map((share) => shareRecordToSummary(share, request, env))
    },
    200
  );
}

async function createShare(
  request: Request,
  env: Env,
  fetcher: typeof fetch
): Promise<Response> {
  const accessToken = bearerToken(request);
  if (accessToken == null) {
    return jsonResponse({ error: "unauthorized", message: "Missing bearer token." }, 401);
  }

  const user = await validateGitHubUser(accessToken, env, fetcher);
  if (user == null) {
    return jsonResponse({ error: "unauthorized", message: "GitHub token could not be validated." }, 401);
  }

  const body = await parseJson<CreateShareRequest>(request);
  if (body == null) {
    return jsonResponse({ error: "invalid_json", message: "Request body must be valid JSON." }, 400);
  }

  const validation = validateCreateShareRequest(body, env);
  if (!validation.ok) {
    return jsonResponse({ error: "invalid_request", message: validation.message }, 400);
  }

  const id = createShareId();
  const createdAt = new Date();
  const expiresAt = expiryDate(createdAt, validation.expiresInDays, env);

  await env.DB.prepare(
    `INSERT INTO document_shares (
      id,
      owner_github_user_id,
      owner_login,
      repo_full_name,
      document_path,
      source_sha,
      title,
      markdown,
      expires_at,
      created_at,
      revoked_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL)`
  )
    .bind(
      id,
      String(user.id),
      user.login,
      validation.repoFullName,
      validation.documentPath,
      validation.sourceSha,
      validation.title,
      validation.markdown,
      expiresAt?.toISOString() ?? null,
      createdAt.toISOString()
    )
    .run();

  const shareUrl = `${publicBaseUrl(env, request)}/s/${id}`;
  return jsonResponse(
    {
      id,
      shareUrl,
      expiresAt: expiresAt?.toISOString() ?? null,
      createdAt: createdAt.toISOString()
    },
    201
  );
}

async function revokeShare(
  request: Request,
  env: Env,
  fetcher: typeof fetch,
  id: string
): Promise<Response> {
  const accessToken = bearerToken(request);
  if (accessToken == null) {
    return jsonResponse({ error: "unauthorized", message: "Missing bearer token." }, 401);
  }

  const user = await validateGitHubUser(accessToken, env, fetcher);
  if (user == null) {
    return jsonResponse({ error: "unauthorized", message: "GitHub token could not be validated." }, 401);
  }

  const existing = await env.DB.prepare(
    "SELECT owner_github_user_id, revoked_at FROM document_shares WHERE id = ?"
  )
    .bind(id)
    .first<{ owner_github_user_id: string; revoked_at: string | null }>();

  if (existing == null) {
    return jsonResponse({ error: "not_found", message: "Share not found." }, 404);
  }
  if (existing.owner_github_user_id !== String(user.id)) {
    return jsonResponse({ error: "forbidden", message: "Only the share owner can revoke this link." }, 403);
  }
  if (existing.revoked_at != null) {
    return jsonResponse({ id, revokedAt: existing.revoked_at }, 200);
  }

  const revokedAt = new Date().toISOString();
  await env.DB.prepare("UPDATE document_shares SET revoked_at = ? WHERE id = ?")
    .bind(revokedAt, id)
    .run();

  return jsonResponse({ id, revokedAt }, 200);
}

async function renderShare(env: Env, id: string): Promise<Response> {
  const share = await env.DB.prepare(
    `SELECT
      id,
      owner_github_user_id,
      owner_login,
      repo_full_name,
      document_path,
      source_sha,
      title,
      markdown,
      expires_at,
      created_at,
      revoked_at
    FROM document_shares
    WHERE id = ?`
  )
    .bind(id)
    .first<ShareRecord>();

  if (share == null || share.revoked_at != null || isExpired(share.expires_at)) {
    return new Response(renderMessagePage("Share unavailable", "This document share was not found, expired, or revoked."), {
      status: 404,
      headers: htmlHeaders()
    });
  }

  return new Response(renderSharePage(share), {
    status: 200,
    headers: htmlHeaders()
  });
}

export async function validateGitHubUser(
  accessToken: string,
  env: Pick<Env, "GITHUB_API_BASE_URL">,
  fetcher: typeof fetch = fetch
): Promise<GitHubUser | null> {
  const response = await fetcher(`${env.GITHUB_API_BASE_URL.replace(/\/+$/, "")}/user`, {
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Accept": "application/vnd.github+json",
      "X-GitHub-Api-Version": "2022-11-28",
      "User-Agent": "rRepoDocs Share Worker"
    }
  });

  if (!response.ok) return null;
  const payload = await response.json<Partial<GitHubUser>>();
  if (typeof payload.id !== "number" || typeof payload.login !== "string" || payload.login.trim() === "") {
    return null;
  }
  return { id: payload.id, login: payload.login };
}

export function shareRecordToSummary(
  share: Pick<ShareRecord, "id" | "repo_full_name" | "document_path" | "source_sha" | "title" | "expires_at" | "created_at" | "revoked_at">,
  request: Request,
  env: Pick<Env, "PUBLIC_BASE_URL">
): Record<string, string | null> {
  return {
    id: share.id,
    shareUrl: `${publicBaseUrl(env, request)}/s/${share.id}`,
    repoFullName: share.repo_full_name,
    documentPath: share.document_path,
    sourceSha: share.source_sha,
    title: share.title,
    expiresAt: share.expires_at,
    createdAt: share.created_at,
    revokedAt: share.revoked_at
  };
}

export function validateCreateShareRequest(
  body: CreateShareRequest,
  env: Pick<Env, "MAX_MARKDOWN_BYTES">
):
  | {
      ok: true;
      repoFullName: string;
      documentPath: string;
      sourceSha: string | null;
      title: string;
      markdown: string;
      expiresInDays: number | null | undefined;
    }
  | { ok: false; message: string } {
  const repoFullName = stringField(body.repoFullName).trim();
  const documentPath = stringField(body.documentPath).trim();
  const title = stringField(body.title).trim();
  const markdown = stringField(body.markdown);
  const sourceSha = optionalStringField(body.sourceSha)?.trim() || null;
  const expiresInDays = normalizeExpiryDays(body.expiresInDays);

  if (repoFullName.length === 0 || repoFullName.length > 160 || !repoFullName.includes("/")) {
    return { ok: false, message: "Repository full name is required." };
  }
  if (documentPath.length === 0 || documentPath.length > 512 || documentPath.startsWith("/")) {
    return { ok: false, message: "Document path is required." };
  }
  if (title.length === 0 || title.length > 180) {
    return { ok: false, message: "Title is required." };
  }
  if (byteLength(markdown) > maxMarkdownBytes(env)) {
    return { ok: false, message: "Markdown content is too large to share." };
  }
  if (typeof expiresInDays === "number" && (expiresInDays < 1 || expiresInDays > MaxExpiryDays)) {
    return { ok: false, message: "Expiry must be between 1 and 365 days, or null." };
  }

  return {
    ok: true,
    repoFullName,
    documentPath,
    sourceSha,
    title,
    markdown,
    expiresInDays
  };
}

function bearerToken(request: Request): string | null {
  const authorization = request.headers.get("Authorization") ?? "";
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  return match?.[1]?.trim() || null;
}

async function parseJson<T>(request: Request): Promise<T | null> {
  try {
    return await request.json<T>();
  } catch {
    return null;
  }
}

function createShareId(): string {
  const bytes = new Uint8Array(18);
  crypto.getRandomValues(bytes);
  return base64Url(bytes);
}

function base64Url(bytes: Uint8Array): string {
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

function expiryDate(createdAt: Date, expiresInDays: number | null | undefined, env: Env): Date | null {
  if (expiresInDays === null) return null;
  const days = expiresInDays ?? defaultExpiryDays(env);
  return new Date(createdAt.getTime() + days * 24 * 60 * 60 * 1000);
}

function defaultExpiryDays(env: Pick<Env, "DEFAULT_EXPIRY_DAYS">): number {
  const parsed = Number.parseInt(env.DEFAULT_EXPIRY_DAYS, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : DefaultExpiryDays;
}

function normalizeExpiryDays(value: unknown): number | null | undefined {
  if (value === undefined) return undefined;
  if (value === null) return null;
  if (typeof value === "number" && Number.isInteger(value)) return value;
  return -1;
}

function stringField(value: unknown): string {
  return typeof value === "string" ? value : "";
}

function optionalStringField(value: unknown): string | null {
  return typeof value === "string" ? value : null;
}

function maxMarkdownBytes(env: Pick<Env, "MAX_MARKDOWN_BYTES">): number {
  const parsed = Number.parseInt(env.MAX_MARKDOWN_BYTES, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : DefaultMaxMarkdownBytes;
}

function byteLength(value: string): number {
  return new TextEncoder().encode(value).length;
}

function isExpired(expiresAt: string | null): boolean {
  return expiresAt != null && Date.parse(expiresAt) <= Date.now();
}

function publicBaseUrl(env: Pick<Env, "PUBLIC_BASE_URL">, request: Request): string {
  const configured = env.PUBLIC_BASE_URL.trim().replace(/\/+$/, "");
  if (isNotBlankLocalhost(configured)) return configured;
  return new URL(request.url).origin;
}

function isNotBlankLocalhost(value: string): boolean {
  return value !== "" &&
    value !== "http://127.0.0.1:8787" &&
    value !== "http://localhost:8787";
}

function jsonResponse(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "no-store"
    }
  });
}

function htmlHeaders(): HeadersInit {
  return {
    "Content-Type": "text/html; charset=utf-8",
    "Cache-Control": "public, max-age=120"
  };
}

export function renderSharePage(share: ShareRecord): string {
  const renderedMarkdown = renderMarkdown(share.markdown);
  const shareMeta = [
    `Shared by ${share.owner_login}`,
    share.expires_at == null ? null : `Expires ${formatDate(share.expires_at)}`
  ].filter((part): part is string => part != null).join(" · ");
  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${escapeHtml(share.title)} - rRepoDocs</title>
  <style>${pageCss()}</style>
</head>
<body>
  <main>
    <article>${renderedMarkdown}</article>
    <footer class="share-meta">
      <a href="https://rdevhq.github.io">Shared with rRepoDocs</a>
      <div>${escapeHtml(shareMeta)}</div>
    </footer>
  </main>
</body>
</html>`;
}

function renderMessagePage(title: string, message: string): string {
  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>${escapeHtml(title)} - rRepoDocs</title>
  <style>${pageCss()}</style>
</head>
<body>
  <main>
    <header>
      <div class="brand">rRepoDocs</div>
      <h1>${escapeHtml(title)}</h1>
      <p>${escapeHtml(message)}</p>
    </header>
  </main>
</body>
</html>`;
}

export function renderMarkdown(markdown: string): string {
  const blocks: string[] = [];
  const lines = markdown.replace(/\r\n/g, "\n").split("\n");
  let index = 0;

  while (index < lines.length) {
    const line = lines[index] ?? "";
    if (line.trim() === "") {
      index += 1;
      continue;
    }

    const fenceMatch = line.match(/^```(\S*)\s*$/);
    if (fenceMatch) {
      const language = fenceMatch[1] ?? "";
      const codeLines: string[] = [];
      index += 1;
      while (index < lines.length && !(lines[index] ?? "").startsWith("```")) {
        codeLines.push(lines[index] ?? "");
        index += 1;
      }
      if (index < lines.length) index += 1;
      const languageLabel = language ? `<div class="code-lang">${escapeHtml(language)}</div>` : "";
      blocks.push(`${languageLabel}<pre><code>${escapeHtml(codeLines.join("\n"))}</code></pre>`);
      continue;
    }

    const headingMatch = line.match(/^(#{1,6})\s+(.+)$/);
    if (headingMatch) {
      const level = headingMatch[1]?.length ?? 1;
      blocks.push(`<h${level}>${renderInline(headingMatch[2] ?? "")}</h${level}>`);
      index += 1;
      continue;
    }

    if (/^\s*[-*]\s+/.test(line)) {
      const items: string[] = [];
      while (index < lines.length && /^\s*[-*]\s+/.test(lines[index] ?? "")) {
        items.push((lines[index] ?? "").replace(/^\s*[-*]\s+/, ""));
        index += 1;
      }
      blocks.push(`<ul>${items.map((item) => `<li>${renderInline(item)}</li>`).join("")}</ul>`);
      continue;
    }

    const paragraphLines: string[] = [];
    while (
      index < lines.length &&
      (lines[index] ?? "").trim() !== "" &&
      !/^(#{1,6})\s+/.test(lines[index] ?? "") &&
      !/^\s*[-*]\s+/.test(lines[index] ?? "") &&
      !/^```/.test(lines[index] ?? "")
    ) {
      paragraphLines.push((lines[index] ?? "").trim());
      index += 1;
    }
    blocks.push(`<p>${renderInline(paragraphLines.join(" "))}</p>`);
  }

  return blocks.join("\n");
}

function renderInline(value: string): string {
  let escaped = escapeHtml(value);
  escaped = escaped.replace(/`([^`]+)`/g, "<code>$1</code>");
  escaped = escaped.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
  escaped = escaped.replace(/\*([^*]+)\*/g, "<em>$1</em>");
  escaped = escaped.replace(/\[([^\]]+)\]\((https?:\/\/[^)\s]+)\)/g, '<a href="$2" rel="nofollow noopener noreferrer">$1</a>');
  return escaped;
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function formatDate(value: string): string {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString("en-US", { year: "numeric", month: "short", day: "numeric", timeZone: "UTC" });
}

function pageCss(): string {
  return `
:root { color-scheme: light; font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: #f8f7f4; color: #1d1c1a; }
body { margin: 0; }
main { width: min(760px, calc(100vw - 32px)); margin: 0 auto; padding: 48px 0 68px; }
.brand { display: inline-flex; align-items: center; gap: 8px; color: #4f4a43; font-size: 13px; font-weight: 700; letter-spacing: .02em; }
.brand-mark { width: 15px; height: 15px; border-radius: 4px; background: #1d1c1a; box-shadow: inset 0 0 0 4px #d8c7a5; }
h1 { font-family: Georgia, "Times New Roman", serif; font-size: clamp(38px, 7vw, 64px); line-height: 1.02; margin: 22px 0 0; font-weight: 600; }
h2, h3, h4, h5, h6 { margin: 30px 0 12px; line-height: 1.2; }
p, li { font-size: 18px; line-height: 1.72; }
p { margin: 0 0 18px; }
ul { padding-left: 26px; }
a { color: #245c99; }
pre { overflow-x: auto; background: #efede7; border: 1px solid #dedbd2; border-radius: 8px; padding: 16px; }
code { font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace; font-size: .92em; }
p code, li code { background: #efede7; border-radius: 5px; padding: 2px 5px; }
.code-lang, .share-meta { color: #6f685e; font-size: 13px; }
.share-meta { border-top: 1px solid #dedbd2; margin-top: 36px; padding-top: 16px; line-height: 1.35; }
.share-meta a { color: inherit; display: block; font-weight: 600; margin-bottom: 6px; white-space: nowrap; }
article { padding: 0; }
`;
}

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
