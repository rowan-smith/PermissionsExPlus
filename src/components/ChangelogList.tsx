import releases from '@site/src/data/releases.json';
import styles from './ChangelogList.module.css';

type Release = {
  tag: string;
  name: string;
  publishedAt: string;
  url: string;
  body: string;
  prerelease: boolean;
};

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('en-GB', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export default function ChangelogList(): JSX.Element {
  const items = releases as Release[];

  if (items.length === 0) {
    return <p>No releases found.</p>;
  }

  return (
    <div className={styles.list}>
      {items.map((release) => (
        <article key={release.tag} className={styles.entry}>
          <header className={styles.header}>
            <div>
              <h2 className={styles.tag}>
                <a href={release.url} target="_blank" rel="noopener noreferrer">
                  {release.name}
                </a>
                {release.prerelease && <span className={styles.pre}>Pre-release</span>}
              </h2>
              <time className={styles.date} dateTime={release.publishedAt}>
                {formatDate(release.publishedAt)}
              </time>
            </div>
            <a
              className={styles.download}
              href={release.url}
              target="_blank"
              rel="noopener noreferrer">
              View on GitHub →
            </a>
          </header>
          {release.body ? (
            <div className={`${styles.body} markdown`}>
              {release.body.split('\n').map((line, i) => (
                <p key={i}>{line || '\u00A0'}</p>
              ))}
            </div>
          ) : (
            <p className={styles.empty}>No release notes provided.</p>
          )}
        </article>
      ))}
    </div>
  );
}
