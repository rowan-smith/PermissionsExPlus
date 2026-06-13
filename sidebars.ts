import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  docsSidebar: [
    {
      type: 'category',
      label: 'Getting Started',
      collapsed: false,
      items: [
        'index',
        'requirements',
        'concepts/permissions',
        'concepts/inheritance',
        'concepts/context',
        'concepts/weight',
        'concepts/meta',
      ],
    },
    {
      type: 'category',
      label: 'Configuration',
      collapsed: false,
      items: ['configuration', 'storage'],
    },
    {
      type: 'category',
      label: 'Commands',
      collapsed: false,
      items: [
        'commands/general',
        'commands/users',
        'commands/groups',
        'commands/permissions',
        'commands/worlds',
        'commands/ranks',
      ],
    },
    {
      type: 'category',
      label: 'Guides',
      collapsed: false,
      items: [
        'guides/recipes',
        'guides/troubleshooting',
        'faq/default-groups',
        'faq/migration',
        'changelog',
      ],
    },
    {
      type: 'category',
      label: 'Developer Docs',
      collapsed: false,
      items: [
        'developers/index',
        'developers/cookbook',
        'developers/reference',
        'developers/contributing',
      ],
    },
    {
      type: 'category',
      label: 'Project',
      collapsed: true,
      items: ['about'],
    },
  ],
};

export default sidebars;
