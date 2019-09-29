import Vue from 'vue'
import { addClass, getValueByPath } from '../../../../src/util'

const ASCENDING_ORDER = 'asc'
const DESCENDING_ORDER = 'desc'
const ORDERS = [ASCENDING_ORDER, DESCENDING_ORDER]

export default class TableStore {
  constructor (table) {
    this.table = table
    this.states = {
      // 原始数据
      _data: null,
      _sortedData: null,
      // 当前展示数据
      data: null,
      _columns: [],
      columns: [],
      leafColumns: [],
      fixedLeftColumns: [],
      fixedLeftLeafColumns: [],
      fixedRightColumns: [],
      fixedRightLeafColumns: [],
      hasFixedTable: false,
      hoverRowIndex: null,
      selection: [],
      isAllSelected: false,
      expandable: false,
      expandRows: [],
      treeType: false,
      childrenProp: table.childrenProp,
      parentRows: [],
      unfoldedRows: [],
      sortingColumn: null,
      sortProp: null,
      sortOrder: '',
      defaultSortOrders: ORDERS,
      dragging: false,
      draggingColumn: null,
      dragModel: {},
      paging: false,
      startRowIndex: -1
    }

    if (table.defaultSortOrders) {
      if (checkSortOrders(table.defaultSortOrders)) {
        this.states.defaultSortOrders = table.defaultSortOrders
      }
    }

    this.mutations = {
      insertColumn (states, column, index, parent) {
        if (!column.sortOrders || !checkSortOrders(column.sortOrders)) {
          column.sortOrders = states.defaultSortOrders
        }

        let array = states._columns
        if (parent) {
          array = parent.children
          if (!array) array = parent.children = []
        }
        if (typeof index !== 'undefined') {
          array.splice(index, 0, column)
        } else {
          array.push(column)
        }
        if (column.type === 'expand') {
          states.expandable = true
        }
        if (this.table.$ready) {
          this.updateColumns()
          this.table.doLayout()
        }
      },

      removeColumn (states, column, parent) {
        let array = states._columns
        if (parent) {
          array = parent.children
          if (!array) array = parent.children = []
        }
        if (array) {
          array.splice(array.indexOf(column), 1)
        }
        if (this.table.$ready) {
          this.updateColumns()
          this.table.doLayout()
        }
      },

      rowSelectionChanged (states, row) {
        const changed = toggleRowSelection(states, row)

        if (changed) {
          const selection = states.selection ? states.selection.slice() : []
          const table = this.table
          table.$emit('on-selection-change', selection)
          table.$emit('on-select', selection, row)
        }

        this.updateAllSelected()
      },

      allSelectionChanged (states) {
        states.isAllSelected = !states.isAllSelected
        if (states.isAllSelected) {
          states.selection = states._sortedData.slice()
        } else {
          states.selection = []
        }
        table.$emit('on-selection-change', states.selection)
        table.$emit('on-select-all', states.selection)
      },

      rowExpansionChanged (states, row) {
        this.toggleRowExpansion(row)
      },

      rowUnfoldingChanged (states, row) {
        this.toggleRowUnfolding(row)
      },

      sortConditionChanged (states, column, asc) {
        let order
        if (typeof asc === 'boolean') {
          order = column.order = asc ? ASCENDING_ORDER : DESCENDING_ORDER
        } else {
          order = column.order = getNextOrder(column)
        }
        const changed = toggleSortCondition(states, column, order)
        if (changed) {
          if (column.sortable !== 'custom') {
            states._sortedData = sortData(states._data, states)
            this.jumpTo(0, 0, true)
            Vue.nextTick(() => this.table.doLayout())
          }
          this.table.$emit('on-sort-change', column, states.sortProp, states.sortOrder)
        }
      },

      setHoverRowIndex (states, rowIndex) {
        states.hoverRowIndex = rowIndex
      },

      setDraggingState (states, dragging) {
        states.dragging = dragging
      },

      setDraggingColumn (states, draggingColumn) {
        states.draggingColumn = draggingColumn
      },

      setDragModel (states, dragModel) {
        states.dragModel = dragModel
      }
    }
  }

  setData (data) {
    const states = this.states
    const dataInstanceChanged = states._data !== data
    states._data = data
    if (states.childrenProp) {
      processTreeData(states, this.table)
    }
    states._sortedData = sortData(states._data, states)

    const { defaultExpandAll, rowKey, reserveStates, defaultUnfoldChildren } = this.table
    if (defaultExpandAll) {
      states.expandRows = (states._sortedData || []).slice()
    } else {
      if (reserveStates) {
        states.expandRows = reserve(states.expandRows, states._sortedData, rowKey)
      } else {
        if (dataInstanceChanged) {
          this.clearExpandRows()
        } else {
          this.cleanExpandRows()
        }
      }
    }

    if (reserveStates) {
      states.selection = reserve(states.selection, states._sortedData, rowKey)
      this.updateAllSelected()
    } else {
      if (dataInstanceChanged) {
        this.clearSelection()
      } else {
        this.cleanSelection()
        this.updateAllSelected()
      }
    }

    this.jumpTo(0, 0, true)

    if (defaultUnfoldChildren) {
      states.parentRows.forEach((r) => {
        toggleRowUnfolding(states, r, true)
      })
    }
  }

  toggleAllSelection () {
    const states = this.states
    states.isAllSelected = !states.isAllSelected
    if (states.isAllSelected) {
      states.selection = states._sortedData.slice()
    } else {
      states.selection = []
    }
    this.table.$emit('on-selection-change', states.selection)
  }

  clearSelection () {
    const states = this.states
    states.isAllSelected = false
    const oldSelection = states.selection
    if (states.selection.length) {
      states.selection = []
    }
    if (oldSelection.length > 0) {
      this.table.$emit('on-selection-change', [])
    }
  }

  cleanSelection () {
    const states = this.states
    const rowKey = this.table.rowKey
    const { changed, list: selection } = clean(states.selection, states._sortedData, rowKey)
    states.selection = selection

    if (changed) {
      this.table.$emit('on-selection-change', selection ? selection.slice() : [])
    }
  }

  toggleRowSelection (row, selected) {
    const changed = toggleRowSelection(this.states, row, selected)
    const selection = this.states.selection
    if (changed) {
      this.table.$emit('on-selection-change', selection ? selection.slice() : [])
    }
    this.updateAllSelected()
  }

  updateAllSelected () {
    const { selection, _sortedData } = this.states
    // 简易判断
    this.states.isAllSelected = selection && _sortedData && selection.length === _sortedData.length
  }

  clearExpandRows () {
    const states = this.states
    const oldexpandRows = states.expandRows
    if (states.expandRows.length) {
      states.expandRows = []
    }
    if (oldexpandRows.length > 0) {
      this.table.$emit('on-expand-change', [])
    }
  }

  cleanExpandRows () {
    const states = this.states
    const rowKey = this.table.rowKey
    const { changed, list: expandRows } = clean(states.expandRows, states._sortedData, rowKey)
    states.expandRows = expandRows

    if (changed) {
      this.table.$emit('on-expand-change', expandRows ? expandRows.slice() : [])
    }
  }

  toggleRowExpansion (row, expanded) {
    const changed = toggleRowExpansion(this.states, row, expanded)
    if (changed) {
      this.table.$emit('on-expand-change', row, this.states.expandRows)
      Vue.nextTick(() => this.table.doLayout())
    }
  }

  toggleRowUnfolding (row, unfolded) {
    const changed = toggleRowUnfolding(this.states, row, unfolded)
    if (changed) {
      this.table.$emit('on-unfolded-change', row, this.states.unfoldedRows)
      Vue.nextTick(() => this.table.doLayout())
    }
  }

  setExpandRowKeys = (rowKeys) => {
    const expandRows = []
    const rowKey = this.table.rowKey
    if (!rowKey) throw new Error('Table Store: Prop row-key should not be empty.')
    const keysMap = getKeysMap(this.states._sortedData || [], rowKey)
    rowKeys.forEach((key) => {
      const info = keysMap[key]
      if (info) {
        expandRows.push(info.row)
      }
    })
    this.states.expandRows = expandRows

    Vue.nextTick(() => this.table.doLayout())
  }

  clearSort () {
    if (!this.states.sortingColumn) return

    const changed = toggleSortCondition(this.states, this.states.sortingColumn, '')
    if (changed) {
      this.table.$emit('on-sort-change', null, null, '')
      Vue.nextTick(() => this.table.doLayout())
    }
  }

  sort (prop, order) {
    const states = this.states
    const column = states.leafColumns.find((c) => c.prop === prop)
    if (!column) {
      throw new Error(`Table Store: Can't find target column with prop '${prop}'!`)
    }

    if (typeof order === 'string' && !ORDERS.includes(order)) {
      throw new Error(`Table Store: Sort order must be '${ASCENDING_ORDER}' or '${DESCENDING_ORDER}'!`)
    }
    if (typeof order === 'undefined') {
      order = column.order = getNextOrder(column)
    }
    const changed = toggleSortCondition(states, column, order)
    if (changed) {
      if (column.sortable !== 'custom') {
        states._sortedData = sortData(states._data, states)
        this.jumpTo(0, 0, true)
        Vue.nextTick(() => this.table.doLayout())
      }
      this.table.$emit('on-sort-change', column, states.sortProp, states.sortOrder)
    }
  }

  updateColumns () {
    const states = this.states
    const _columns = states._columns || []
    states.fixedLeftColumns = _columns.filter(column => column.fixed === true || column.fixed === 'left')
    states.fixedRightColumns = _columns.filter(column => column.fixed === 'right')

    if (states.fixedLeftColumns.length > 0 && _columns[0] && _columns[0].type === 'selection' && !_columns[0].fixed) {
      _columns[0].fixed = true
      states.fixedLeftColumns.unshift(_columns[0])
    }

    const notFixedColumns = _columns.filter(column => {
      column.__hiddenInFixed = !column.fixed
      return !column.fixed
    })
    states.columns = [].concat(states.fixedLeftColumns).concat(notFixedColumns).concat(states.fixedRightColumns)

    states.leafColumns = doFlattenColumns(states.columns)
    states.fixedLeftLeafColumns = doFlattenColumns(states.fixedLeftColumns)
    states.fixedRightLeafColumns = doFlattenColumns(states.fixedRightColumns)

    states.hasFixedTable = states.fixedLeftColumns.length > 0 || states.fixedRightColumns.length > 0
  }

  commit (name, ...args) {
    const mutations = this.mutations
    if (mutations[name]) {
      mutations[name].apply(this, [this.states].concat(args))
    } else {
      throw new Error(`Table Store: Action not found: ${name}`)
    }
  }

  jumpTo (topIndex, offset, sortChanged) {
    const { internalPaging, rowLimit, layout } = this.table
    const states = this.states

    if (internalPaging && states._sortedData && states._sortedData.length > rowLimit) {
      if (topIndex <= -rowLimit || topIndex >= states._sortedData.length) return

      states.paging = true
      if (typeof offset === 'undefined') {
        offset = Math.round(rowLimit * 0.5)
      }
      let start = Math.max(0, topIndex - offset)
      let end = start + rowLimit
      if (end > states._sortedData.length) start = states._sortedData.length - rowLimit

      if (!sortChanged && start === states.startRowIndex) return

      const goBack = start < states.startRowIndex
      states.startRowIndex = start
      this.table.scrollerDisabled = true
      layout.slicing = true
      // disable window scroll
      addClass(document.body, 'spin-lock-overflow')
      // for scroll transition lag
      setTimeout(() => {
        layout.beforeJump(goBack)
        // for loading lag
        setTimeout(() => {
          states.data = states._sortedData.slice(start, end)
          layout.afterJump(sortChanged)
        }, 100)
      }, 200)
    } else {
      states.data = this.states._sortedData.slice()
      states.paging = false
    }
  }

  prevPage () {
    this.jumpTo(this.states.startRowIndex - this.table.rowLimit, -this.table.rowLimit * 0.5)
  }

  nextPage () {
    this.jumpTo(this.states.startRowIndex + this.table.rowLimit, this.table.rowLimit * 0.5)
  }
}

const toggleRowSelection = (states, row, selected) => {
  return toggleStateWithArray(states.selection, row, selected)
}

const toggleRowExpansion = (states, row, expanded) => {
  return toggleStateWithArray(states.expandRows, row, expanded)
}

const toggleRowUnfolding = (states, row, unfolded) => {
  let changed = toggleStateWithArray(states.unfoldedRows, row, unfolded)
  if (changed) {
    const index = states.data.indexOf(row)
    if (states.unfoldedRows.includes(row)) {
      const insertionArray = []
      fillInsersionArray(states, row[states.childrenProp], insertionArray)
      states.data.splice(index + 1, 0, ...insertionArray)
    } else {
      const nextSiblingIndex = states.data.findIndex((r, i) => {
        return i > index && r.__level === row.__level
      })
      if (nextSiblingIndex !== -1) {
        states.data.splice(index + 1, nextSiblingIndex - index - 1)
      } else {
        states.data.splice(index + 1, states.data.length - index - 1)
      }
    }
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

const toggleSortCondition = (states, column, order) => {
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

const sortData = (data, states) => {
  data = data || []
  const { treeType, sortingColumn, sortProp: key, sortOrder: order } = states
  if (!sortingColumn || !key || !order || treeType || sortingColumn.sortable === 'custom') {
    return data
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

const getKeysMap = (array, rowKey) => {
  const arrayMap = {}
  array.forEach((row, index) => {
    arrayMap[getValueByPath(row, rowKey)] = { row, index }
  })
  return arrayMap
}

const doFlattenColumns = (columns) => {
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

const checkSortOrders = (list) => {
  let pass = true
  for (const order of list) {
    if (order && !ORDERS.includes(order)) {
      pass = false
      break
    }
  }
  return pass
}

const getNextOrder = ({ order, sortOrders }) => {
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

const reserve = (source, dataList, rowKey) => {
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

const clean = (source, dataList, rowKey) => {
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

const processTreeData = (states, table) => {
  const { _data } = states
  const { childrenProp } = table
  if (!_data) return
  const parentRows = []
  const unfoldedRows = []
  checkParentRows(_data, childrenProp, parentRows, unfoldedRows, 1)
  states.treeType = parentRows.length !== 0
  states.unfoldedRows = unfoldedRows
  states.parentRows = parentRows
}

const checkParentRows = (list, childrenProp, parentRows, unfoldedRows, level) => {
  list.forEach((row) => {
    row.__level = level
    const children = row[childrenProp]
    if (children && children.length) {
      parentRows.push(row)
      checkParentRows(children, childrenProp, parentRows, unfoldedRows, level + 1)
    }
  })
}
