import Link from '@docusaurus/Link';
import {getJavadocVersions} from '@site/src/config/javadocVersions';
import siteVars from '@site/site-vars.json';
import styles from './VersionCards.module.css';

type Card = {
  href: string;
  label: string;
  description: string;
  external?: boolean;
};

type Props = {
  type: 'home' | 'javadoc';
};

export default function VersionCards({type}: Props): JSX.Element {
  const homeCards: Card[] = [
    {
      href: '/guides/recipes',
      label: 'Common Setups',
      description: 'Staff ranks, VIP, survival — copy-paste recipes',
    },
    {
      href: '/commands',
      label: 'Command Reference',
      description: 'Search and browse every /pex command',
    },
    {
      href: '/developers/cookbook',
      label: 'API Cookbook',
      description: 'Integrate your plugin with PEX',
    },
  ];

  const javadocCards: Card[] = getJavadocVersions().map((entry) => ({
    href: entry.href,
    label: entry.label,
    description: entry.description,
  }));

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
