export type CommandPart =
  | {type: 'text'; value: string}
  | {type: 'required' | 'optional'; value: string};

/** Split a command string into plain text + required/optional argument parts. */
export function parseCommandParts(command: string): CommandPart[] {
  const parts: CommandPart[] = [];
  const pattern = /(<[^>]+>|\[[^\]]+\])/g;
  let lastIndex = 0;
  let match: RegExpExecArray | null;

  while ((match = pattern.exec(command)) !== null) {
    if (match.index > lastIndex) {
      parts.push({type: 'text', value: command.slice(lastIndex, match.index)});
    }
    const token = match[0];
    parts.push({
      type: token.startsWith('<') ? 'required' : 'optional',
      value: token,
    });
    lastIndex = match.index + token.length;
  }

  if (lastIndex < command.length) {
    parts.push({type: 'text', value: command.slice(lastIndex)});
  }

  return parts.length > 0 ? parts : [{type: 'text', value: command}];
}

export function argClassName(type: 'required' | 'optional'): string {
  return type === 'required' ? 'cmd-arg-required' : 'cmd-arg-optional';
}
