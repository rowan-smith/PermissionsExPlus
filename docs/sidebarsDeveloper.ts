import type { SidebarsConfig } from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  developerSidebar: [
    'overview',
    {
      type: 'category',
      label: 'Setup',
      collapsed: false,
      items: [
        'installing',
        'depending',
      ],
    },
    {
      type: 'category',
      label: 'Users',
      collapsed: false,
      items: [
        'user-lookup',
        'user-membership',
        'user-examples',
      ],
    },
    {
      type: 'category',
      label: 'Groups',
      collapsed: false,
      items: [
        'groups',
        'group-inheritance',
        'ladders',
        'promote-demote',
      ],
    },
    {
      type: 'category',
      label: 'Permissions',
      collapsed: false,
      items: [
        'permissions',
        'permission-checks',
        'options',
        'prefix-suffix-weight',
      ],
    },
    {
      type: 'category',
      label: 'Advanced',
      collapsed: true,
      items: [
        'events',
        'worlds',
        'backends',
        'debug-and-matching',
        'caching',
      ],
    },
  ],
};

export default sidebars;
