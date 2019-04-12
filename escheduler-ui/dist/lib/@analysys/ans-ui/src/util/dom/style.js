const formatSize = (prop) => {
  const propType = typeof prop
  if (propType === 'number') {
    return prop + 'px'
  }
  if (propType === 'string') {
    if (/^\d+$/.test(prop)) return parseInt(prop, 10) + 'px'
    return prop
  }
  return null
}

export {
  formatSize
}
