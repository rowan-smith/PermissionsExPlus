import type {CommandEntry} from '@site/src/data/commands';
import commands from '@site/src/data/commands.json';
import Link from '@docusaurus/Link';
import styles from './CommandIndex.module.css';

const CATEGORIES = [
  {
    slug: '/commands/general',
    label: 'General',
    description: 'Reload, config, backends, import, export, debug, and server-wide tools.',
  },
  {
    slug: '/commands/users',
    label: 'Users',
    description: 'Player permissions, groups, prefixes, timed nodes, and cleanup.',
  },
  {
    slug: '/commands/groups',
    label: 'Groups',
    description: 'Create groups, assign permissions, parents, weight, and defaults.',
  },
  {
    slug: '/commands/permissions',
    label: 'Permissions',
    description: 'Grant, remove, swap, and inspect permission nodes.',
  },
  {
    slug: '/commands/worlds',
    label: 'Worlds',
    description: 'Multi-world contexts, inheritance, and realm-specific rules.',
  },
  {
    slug: '/commands/ranks',
    label: 'Ranks',
    description: 'Rank ladders — modern `/pex ladder` subcommands; classic promote/demote shortcuts.',
  },
] as const;

function countForCategory(label: string): number {
  return (commands as CommandEntry[]).filter((entry) => entry.category === label).length;
}

export default function CommandIndex(): JSX.Element {
  return (
    <div className={styles.grid}>
      {CATEGORIES.map((category) => {
        const count = countForCategory(category.label);
        return (
          <Link key={category.slug} className={styles.card} to={category.slug}>
            <div className={styles.cardHeader}>
              <span className={styles.label}>{category.label}</span>
              <span className={styles.count}>
                {count} command{count === 1 ? '' : 's'}
              </span>
            </div>
            <p className={styles.description}>{category.description}</p>
          </Link>
        );
      })}
    </div>
  );
}
