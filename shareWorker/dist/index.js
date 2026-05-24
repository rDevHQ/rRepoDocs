var __defProp = Object.defineProperty;
var __name = (target, value) => __defProp(target, "name", { value, configurable: true });

// src/index.ts
var DefaultExpiryDays = 30;
var MaxExpiryDays = 365;
var DefaultMaxMarkdownBytes = 256 * 1024;
var index_default = {
  async fetch(request, env, ctx) {
    return handleRequest(request, env, ctx);
  }
};
async function handleRequest(request, env, _ctx, fetcher = fetch) {
  const url = new URL(request.url);
  try {
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
__name(handleRequest, "handleRequest");
async function createShare(request, env, fetcher) {
  const accessToken = bearerToken(request);
  if (accessToken == null) {
    return jsonResponse({ error: "unauthorized", message: "Missing bearer token." }, 401);
  }
  const user = await validateGitHubUser(accessToken, env, fetcher);
  if (user == null) {
    return jsonResponse({ error: "unauthorized", message: "GitHub token could not be validated." }, 401);
  }
  const body = await parseJson(request);
  if (body == null) {
    return jsonResponse({ error: "invalid_json", message: "Request body must be valid JSON." }, 400);
  }
  const validation = validateCreateShareRequest(body, env);
  if (!validation.ok) {
    return jsonResponse({ error: "invalid_request", message: validation.message }, 400);
  }
  const id = createShareId();
  const createdAt = /* @__PURE__ */ new Date();
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
  ).bind(
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
  ).run();
  const shareUrl = `${publicBaseUrl(env)}/s/${id}`;
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
__name(createShare, "createShare");
async function revokeShare(request, env, fetcher, id) {
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
  ).bind(id).first();
  if (existing == null) {
    return jsonResponse({ error: "not_found", message: "Share not found." }, 404);
  }
  if (existing.owner_github_user_id !== String(user.id)) {
    return jsonResponse({ error: "forbidden", message: "Only the share owner can revoke this link." }, 403);
  }
  if (existing.revoked_at != null) {
    return jsonResponse({ id, revokedAt: existing.revoked_at }, 200);
  }
  const revokedAt = (/* @__PURE__ */ new Date()).toISOString();
  await env.DB.prepare("UPDATE document_shares SET revoked_at = ? WHERE id = ?").bind(revokedAt, id).run();
  return jsonResponse({ id, revokedAt }, 200);
}
__name(revokeShare, "revokeShare");
async function renderShare(env, id) {
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
  ).bind(id).first();
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
__name(renderShare, "renderShare");
async function validateGitHubUser(accessToken, env, fetcher = fetch) {
  const response = await fetcher(`${env.GITHUB_API_BASE_URL.replace(/\/+$/, "")}/user`, {
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Accept": "application/vnd.github+json",
      "X-GitHub-Api-Version": "2022-11-28",
      "User-Agent": "rRepoDocs Share Worker"
    }
  });
  if (!response.ok) return null;
  const payload = await response.json();
  if (typeof payload.id !== "number" || typeof payload.login !== "string" || payload.login.trim() === "") {
    return null;
  }
  return { id: payload.id, login: payload.login };
}
__name(validateGitHubUser, "validateGitHubUser");
function validateCreateShareRequest(body, env) {
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
__name(validateCreateShareRequest, "validateCreateShareRequest");
function bearerToken(request) {
  const authorization = request.headers.get("Authorization") ?? "";
  const match = authorization.match(/^Bearer\s+(.+)$/i);
  return match?.[1]?.trim() || null;
}
__name(bearerToken, "bearerToken");
async function parseJson(request) {
  try {
    return await request.json();
  } catch {
    return null;
  }
}
__name(parseJson, "parseJson");
function createShareId() {
  const bytes = new Uint8Array(18);
  crypto.getRandomValues(bytes);
  return base64Url(bytes);
}
__name(createShareId, "createShareId");
function base64Url(bytes) {
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
__name(base64Url, "base64Url");
function expiryDate(createdAt, expiresInDays, env) {
  if (expiresInDays === null) return null;
  const days = expiresInDays ?? defaultExpiryDays(env);
  return new Date(createdAt.getTime() + days * 24 * 60 * 60 * 1e3);
}
__name(expiryDate, "expiryDate");
function defaultExpiryDays(env) {
  const parsed = Number.parseInt(env.DEFAULT_EXPIRY_DAYS, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : DefaultExpiryDays;
}
__name(defaultExpiryDays, "defaultExpiryDays");
function normalizeExpiryDays(value) {
  if (value === void 0) return void 0;
  if (value === null) return null;
  if (typeof value === "number" && Number.isInteger(value)) return value;
  return -1;
}
__name(normalizeExpiryDays, "normalizeExpiryDays");
function stringField(value) {
  return typeof value === "string" ? value : "";
}
__name(stringField, "stringField");
function optionalStringField(value) {
  return typeof value === "string" ? value : null;
}
__name(optionalStringField, "optionalStringField");
function maxMarkdownBytes(env) {
  const parsed = Number.parseInt(env.MAX_MARKDOWN_BYTES, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : DefaultMaxMarkdownBytes;
}
__name(maxMarkdownBytes, "maxMarkdownBytes");
function byteLength(value) {
  return new TextEncoder().encode(value).length;
}
__name(byteLength, "byteLength");
function isExpired(expiresAt) {
  return expiresAt != null && Date.parse(expiresAt) <= Date.now();
}
__name(isExpired, "isExpired");
function publicBaseUrl(env) {
  return env.PUBLIC_BASE_URL.replace(/\/+$/, "");
}
__name(publicBaseUrl, "publicBaseUrl");
function jsonResponse(body, status) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "no-store"
    }
  });
}
__name(jsonResponse, "jsonResponse");
function htmlHeaders() {
  return {
    "Content-Type": "text/html; charset=utf-8",
    "Cache-Control": "public, max-age=120"
  };
}
__name(htmlHeaders, "htmlHeaders");
function renderSharePage(share) {
  const renderedMarkdown = renderMarkdown(share.markdown);
  const subtitle = `${share.repo_full_name} / ${share.document_path}`;
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
    <header>
      <div class="brand">rRepoDocs</div>
      <h1>${escapeHtml(share.title)}</h1>
      <p>${escapeHtml(subtitle)}</p>
      <p class="meta">Shared by ${escapeHtml(share.owner_login)} on ${formatDate(share.created_at)}${share.expires_at ? ` \xB7 Expires ${formatDate(share.expires_at)}` : ""}</p>
    </header>
    <article>${renderedMarkdown}</article>
  </main>
</body>
</html>`;
}
__name(renderSharePage, "renderSharePage");
function renderMessagePage(title, message) {
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
__name(renderMessagePage, "renderMessagePage");
function renderMarkdown(markdown) {
  const blocks = [];
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
      const codeLines = [];
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
      const items = [];
      while (index < lines.length && /^\s*[-*]\s+/.test(lines[index] ?? "")) {
        items.push((lines[index] ?? "").replace(/^\s*[-*]\s+/, ""));
        index += 1;
      }
      blocks.push(`<ul>${items.map((item) => `<li>${renderInline(item)}</li>`).join("")}</ul>`);
      continue;
    }
    const paragraphLines = [];
    while (index < lines.length && (lines[index] ?? "").trim() !== "" && !/^(#{1,6})\s+/.test(lines[index] ?? "") && !/^\s*[-*]\s+/.test(lines[index] ?? "") && !/^```/.test(lines[index] ?? "")) {
      paragraphLines.push((lines[index] ?? "").trim());
      index += 1;
    }
    blocks.push(`<p>${renderInline(paragraphLines.join(" "))}</p>`);
  }
  return blocks.join("\n");
}
__name(renderMarkdown, "renderMarkdown");
function renderInline(value) {
  let escaped = escapeHtml(value);
  escaped = escaped.replace(/`([^`]+)`/g, "<code>$1</code>");
  escaped = escaped.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
  escaped = escaped.replace(/\*([^*]+)\*/g, "<em>$1</em>");
  escaped = escaped.replace(/\[([^\]]+)\]\((https?:\/\/[^)\s]+)\)/g, '<a href="$2" rel="nofollow noopener noreferrer">$1</a>');
  return escaped;
}
__name(renderInline, "renderInline");
function escapeHtml(value) {
  return value.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}
__name(escapeHtml, "escapeHtml");
function formatDate(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString("en", { year: "numeric", month: "short", day: "numeric" });
}
__name(formatDate, "formatDate");
function pageCss() {
  return `
:root { color-scheme: light; font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; background: #f8f7f4; color: #1d1c1a; }
body { margin: 0; }
main { width: min(820px, calc(100vw - 32px)); margin: 0 auto; padding: 42px 0 64px; }
header { border-bottom: 1px solid #dedbd2; margin-bottom: 30px; padding-bottom: 24px; }
.brand { color: #676158; font-size: 13px; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; }
h1 { font-family: Georgia, "Times New Roman", serif; font-size: clamp(34px, 7vw, 58px); line-height: 1.02; margin: 16px 0 10px; font-weight: 600; }
h2, h3, h4, h5, h6 { margin: 30px 0 12px; line-height: 1.2; }
p, li { font-size: 18px; line-height: 1.72; }
p { margin: 0 0 18px; }
ul { padding-left: 26px; }
a { color: #245c99; }
pre { overflow-x: auto; background: #efede7; border: 1px solid #dedbd2; border-radius: 8px; padding: 16px; }
code { font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace; font-size: .92em; }
p code, li code { background: #efede7; border-radius: 5px; padding: 2px 5px; }
.code-lang, .meta { color: #6f685e; font-size: 13px; }
article { background: #fffdf8; border: 1px solid #e5e1d8; border-radius: 8px; padding: clamp(22px, 5vw, 46px); }
`;
}
__name(pageCss, "pageCss");
function errorMessage(error) {
  return error instanceof Error ? error.message : String(error);
}
__name(errorMessage, "errorMessage");
export {
  index_default as default,
  handleRequest,
  renderMarkdown,
  validateCreateShareRequest,
  validateGitHubUser
};
//# sourceMappingURL=index.js.map
