import type {CommandEntry} from '@site/src/data/commands';
import commands from '@site/src/data/commands.json';
import Link from '@docusaurus/Link';
import {useEffect, useMemo, useRef, useState} from 'react';
import styles from './CommandSearch.module.css';

const MAX_RESULTS = 12;

function scoreCommand(entry: CommandEntry, query: string): number {
  const q = query.toLowerCase();
  const cmd = entry.command.toLowerCase();
  const syntax = entry.syntax.toLowerCase();
  const summary = entry.summary.toLowerCase();
  const category = entry.category.toLowerCase();

  if (cmd.startsWith(q)) return 100;
  if (cmd.includes(q)) return 80;
  if (syntax.includes(q)) return 60;
  if (summary.toLowerCase().includes(q)) return 40;
  if (category.includes(q)) return 20;
  return 0;
}

export default function CommandSearch(): JSX.Element {
  const [query, setQuery] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  const results = useMemo(() => {
    const q = query.trim();
    if (!q) return [];

    return (commands as CommandEntry[])
      .map((entry) => ({entry, score: scoreCommand(entry, q)}))
      .filter(({score}) => score > 0)
      .sort((a, b) => b.score - a.score || a.entry.command.localeCompare(b.entry.command))
      .slice(0, MAX_RESULTS)
      .map(({entry}) => entry);
  }, [query]);

  useEffect(() => {
    const handler = (event: KeyboardEvent) => {
      if (event.key === '/' && document.activeElement?.tagName !== 'INPUT') {
        event.preventDefault();
        inputRef.current?.focus();
      }
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, []);

  return (
    <section className={styles.wrapper} aria-label="Command search">
      <div className={styles.header}>
        <h2 className={styles.title}>Find a command</h2>
        <p className={styles.hint}>
          Search <code>/pex</code> commands — press <kbd className={styles.kbd}>/</kbd> to focus
          this field.
        </p>
      </div>
      <div className={styles.inputRow}>
        <span className={styles.icon} aria-hidden="true">
          ⌕
        </span>
        <input
          ref={inputRef}
          className={styles.input}
          type="search"
          placeholder="e.g. ladder promote, user permissions add, backend export…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          aria-label="Search commands"
        />
        {query && (
          <button
            type="button"
            className={styles.clear}
            onClick={() => setQuery('')}
            aria-label="Clear search">
            ×
          </button>
        )}
      </div>

      {query.trim() && (
        <ul className={styles.results}>
          {results.length === 0 ? (
            <li className={styles.empty}>
              No commands matched. Try a different keyword or{' '}
              <Link to="/commands#browse-by-category">browse by category</Link>.
            </li>
          ) : (
            results.map((entry) => (
              <li key={`${entry.docSlug}-${entry.command}`}>
                <Link
                  className={styles.result}
                  to={`${entry.docSlug}#${entry.anchor}`}>
                  <span className={styles.resultCommand}>{entry.command}</span>
                  <span className={styles.resultMeta}>
                    <span className={styles.category}>{entry.category}</span>
                    {entry.summary && (
                      <span className={styles.summary}>{entry.summary}</span>
                    )}
                  </span>
                </Link>
              </li>
            ))
          )}
        </ul>
      )}

      {!query.trim() && (
        <div className={styles.quickTags}>
          {['reload', 'group create', 'user permissions add', 'ladder promote', 'backend export', 'hierarchy'].map((tag) => (
            <button
              key={tag}
              type="button"
              className={styles.tag}
              onClick={() => setQuery(tag)}>
              {tag}
            </button>
          ))}
        </div>
      )}
    </section>
  );
}
