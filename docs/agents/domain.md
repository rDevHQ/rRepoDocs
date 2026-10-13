# Domain Docs

This repo uses a single-context domain documentation layout.

## Before exploring, read these

- `GLOSSARY.md` at the repo root.
- ADRs in `docs/adr/` that touch the area you are about to work in.

If these files do not exist, proceed silently. Do not flag their absence
or suggest creating them upfront. The domain-modeling skill creates them
lazily when terms or decisions are resolved.

## File structure

- `GLOSSARY.md`: shared domain vocabulary.
- `docs/adr/NNNN-short-title.md`: architectural decision records.

## Use the glossary's vocabulary

When naming domain concepts in issue titles, proposals, hypotheses, or tests,
use the terms defined in `GLOSSARY.md`, including its preferred synonyms.

If a needed concept is absent, reconsider whether it belongs to the project's
domain or note the gap for domain-modeling.

## Flag ADR conflicts

If a proposal contradicts an existing ADR, surface the conflict explicitly
and explain why reopening the decision may be worthwhile.
