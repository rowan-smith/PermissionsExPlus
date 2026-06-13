import Link from '@docusaurus/Link';
import PlatformBadges from '@site/src/components/PlatformBadges';
import siteVars from '@site/site-vars.json';
import styles from './HomepageHero.module.css';

export default function HomepageHero(): JSX.Element {
  return (
    <header className={styles.hero}>
      <div className={styles.grid} aria-hidden="true" />
      <div className={styles.inner}>
        <span className={styles.badge}>v{siteVars.version} · Minecraft permissions</span>
        <h1 className={styles.title}>
          Control who can do <em>what</em> on your server
        </h1>
        <p className={styles.subtitle}>
          PermissionsExPlus brings familiar <code>/pex</code> commands, group inheritance,
          world contexts, and a modern plugin API — one jar for Spigot, Paper, BungeeCord,
          Velocity, and Sponge.
        </p>
        <div className={styles.actions}>
          <a
            className={styles.primary}
            href={`https://github.com/${siteVars.repo}/releases`}
            target="_blank"
            rel="noopener noreferrer">
            Download {siteVars.jarName}
          </a>
          <Link className={styles.secondary} to="/commands/general">
            Command reference
          </Link>
          <Link className={styles.ghost} to="/developers/cookbook">
            API cookbook
          </Link>
        </div>
        <PlatformBadges />
      </div>
    </header>
  );
}
