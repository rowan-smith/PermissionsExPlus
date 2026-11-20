import React, { useMemo, useState } from 'react';
import styles from './styles.module.css';
import commandsData from '../../data/plus-commands.json';

interface Command {
  command: string;
  description: string;
  category: string;
}

const categories = ['All', 'General', 'Users', 'Groups', 'Permissions', 'Worlds', 'Ranks'];

export default function CommandList() {
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');

  const filteredCommands = useMemo(() => {
    return commandsData.filter((cmd) => {
      const matchesSearch =
        cmd.command.toLowerCase().includes(search.toLowerCase()) ||
        cmd.description.toLowerCase().includes(search.toLowerCase());
      const matchesCategory = selectedCategory === 'All' || cmd.category === selectedCategory;
      return matchesSearch && matchesCategory;
    });
  }, [search, selectedCategory]);

  return (
    <div className={styles.commandListContainer}>
      <div className={styles.searchSection}>
        <div className={styles.searchHeader}>
          <h3>Find a command</h3>
          <p>Search /pex commands — press <code>/</code> to focus this field.</p>
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
          {['reload', 'user permissions add', 'ladder promote', 'backend export'].map((tag) => (
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
                  <div key={idx} className={styles.commandItem}>
                    <div className={styles.itemMeta}>
                      <span className={styles.tagModern}>MODERN</span>
                      <span className={styles.itemCategoryTag}>{cmd.category}</span>
                    </div>
                    <code>{cmd.command}</code>
                    <p>{cmd.description}</p>
                  </div>
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

function getCategoryDescription(category: string) {
  switch (category) {
    case 'General': return 'Reload, config, backends, import, export, debug, and server-wide tools.';
    case 'Users': return 'Player permissions, groups, prefixes, timed nodes, and cleanup.';
    case 'Groups': return 'Create groups, assign permissions, parents, weight, and defaults.';
    case 'Permissions': return 'Grant, remove, swap, and inspect permission nodes.';
    case 'Worlds': return 'Multi-world contexts, inheritance, and world-specific rules.';
    case 'Ranks': return 'Rank ladders — modern `/pex ladder` subcommands; classic promote/demote shortcuts.';
    default: return '';
  }
}
