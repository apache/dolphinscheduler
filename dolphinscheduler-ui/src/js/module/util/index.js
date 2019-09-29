/**
 * Find component downward
 */
export function findComponentDownward (context, componentName) {
  const childrens = context.$children
  let children = null
  if (childrens.length) {
    childrens.forEach(child => {
      const name = child.$options.name
      if (name === componentName) {
        children = child
      }
    })
    for (let i = 0; i < childrens.length; i++) {
      const child = childrens[i]
      const name = child.$options.name
      if (name === componentName) {
        children = child
        break
      } else {
        children = findComponentDownward(child, componentName)
        if (children) break
      }
    }
  }
  return children
}

/**
 * A simple uuid generator, support prefix and template pattern.
 *
 * @example
 *
 *  uuid('v-') // -> v-xxx
 *  uuid('v-ani-%{s}-translate')  // -> v-ani-xxx
 */
export function uuid (prefix) {
  let id = Math.floor(Math.random() * 10000).toString(36)
  return prefix ? (
    ~prefix.indexOf('%{s}') ? (
      prefix.replace(/%\{s\}/g, id)
    ) : (prefix + id)
  ) : id
}


/**
 * template
 *
 * @param {String} string
 * @param {Array} ...args
 * @return {String}
 */
const { hasOwnProperty } = {}
const RE_NARGS = /(%|)\{([0-9a-zA-Z_]+)\}/g
const hasOwn = (o, k) => hasOwnProperty.call(o, k)

export function template (string, ...args) {
  if (args.length === 1 && typeof args[0] === 'object') {
    args = args[0]
  }
  if (!args || !args.hasOwnProperty) {
    args = {}
  }
  return string.replace(RE_NARGS, (match, prefix, i, index) => {
    let result
    if (string[index - 1] === '{' &&
      string[index + match.length] === '}') {
      return i
    } else {
      result = hasOwn(args, i) ? args[i] : null
      if (result === null || result === undefined) {
        return ''
      }
      return result
    }
  })
}

