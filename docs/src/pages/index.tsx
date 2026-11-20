import type { ReactNode } from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import useBaseUrl from '@docusaurus/useBaseUrl';
import ThemedImage from '@theme/ThemedImage';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import Heading from '@theme/Heading';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero', styles.heroBanner)}>
      <div className="container">
        <div className={styles.heroLogoSection}>
          <ThemedImage
            alt="PermissionsExPlus Logo"
            sources={{
              light: useBaseUrl('/img/logo.svg'),
              dark: useBaseUrl('/img/logo-dark.svg'),
            }}
            className={styles.heroLogoBanner}
          />
          <Heading as="h1" className={styles.heroTitleHidden}>
            PermissionsExPlus
          </Heading>
        </div>
        <p className={styles.heroSubtitle}>
          PermissionsEx you know and love, updated and maintained for modern servers.
        </p>
        <div className={styles.buttons}>
          <Link
            className={clsx('button button--primary button--lg', styles.heroButton)}
            to="/docs/getting-started/overview">
            Get Started
          </Link>
          <Link
            className={clsx('button button--outline button--lg', styles.heroButtonSecondary)}
            to="/docs/intro">
            Documentation
          </Link>
        </div>
      </div>
    </header>
  );
}

export default function Home(): ReactNode {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`Hello from ${siteConfig.title}`}
      description="Description will go into a meta tag in <head />">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
