#!/usr/bin/env node
/**
 * Import markdown from main branch repo paths into Docusaurus docs/ structure.
 */
import fs from 'node:fs';
import path from 'node:path';

const ROOT = path.resolve(import.meta.dirname, '..');

const IMPORTS = [
  {
    source: 'docs/api/README.md',
    dest: 'docs/developers/api/index.md',
    meta: {
      title: 'Hook Plugin API',
      description: 'Modern and legacy API surfaces for PermissionsExPlus companion plugins.',
      slug: '/developers/api',
    },
  },
  {
    source: 'docs/api/MODERN_API.md',
    dest: 'docs/developers/api/modern.md',
    meta: {
      title: 'Modern API Reference',
      description: 'dev.rono.permissions.api reference for new companion plugins.',
      slug: '/developers/api/modern',
    },
  },
  {
    source: 'docs/api/LEGACY_API.md',
    dest: 'docs/developers/api/legacy.md',
    meta: {
      title: 'Legacy API Reference',
      description: 'Frozen ru.tehkode.permissions API for classic hook plugins.',
      slug: '/developers/api/legacy',
    },
  },
  {
    source: 'docs/api/API_INVARIANTS.md',
    dest: 'docs/developers/api/invariants.md',
    meta: {
      title: 'API Invariants',
      description: 'Layering rules and architectural invariants for the permission API.',
      slug: '/developers/api/invariants',
    },
  },
  {
    source: 'docs/api/FUTURE.md',
    dest: 'docs/developers/api/future.md',
    meta: {
      title: 'API Roadmap',
      description: 'Planned API additions and known gaps.',
      slug: '/developers/api/future',
    },
  },
  {
    source: 'ARCHITECTURE.md',
    dest: 'docs/developers/architecture.md',
    meta: {
      title: 'Architecture',
      description: 'Module stack, dependency direction, and design rules.',
      slug: '/developers/architecture',
    },
  },
  {
    source: 'docs/COMPATIBILITY.md',
    dest: 'docs/developers/compatibility.md',
    meta: {
      title: 'Platform Compatibility',
      description: 'Minecraft, JVM, proxy, and legacy hook plugin compatibility.',
      slug: '/developers/compatibility',
    },
  },
  {
    source: 'docs/testing/REAL_SERVER_MATRIX.md',
    dest: 'docs/developers/testing-matrix.md',
    meta: {
      title: 'Real-Server Test Matrix',
      description: 'Pre-release verification checklist for game servers and proxies.',
      slug: '/developers/testing-matrix',
    },
  },
  {
    source: 'bootstrap/permissionsex-bootstrap/README.md',
    dest: 'docs/developers/bootstrap.md',
    meta: {
      title: 'Universal Bootstrap Jar',
      description: 'Merged jar routing, install, and build instructions.',
      slug: '/developers/bootstrap',
    },
  },
];

const LINK_REPLACEMENTS = [
  [/\]\(MODERN_API\.md\)/g, '](/developers/api/modern)'],
  [/\]\(LEGACY_API\.md\)/g, '](/developers/api/legacy)'],
  [/\]\(API_INVARIANTS\.md\)/g, '](/developers/api/invariants)'],
  [/\]\(FUTURE\.md\)/g, '](/developers/api/future)'],
  [/\]\(README\.md\)/g, '](/developers/api)'],
  [/\]\(\.\.\/\.\.\/ARCHITECTURE\.md\)/g, '](/developers/architecture)'],
  [/\]\(ARCHITECTURE\.md\)/g, '](/developers/architecture)'],
  [/\]\(docs\/api\/README\.md\)/g, '](/developers/api)'],
  [/\]\(docs\/api\/MODERN_API\.md\)/g, '](/developers/api/modern)'],
  [/\]\(docs\/api\/LEGACY_API\.md\)/g, '](/developers/api/legacy)'],
  [/\]\(docs\/api\/FUTURE\.md\)/g, '](/developers/api/future)'],
  [/\]\(docs\/api\/API_INVARIANTS\.md\)/g, '](/developers/api/invariants)'],
  [/\]\(docs\/api\/\)/g, '](/developers/api)'],
  [/\]\(api\/LEGACY_API\.md\)/g, '](/developers/api/legacy)'],
  [/\]\(api\/MODERN_API\.md\)/g, '](/developers/api/modern)'],
  [/\]\(testing\/REAL_SERVER_MATRIX\.md\)/g, '](/developers/testing-matrix)'],
  [/\]\(REAL_SERVER_MATRIX\.md\)/g, '](/developers/testing-matrix)'],
  [/\]\(docs\/COMPATIBILITY\.md\)/g, '](/developers/compatibility)'],
  [/\]\(docs\/testing\/REAL_SERVER_MATRIX\.md\)/g, '](/developers/testing-matrix)'],
  [/\]\(bootstrap\/README\.md\)/g, '](/developers/bootstrap)'],
  [/\]\(bootstrap\/permissionsex-bootstrap\/README\.md\)/g, '](/developers/bootstrap)'],
  [/\]\(\.\.\/\.\.\/plugin\//g, '](https://github.com/%%site.repo%%/tree/main/plugin/'],
  [/\]\(plugin\//g, '](https://github.com/%%site.repo%%/tree/main/plugin/'],
  [/\b1\.23\.5\b/g, '%%site.version%%'],
];

function frontmatter(meta) {
  const lines = ['---'];
  for (const [key, value] of Object.entries(meta)) {
    lines.push(`${key}: ${value}`);
  }
  lines.push('---', '');
  return lines.join('\n');
}

function transformBody(body) {
  let out = body.replace(/^#\s+.+\n\n?/, '');
  for (const [pattern, replacement] of LINK_REPLACEMENTS) {
    out = out.replace(pattern, replacement);
  }
  // Javadoc inline tags break MDX parsing
  out = out
    .replace(/\{@link\s+([^}]+)\}/g, (_, target) => {
      const simple = target.split(/[#.]/).pop();
      return `\`${simple}\` (\`${target.trim()}\`)`;
    })
    .replace(/\{@code\s+([^}]+)\}/g, '`$1`');
  return out.trimEnd() + '\n';
}

function importDoc({source, dest, meta}) {
  const sourcePath = path.join(ROOT, source);
  if (!fs.existsSync(sourcePath)) {
    throw new Error(`Missing source file: ${source}`);
  }
  const raw = fs.readFileSync(sourcePath, 'utf8');
  const content = frontmatter(meta) + transformBody(raw);
  const destPath = path.join(ROOT, dest);
  fs.mkdirSync(path.dirname(destPath), {recursive: true});
  fs.writeFileSync(destPath, content);
  console.log(`Wrote ${dest}`);
}

for (const entry of IMPORTS) {
  importDoc(entry);
}

const removable = [
  'docs/api/README.md',
  'docs/api/MODERN_API.md',
  'docs/api/LEGACY_API.md',
  'docs/api/API_INVARIANTS.md',
  'docs/api/FUTURE.md',
  'docs/testing/REAL_SERVER_MATRIX.md',
  'docs/COMPATIBILITY.md',
  'ARCHITECTURE.md',
];

for (const rel of removable) {
  const full = path.join(ROOT, rel);
  if (fs.existsSync(full)) {
    fs.unlinkSync(full);
    console.log(`Removed ${rel}`);
  }
}

const apiDir = path.join(ROOT, 'docs/api');
if (fs.existsSync(apiDir) && fs.readdirSync(apiDir).length === 0) {
  fs.rmdirSync(apiDir);
  console.log('Removed empty docs/api/');
}

const testingDir = path.join(ROOT, 'docs/testing');
if (fs.existsSync(testingDir) && fs.readdirSync(testingDir).length === 0) {
  fs.rmdirSync(testingDir);
  console.log('Removed empty docs/testing/');
}

const bootstrapReadme = path.join(ROOT, 'bootstrap/permissionsex-bootstrap/README.md');
fs.writeFileSync(
  bootstrapReadme,
  `# PermissionsExPlus bootstrap (merged jar)

See the [Universal Bootstrap Jar](https://github.com/%%site.repo%%/blob/gh-pages/docs/developers/bootstrap.md) documentation on the project website.

Build from repo root:

\`\`\`bash
mvn -pl bootstrap -am package
\`\`\`

Output: \`bootstrap/target/PermissionsExPlus-{version}.jar\`
`,
);

console.log('Updated bootstrap/permissionsex-bootstrap/README.md pointer');
