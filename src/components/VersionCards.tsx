import Link from '@docusaurus/Link';
import siteVars from '@site/site-vars.json';
import styles from './VersionCards.module.css';

type Card = {
  href: string;
  label: string;
  description: string;
  external?: boolean;
};

const LEGACY_JAVADOC_VERSIONS = ['1.23.4', '1.23.3', '1.23.2', '1.23.1'] as const;

type Props = {
  type: 'home' | 'javadoc';
};

export default function VersionCards({type}: Props): JSX.Element {
  const homeCards: Card[] = [
    {
      href: `https://github.com/${siteVars.repo}/releases`,
      label: `Download v${siteVars.version}`,
      description: 'Get the latest release jar',
      external: true,
    },
    {
      href: '/guides/recipes',
      label: 'Common Setups',
      description: 'Staff ranks, VIP, survival — copy-paste recipes',
    },
    {
      href: '/commands/general',
      label: 'Command Reference',
      description: 'Full /pex command documentation',
    },
  ];

  const javadocCards: Card[] = [
    {
      label: `${siteVars.version} (current)`,
      href: `pathname:///apidocs/${siteVars.version}/index.html`,
      description: 'Modern + legacy API (PermissionsExPlus)',
    },
    ...LEGACY_JAVADOC_VERSIONS.map((version) => ({
      label: version,
      href: `pathname:///apidocs/${version}/index.html`,
      description: 'Classic PermissionsEx API',
    })),
    {
      label: '1.22.1',
      href: 'pathname:///apidocs/1.22.1/apidocs/index.html',
      description: 'Original PermissionsEx API',
    },
  ];

  const cards = type === 'home' ? homeCards : javadocCards;

  return (
    <div className={styles.grid}>
      {cards.map((card) =>
        card.external ? (
          <a
            key={card.href}
            className={styles.card}
            href={card.href}
            target="_blank"
            rel="noopener noreferrer">
            <span className={styles.label}>{card.label}</span>
            <span className={styles.description}>{card.description}</span>
          </a>
        ) : (
          <Link key={card.href} className={styles.card} to={card.href}>
            <span className={styles.label}>{card.label}</span>
            <span className={styles.description}>{card.description}</span>
          </Link>
        ),
      )}
    </div>
  );
}
