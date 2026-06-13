#!/usr/bin/env node
/**
 * Fetch GitHub releases and write changelog data + docs page.
 */
import fs from 'node:fs';
import path from 'node:path';

const ROOT = path.resolve(import.meta.dirname, '..');
const siteVars = JSON.parse(fs.readFileSync(path.join(ROOT, 'site-vars.json'), 'utf8'));
const RELEASES_JSON = path.join(ROOT, 'src', 'data', 'releases.json');
const CHANGELOG_MD = path.join(ROOT, 'docs', 'changelog.md');

const FALLBACK_RELEASES = [
  {
    tag: `v${siteVars.version}`,
    name: `PermissionsExPlus ${siteVars.version}`,
    publishedAt: new Date().toISOString(),
    url: `https://github.com/${siteVars.repo}/releases/tag/v${siteVars.version}`,
    body: 'See GitHub Releases for full release notes.',
    prerelease: false,
  },
];

async function fetchReleases() {
  const url = `https://api.github.com/repos/${siteVars.repo}/releases?per_page=20`;
  const response = await fetch(url, {
    headers: {Accept: 'application/vnd.github+json', 'User-Agent': 'PermissionsExPlus-Docs'},
  });

  if (!response.ok) {
    console.warn(`GitHub API ${response.status} — using cached/fallback releases`);
    if (fs.existsSync(RELEASES_JSON)) {
      return JSON.parse(fs.readFileSync(RELEASES_JSON, 'utf8'));
    }
    return FALLBACK_RELEASES;
  }

  const data = await response.json();
  const mapped = data.map((release) => ({
    tag: release.tag_name,
    name: release.name || release.tag_name,
    publishedAt: release.published_at,
    url: release.html_url,
    body: release.body || '',
    prerelease: release.prerelease,
  }));

  return mapped.length > 0 ? mapped : FALLBACK_RELEASES;
}

function renderChangelogMarkdown(releases) {
  const lines = [
    '---',
    'title: Changelog',
    'description: Release history and notes for PermissionsExPlus.',
    'slug: /changelog',
    '---',
    '',
    'Release notes for PermissionsExPlus. Full details and downloads on [GitHub Releases]',
    `(https://github.com/${siteVars.repo}/releases).`,
    '',
    'import ChangelogList from "@site/src/components/ChangelogList";',
    '',
    '<ChangelogList />',
    '',
  ];

  return lines.join('\n');
}

const releases = await fetchReleases();

fs.mkdirSync(path.dirname(RELEASES_JSON), {recursive: true});
fs.writeFileSync(RELEASES_JSON, `${JSON.stringify(releases, null, 2)}\n`);
fs.writeFileSync(CHANGELOG_MD, renderChangelogMarkdown(releases));

console.log(`Synced ${releases.length} releases → ${RELEASES_JSON}`);
