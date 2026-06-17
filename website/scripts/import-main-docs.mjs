#!/usr/bin/env node
/**
 * One-time migration helper: import repo-root markdown into Docusaurus docs/.
 * Legacy source files were removed after the gh-pages merge; this script now
 * only refreshes the universal README pointer.
 */
import fs from 'node:fs';
import path from 'node:path';

const WEBSITE_ROOT = path.resolve(import.meta.dirname, '..');
const REPO_ROOT = path.resolve(WEBSITE_ROOT, '..');
const siteVars = JSON.parse(
  fs.readFileSync(path.join(WEBSITE_ROOT, 'site-vars.json'), 'utf8'),
);

const universalReadme = path.join(REPO_ROOT, 'universal/README.md');
fs.writeFileSync(
  universalReadme,
  `# PermissionsExPlus universal jar

See the [Universal Bootstrap Jar](https://permissionsexplus.rono.dev/developers/bootstrap) documentation on the project website.

Source directory: **\`universal/\`** (Maven module **\`permissionsex-bootstrap\`**, \`dev.rono.permissions:permissionsex-bootstrap\`).

Output jar:

**\`universal/target/PermissionsExPlus-{version}.jar\`**

## Build

From repo root:

\`\`\`bash
mvn -pl universal -am package
\`\`\`

Output: \`universal/target/PermissionsExPlus-${siteVars.version}.jar\`
`,
);

console.log('Updated universal/README.md');
