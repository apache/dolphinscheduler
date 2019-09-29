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
    this.fixedLeftWidth = 0
    this.fixedRightWidth = 0
    this.height = null
    this.tableHeight = null
    this.headerHeight = null
    this.bodyHeight = null
    this.parentHeight = null
    this.referenceRowId = null
    this.referenceRowIndex = null
    this.referenceRowPositionY = null
    this.referenceRowOffsetTop = null
    this.loading = false
    this.slicing = false

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

      if (this.table.restrict) {
        const parentHeight = this.parentHeight = this.table.$el.parentNode.clientHeight
        if (this.tableHeight > parentHeight) {
          this.table.$el.style.height = parentHeight + 'px'
          this.tableHeight = parentHeight
          this.bodyHeight = this.tableHeight - this.headerHeight
          this.scrollY = true
        } else {
          if (!this.bodyHeight) {
            this.scrollY = false
          }
        }
        Vue.nextTick(() => this.table.checkScrollable())
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
    const { headerWrapper } = this.table.$refs
    const { $el, restrict } = this.table

    if (this.showHeader && !headerWrapper) return
    const headerHeight = this.headerHeight = !this.showHeader ? 0 : headerWrapper.offsetHeight
    if (this.showHeader && headerWrapper.clientWidth > 0 && (this.store.states.columns || []).length > 0 && headerHeight < 10) {
      return limitedLoop.nextTick(this.updateElsHeight, this)
    }
    if (restrict && this.parentHeight && this.parentHeight !== $el.parentNode.clientHeight) {
      $el.style.height = 'auto'
      this.bodyHeight = null
      this.parentHeight = $el.parentNode.clientHeight
      return Vue.nextTick(() => this.updateElsHeight())
    }

    const tableHeight = this.tableHeight = this.table.$el.clientHeight
    const oldBodyHeight = this.bodyHeight
    if (this.height !== null && (!isNaN(this.height) || typeof this.height === 'string')) {
      this.bodyHeight = tableHeight - headerHeight
    }

    this.updateScrollY()
    if (oldBodyHeight !== this.bodyHeight) {
      Vue.nextTick(() => this.table.checkScrollable())
    }
  }

  updateColumnsWidth () {
    const bodyWidth = this.table.$el.clientWidth
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

    Vue.nextTick(() => {
      this.notifyObservers('columns')
      this.table.checkScrollable()
    })
  }

  beforeJump (goBack) {
    const bodyWrapper = this.table.$refs.bodyWrapper
    if (bodyWrapper) {
      this.loading = true
      this.table.scrollerDisabled = true
      this.store.commit('setHoverRowIndex', null)
      const referenceRow = goBack
        ? bodyWrapper.querySelector('.table-row:first-child')
        : bodyWrapper.querySelector('.table-row:last-child')
      if (referenceRow) {
        this.referenceRowId = referenceRow.getAttribute('table-row-key')
        this.referenceRowIndex = Array.prototype.indexOf.call(referenceRow.parentNode.children, referenceRow)
        this.referenceRowPositionY = referenceRow.getBoundingClientRect().top
        this.referenceRowOffsetTop = referenceRow.offsetTop
      }
    }
  }

  afterJump (noReference) {
    const bodyWrapper = this.table.$refs.bodyWrapper
    if (bodyWrapper) {
      Vue.nextTick(() => {
        if (!this.referenceRowId || noReference) return this.scrollTableToView()

        const referenceRow = bodyWrapper.querySelector(`.table-row[table-row-key='${this.referenceRowId}']`)
        if (!referenceRow) return this.scrollTableToView()

        const referenceRowIndex = Array.prototype.indexOf.call(referenceRow.parentNode.children, referenceRow)

        if (referenceRowIndex === this.referenceRowIndex) return limitedLoop.nextTick(this.afterJump, this)

        // finished
        let diff = this.scrollY
          ? referenceRow.offsetTop - this.referenceRowOffsetTop
          : referenceRow.getBoundingClientRect().top - this.referenceRowPositionY
        if (this.scrollY) {
          this.table.moveBodyTopByDiff(diff)
        } else {
          window.scrollBy(0, diff)
        }
        this.finishSlicing()
      })
    }
  }

  scrollTableToView () {
    if (this.scrollY) {
      this.table.setBodyTop(0)
    } else {
      const tableY = this.table.$el.getBoundingClientRect().top
      if (tableY < 0) {
        window.scrollBy(0, this.table.$el.getBoundingClientRect().top)
      }
    }
    this.finishSlicing()
  }

  finishSlicing () {
    Vue.nextTick(() => {
      this.slicing = false
      this.loading = false
      // hack scroller event
      setTimeout(() => {
        this.table.scrollerDisabled = false
        this.table.checkScrollable()
      }, 500)
    })
  }
}
