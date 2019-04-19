"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const postcss = require("postcss");
exports.default = postcss.plugin('trim', () => (css) => {
    css.walk(({ type, raws }) => {
        if (type === 'rule' || type === 'atrule') {
            raws.before = raws.after = '\n';
        }
    });
});
