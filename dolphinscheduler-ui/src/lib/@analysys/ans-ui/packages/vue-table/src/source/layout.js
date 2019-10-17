import Vue from 'vue'
import { limitedLoop } from '../../../../src/util'

export default class TableLayout {
  constructor (options) {
    this.store = null
    this.table = null
    this.fit = true
    this.showHeader = true

    this.scrollX = false
    this.scrollY = false
    this.bodyWidth = null
    this.tableWidth = null
    this.tableWrapperWidth = null
    this.fixedLeftWidth = 0
    this.fixedRightWidth = 0
    this.height = null
    this.tableHeight = null
    this.headerHeight = null
    this.bodyHeight = null
    this.parentHeight = null

    this.observers = []
    for (let name in options) {
      if (options.hasOwnProperty(name)) {
        this[name] = options[name]
      }
    }
  }

  addObserver (observer) {
    this.observers.push(observer)
  }

  removeObserver (observer) {
    const index = this.observers.indexOf(observer)
    if (index !== -1) {
      this.observers.splice(index, 1)
    }
  }

  notifyObservers (event) {
    const observers = this.observers
    observers.forEach((observer) => {
      switch (event) {
        case 'columns':
          observer.onColumnsChange(this)
          break
        default:
          throw new Error(`Table Layout: Can't find event ${event}.`)
      }
    })
  }

  updateScrollY () {
    const restrict = this.table.restrict
    if (!restrict) {
      const height = this.height
      if (typeof height !== 'string' && typeof height !== 'number') return
    }
    const bodyTable = this.table.$refs.bodyTable
    if (this.table.$el && bodyTable) {
      const oldScrollY = this.scrollY
      const tableBodyHeight = bodyTable.$el.offsetHeight

      if (tableBodyHeight === 0) {
        // can't use nexttick, dead loop
        return limitedLoop.setTimeout(this.updateScrollY, this, 0)
      }
      // reset
      this.updateScrollY.startTime = null

      if (this.table.restrict) {
        const parentHeight = this.parentHeight = this.table.$el.parentNode.clientHeight
        if (this.table.$refs.scroller.$refs.content.clientHeight + this.headerHeight > parentHeight) {
          this.table.$el.style.height = parentHeight + 'px'
          this.tableHeight = parentHeight
          this.bodyHeight = this.tableHeight - this.headerHeight
          this.scrollY = true
        } else {
          if (!this.bodyHeight) {
            this.scrollY = false
          }
        }
        Vue.nextTick(() => this.table.debouncedCheckScrollable())
      } else {
        this.scrollY = tableBodyHeight > this.bodyHeight
      }

      if (oldScrollY !== this.scrollY) {
        this.updateColumnsWidth()
      }
    }
  }

  setHeight (value) {
    const el = this.table.$el
    if (typeof value === 'string' && /^\d+$/.test(value)) {
      value = Number(value)
    }
    this.height = value
    if (!el && (value || value === 0)) {
      return limitedLoop.nextTick(this.setHeight, this, value)
    }
    // reset
    this.setHeight.startTime = null

    if (typeof value === 'number') {
      el.style.height = value + 'px'
      this.updateElsHeight()
    } else if (typeof value === 'string') {
      el.style.height = value
      this.updateElsHeight()
    }
  }

  updateElsHeight () {
    if (!this.table.$ready) return
    const { headerWrapper, scroller } = this.table.$refs
    const { $el, restrict } = this.table

    if (this.showHeader && !headerWrapper) return
    const headerHeight = this.headerHeight = !this.showHeader ? 0 : headerWrapper.offsetHeight
    if (this.showHeader && headerWrapper.clientWidth > 0 && (this.store.states.columns || []).length > 0 && headerHeight < 10) {
      return limitedLoop.nextTick(this.updateElsHeight, this)
    }
    // reset
    this.updateElsHeight.startTime = null

    if (restrict && this.parentHeight) {
      const data = this.store.states.data
      if (!data || !data.length) return

      if (scroller.$el.clientHeight > scroller.$refs.content.clientHeight) {
        $el.style.height = scroller.$refs.content.clientHeight + this.headerHeight + 'px'
        this.bodyHeight = null
        return limitedLoop.nextTick(this.updateElsHeight, this)
      } else if (Math.abs(scroller.$el.clientHeight + this.headerHeight - $el.clientHeight) > 2) {
        $el.style.height = 'auto'
        this.bodyHeight = null
        return limitedLoop.nextTick(this.updateElsHeight, this)
      } else if ($el.parentNode && this.parentHeight !== $el.parentNode.clientHeight) {
        $el.style.height = 'auto'
        this.bodyHeight = null
        this.parentHeight = $el.parentNode.clientHeight
        return limitedLoop.nextTick(this.updateElsHeight, this)
      }
    }

    const tableHeight = this.tableHeight = $el.clientHeight
    if (this.height !== null && (!isNaN(this.height) || typeof this.height === 'string')) {
      this.bodyHeight = tableHeight - headerHeight
    }

    this.updateScrollY()
    Vue.nextTick(() => this.table.debouncedCheckScrollable())
  }

  updateColumnsWidth () {
    const bodyWidth = this.tableWrapperWidth = this.table.$el.clientWidth
    const fit = this.fit
    let bodyMinWidth = 0

    const columns = this.store.states.leafColumns
    const flexColumns = columns.filter((column) => typeof column.width !== 'number')

    if (flexColumns.length > 0 && fit) {
      columns.forEach((column) => {
        bodyMinWidth += column.width || column.minWidth
      })


      if (bodyMinWidth - bodyWidth <= 1) {
        this.scrollX = false
        const totalFlexWidth = bodyWidth - bodyMinWidth
        const columnFlexWidth = totalFlexWidth / flexColumns.length
        flexColumns.forEach((column) => {
          column.currentWidth = column.minWidth + columnFlexWidth
        })
      } else {
        this.scrollX = true
        flexColumns.forEach((column) => {
          column.currentWidth = column.minWidth
        })
      }

      this.bodyWidth = Math.max(bodyMinWidth, bodyWidth)
    } else {
      columns.forEach((column) => {
        bodyMinWidth += column.width
        column.currentWidth = column.width
      })
      this.scrollX = bodyMinWidth - bodyWidth > 1
      this.bodyWidth = bodyMinWidth
    }

    const fixedLeftLeafColumns = this.store.states.fixedLeftLeafColumns
    if (fixedLeftLeafColumns.length > 0) {
      let fixedLeftWidth = 0
      fixedLeftLeafColumns.forEach(column => {
        fixedLeftWidth += column.currentWidth
      })
      this.fixedLeftWidth = fixedLeftWidth
    }

    const fixedRightLeafColumns = this.store.states.fixedRightLeafColumns
    if (fixedRightLeafColumns.length > 0) {
      let fixedRightWidth = 0
      fixedRightLeafColumns.forEach(column => {
        fixedRightWidth += column.currentWidth
      })
      this.fixedRightWidth = fixedRightWidth
    }
    this.store.updateRenderData()
  }

  updateTableWidth (width) {
    this.tableWidth = width || this.bodyWidth
  }
}
