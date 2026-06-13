#!/usr/bin/env node
/**
 * Build a searchable command index from docs/commands/*.md
 */
import fs from 'node:fs';
import path from 'node:path';

const ROOT = path.resolve(import.meta.dirname, '..');
const COMMANDS_DIR = path.join(ROOT, 'docs', 'commands');
const OUTPUT = path.join(ROOT, 'src', 'data', 'commands.json');

const CATEGORY_LABELS = {
  general: 'General',
  users: 'Users',
  groups: 'Groups',
  permissions: 'Permissions',
  worlds: 'Worlds',
  ranks: 'Ranks',
};

function slugifyHeading(heading) {
  return heading
    .replace(/`/g, '')
    .toLowerCase()
    .replace(/[^\w\s-]/g, '')
    .trim()
    .replace(/\s+/g, '-');
}

function extractCommandsFromFile(filePath, category, categoryLabel) {
  const content = fs.readFileSync(filePath, 'utf8');
  const body = content.replace(/^---[\s\S]*?---\n/, '');
  const commands = [];
  const seen = new Set();

  const addCommand = (command, syntax, summary, docSlug) => {
    const key = `${docSlug}:${command}`;
    if (seen.has(key)) return;
    seen.add(key);
    commands.push({
      command,
      syntax: syntax || command,
      summary: summary || '',
      category: categoryLabel,
      docSlug,
      anchor: slugifyHeading(command),
    });
  };

  const sections = body.split(/\n---\n/);

  for (const section of sections) {
    const headingMatch = section.match(/^## `?(\/pex[^`\n]+)`?/m);
    if (headingMatch) {
      const command = headingMatch[1].trim();
      const syntaxMatch = section.match(/\*\*Syntax:\*\*\s*`([^`]+)`/);
      const summaryMatch = section.match(/\*\*Syntax:\*\*[^\n]*\n\n([^\n`][^\n]*)/);
      addCommand(
        command,
        syntaxMatch?.[1],
        summaryMatch?.[1]?.trim(),
        `/commands/${category}`,
      );
      continue;
    }

    const headingText = section.match(/^## ([^\n]+)/m)?.[1]?.trim();
    const codeMatch = section.match(/```text\n(\/pex[^\n]+)\n```/);
    if (codeMatch && headingText) {
      addCommand(
        codeMatch[1],
        codeMatch[1],
        headingText,
        `/commands/${category}`,
      );
    }
  }

  // Standalone promote/demote
  if (category === 'general') {
    for (const cmd of ['/promote', '/demote']) {
      addCommand(cmd, `${cmd} <user> [ladder]`, 'Rank ladder shortcut', '/commands/ranks');
      commands[commands.length - 1].category = 'Ranks';
    }
  }

  return commands;
}

const allCommands = [];

for (const [category, label] of Object.entries(CATEGORY_LABELS)) {
  const filePath = path.join(COMMANDS_DIR, `${category}.md`);
  if (!fs.existsSync(filePath)) continue;
  allCommands.push(...extractCommandsFromFile(filePath, category, label));
}

allCommands.sort((a, b) => a.command.localeCompare(b.command));

fs.mkdirSync(path.dirname(OUTPUT), {recursive: true});
fs.writeFileSync(OUTPUT, `${JSON.stringify(allCommands, null, 2)}\n`);
console.log(`Built command index: ${allCommands.length} commands → ${OUTPUT}`);
