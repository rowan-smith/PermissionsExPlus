import type { ReactNode } from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  icon: string;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Familiar PEX Workflow',
    icon: '⌨️',
    description: (
      <>
        Same commands, groups, ladders, and inheritance model you already know.
        Drop in PermissionsExPlus and keep managing permissions the PEX way.
      </>
    ),
  },
  {
    title: 'Modern Server Ready',
    icon: '🧩',
    description: (
      <>
        Maintained for current Paper and Spigot builds, with UUID players,
        Adventure messaging, Vault, and PlaceholderAPI support.
      </>
    ),
  },
  {
    title: 'Flexible by Design',
    icon: '🗂️',
    description: (
      <>
        File, SQL, H2, or multi-backend storage, plus world-scoped permissions,
        timed grants, prefixes, and a plugin API for custom integrations.
      </>
    ),
  },
];

function Feature({title, icon, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className={styles.featureCard}>
        <div className={styles.featureIcon}>{icon}</div>
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
