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
      label: 'Commands',
      collapsed: false,
      link: {type: 'doc', id: 'commands/index'},
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
      label: 'Configuration',
      collapsed: false,
      items: ['configuration', 'storage'],
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
        {
          type: 'category',
          label: 'API Reference',
          collapsed: false,
          items: [
            'developers/api/index',
            'developers/api/modern',
            'developers/api/legacy',
            'developers/api/invariants',
            'developers/api/future',
          ],
        },
        'developers/architecture',
        'developers/compatibility',
        'developers/bootstrap',
        'developers/testing-matrix',
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
