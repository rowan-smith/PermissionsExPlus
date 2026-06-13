import siteVars from '@site/site-vars.json';

export type JavadocVersion = {
  version: string;
  label: string;
  href: string;
  description: string;
  current?: boolean;
};

const LEGACY_VERSIONS = ['1.23.4', '1.23.3', '1.23.2', '1.23.1'] as const;

export function getJavadocVersions(): JavadocVersion[] {
  return [
    {
      version: siteVars.version,
      label: `${siteVars.version} (current)`,
      href: `pathname:///apidocs/${siteVars.version}/index.html`,
      description: 'Modern + legacy API',
      current: true,
    },
    ...LEGACY_VERSIONS.map((version) => ({
      version,
      label: version,
      href: `pathname:///apidocs/${version}/index.html`,
      description: 'Classic PermissionsEx API',
    })),
    {
      version: '1.22.1',
      label: '1.22.1',
      href: 'pathname:///apidocs/1.22.1/apidocs/index.html',
      description: 'Original PermissionsEx API',
    },
  ];
}
