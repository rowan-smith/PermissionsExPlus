---
sidebar_position: 10
---

# Contributing

Contributions to PermissionsExPlus are welcome. This page covers how to get involved.

## Reporting Issues

If you find a bug or have a feature request:

1. Search [existing issues](https://github.com/rowan-smith/PermissionsExPlus/issues) to avoid duplicates
2. Open a new issue with:
   - Server version and software (Spigot, Paper, Purpur)
   - PermissionsExPlus version
   - Steps to reproduce the problem
   - Expected vs actual behavior
   - Console log excerpts (enable debug mode with `/pex toggle debug` first)

## Submitting Code

### Setup

1. Fork the repository on GitHub
2. Clone your fork:
   ```bash
   git clone https://github.com/<your-username>/PermissionsExPlus.git
   ```
3. Open the project in your IDE
4. Maven import and build the project

### Making Changes

1. Create a branch for your change:
   ```bash
   git checkout -b feature/my-change
   ```
2. Make your changes following the code style below
3. Test your changes on a local server
4. Commit with a clear message describing what you changed
5. Push and open a pull request

### Code Style

- Follow existing code conventions in the file you're editing
- Use 4 spaces for indentation (no tabs)
- Keep methods focused, one responsibility per method
- Add Javadoc comments for public API methods
- Use meaningful variable and method names

### Pull Requests

- Keep PRs focused on a single change
- Follow the pull request template that appears when opening a PR
- Reference any related issues (e.g. "Fixes #42")
- Make sure the project builds without errors before submitting

## Updating Documentation

Documentation lives in the `docs/` directory and is built with [Docusaurus](https://docusaurus.io). If your change adds or modifies user-facing features, update the docs too.

### Running the docs locally

```bash
cd docs
npm install
npm run start
```

This starts a local dev server at `http://localhost:3000` with hot reload.

### Documentation structure

```text
docs/
  documentation/              # Current / next docs (Documentation tab)
  developer/                  # Current / next docs (Developer tab)
  versioned_docs/             # Frozen Documentation versions (e.g. 1.23.5)
  developer_versioned_docs/   # Frozen Developer versions
  versions.json               # Documentation version list
  developer_versions.json     # Developer version list
```

Sidebars live in `docs/sidebarsDocumentation.ts` and `docs/sidebarsDeveloper.ts`. Each tree is its own Docusaurus docs plugin (`/docs` and `/developer`).

### Versioning

The site ships **1.23.5** as the default (stable) docs and **2.0.0** as the unreleased `current` docs under `/docs/next` and `/developer/next`.

| Edit these folders | Served as |
|--------------------|-----------|
| `documentation/`, `developer/` | 2.0.0 (Next) |
| `versioned_docs/version-1.23.5/`, `developer_versioned_docs/version-1.23.5/` | 1.23.5 (stable) |

When you cut a release from the next line:

```bash
cd docs
npm run docs:version -- 2.0.0
npm run docs:version:developer -- 2.0.0
```

Then update `lastVersion` and the `versions` map in `docusaurus.config.ts` so both plugins stay aligned.

Cross-links between Documentation and Developer in the **next** (current) tree should use `/docs/next/...` and `/developer/next/...`. Stable (1.23.5) pages should use `/docs/...` and `/developer/...`.

### Adding or updating pages

1. Create or edit the relevant `.md` file under `docs/documentation/` or `docs/developer/` (for 2.0.0), or under the matching `versioned_*` folder for a patch on 1.23.5
2. Use frontmatter to set sidebar position and title:
   ```yaml
   ---
   sidebar_position: 1
   ---
   # Page Title
   ```
3. Use Docusaurus admonitions for callouts:
   ```markdown
   :::note
   This is a note.
   :::

   :::caution
   This is a warning.
   :::

   :::tip
   This is a helpful tip.
   :::
   ```
4. Link to other pages using relative paths within the same tree, or absolute paths across trees:
   ```markdown
   [Page Name](other-page)
   [Section](other-page#section-id)
   [Developer overview](/developer/next/overview)
   [Docs intro](/docs/next/intro)
   ```

### Style guide

- Write in second person ("you can..." not "the user can...")
- Keep sentences concise and direct
- Use code blocks for commands, paths, and config examples
- Use tables for structured data (options, comparisons)
- Use admonitions for warnings, notes, and tips
- Test all commands and config examples before publishing

## Building from Source

```bash
mvn clean package
```

The built jar will be in `target/`.

## Community

- [GitHub Issues](https://github.com/rowan-smith/PermissionsExPlus/issues): bug reports and feature requests
