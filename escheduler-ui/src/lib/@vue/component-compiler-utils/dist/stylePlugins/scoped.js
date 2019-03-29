"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const postcss = require("postcss");
// postcss-selector-parser does have typings but it's problematic to work with.
const selectorParser = require('postcss-selector-parser');
exports.default = postcss.plugin('add-id', (options) => (root) => {
    const id = options;
    const keyframes = Object.create(null);
    root.each(function rewriteSelector(node) {
        if (!node.selector) {
            // handle media queries
            if (node.type === 'atrule') {
                if (node.name === 'media' || node.name === 'supports') {
                    node.each(rewriteSelector);
                }
                else if (/-?keyframes$/.test(node.name)) {
                    // register keyframes
                    keyframes[node.params] = node.params = node.params + '-' + id;
                }
            }
            return;
        }
        node.selector = selectorParser((selectors) => {
            selectors.each((selector) => {
                let node = null;
                let hasDeep = false;
                selector.each((n) => {
                    // ">>>" combinator
                    if (n.type === 'combinator' && n.value === '>>>') {
                        n.value = ' ';
                        n.spaces.before = n.spaces.after = '';
                        hasDeep = true;
                        return false;
                    }
                    // /deep/ alias for >>>, since >>> doesn't work in SASS
                    if (n.type === 'tag' && n.value === '/deep/') {
                        const prev = n.prev();
                        if (prev && prev.type === 'combinator' && prev.value === ' ') {
                            prev.remove();
                        }
                        n.remove();
                        hasDeep = true;
                        return false;
                    }
                    if (n.type !== 'pseudo' && n.type !== 'combinator') {
                        node = n;
                    }
                });
                if (node) {
                    node.spaces.after = '';
                    selector.insertAfter(node, selectorParser.attribute({
                        attribute: id
                    }));
                }
                else if (hasDeep) {
                    selector.prepend(selectorParser.attribute({
                        attribute: id
                    }));
                }
            });
        }).processSync(node.selector);
    });
    // If keyframes are found in this <style>, find and rewrite animation names
    // in declarations.
    // Caveat: this only works for keyframes and animation rules in the same
    // <style> element.
    if (Object.keys(keyframes).length) {
        root.walkDecls(decl => {
            // individual animation-name declaration
            if (/^(-\w+-)?animation-name$/.test(decl.prop)) {
                decl.value = decl.value
                    .split(',')
                    .map(v => keyframes[v.trim()] || v.trim())
                    .join(',');
            }
            // shorthand
            if (/^(-\w+-)?animation$/.test(decl.prop)) {
                decl.value = decl.value
                    .split(',')
                    .map(v => {
                    const vals = v.trim().split(/\s+/);
                    const i = vals.findIndex(val => keyframes[val]);
                    if (i !== -1) {
                        vals.splice(i, 1, keyframes[vals[i]]);
                        return vals.join(' ');
                    }
                    else {
                        return v;
                    }
                })
                    .join(',');
            }
        });
    }
});
