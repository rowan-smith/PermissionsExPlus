import React, { useMemo, useState } from 'react';
import Link from '@docusaurus/Link';
import styles from './styles.module.css';
import commandsData from '../../data/commands.json';
import CommandSyntax from '../CommandSyntax';

interface Command {
  command: string;
  description: string;
  category: string;
}

const categories = ['All', 'Users', 'Groups', 'Worlds', 'Utility', 'Promotion'];

const categoryUrls: Record<string, string> = {
  Users: '/docs/commands/user-commands',
  Groups: '/docs/commands/group-commands',
  Worlds: '/docs/commands/world-commands',
  Utility: '/docs/commands/utility-commands',
  Promotion: '/docs/commands/promotion-commands',
};

function commandToSlug(command: string): string {
  return command
    .toLowerCase()
    .replace(/[^a-z0-9\s-]/g, '')
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}

export default function CommandList() {
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');

  const filteredCommands = useMemo(() => {
    const query = search.toLowerCase().trim();
    const words = query ? query.split(/\s+/) : [];

    return commandsData
      .filter((cmd) => {
        const matchesCategory = selectedCategory === 'All' || cmd.category === selectedCategory;
        if (!matchesCategory) return false;
        if (words.length === 0) return true;

        const commandLower = cmd.command.toLowerCase();
        const descLower = cmd.description.toLowerCase();

        return words.every((word) =>
          commandLower.includes(word) || descLower.includes(word)
        );
      })
      .sort((a, b) => {
        if (words.length === 0) return 0;
        return scoreCommand(b, words) - scoreCommand(a, words);
      });
  }, [search, selectedCategory]);

  return (
    <div className={styles.commandListContainer}>
      <div className={styles.searchSection}>
        <div className={styles.searchHeader}>
          <h3>Find a command</h3>
          <p>Search /pex commands. Press <code>/</code> to focus this field.</p>
        </div>
        <div className={styles.inputWrapper}>
          <input
            type="text"
            placeholder="Search commands..."
            className={styles.searchInput}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === '/') {
                e.stopPropagation();
              }
            }}
          />
          {search && (
            <button className={styles.clearButton} onClick={() => setSearch('')}>
              ×
            </button>
          )}
        </div>

        {/* Quick Tags */}
        <div className={styles.tags}>
          <span className={styles.tagLabel}>Try:</span>
          {['user', 'user add', 'group remove permission', 'user timed', 'user promote'].map((tag) => (
            <button
              key={tag}
              className={styles.tagButton}
              onClick={() => setSearch(tag)}
            >
              {tag}
            </button>
          ))}
        </div>

        {/* Results right under the search box */}
        {(search || selectedCategory !== 'All') && (
          <div className={styles.resultsDropdown}>
            <div className={styles.resultsHeader}>
              <span>{filteredCommands.length} commands found</span>
              {selectedCategory !== 'All' && (
                <span className={styles.activeFilter}>Category: {selectedCategory}</span>
              )}
            </div>
            {filteredCommands.length > 0 ? (
              <div className={styles.commandGrid}>
                {filteredCommands.map((cmd, idx) => (
                  <Link
                    key={idx}
                    to={`${categoryUrls[cmd.category]}#${commandToSlug(cmd.command)}`}
                    className={styles.commandItem}
                  >
                    <div className={styles.itemMeta}>
                      <span className={styles.itemCategoryTag}>{cmd.category}</span>
                    </div>
                    <CommandSyntax command={cmd.command} className={styles.commandCode} />
                    <p>{cmd.description}</p>
                  </Link>
                ))}
              </div>
            ) : (
              <p className={styles.noResults}>No commands found matching your search.</p>
            )}
          </div>
        )}
      </div>

      <div className={styles.categoryBrowse}>
        <h3>Browse by category</h3>
        <div className={styles.categoryGrid}>
          {categories.filter(c => c !== 'All').map((cat) => (
            <div
              key={cat}
              className={`${styles.categoryCard} ${selectedCategory === cat ? styles.activeCard : ''}`}
              onClick={() => setSelectedCategory(selectedCategory === cat ? 'All' : cat)}
            >
              <div className={styles.categoryHeader}>
                <span className={styles.categoryName}>{cat}</span>
                <span className={styles.commandCount}>
                  {commandsData.filter(cmd => cmd.category === cat).length} commands
                </span>
              </div>
              <p className={styles.categoryDescription}>
                {getCategoryDescription(cat)}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function scoreCommand(cmd: Command, words: string[]): number {
  const commandLower = cmd.command.toLowerCase();
  const descLower = cmd.description.toLowerCase();
  let score = 0;

  for (const word of words) {
    if (commandLower.includes(word)) score += 10;
    if (descLower.includes(word)) score += 5;
    if (commandLower.startsWith(word)) score += 5;
    if (commandLower === word) score += 10;
  }

  return score;
}

function getCategoryDescription(category: string) {
  switch (category) {
    case 'Users': return 'Player permissions, groups, prefixes, timed nodes, and cleanup.';
    case 'Groups': return 'Create groups, assign permissions, parents, weight, and defaults.';
    case 'Worlds': return 'Multi-world contexts, inheritance, and world-specific rules.';
    case 'Utility': return 'Reload, config, backends, import, export, debug, and server-wide tools.';
    case 'Promotion': return 'Rank ladders: promote and demote users between groups.';
    default: return '';
  }
}
