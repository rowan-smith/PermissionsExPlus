import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import siteVars from './site-vars.json';
import siteVarsPlugin from './src/remark/siteVarsPlugin';

const JAVADOC_VERSIONS = [
  {label: `${siteVars.version} (current)`, href: `pathname:///apidocs/${siteVars.version}/index.html`},
  {label: '1.23.4', href: 'pathname:///apidocs/1.23.4/index.html'},
  {label: '1.23.3', href: 'pathname:///apidocs/1.23.3/index.html'},
  {label: '1.23.2', href: 'pathname:///apidocs/1.23.2/index.html'},
  {label: '1.23.1', href: 'pathname:///apidocs/1.23.1/index.html'},
  {label: '1.22.1', href: 'pathname:///apidocs/1.22.1/apidocs/index.html'},
];

const config: Config = {
  title: 'PermissionsExPlus',
  tagline:
    'Permissions plugin for Minecraft servers — users, groups, worlds, and /pex commands.',
  favicon: 'img/favicon.ico',

  url: siteVars.siteUrl,
  baseUrl: siteVars.baseUrl,

  organizationName: 'rowan-smith',
  projectName: 'PermissionsExPlus',

  customFields: {
    ...siteVars,
  },

  onBrokenLinks: 'throw',
  trailingSlash: false,

  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
  },

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          routeBasePath: '/',
          sidebarPath: './sidebars.ts',
          editUrl: `https://github.com/${siteVars.repo}/edit/gh-pages/`,
          remarkPlugins: [siteVarsPlugin],
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themes: [
    [
      require.resolve('@easyops-cn/docusaurus-search-local'),
      {
        hashed: true,
        language: ['en'],
        indexDocs: true,
        docsRouteBasePath: '/',
        highlightSearchTermsOnTargetPage: true,
        searchResultLimits: 12,
        searchBarShortcutHint: true,
      },
    ],
  ],

  themeConfig: {
    announcementBar: {
      id: 'pex_migration',
      content:
        'New to PermissionsExPlus or migrating from original PEX? Read the <a href="/faq/migration">migration guide</a> · <a href="/changelog">Changelog</a>',
      backgroundColor: '#0f2922',
      textColor: '#86efac',
      isCloseable: true,
    },
    metadata: [
      {
        name: 'description',
        content:
          'Permissions plugin for Minecraft servers. Manage users, groups, permissions, prefixes, and multi-world setups with familiar /pex commands.',
      },
    ],
    image: 'img/pex-social-card.png',
    navbar: {
      title: 'PermissionsExPlus',
      logo: {
        alt: 'PermissionsExPlus',
        src: 'img/pex-logo.svg',
      },
      hideOnScroll: true,
      items: [
        {
          type: 'dropdown',
          label: 'Getting Started',
          position: 'left',
          items: [
            {type: 'doc', docId: 'index', label: 'Overview'},
            {type: 'doc', docId: 'requirements', label: 'Requirements'},
            {type: 'doc', docId: 'concepts/permissions', label: 'How Permissions Work'},
            {type: 'doc', docId: 'concepts/inheritance', label: 'Inheritance'},
            {type: 'doc', docId: 'concepts/context', label: 'Context & Worlds'},
            {type: 'doc', docId: 'concepts/weight', label: 'Weight'},
            {type: 'doc', docId: 'concepts/meta', label: 'Prefix & Meta'},
          ],
        },
        {
          type: 'dropdown',
          label: 'Commands',
          position: 'left',
          items: [
            {type: 'doc', docId: 'commands/general', label: 'General'},
            {type: 'doc', docId: 'commands/users', label: 'Users'},
            {type: 'doc', docId: 'commands/groups', label: 'Groups'},
            {type: 'doc', docId: 'commands/permissions', label: 'Permissions'},
            {type: 'doc', docId: 'commands/worlds', label: 'Worlds'},
            {type: 'doc', docId: 'commands/ranks', label: 'Ranks'},
          ],
        },
        {
          type: 'dropdown',
          label: 'Configuration',
          position: 'left',
          items: [
            {type: 'doc', docId: 'configuration', label: 'config.yml & permissions.yml'},
            {type: 'doc', docId: 'storage', label: 'Storage & Backends'},
          ],
        },
        {
          type: 'dropdown',
          label: 'Guides',
          position: 'left',
          items: [
            {type: 'doc', docId: 'guides/recipes', label: 'Common Setups'},
            {type: 'doc', docId: 'guides/troubleshooting', label: 'Troubleshooting'},
            {type: 'doc', docId: 'faq/default-groups', label: 'Default Groups'},
            {type: 'doc', docId: 'faq/migration', label: 'Migration'},
            {type: 'doc', docId: 'changelog', label: 'Changelog'},
          ],
        },
        {
          type: 'dropdown',
          label: 'API',
          position: 'left',
          items: [
            {type: 'doc', docId: 'developers/index', label: 'Developer Overview'},
            {type: 'doc', docId: 'developers/cookbook', label: 'API Cookbook'},
            {type: 'doc', docId: 'developers/api/index', label: 'Hook Plugin API'},
            {type: 'doc', docId: 'developers/api/modern', label: 'Modern API Reference'},
            {type: 'doc', docId: 'developers/api/legacy', label: 'Legacy API Reference'},
            {type: 'doc', docId: 'developers/architecture', label: 'Architecture'},
            {type: 'doc', docId: 'developers/reference', label: 'Javadoc Hub'},
            {type: 'doc', docId: 'developers/contributing', label: 'Contributing'},
            {type: 'html', value: '<hr style="margin:0.35rem 0;border-color:var(--ifm-color-emphasis-200)">'},
            ...JAVADOC_VERSIONS.map((v) => ({
              label: `Javadoc ${v.label}`,
              href: v.href,
            })),
            {type: 'html', value: '<hr style="margin:0.35rem 0;border-color:var(--ifm-color-emphasis-200)">'},
            {
              href: `https://github.com/${siteVars.repo}/releases`,
              label: 'Release notes (GitHub)',
            },
            {type: 'doc', docId: 'changelog', label: 'Changelog'},
          ],
        },
        {
          href: `https://github.com/${siteVars.repo}/releases`,
          label: 'Download',
          position: 'right',
          className: 'navbar-download-btn',
        },
        {
          type: 'dropdown',
          label: siteVars.version,
          position: 'right',
          className: 'navbar-version-badge',
          items: [
            {
              href: `https://github.com/${siteVars.repo}/releases/tag/v${siteVars.version}`,
              label: `Release v${siteVars.version}`,
            },
            {
              href: `pathname:///apidocs/${siteVars.version}/index.html`,
              label: 'Current Javadoc',
            },
            {type: 'doc', docId: 'developers/reference', label: 'All API versions'},
            {type: 'doc', docId: 'changelog', label: 'Changelog'},
            {
              href: `https://github.com/${siteVars.repo}/releases`,
              label: 'Older releases',
            },
          ],
        },
        {
          href: `https://github.com/${siteVars.repo}`,
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Getting Started',
          items: [
            {label: 'Overview', to: '/'},
            {label: 'Requirements', to: '/requirements'},
            {label: 'Concepts', to: '/concepts/permissions'},
          ],
        },
        {
          title: 'Reference',
          items: [
            {label: 'Commands', to: '/commands/general'},
            {label: 'Configuration', to: '/configuration'},
            {label: 'API Cookbook', to: '/developers/cookbook'},
            {label: 'Hook Plugin API', to: '/developers/api'},
            {label: 'Architecture', to: '/developers/architecture'},
            {label: 'Javadoc', to: '/developers/reference'},
          ],
        },
        {
          title: 'Project',
          items: [
            {label: 'Changelog', to: '/changelog'},
            {label: 'GitHub', href: `https://github.com/${siteVars.repo}`},
            {label: 'Releases', href: `https://github.com/${siteVars.repo}/releases`},
            {label: 'Issues', href: `https://github.com/${siteVars.repo}/issues`},
            {
              label: 'License',
              href: `https://github.com/${siteVars.repo}/blob/main/LICENSE`,
            },
          ],
        },
      ],
      copyright: `© ${new Date().getFullYear()} PermissionsExPlus v${siteVars.version}`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: ['bash', 'yaml', 'java'],
    },
    colorMode: {
      defaultMode: 'light',
      respectPrefersColorScheme: true,
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
