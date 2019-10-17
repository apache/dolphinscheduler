<template>
  <table
    :class="wrapperClass"
    cellspacing="0"
    cellpadding="0"
    border="0">
    <colgroup>
      <col :name="column.id" v-for="(column, i) in colColumns" :key="i">
    </colgroup>
    <thead>
      <tr v-if="!multiLayer" class="table-header-row" :class="{stripe}">
        <x-table-th
          v-for="(column, i) in renderedColumns"
          :key="i"
          :store="store"
          :border="border"
          :column="column"
          :column-index="column._index"
          :class="columnClasses[column.id]"
        ></x-table-th>
      </tr>
      <tr
        v-else
        v-for="(row, i) in renderedRows"
        :key="i"
        class="table-header-row"
        :class="{stripe}">
        <x-table-th
          v-for="column in row"
          :key="i + '-' + column.id"
          :store="store"
          :border="border"
          :column="column"
          :column-index="column._indexInRow"
          :colspan="column.colSpan"
          :rowspan="column.rowSpan"
          :class="columnClasses[column.id]"
        ></x-table-th>
      </tr>
    </thead>
  </table>
</template>

<script>
import { LIB_NAME } from '../../../../src/util'
import layoutObserver from './layoutObserver.js'
import xTableTh from './TableTh'

export default {
  name: 'xTableHeader',

  components: { xTableTh },

  mixins: [layoutObserver],

  data () {
    return {
      wrapperClass: `${LIB_NAME}-table-header`
    }
  },

  computed: {
    table () {
      return this.store.table
    },

    columns () {
      return this.store.states.columns
    },

    renderedColumns () {
      return this.store.states.renderedColumns
    },

    columnClasses () {
      if (this.multiLayer) {
        const r = {}
        for (let i = 0; i < this.rows.length; i++) {
          const row = this.rows[i]
          const maxIndex = row.length - 1
          for (let j = 0; j < row.length; j++) {
            const c = row[j]
            r[c.id] = this.getCellClasses(j === maxIndex, c)
          }
        }
        return r
      } else {
        const maxIndex = this.columns.length - 1
        const r = {}
        this.columns.forEach((c, i) => r[c.id] = this.getCellClasses(i === maxIndex, c))
        return r
      }
    },

    rows () {
      return this.store.states.rows
    },

    renderedRows () {
      return this.store.states.renderedRows
    },

    multiLayer () {
      return this.store.states.multiLayer
    },

    colColumns () {
      return this.store.states.renderedColColumns
    },

    sortingColumn () {
      return this.store.states.sortingColumn
    },

    sortOrder () {
      return this.store.states.sortOrder
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

  methods: {
    getCellClasses (transparent, column) {
      return {
        'right-border': this.border,
        'bottom-border': this.border,
        'transparent-border': this.fixed && transparent,
        [this.sortOrder]: this.sortingColumn === column,
        'hidden-column': (this.fixed && !column.fixed) || (!this.fixed && column.fixed)
      }
    }
  }
}
</script>
