import Vue from 'vue'
import * as util from './util'
import { debounce, throttle } from 'throttle-debounce'

export default class TableStore {
  constructor (table) {
    this.table = table
    this.scheduleMappingSortedData = debounce(10, () => {
      table.layout.updateColumnsWidth()
      this.mappingSortedData()
    })
    this.scheduleUpdateColumns = debounce(10, (flag) => this.updateColumns(flag))
    this.throttleCalculateIndexes = throttle(50, (top) => this.calculateIndexes(top))
    this.throttleCalculateColumnIndexes = throttle(20, (left) => this.calculateColumnIndexes(left))
    this.states = {
      // 原始数据
      _data: null,
      _sortedData: null,
      _mappedData: null,
      _slicedData: null,
      // 当前展示数据
      data: null,
      rows: [],
      renderedRows: [],
      _columns: [],
      columns: [],
      renderedColumns: [],
      notFixedColumns: [],
      notFixedLeafColumns: [],
      leafColumns: [],
      leafColumnMap: {},
      colColumns: [],
      colColumnIds: [],
      renderedColColumns: [],
      fixedLeftColumns: [],
      fixedLeftLeafColumns: [],
      fixedRightColumns: [],
      fixedRightLeafColumns: [],
      multiLayer: false,
      hasFixedTable: false,
      hoverRowIndex: null,
      selection: [],
      isAllSelected: false,
      isIndeterminate: false,
      expandable: false,
      expandRows: [],
      treeType: false,
      childrenProp: table.childrenProp,
      parentRows: [],
      unfoldedRows: [],
      sortingColumn: null,
      sortProp: null,
      sortOrder: '',
      defaultSortOrders: util.ORDERS,
      dragging: false,
      draggingColumn: null,
      dragModel: {},
      rowLimit: null,
      startRowIndex: 0,
      endRowIndex: null,
      startColumnIndex: 0,
      transformBodyY: false,
      transformXAmount: 0,
      transformYAmount: 0,
      contentHeight: null,
      firstRowSpan: 1,
      renderedRowCount: 0,
      renderedColumnCount: 0,
      destroying: false
    }

    if (table.defaultSortOrders) {
      if (util.checkSortOrders(table.defaultSortOrders)) {
        this.states.defaultSortOrders = table.defaultSortOrders
      }
    }

    this.mutations = {
      insertColumn (states, column, index, parent) {
        if (!column.sortOrders || !util.checkSortOrders(column.sortOrders)) {
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
          this.scheduleUpdateColumns(true)
          this.scheduleLayout()
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
          this.scheduleUpdateColumns(true)
          this.scheduleLayout()
        }
      },

      rowSelectionChanged (states, row) {
        const changed = util.toggleRowSelection(states, row)

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
        states.isIndeterminate = false
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
          order = column.order = asc ? util.ASCENDING_ORDER : util.DESCENDING_ORDER
        } else {
          order = column.order = util.getNextOrder(column)
        }
        const changed = util.toggleSortCondition(states, column, order)
        if (changed) {
          if (column.sortable !== 'custom') {
            states._sortedData = Object.freeze(util.sortData(states._data, states))
            this.mappingSortedData()
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

  scheduleLayout (updateColumns) {
    if (updateColumns) {
      this.updateColumns()
    }
    this.table.debouncedUpdateLayout()
  }

  setData (data) {
    const states = this.states
    const dataInstanceChanged = states._data !== data
    states._data = this.table.frozenData ? Object.freeze(data) : data
    if (states.childrenProp) {
      util.processTreeData(states, this.table)
    }
    states._sortedData = Object.freeze(util.sortData(states._data, states))

    const { defaultExpandAll, rowKey, reserveStates, defaultUnfoldChildren } = this.table
    if (defaultExpandAll) {
      states.expandRows = (states._sortedData || []).slice()
    } else {
      if (reserveStates) {
        states.expandRows = util.reserve(states.expandRows, states._sortedData, rowKey)
      } else {
        if (dataInstanceChanged) {
          this.clearExpandRows()
        } else {
          this.cleanExpandRows()
        }
      }
    }

    if (reserveStates) {
      states.selection = util.reserve(states.selection, states._sortedData, rowKey)
      this.updateAllSelected()
    } else {
      if (dataInstanceChanged) {
        this.clearSelection()
      } else {
        this.cleanSelection()
        this.updateAllSelected()
      }
    }

    if (defaultUnfoldChildren) {
      states.parentRows.forEach((r) => {
        util.toggleRowUnfolding(states, r, true)
      })
    }

    this.mappingSortedData()
  }

  toggleAllSelection () {
    const states = this.states
    states.isAllSelected = !states.isAllSelected
    states.isIndeterminate = false
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
    states.isIndeterminate = false
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
    const { changed, list: selection } = util.clean(states.selection, states._sortedData, rowKey)
    states.selection = selection

    if (changed) {
      this.table.$emit('on-selection-change', selection ? selection.slice() : [])
    }
  }

  toggleRowSelection (row, selected) {
    const changed = util.toggleRowSelection(this.states, row, selected)
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
    this.states.isIndeterminate = !this.states.isAllSelected && selection && selection.length
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
    const { changed, list: expandRows } = util.clean(states.expandRows, states._sortedData, rowKey)
    states.expandRows = expandRows

    if (changed) {
      this.table.$emit('on-expand-change', expandRows ? expandRows.slice() : [])
    }
  }

  toggleRowExpansion (row, expanded) {
    const changed = util.toggleRowExpansion(this.states, row, expanded)
    if (changed) {
      this.table.$emit('on-expand-change', row, this.states.expandRows)
      Vue.nextTick(() => this.scheduleLayout())
    }
  }

  toggleRowUnfolding (row, unfolded) {
    const changed = util.toggleRowUnfolding(this.states, row, unfolded)
    if (changed) {
      this.mappingSortedData()
      this.table.$emit('on-unfolded-change', row, this.states.unfoldedRows)
      Vue.nextTick(() => this.table.doLayout())
    }
  }

  setExpandRowKeys (rowKeys) {
    const expandRows = []
    const rowKey = this.table.rowKey
    if (!rowKey) throw new Error('Table Store: Prop row-key should not be empty.')
    const keysMap = util.getKeysMap(this.states._sortedData || [], rowKey)
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

    const changed = util.toggleSortCondition(this.states, this.states.sortingColumn, '')
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

    if (typeof order === 'string' && !util.ORDERS.includes(order)) {
      throw new Error(`Table Store: Sort order must be '${util.ASCENDING_ORDER}' or '${util.DESCENDING_ORDER}'!`)
    }
    if (typeof order === 'undefined') {
      order = column.order = util.getNextOrder(column)
    }
    const changed = util.toggleSortCondition(states, column, order)
    if (changed) {
      if (column.sortable !== 'custom') {
        states._sortedData = Object.freeze(util.sortData(states._data, states))
        this.mappingSortedData()
        Vue.nextTick(() => this.table.doLayout())
      }
      this.table.$emit('on-sort-change', column, states.sortProp, states.sortOrder)
    }
  }

  updateColumns (debouncedMapping) {
    if (this.states.destroying) return
    const states = this.states
    const _columns = states._columns || []
    states.fixedLeftColumns = Object.freeze(_columns.filter(column => column.fixed === true || column.fixed === 'left'))
    states.fixedRightColumns = Object.freeze(_columns.filter(column => column.fixed === 'right'))

    if (states.fixedLeftColumns.length > 0 && _columns[0] && _columns[0].type === 'selection' && !_columns[0].fixed) {
      _columns[0].fixed = true
      const temp = states.fixedLeftColumns.slice()
      temp.unshift(_columns[0])
      states.fixedLeftColumns = Object.freeze(temp)
    }

    const notFixedColumns = _columns.filter(column => !column.fixed)
    states.notFixedColumns = Object.freeze(notFixedColumns)
    states.notFixedLeafColumns = Object.freeze(util.doFlattenColumns(notFixedColumns))
    states.columns = Object.freeze([].concat(states.fixedLeftColumns).concat(notFixedColumns).concat(states.fixedRightColumns))
    for (let i = 0; i < states.columns.length; i++) {
      const c = states.columns[i]
      c._index = i
    }
    states.rows = Object.freeze(util.convertToRows(states.columns))
    for (const r of states.rows) {
      for (let i = 0; i < r.length; i++) {
        const c = r[i]
        c._indexInRow = i
      }
    }
    states.multiLayer = states.rows.length > 1

    states.leafColumns = Object.freeze(util.doFlattenColumns(states.columns))
    const columnsMap = {}
    states.leafColumns.forEach((column) => {
      columnsMap[column.id] = column
    })
    states.leafColumnMap = columnsMap
    states.colColumns = Object.freeze(states.multiLayer ? states.leafColumns : states.columns)

    states.fixedLeftLeafColumns = Object.freeze(util.doFlattenColumns(states.fixedLeftColumns))
    states.fixedRightLeafColumns = Object.freeze(util.doFlattenColumns(states.fixedRightColumns))

    states.hasFixedTable = states.fixedLeftColumns.length > 0 || states.fixedRightColumns.length > 0
    if (debouncedMapping) {
      this.scheduleMappingSortedData()
    } else {
      this.mappingSortedData()
    }
  }

  commit (name, ...args) {
    const mutations = this.mutations
    if (mutations[name]) {
      mutations[name].apply(this, [this.states].concat(args))
    } else {
      throw new Error(`Table Store: Action not found: ${name}`)
    }
  }

  updateRenderData () {
    const { colColumns, colColumnIds } = this.states
    const changed = colColumns.some(c => !colColumnIds.includes(c.id))
    if (changed) {
      this.mappingSortedData()
    } else {
      this.sliceData()
    }
  }

  mappingSortedData () {
    const { rowHeight, rowKey, stripe, cellSpanMethod, $el } = this.table
    const states = this.states
    if (states.destroying || ($el && $el.clientHeight === 0)) return

    // mapping all data
    if (states.colColumns.length) {
      states.colColumnIds = Object.freeze(states.colColumns.map(c => c.id))
      states.contentHeight = states._sortedData.length * rowHeight

      // data + columns => cells
      states._mappedData = Object.freeze(states._sortedData.map((d, i) => {
        const cells = []
        states.colColumns.forEach((c, j) => {
          const model = {
            columnIndex: j,
            column: c,
            content: util.getCellContent(d, c, i, j),
            spanModel: util.getSpanModel(d, c, i, j, cellSpanMethod)
          }
          if (model.spanModel.rowspan > 1 && states.firstRowSpan === 1) {
            states.firstRowSpan = model.spanModel.rowspan
          }
          cells.push(model)
        })
        return {
          item: d,
          rowIndex: i,
          cells,
          key: util.getRowKey(i, d, rowKey),
          classes: util.getRowClasses(i, stripe)
        }
      }))
      this.sliceData()
    }
  }

  sliceData () {
    const { virtualScroll, rowHeight, layout, $el, maxTreeRow } = this.table
    const states = this.states
    if (!states._mappedData || ($el && $el.clientHeight === 0)) return

    if (virtualScroll) {
      if (layout.scrollY) {
        let rowCount
        if (!layout.bodyHeight) {
          // init render
          rowCount = 10
        } else {
          rowCount = Math.ceil(layout.bodyHeight / rowHeight)
        }

        states.transformBodyY = rowCount < states._mappedData.length
        states.rowLimit = states.firstRowSpan !== 1
          ? (Math.ceil(rowCount / states.firstRowSpan) + 1) * states.firstRowSpan
          : rowCount + 6
        this.calculateIndexes()
        return
      } else if (states.treeType) {
        states._slicedData = Object.freeze(states._mappedData.slice(states.startRowIndex, states.renderedRowCount + maxTreeRow))
        this.calculateColumnIndexes()
        return
      }
    }
    states._slicedData = Object.freeze(states._mappedData.slice())
    states.transformBodyY = false
    this.calculateColumnIndexes()
  }

  calculateIndexes (top) {
    const states = this.states
    const { rowLimit, _mappedData, firstRowSpan } = states
    if (!rowLimit || !_mappedData || !_mappedData.length) return

    const { $refs, rowHeight, cellSpanMethod } = this.table
    this.commit('setHoverRowIndex', null)
    const currentTop = top || ($refs.scroller && $refs.scroller.currentTop) || 0
    const remainCount = firstRowSpan === 1 ? 3 : firstRowSpan
    let startIndex = Math.floor(Math.abs(currentTop) / rowHeight) - remainCount
    if (startIndex < 0) {
      startIndex = 0
    }

    // fix rowspan
    let realStartIndex = startIndex
    if (cellSpanMethod) {
      if (startIndex > states.startRowIndex) {
        for (let i = states.startRowIndex; i < startIndex; i++) {
          const currentRow = _mappedData[i]
          const targetCell = currentRow.cells.find(c => c.spanModel.rowspan > 1)
          if (targetCell && targetCell.spanModel.rowspan + i >= startIndex) {
            realStartIndex = i
            break
          }
        }
      } else if (startIndex < states.startRowIndex) {
        for (let i = states.startRowIndex; i >= 0; i--) {
          const currentRow = _mappedData[i]
          const targetCell = currentRow.cells.find(c => c.spanModel.rowspan > 1)
          if (targetCell && i < startIndex && targetCell.spanModel.rowspan + i >= startIndex) {
            realStartIndex = i
            break
          }
        }
      }
    }
    const diff = startIndex - realStartIndex

    let endIndex = realStartIndex + rowLimit + diff
    if (endIndex - realStartIndex < states.renderedRowCount) {
      endIndex = states.renderedRowCount + realStartIndex
      if (endIndex > _mappedData.length) {
        endIndex = _mappedData.length
        realStartIndex = Math.max(0, endIndex - states.renderedRowCount)
      }
    }

    states.renderedRowCount = endIndex - realStartIndex
    states.startRowIndex = realStartIndex
    states.endRowIndex = endIndex
    states.transformYAmount = realStartIndex * rowHeight
    this.table.updateTranslateY(states.transformYAmount)

    states._slicedData = Object.freeze(_mappedData.slice(realStartIndex, endIndex))
    this.calculateColumnIndexes()
  }

  calculateColumnIndexes (left) {
    const { scrollX, tableWrapperWidth, fixedLeftWidth, fixedRightWidth } = this.table.layout
    const states = this.states
    const { notFixedColumns, notFixedLeafColumns, fixedLeftColumns, fixedRightColumns, multiLayer } = states
    const contentWidth = tableWrapperWidth - fixedLeftWidth - fixedRightWidth
    if (contentWidth < 0) return

    if (!states._slicedData || !states._slicedData.length) {
      states.data = []
      Vue.nextTick(() => {
        this.table.layout.notifyObservers('columns')
        this.table.debouncedCheckScrollable()
      })
      return
    }

    // 不需要切割列
    if (!scrollX || !this.table.virtualScroll) {
      states.transformXAmount = 0
      states.renderedColumns = Object.freeze(states.columns)
      states.renderedColColumns = Object.freeze(states.colColumns)
      states.renderedRows = Object.freeze(states.rows)
      states.renderedColumnCount = 0
      if (states._slicedData) {
        let columnCount = states._slicedData[0] ? states._slicedData[0].cells.length : 0
        if (states.colColumns.length === columnCount) {
          states._slicedData.forEach(r => {
            r.renderedCells = r.cells.slice()
          })
          states.data = Object.freeze(states._slicedData.slice())
          this.table.$emit('on-slice')
          this.table.layout.updateTableWidth()
          Vue.nextTick(() => {
            this.table.layout.notifyObservers('columns')
            this.table.debouncedCheckScrollable()
          })
        }
      }
      return
    }

    const currentLeft = left || (this.table.$refs.scroller && this.table.$refs.scroller.currentLeft) || 0

    let _left = 0
    let _width = 0
    let startIndex = 0
    let endIndex = 1
    let findStart = false
    for (let i = 0; i < notFixedLeafColumns.length; i++) {
      const leaf = notFixedLeafColumns[i]
      const cWidth = leaf.currentWidth
      _left -= cWidth
      if (!findStart) {
        if (_left < currentLeft) {
          _width = currentLeft - _left
          // multiLayer header
          let parent = leaf.parent
          if (multiLayer && parent) {
            while (parent.parent) {
              parent = parent.parent
            }
            startIndex = notFixedColumns.indexOf(parent)
            const siblings = util.doFlattenColumns([parent])
            for (const s of siblings) {
              if (s === leaf) {
                break
              } else {
                _width -= s.currentWidth
              }
            }
          } else {
            startIndex = Math.max(0, i - 2)
          }
          findStart = true
        }
      } else {
        _width += cWidth
        if (_width >= contentWidth) {
          let parent = leaf.parent
          if (multiLayer && parent) {
            while (parent.parent) {
              parent = parent.parent
            }
            endIndex = notFixedColumns.indexOf(parent)
          } else {
            endIndex = i + 2
          }
          break
        }
      }
    }

    if (endIndex < startIndex || endIndex > notFixedColumns.length - 1) {
      endIndex = notFixedColumns.length - 1
      if (states.renderedColumnCount !== 0) {
        startIndex = Math.max(0, endIndex - states.renderedColumnCount + 1)
      }
    } else if (endIndex - startIndex + 1 < states.renderedColumnCount) {
      endIndex = states.renderedColumnCount + startIndex - 1
    }

    let transformXAmount = states.transformXAmount
    if (states.startColumnIndex > startIndex) {
      // to right
      const leafs = util.doFlattenColumns(notFixedColumns.slice(startIndex, states.startColumnIndex))
      for (const l of leafs) {
        transformXAmount -= l.currentWidth
      }
    } else {
      // to left
      const leafs = util.doFlattenColumns(notFixedColumns.slice(states.startColumnIndex, startIndex))
      for (const l of leafs) {
        transformXAmount += l.currentWidth
      }
    }
    states.transformXAmount = transformXAmount

    states.startColumnIndex = startIndex

    states.renderedColumns = Object.freeze([].concat(fixedLeftColumns).concat(notFixedColumns.slice(startIndex, endIndex + 1)).concat(fixedRightColumns))
    states.renderedColumnCount = states.renderedColumns.length - fixedLeftColumns.length - fixedRightColumns.length
    states.renderedColColumns = Object.freeze(multiLayer ? util.doFlattenColumns(states.renderedColumns) : states.renderedColumns)
    states.renderedRows = Object.freeze(util.convertToRows(states.renderedColumns))

    states._slicedData.forEach(r => {
      r.renderedCells = r.cells.filter(c => states.renderedColColumns.includes(c.column))
    })

    states.data = Object.freeze(states._slicedData.slice())
    this.table.$emit('on-slice')
    const tableWidth = states.renderedColColumns.reduce((p, c) => p + c.currentWidth, 0)
    this.table.layout.updateTableWidth(tableWidth)

    Vue.nextTick(() => {
      this.table.layout.notifyObservers('columns')
      this.table.debouncedCheckScrollable()
    })
  }
}
