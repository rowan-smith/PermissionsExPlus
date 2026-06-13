import type {Plugin} from 'unified';
import type {Root} from 'mdast';
import {visit} from 'unist-util-visit';
import siteVars from '../../site-vars.json';

function substitute(value: string): string {
  return value
    .replace(/%%site\.version%%/g, siteVars.version)
    .replace(/%%site\.repo%%/g, siteVars.repo)
    .replace(/%%site\.baseurl%%/g, siteVars.baseUrl);
}

const siteVarsPlugin: Plugin<[], Root> = () => (tree) => {
  visit(tree, (node) => {
    if (node.type === 'text' || node.type === 'inlineCode') {
      node.value = substitute(node.value);
    }
    if (node.type === 'code') {
      node.value = substitute(node.value);
    }
    if (node.type === 'link' || node.type === 'definition') {
      node.url = substitute(node.url);
    }
  });
};

export default siteVarsPlugin;
