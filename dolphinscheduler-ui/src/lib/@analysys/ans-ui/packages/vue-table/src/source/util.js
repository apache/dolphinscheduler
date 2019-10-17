import { getValueByPath } from '../../../../src/util'

export const ASCENDING_ORDER = 'asc'
export const DESCENDING_ORDER = 'desc'
export const ORDERS = [ASCENDING_ORDER, DESCENDING_ORDER]

export const toggleRowSelection = (states, row, selected) => {
  return toggleStateWithArray(states.selection, row, selected)
}

export const toggleRowExpansion = (states, row, expanded) => {
  return toggleStateWithArray(states.expandRows, row, expanded)
}

export const toggleRowUnfolding = (states, row, unfolded) => {
  let changed = toggleStateWithArray(states.unfoldedRows, row, unfolded)
  if (changed) {
    const rows = states._sortedData.slice()
    const index = rows.indexOf(row)
    if (states.unfoldedRows.includes(row)) {
      const insertionArray = []
      fillInsersionArray(states, row[states.childrenProp], insertionArray)
      rows.splice(index + 1, 0, ...insertionArray)
    } else {
      const nextSiblingIndex = rows.findIndex((r, i) => {
        return i > index && r.__level === row.__level
      })
      if (nextSiblingIndex !== -1) {
        rows.splice(index + 1, nextSiblingIndex - index - 1)
      } else {
        rows.splice(index + 1, rows.length - index - 1)
      }
    }
    states._sortedData = Object.freeze(rows.slice())
  }
  return changed
}

const fillInsersionArray = (states, children, array) => {
  const { unfoldedRows, childrenProp } = states
  children.forEach((c) => {
    array.push(c)
    const sub = c[childrenProp]
    if (sub && sub.length && unfoldedRows.includes(c)) {
      fillInsersionArray(states, sub, array)
    }
  })
}

const toggleStateWithArray = (array, row, insert) => {
  let changed = false
  const index = array.indexOf(row)
  if (typeof insert !== 'undefined') {
    if (insert) {
      if (index === -1) {
        array.push(row)
        changed = true
      }
    } else {
      if (index !== -1) {
        array.splice(index, 1)
        changed = true
      }
    }
  } else {
    if (index === -1) {
      array.push(row)
    } else {
      array.splice(index, 1)
    }
    changed = true
  }
  return changed
}

export const toggleSortCondition = (states, column, order) => {
  if (!column) {
    throw new Error('Table Store: Column is required!')
  }

  const prevSortingColumn = states.sortingColumn

  if (!order) {
    if (prevSortingColumn) {
      states.sortingColumn = null
    }
  } else if (prevSortingColumn !== column) {
    states.sortingColumn = column
  }

  if (prevSortingColumn && prevSortingColumn !== column) {
    prevSortingColumn.order = ''
  }

  const changed = prevSortingColumn !== states.sortingColumn || states.sortOrder !== order

  if (changed) {
    states.sortProp = states.sortingColumn ? states.sortingColumn.prop : null
    states.sortOrder = column.order = order
  }

  return changed
}

export const sortData = (data, states) => {
  data = data || []
  const { treeType, sortingColumn, sortProp: key, sortOrder: order } = states
  if (!sortingColumn || !key || !order || treeType || sortingColumn.sortable === 'custom') {
    return data.slice()
  }
  const reverse = order === DESCENDING_ORDER ? -1 : 1

  const sortMethod = sortingColumn.sortMethod

  const compare = (a, b) => {
    if (sortMethod) {
      return sortMethod(a.content, b.content, a.row, b.row)
    }
    // 默认排序规则
    for (let i = 0, len = a.content.length; i < len; i++) {
      if (a.content[i] < b.content[i]) {
        return -1
      }
      if (a.content[i] > b.content[i]) {
        return 1
      }
    }
    return 0
  }

  return data.map((row, index) => {
    return {
      row,
      index,
      content: getValueByPath(row, key)
    }
  }).sort((a, b) => {
    let order = compare(a, b)
    if (!order) {
      // make stable https://en.wikipedia.org/wiki/Sorting_algorithm#Stability
      order = a.index - b.index
    }
    return order * reverse
  }).map(item => item.row)
}

export const getKeysMap = (array, rowKey) => {
  const arrayMap = {}
  array.forEach((row, index) => {
    arrayMap[getValueByPath(row, rowKey)] = { row, index }
  })
  return arrayMap
}

export const doFlattenColumns = (columns) => {
  const result = []
  columns.forEach((column) => {
    if (column.children) {
      result.push.apply(result, doFlattenColumns(column.children))
    } else {
      result.push(column)
    }
  })
  return result
}

export const checkSortOrders = (list) => {
  let pass = true
  for (const order of list) {
    if (order && !ORDERS.includes(order)) {
      pass = false
      break
    }
  }
  return pass
}

export const getNextOrder = ({ order, sortOrders }) => {
  let index
  if (order) {
    index = sortOrders.indexOf(order)
  } else {
    index = sortOrders.findIndex(o => !o)
  }
  let nextOrder
  if (index !== -1) {
    nextOrder = sortOrders[index > sortOrders.length - 2 ? 0 : index + 1]
  } else {
    nextOrder = sortOrders[0]
  }
  return nextOrder || ''
}

export const reserve = (source, dataList, rowKey) => {
  source = source || []
  dataList = dataList || []
  if (!rowKey) {
    throw new Error('Table Store: Prop row-key should not be empty when reserve-states enabled.')
  }
  const idMap = getKeysMap(source, rowKey)
  const result = []
  for (const row of dataList) {
    const rowId = getValueByPath(row, rowKey)
    if (idMap[rowId]) {
      result.push(row)
    }
  }
  return result
}

export const clean = (source, dataList, rowKey) => {
  source = source || []
  dataList = dataList || []
  let list
  if (rowKey) {
    list = reserve(source, dataList, rowKey)
  } else {
    list = source.filter((item) => {
      return dataList.indexOf(item) !== -1
    })
  }

  let changed = list.length !== source.length
  return { changed, list }
}

export const processTreeData = (states, table) => {
  const { _data } = states
  const { childrenProp } = table
  if (!_data || !_data.length) return
  const parentRows = []
  const unfoldedRows = []
  checkParentRows(_data, childrenProp, parentRows, unfoldedRows, 1)
  states.treeType = parentRows.length !== 0
  states.unfoldedRows = unfoldedRows
  states.parentRows = parentRows
}

export const checkParentRows = (list, childrenProp, parentRows, unfoldedRows, level) => {
  list.forEach((row) => {
    row.__level = level
    const children = row[childrenProp]
    if (children && children.length) {
      parentRows.push(row)
      checkParentRows(children, childrenProp, parentRows, unfoldedRows, level + 1)
    }
  })
}

export const getRowKey = (i, row, rowKey) => {
  if (!row) {
    throw new Error('Table Body: Find invalid row!')
  }
  if (rowKey) {
    return getValueByPath(row, rowKey)
  }
  return i
}

export const getRowClasses = (i, stripe) => {
  const classes = []
  if (stripe) {
    classes.push(i % 2 === 1 ? 'striped-row' : 'no-striped-row')
  }
  return classes
}

export const getCellContent = (row, column, rowIndex, columnIndex) => {
  const value = getValueByPath(row, column.prop)
  if (column.formatter) {
    return column.formatter(row, column, value, rowIndex, columnIndex)
  }
  return value
}

export const getSpanModel = (row, column, rowIndex, columnIndex, cellSpanMethod) => {
  if (cellSpanMethod) {
    const result = cellSpanMethod({ row, column, rowIndex, columnIndex })
    let rowspan, colspan
    if (Array.isArray(result)) {
      rowspan = result[0]
      colspan = result[1]
    } else if (typeof result === 'object') {
      rowspan = result.rowspan
      colspan = result.colspan
    }
    return { colspan, rowspan }
  }
  return { colspan: 1, rowspan: 1 }
}

const getAllColumns = (columns) => {
  const result = []
  columns.forEach((column) => {
    if (column.children && column.children.length !== 0) {
      result.push(column)
      result.push.apply(result, getAllColumns(column.children))
    } else {
      result.push(column)
    }
  })
  return result
}

export const convertToRows = (originColumns) => {
  let maxLevel = 1
  const traverse = (column, parent) => {
    if (parent) {
      column.level = parent.level + 1
      if (maxLevel < column.level) {
        maxLevel = column.level
      }
    }
    if (column.children) {
      let colSpan = 0
      column.children.forEach((subColumn) => {
        traverse(subColumn, column)
        colSpan += subColumn.colSpan
      })
      column.colSpan = colSpan
    } else {
      column.colSpan = 1
    }
  }

  originColumns.forEach((column) => {
    column.level = 1
    traverse(column)
  })

  const rows = []
  for (let i = 0; i < maxLevel; i++) {
    rows.push([])
  }

  const allColumns = getAllColumns(originColumns)

  allColumns.forEach((column) => {
    if (!column.children) {
      column.rowSpan = maxLevel - column.level + 1
    } else {
      column.rowSpan = 1
    }
    rows[column.level - 1].push(column)
  })

  return rows
}
