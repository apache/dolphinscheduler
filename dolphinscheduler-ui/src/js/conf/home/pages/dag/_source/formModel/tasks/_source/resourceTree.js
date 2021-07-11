export function diGuiTree (item) { // Recursive convenience tree structure
  item.forEach(item => {
    item.children === '' || item.children === undefined || item.children === null || item.children.length === 0
      ? operationTree(item) : diGuiTree(item.children)
  })
}

export function operationTree (item) {
  if (item.dirctory) {
    item.isDisabled = true
  }
  delete item.children
}

export function searchTree (element, id) {
  // 根据id查找节点
  if (element.id === id) {
    return element
  } else if (element.children) {
    let i
    let result = null
    for (i = 0; result === null && i < element.children.length; i++) {
      result = searchTree(element.children[i], id)
    }
    return result
  }
  return null
}
