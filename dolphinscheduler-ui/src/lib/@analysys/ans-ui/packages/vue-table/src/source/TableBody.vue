<template>
  <table
    :class="wrapperClass"
    cellspacing="0"
    cellpadding="0"
    border="0">
    <colgroup>
      <col :name="column.id" v-for="(column, i) in renderedColColumns" :key="i">
    </colgroup>
    <tbody>
      <template v-for="(row, i) in data">
        <tr
          :key="row.key"
          class="table-row"
          :class="[row.classes, {'highlight-row': row.key === table.currentRowKey}]"
          @mouseenter="handleMouseEnter(i)"
          @mouseleave="handleMouseLeave()">
          <x-table-td
            v-for="(cell, j) in row.renderedCells"
            :key="row.key + '-' + cell.column.id"
            :class="columnClasses[cell.columnIndex]"
            :store="store"
            :row="row.item"
            :column="cell.column"
            :first="firstTextColumnIndex === j"
            :row-index="row.rowIndex"
            :cell="cell"
          ></x-table-td>
        </tr>
        <tr v-if="expandable && expandRows.includes(row.item)" :key="'expand_' + i">
          <td class="table-cell" :class="getCellClasses(false)" :colspan="renderedColColumns.length">
            <x-cell-renderer
              expand
              :row="row.item"
              :r-index="row.rowIndex"
              :custom-render="table.expendRender">
            </x-cell-renderer>
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>

<script>
import { LIB_NAME, addClass, removeClass } from '../../../../src/util'
import xTableTd from './TableTd'
import xCellRenderer from './CellRenderer.vue'
import layoutObserver from './layoutObserver.js'

export default {
  name: 'xTableBody',

  components: { xTableTd, xCellRenderer },

  mixins: [layoutObserver],

  data () {
    return {
      wrapperClass: `${LIB_NAME}-table-body`
    }
  },

  computed: {
    table () {
      return this.store.table
    },

    data () {
      return this.store.states.data
    },

    renderedColColumns () {
      return this.store.states.renderedColColumns
    },

    colColumns () {
      return this.store.states.colColumns
    },

    columnClasses () {
      const target = this.colColumns
      const maxIndex = target.length - 1
      return target.map((c, i) => this.getCellClasses(i === maxIndex, c))
    },

    firstTextColumnIndex () {
      return this.colColumns.findIndex(c => c.type === 'default')
    },

    expandable () {
      return this.store.states.expandable
    },

    expandRows () {
      return this.store.states.expandRows
    },

    sortingColumn () {
      return this.store.states.sortingColumn
    }
  },

  props: {
    store: {
      required: true
    },
    stripe: Boolean,
    border: Boolean,
    fixed: String
  },

  watch: {
    'store.states.hoverRowIndex' (newVal, oldVal) {
      if (!this.store.states.hasFixedTable) return
      const el = this.$el
      if (!el) return
      const rows = el.querySelectorAll('.table-row')
      const oldRow = rows[oldVal]
      const newRow = rows[newVal]
      if (oldRow) {
        removeClass(oldRow, 'hover-state')
      }
      if (newRow) {
        addClass(newRow, 'hover-state')
      }
    }
  },

  methods: {
    getCellClasses (transparent, column) {
      return {
        'right-border': this.border,
        'bottom-border': this.border || !this.stripe,
        'transparent-border': this.fixed && transparent,
        'sorting-column': column && this.sortingColumn === column,
        'hidden-column': column && ((this.fixed && !column.fixed) || (!this.fixed && column.fixed))
      }
    },

    handleMouseEnter (index) {
      this.store.commit('setHoverRowIndex', index)
    },

    handleMouseLeave () {
      this.store.commit('setHoverRowIndex', null)
    }
  }
}
</script>
