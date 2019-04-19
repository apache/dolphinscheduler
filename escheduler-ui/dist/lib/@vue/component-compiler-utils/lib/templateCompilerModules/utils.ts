export interface Attr {
  name: string
  value: string
}

export interface ASTNode {
  tag: string
  attrs: Attr[]
}

export function urlToRequire(url: string): string {
  // same logic as in transform-require.js
  const firstChar = url.charAt(0)
  if (firstChar === '.' || firstChar === '~' || firstChar === '@') {
    if (firstChar === '~') {
      const secondChar = url.charAt(1)
      url = url.slice(secondChar === '/' ? 2 : 1)
    }
    return `require("${url}")`
  } else {
    return `"${url}"`
  }
}
