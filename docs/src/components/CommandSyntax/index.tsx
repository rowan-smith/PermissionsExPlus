import React, { type ReactNode } from 'react';
import { argClassName, parseCommandParts } from '../../utils/commandSyntax';

type Props = {
  command: string;
  className?: string;
};

/** Renders a PEX command with required/optional argument highlighting. */
export default function CommandSyntax({command, className}: Props): ReactNode {
  return (
    <code className={['command-syntax', className].filter(Boolean).join(' ')}>
      {parseCommandParts(command).map((part, index) => {
        if (part.type === 'text') {
          return <React.Fragment key={index}>{part.value}</React.Fragment>;
        }
        return (
          <span key={index} className={argClassName(part.type)}>
            {part.value}
          </span>
        );
      })}
    </code>
  );
}
