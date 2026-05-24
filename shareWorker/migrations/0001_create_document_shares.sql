CREATE TABLE IF NOT EXISTS document_shares (
  id TEXT PRIMARY KEY NOT NULL,
  owner_github_user_id TEXT NOT NULL,
  owner_login TEXT NOT NULL,
  repo_full_name TEXT NOT NULL,
  document_path TEXT NOT NULL,
  source_sha TEXT,
  title TEXT NOT NULL,
  markdown TEXT NOT NULL,
  expires_at TEXT,
  created_at TEXT NOT NULL,
  revoked_at TEXT
);

CREATE INDEX IF NOT EXISTS idx_document_shares_owner
  ON document_shares(owner_github_user_id, created_at);

CREATE INDEX IF NOT EXISTS idx_document_shares_expiry
  ON document_shares(expires_at);
