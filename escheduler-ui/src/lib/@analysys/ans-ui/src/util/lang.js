'use strict'

/**
 * A simple uuid generator, support prefix and template pattern.
 *
 * @example
 *
 *  uuid('v-') // -> v-xxx
 *  uuid('v-ani-%{s}-translate')  // -> v-ani-xxx
 */
function uuid (prefix) {
  var id = Math.floor(Math.random() * 10000).toString(36)
  return prefix ? (
    ~prefix.indexOf('%{s}') ? (
      prefix.replace(/%\{s\}/g, id)
    ) : (prefix + id)
  ) : id
}

const getValueByPath = (model, path) => {
  if (!path || !model) return ''
  if (path.indexOf('.') < 0) {
    return getArrayValue(model, path)
  }
  let key = path.split('.')
  let current = model
  for (let i = 0; i < key.length; i++) {
    let currentKey = key[i]
    current = getArrayValue(current, currentKey)
  }
  return current
}

function getArrayValue (model, key) {
  if (key.includes('[')) {
    let parts = key.split(/[[\]]/).filter(p => p !== '')
    let value = model[parts[0]]
    for (const index of parts.slice(1)) {
      value = value[index]
    }
    return value
  } else {
    return model[key]
  }
}

export {
  uuid,
  getValueByPath
}
