#!/usr/bin/env node
/**
 * Sync site-vars.json from pom.xml (when present) or package.json.
 * Ready for a future Maven profile to call before Docusaurus build.
 */
import fs from 'node:fs';
import path from 'node:path';

const ROOT = path.resolve(import.meta.dirname, '..');
const VARS_PATH = path.join(ROOT, 'site-vars.json');
const PACKAGE_PATH = path.join(ROOT, 'package.json');
const POM_PATH = path.join(ROOT, 'pom.xml');

const DEFAULTS = {
  repo: 'rowan-smith/PermissionsExPlus',
  siteUrl: 'https://permissionsexplus.rono.dev',
  baseUrl: '/',
};

function readPomVersion() {
  if (!fs.existsSync(POM_PATH)) {
    return null;
  }

  const xml = fs.readFileSync(POM_PATH, 'utf8');
  const projectMatch = xml.match(
    /<artifactId>PermissionsExPlus<\/artifactId>\s*\n\s*<version>([^<]+)<\/version>/,
  );
  if (projectMatch) {
    return projectMatch[1].trim();
  }

  const versionMatch = xml.match(/<version>([^<]+)<\/version>/);
  return versionMatch ? versionMatch[1].trim() : null;
}

function readPackageVersion() {
  const pkg = JSON.parse(fs.readFileSync(PACKAGE_PATH, 'utf8'));
  return pkg.version;
}

function readExistingVars() {
  if (!fs.existsSync(VARS_PATH)) {
    return {...DEFAULTS};
  }
  return JSON.parse(fs.readFileSync(VARS_PATH, 'utf8'));
}

const existing = readExistingVars();
const pomVersion = readPomVersion();
const version = pomVersion || readPackageVersion();

const vars = {
  version,
  repo: existing.repo || DEFAULTS.repo,
  siteUrl: existing.siteUrl || DEFAULTS.siteUrl,
  baseUrl: existing.baseUrl || DEFAULTS.baseUrl,
  jarName: `PermissionsExPlus-${version}.jar`,
};

fs.writeFileSync(VARS_PATH, `${JSON.stringify(vars, null, 2)}\n`);

if (pomVersion) {
  const pkg = JSON.parse(fs.readFileSync(PACKAGE_PATH, 'utf8'));
  if (pkg.version !== pomVersion) {
    pkg.version = pomVersion;
    fs.writeFileSync(PACKAGE_PATH, `${JSON.stringify(pkg, null, 2)}\n`);
    console.log(`Synced package.json version → ${pomVersion}`);
  }
  console.log(`Synced site-vars.json from pom.xml → ${pomVersion}`);
} else {
  console.log(`Synced site-vars.json from package.json → ${version}`);
}
