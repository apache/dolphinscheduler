<template>
  <th
    :colspan="colspan"
    :rowspan="rowspan"
    :class="{ [sortOrder]: sortingColumn === column, 'hidden-column': fixed && column.__hiddenInFixed }"
    class="table-header-cell"
    @mousedown="handleMouseDown($event, column)"
    @mousemove="handleMouseMove($event, column)"
    @mouseout="handleMouseOut($event, column)">
    <x-cell-renderer
      v-if="column.customHeader"
      header
      :column="column"
      :c-index="columnIndex"
      :render="column.customHeader"
    ></x-cell-renderer>
    <template v-else>
      <div v-if="column.type === 'selection'" class="table-header-cell-content icon-column">
        <x-checkbox
          :value="store.states.isAllSelected"
          @on-change="store.commit('allSelectionChanged')">
        </x-checkbox>
      </div>
      <div v-else-if="column.type === 'expand'" class="table-header-cell-content">{{column.label || ''}}</div>
      <template v-else>
        <div
          v-if="!column.sortable && !column.prependHeader && !column.appendHeader && !column.headerText"
          class="table-header-cell-content"
          :class="alignClass"
        >{{column.label}}</div>
        <div v-else class="table-header-cell-content flex-cell" :class="alignClass">
          <x-cell-renderer
            v-if="column.prependHeader"
            header
            :column="column"
            :c-index="columnIndex"
            :render="column.prependHeader"
          ></x-cell-renderer>
          <span
            v-if="column.headerText"
            class="table-header-text"
            :class="{'sortable-column':column.sortable}"
            @click="column.sortable && store.commit('sortConditionChanged', column)"
          >
            <x-cell-renderer
              header
              :column="column"
              :c-index="columnIndex"
              :render="column.headerText"
            ></x-cell-renderer>
          </span>
          <span
            v-else
            class="table-header-text"
            :class="{'sortable-column':column.sortable}"
            @click="column.sortable && store.commit('sortConditionChanged', column)"
          >{{column.label}}</span>
          <span v-if="column.sortable" class="column-order-area">
            <i class="ascending-order" @click="store.commit('sortConditionChanged', column, true)"></i>
            <i class="descending-order" @click="store.commit('sortConditionChanged', column, false)"></i>
          </span>
          <x-cell-renderer
            v-if="column.appendHeader"
            header
            :column="column"
            :c-index="columnIndex"
            :render="column.appendHeader"
          ></x-cell-renderer>
        </div>
      </template>
    </template>
  </th>
</template>

<script>
import { xCheckbox } from '../../../vue-checkbox/src'
import xCellRenderer from './cellRenderer.js'

const PROXY_GAP = 8

export default {
  name: 'xTableTh',

  components: { xCheckbox, xCellRenderer },

  props: {
    store: {
      required: true
    },
    column: {
      required: true
    },
    columnIndex: {
      required: true
    },
    border: Boolean,
    rowspan: Number,
    colspan: Number,
    fixed: String
  },

  computed: {
    table () {
      return this.store.table
    },

    sortingColumn () {
      return this.store.states.sortingColumn
    },

    sortOrder () {
      return this.store.states.sortOrder
    },

    dragging () {
      return this.store.states.dragging
    },

    draggingColumn () {
      return this.store.states.draggingColumn
    },

    currentDragModel () {
      return this.store.states.dragModel
    },

    alignClass () {
      return `align-${this.column.headerAlign}`
    }
  },

  methods: {
    handleMouseDown (event, column) {
      if (column.children && column.children.length > 0) return

      if (this.draggingColumn && this.border) {
        const table = this.table
        const store = this.store
        store.commit('setDraggingState', true)
        table.resizeProxyVisible = true

        const tableEl = table.$el
        const tableLeft = tableEl.getBoundingClientRect().left
        const columnEl = this.$el
        const columnRect = columnEl.getBoundingClientRect()
        const minLeft = columnRect.left - tableLeft + column.minWidth

        const dragModel = {
          startMouseLeft: event.clientX,
          startLeft: event.clientX - tableLeft,
          startColumnLeft: columnRect.left - tableLeft
        }
        store.commit('setDragModel', dragModel)

        const resizeProxy = table.$refs.resizeProxy
        resizeProxy.style.left = dragModel.startLeft + 'px'

        document.onselectstart = () => false
        document.ondragstart = () => false

        const handleMouseMove = (event) => {
          const deltaLeft = event.clientX - this.currentDragModel.startMouseLeft
          const proxyLeft = this.currentDragModel.startLeft + deltaLeft
          resizeProxy.style.left = Math.max(minLeft, proxyLeft) + 'px'
        }

        const handleMouseUp = () => {
          if (this.dragging) {
            const { startColumnLeft } = this.currentDragModel
            const finalLeft = parseInt(resizeProxy.style.left, 10)
            const columnWidth = finalLeft - startColumnLeft
            column.width = column.currentWidth = columnWidth

            table.doLayout()

            document.body.style.cursor = ''
            store.commit('setDraggingState', false)
            store.commit('setDraggingColumn', null)
            store.commit('setDragModel', {})

            table.resizeProxyVisible = false

            table.checkScrollPosition()
          }

          document.removeEventListener('mousemove', handleMouseMove)
          document.removeEventListener('mouseup', handleMouseUp)
          document.onselectstart = null
          document.ondragstart = null
        }

        document.addEventListener('mousemove', handleMouseMove)
        document.addEventListener('mouseup', handleMouseUp)
      }
    },

    handleMouseMove (event, column) {
      if (column.children && column.children.length > 0) return
      if (!column || !column.resizable) return

      let target = this.$el
      if (!this.dragging && this.border) {
        let rect = target.getBoundingClientRect()
        const bodyStyle = document.body.style
        if (rect.right - event.pageX < PROXY_GAP) {
          bodyStyle.cursor = 'col-resize'
          this.store.commit('setDraggingColumn', column)
        } else if (!this.dragging) {
          bodyStyle.cursor = ''
          this.store.commit('setDraggingColumn', null)
        }
      }
    },

    handleMouseOut () {
      if (!this.dragging) {
        document.body.style.cursor = ''
      }
    }
  }
}
</script>
