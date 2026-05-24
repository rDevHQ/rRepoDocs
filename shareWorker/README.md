# rRepoDocs Share Worker

Cloudflare Worker that stores public, read-only Markdown snapshots for rRepoDocs.

## Local setup

```sh
npm install
npx wrangler d1 create rrepodocs-shares
```

Replace `database_id` in `wrangler.jsonc`, then run:

```sh
npx wrangler d1 migrations apply rrepodocs-shares --local
npm run dev
```

Set `PUBLIC_BASE_URL` to the deployed Worker origin for production. GitHub tokens are only used to validate share creators/revokers and are never stored.
