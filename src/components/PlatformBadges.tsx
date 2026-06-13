import type {CSSProperties} from 'react';
import styles from './PlatformBadges.module.css';

const PLATFORMS = [
  {name: 'Spigot', color: '#f59e0b', abbr: 'S'},
  {name: 'Paper', color: '#ef4444', abbr: 'P'},
  {name: 'BungeeCord', color: '#3b82f6', abbr: 'B'},
  {name: 'Velocity', color: '#06b6d4', abbr: 'V'},
  {name: 'Sponge', color: '#eab308', abbr: 'Sp'},
];

export default function PlatformBadges(): JSX.Element {
  return (
    <div className={styles.row} aria-label="Supported platforms">
      <span className={styles.label}>One jar · runs on</span>
      <ul className={styles.list}>
        {PLATFORMS.map((platform) => (
          <li key={platform.name}>
            <span
              className={styles.badge}
              style={{'--platform-color': platform.color} as CSSProperties}
              title={platform.name}>
              <span className={styles.icon}>{platform.abbr}</span>
              {platform.name}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
