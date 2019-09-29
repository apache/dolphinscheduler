<template>
  <table
    :class="wrapperClass"
    cellspacing="0"
    cellpadding="0"
    border="0">
    <colgroup>
      <col :name="column.id" v-for="column in colColumns" :key="column.id">
    </colgroup>
    <tbody>
      <template v-for="(row, i) in data">
        <tr
          :key="getRowKey(i, row)"
          :table-row-key="getRowKey(i, row)"
          class="table-row"
          :class="getRowClasses(i)"
          @mouseenter="handleMouseEnter(i)"
          @mouseleave="handleMouseLeave()">
          <x-table-td
            v-for="(column, j) in colColumns"
            :key="column.id"
            :class="getCellClasses(j === colColumns.length -1)"
            :store="store"
            :row="row"
            :column="column"
            :first="firstTextColumnIndex === j"
            :row-index="i"
            :column-index="j"
            :fixed="fixed"
          ></x-table-td>
        </tr>
        <tr v-if="expandable && expandRows.includes(row)" :key="'expand_' + getRowKey(i, row)">
          <td class="table-cell" :class="getCellClasses(false)" :colspan="colColumns.length">
            <x-cell-renderer
              expand
              :row="row"
              :r-index="i"
              :render="table.expendRender">
            </x-cell-renderer>
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>

<script>
import { LIB_NAME, addClass, removeClass, getValueByPath } from '../../../../src/util'
import xTableTd from './TableTd'
import xCellRenderer from './cellRenderer.js'
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

    columns () {
      return this.store.states.columns
    },

    leafColumns () {
      return this.store.states.leafColumns
    },

    colColumns () {
      return this.table.multiLayerHeader ? this.leafColumns : this.columns
    },

    firstTextColumnIndex () {
      return this.colColumns.findIndex(c => c.type === 'default')
    },

    expandable () {
      return this.store.states.expandable
    },

    expandRows () {
      return this.store.states.expandRows
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
    getRowKey (i, row) {
      if (!row) {
        throw new Error('Table Body: Find invalid row!')
      }
      const rowKey = this.table.rowKey
      if (rowKey) {
        return getValueByPath(row, rowKey)
      }
      return i
    },

    getRowClasses (i) {
      const classes = []
      if (this.stripe) {
        classes.push(i % 2 === 1 ? 'striped-row' : 'no-striped-row')
      }
      return classes
    },

    getCellClasses (transparent) {
      return {
        'right-border': this.border,
        'bottom-border': this.border || !this.stripe,
        'transparent-border': this.fixed && transparent
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
