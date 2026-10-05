import { themes as prismThemes } from 'prism-react-renderer';
import type { Config } from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';
import type { Options as DocsOptions } from '@docusaurus/plugin-content-docs';
import rehypeCommandSyntax from './src/remark/rehypeCommandSyntax';

/** Shared version labels for Documentation + Developer (keep in sync). */
const docsVersions: NonNullable<DocsOptions['versions']> = {
  current: {
    label: '2.0.0',
    path: 'next',
    banner: 'unreleased',
  },
  '1.23.5': {
    label: '1.23.5',
    banner: 'none',
  },
};

const sharedDocsOptions = {
  lastVersion: '1.23.5',
  includeCurrentVersion: true,
  versions: docsVersions,
  rehypePlugins: [rehypeCommandSyntax],
} satisfies Partial<DocsOptions>;

const config: Config = {
  title: 'PermissionsExPlus',
  tagline: 'Modern Bukkit permissions plugin',
  favicon: 'img/favicon.svg',

  future: {
    v4: true,
  },

  url: 'https://permissionsexplus.rono.dev',
  baseUrl: '/',

  organizationName: 'rowan-smith',
  projectName: 'PermissionsExPlus',

  onBrokenLinks: 'warn',

  plugins: [
    [
      '@docusaurus/plugin-content-docs',
      {
        id: 'developer',
        path: 'developer',
        routeBasePath: 'developer',
        sidebarPath: './sidebarsDeveloper.ts',
        ...sharedDocsOptions,
      } satisfies DocsOptions,
    ],
    [
      require.resolve('@easyops-cn/docusaurus-search-local'),
      {
        hashed: true,
        indexDocs: true,
        indexPages: true,
        docsRouteBasePath: ['docs', 'developer'],
        docsDir: ['documentation', 'developer'],
        highlightSearchTermsOnTargetPage: true,
      },
    ],
  ],

  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      {
        docs: {
          path: 'documentation',
          routeBasePath: 'docs',
          sidebarPath: './sidebarsDocumentation.ts',
          ...sharedDocsOptions,
        },
        blog: false,
        theme: {
          customCss: './src/css/custom.css',
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
    image: 'img/docusaurus-social-card.jpg',
    colorMode: {
      respectPrefersColorScheme: true,
    },
    navbar: {
      title: '',
      logo: {
        alt: 'PermissionsExPlus Logo',
        src: 'img/logo.svg',
        srcDark: 'img/logo-dark.svg',
      },
      items: [
        {
          type: 'docSidebar',
          sidebarId: 'tutorialSidebar',
          position: 'left',
          label: 'Documentation',
        },
        {
          type: 'docSidebar',
          docsPluginId: 'developer',
          sidebarId: 'developerSidebar',
          position: 'left',
          label: 'Developer',
        },
        {
          type: 'docsVersionDropdown',
          position: 'right',
          dropdownActiveClassDisabled: true,
          className: 'navbar-version-dropdown navbar-version-dropdown--docs',
        },
        {
          type: 'docsVersionDropdown',
          docsPluginId: 'developer',
          position: 'right',
          dropdownActiveClassDisabled: true,
          className: 'navbar-version-dropdown navbar-version-dropdown--developer',
        },
        {
          type: 'search',
          position: 'right',
        },
        {
          href: 'https://github.com/rowan-smith/PermissionsExPlus',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [],
      copyright: `Copyright © ${new Date().getFullYear()} Rono.`,
    },
    prism: {
      theme: prismThemes.github,
      darkTheme: prismThemes.dracula,
      additionalLanguages: [
        'bash',
        'java',
        'groovy',
        'kotlin',
        'yaml',
        'diff',
        'json',
      ],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
