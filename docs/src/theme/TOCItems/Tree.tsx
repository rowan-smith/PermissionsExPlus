import React, { type ReactNode } from 'react';
import OriginalTree from '@theme-original/TOCItems/Tree';
import type TreeType from '@theme/TOCItems/Tree';
import type { WrapperProps } from '@docusaurus/types';
import { argClassName, parseCommandParts } from '@site/src/utils/commandSyntax';

type Props = WrapperProps<typeof TreeType>;

type TocHeading = {
  id: string;
  value: string;
  children: TocHeading[];
};

function decodeBasicEntities(html: string): string {
  return html
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'");
}

function stripHtml(html: string): string {
  return decodeBasicEntities(html.replace(/<[^>]*>/g, ''));
}

function isSlashCommand(text: string): boolean {
  return /^\/[a-z]/i.test(text.trim());
}

function tocHasCommands(toc: TocHeading[]): boolean {
  return toc.some(
    (heading) =>
      isSlashCommand(stripHtml(heading.value)) ||
      tocHasCommands(heading.children ?? []),
  );
}

function highlightCommandLabel(value: string): ReactNode {
  const plain = stripHtml(value);

  if (!isSlashCommand(plain) && !plain.includes('<') && !plain.includes('[')) {
    return plain;
  }

  return (
    <span className="command-syntax">
      {parseCommandParts(plain).map((part, index) => {
        if (part.type === 'text') {
          return <React.Fragment key={index}>{part.value}</React.Fragment>;
        }
        return (
          <span key={index} className={argClassName(part.type)}>
            {part.value}
          </span>
        );
      })}
    </span>
  );
}

function HighlightedTree({
  toc,
  className,
  linkClassName,
  isChild = false,
}: {
  toc: TocHeading[];
  className?: string;
  linkClassName?: string | null;
  isChild?: boolean;
}): ReactNode {
  if (!toc.length) {
    return null;
  }

  return (
    <ul className={isChild ? undefined : className}>
      {toc.map((heading) => (
        <li key={heading.id}>
          <a href={`#${heading.id}`} className={linkClassName ?? undefined}>
            {highlightCommandLabel(heading.value)}
          </a>
          <HighlightedTree
            isChild
            toc={heading.children ?? []}
            className={className}
            linkClassName={linkClassName}
          />
        </li>
      ))}
    </ul>
  );
}

export default function TreeWrapper(props: Props): ReactNode {
  const toc = (props.toc ?? []) as TocHeading[];

  if (!tocHasCommands(toc)) {
    return <OriginalTree {...props} />;
  }

  return (
    <HighlightedTree
      toc={toc}
      className={props.className}
      linkClassName={props.linkClassName}
    />
  );
}
