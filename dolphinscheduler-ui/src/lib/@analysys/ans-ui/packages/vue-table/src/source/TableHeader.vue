<template>
  <table
    :class="wrapperClass"
    cellspacing="0"
    cellpadding="0"
    border="0">
    <colgroup>
      <col :name="column.id" v-for="column in colColumns" :key="column.id">
    </colgroup>
    <thead>
      <tr v-if="!multiLayer" class="table-header-row" :class="{stripe}">
        <x-table-th
          v-for="(column, i) in columns"
          :key="column.id"
          :store="store"
          :border="border"
          :column="column"
          :column-index="i"
          :class="getCellClasses(i === columns.length - 1)"
          :fixed="fixed"
        ></x-table-th>
      </tr>
      <tr
        v-else
        v-for="(row, i) in rows"
        :key="i"
        class="table-header-row"
        :class="{stripe}">
        <x-table-th
          v-for="(column, j) in row"
          :key="column.id"
          :store="store"
          :border="border"
          :column="column"
          :column-index="j"
          :colspan="column.colSpan"
          :rowspan="column.rowSpan"
          :class="getCellClasses(j === row.length - 1)"
          :fixed="fixed"
        ></x-table-th>
      </tr>
    </thead>
  </table>
</template>

<script>
import { LIB_NAME } from '../../../../src/util'
import layoutObserver from './layoutObserver.js'
import xTableTh from './TableTh'

const getAllColumns = (columns) => {
  const result = []
  columns.forEach((column) => {
    if (column.children) {
      result.push(column)
      result.push.apply(result, getAllColumns(column.children))
    } else {
      result.push(column)
    }
  })
  return result
}

const convertToRows = (originColumns) => {
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

export default {
  name: 'xTableHeader',

  components: { xTableTh },

  mixins: [layoutObserver],

  data () {
    return {
      wrapperClass: `${LIB_NAME}-table-header`,
      rows: [],
      multiLayer: false
    }
  },

  computed: {
    table () {
      return this.store.table
    },

    columns () {
      return this.store.states.columns
    },

    leafColumns () {
      return this.store.states.leafColumns
    },

    colColumns () {
      return this.multiLayer ? this.leafColumns : this.columns
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
    columns: {
      immediate: true,
      handler (val) {
        this.rows = convertToRows(val)
        this.multiLayer = this.rows.length > 1
        this.table.multiLayerHeader = this.multiLayer
      }
    }
  },

  methods: {
    getCellClasses (transparent) {
      return {
        'right-border': this.border,
        'bottom-border': this.border,
        'transparent-border': this.fixed && transparent
      }
    }
  }
}
</script>
