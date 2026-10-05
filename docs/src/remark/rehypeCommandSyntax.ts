type HastText = {type: 'text'; value: string};
type HastElement = {
  type: 'element';
  tagName: string;
  properties?: Record<string, unknown>;
  children?: HastNode[];
};
type HastNode = HastText | HastElement | {type: string; [key: string]: unknown};

import { argClassName, parseCommandParts } from '../utils/commandSyntax';

function getText(node: HastElement): string {
  return (node.children || [])
    .map((child) => (child.type === 'text' ? (child as HastText).value : ''))
    .join('');
}

function textNode(value: string): HastText {
  return {type: 'text', value};
}

function span(className: string, value: string): HastElement {
  return {
    type: 'element',
    tagName: 'span',
    properties: {className: [className]},
    children: [textNode(value)],
  };
}

function partsToHast(command: string): Array<HastText | HastElement> {
  return parseCommandParts(command).map((part) => {
    if (part.type === 'text') {
      return textNode(part.value);
    }
    return span(argClassName(part.type), part.value);
  });
}

function colorizeCodeElement(node: HastElement, mode: 'command' | 'arg-only'): void {
  const text = getText(node);
  if (!text) {
    return;
  }

  const existingClass = node.properties?.className;
  const classList = Array.isArray(existingClass)
    ? [...(existingClass as string[])]
    : typeof existingClass === 'string'
      ? [existingClass]
      : [];

  if (mode === 'arg-only') {
    if (/^<[^>]+>$/.test(text)) {
      classList.push('cmd-arg-required');
      node.properties = {...node.properties, className: classList};
      return;
    }
    if (/^\[[^\]]+\]$/.test(text)) {
      classList.push('cmd-arg-optional');
      node.properties = {...node.properties, className: classList};
      return;
    }
    return;
  }

  // Always normalize slash-command headings to the same monospace style,
  // even when there are no <required> / [optional] args to color.
  const isSlashCommand = /^\/[a-z]/i.test(text.trim());
  if (!isSlashCommand && !text.includes('<') && !text.includes('[')) {
    return;
  }

  // Use a span wrapper so Docusaurus does not promote this to a code block
  node.tagName = 'span';
  node.properties = {
    className: ['command-syntax'],
  };
  node.children = partsToHast(text);
}

function walk(node: HastNode, inHeading: boolean, colorArgs: boolean): void {
  if (node.type !== 'element') {
    return;
  }

  const el = node as HastElement;
  const isHeading = /^h[1-6]$/.test(el.tagName);
  const nextInHeading = inHeading || isHeading;

  if (el.tagName === 'code') {
    if (nextInHeading) {
      colorizeCodeElement(el, 'command');
    } else if (colorArgs) {
      colorizeCodeElement(el, 'arg-only');
    }
    return;
  }

  for (const child of el.children || []) {
    walk(child, nextInHeading, colorArgs);
  }
}

/**
 * Colorize required `<arg>` (blue) and optional `[arg]` (orange) in command docs.
 */
export default function rehypeCommandSyntax() {
  return (tree: HastElement & {children?: HastNode[]}, file: {path?: string; history?: string[]}) => {
    const path = String(file.path || file.history?.[0] || '');
    const normalized = path.replace(/\\/g, '/');
    const colorArgs = normalized.includes('/commands/');

    for (const child of tree.children || []) {
      walk(child, false, colorArgs);
    }
  };
}
