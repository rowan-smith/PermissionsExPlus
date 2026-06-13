#!/usr/bin/env node
/**
 * One-time migration: Jekyll frontmatter + Liquid → Docusaurus docs/
 */
import fs from 'node:fs';
import path from 'node:path';

const ROOT = path.resolve(import.meta.dirname, '..');
const VERSION = '1.23.5';
const REPO = 'rowan-smith/PermissionsExPlus';

const SOURCE_FILES = [
  'index.md',
  'requirements.md',
  'storage.md',
  'configuration.md',
  'about.md',
  'concepts/permissions.md',
  'concepts/inheritance.md',
  'concepts/context.md',
  'concepts/weight.md',
  'concepts/meta.md',
  'commands/general.md',
  'commands/users.md',
  'commands/groups.md',
  'commands/permissions.md',
  'commands/worlds.md',
  'commands/ranks.md',
  'guides/recipes.md',
  'guides/troubleshooting.md',
  'faq/default-groups.md',
  'faq/migration.md',
  'developers/index.md',
  'developers/cookbook.md',
  'developers/contributing.md',
  'developers/reference.md',
];

function parseFrontmatter(raw) {
  const match = raw.match(/^---\r?\n([\s\S]*?)\r?\n---\r?\n([\s\S]*)$/);
  if (!match) return {meta: {}, body: raw};
  const meta = {};
  for (const line of match[1].split('\n')) {
    const idx = line.indexOf(':');
    if (idx === -1) continue;
    const key = line.slice(0, idx).trim();
    let value = line.slice(idx + 1).trim();
    if (value.endsWith('/')) value = value.slice(0, -1);
    meta[key] = value;
  }
  return {meta, body: match[2]};
}

function slugFromPermalink(permalink) {
  if (!permalink || permalink === '/') return '/';
  return permalink.replace(/\/$/, '');
}

function transformBody(body, destRel) {
  let out = body
    .replace(/\{\{\s*site\.version\s*\}\}/g, '%%site.version%%')
    .replace(/\{\{\s*site\.repo\s*\}\}/g, '%%site.repo%%')
    .replace(/\{\{\s*site\.baseurl\s*\}\}\//g, '%%site.baseurl%%/')
    .replace(/\{\{\s*site\.baseurl\s*\}\}/g, '%%site.baseurl%%');

  // Jekyll reference page version cards → static markdown
  if (destRel === 'developers/reference.md') {
    out = out.replace(
      /<div class="version-cards">[\s\S]*?<\/div>\n\n/,
      `import VersionCards from '@site/src/components/VersionCards';

<VersionCards type="javadoc" />

`,
    );
  }

  if (destRel === 'index.md') {
    out = out.replace(
      /<div class="version-cards">[\s\S]*?<\/div>\n\n/,
      `import VersionCards from '@site/src/components/VersionCards';

<VersionCards type="home" />

`,
    );
  }

  return out.trim() + '\n';
}

function toDestPath(srcRel) {
  if (srcRel === 'index.md') return 'index.mdx';
  if (srcRel === 'developers/reference.md') return 'developers/reference.mdx';
  return srcRel;
}

for (const srcRel of SOURCE_FILES) {
  const src = path.join(ROOT, srcRel);
  const destRel = toDestPath(srcRel);
  const dest = path.join(ROOT, 'docs', destRel);
  const raw = fs.readFileSync(src, 'utf8');
  const {meta, body} = parseFrontmatter(raw);

  const slug = slugFromPermalink(meta.permalink);
  const frontmatterLines = [
    '---',
    `title: ${meta.title || 'Untitled'}`,
    meta.description ? `description: ${meta.description}` : null,
    slug ? `slug: ${slug}` : null,
    '---',
    '',
  ].filter((line) => line !== null);
  const frontmatter = frontmatterLines.join('\n');

  const content = frontmatter + transformBody(body, destRel);
  fs.mkdirSync(path.dirname(dest), {recursive: true});
  fs.writeFileSync(dest, content);
  console.log(`Migrated ${srcRel} → docs/${destRel}`);
}
