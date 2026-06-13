import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import siteVars from './site-vars.json';
import siteVarsPlugin from './src/remark/siteVarsPlugin';

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
          editUrl: `https://github.com/${siteVars.repo}/tree/gh-pages/`,
          remarkPlugins: [siteVarsPlugin],
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
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
      hideOnScroll: true,
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'docsSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          href: `https://github.com/${siteVars.repo}/releases`,
          label: 'Download',
          position: 'right',
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
          title: 'Documentation',
          items: [
            {label: 'Getting Started', to: '/'},
            {label: 'Requirements', to: '/requirements'},
            {label: 'Commands', to: '/commands/general'},
            {label: 'API Cookbook', to: '/developers/cookbook'},
          ],
        },
        {
          title: 'Project',
          items: [
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
      copyright: `PermissionsExPlus v${siteVars.version} · Built with Docusaurus`,
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
