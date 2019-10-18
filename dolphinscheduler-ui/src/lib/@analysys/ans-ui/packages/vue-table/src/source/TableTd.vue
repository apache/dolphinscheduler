<template>
  <td
    v-if="visible"
    :rowspan="rowspan"
    :colspan="colspan"
    :class="{ 'sorting-column': sortingColumn === column, 'hidden-column': fixed && column.__hiddenInFixed }"
    class="table-cell">
    <x-cell-renderer
      v-if="column.customRender"
      :content="getCellContent()"
      :row="row"
      :column="column"
      :r-index="rowIndex"
      :c-index="columnIndex"
      :render="column.customRender">
    </x-cell-renderer>
    <template v-else>
      <div v-if="column.type === 'selection'" class="table-cell-content icon-column">
        <x-checkbox
          :value="selection.includes(row)"
          @on-change="store.commit('rowSelectionChanged', row)">
        </x-checkbox>
      </div>
      <div v-else-if="column.type === 'expand'" class="table-cell-content icon-column">
        <i
          class="expand-icon"
          :class="[expandRows.includes(row) ? 'ans-icon-reduce' : 'ans-icon-increase']"
          @click="store.commit('rowExpansionChanged', row)"></i>
      </div>
      <div
        v-else
        class="table-cell-content"
        :class="contentClasses">
        <template v-if="showUnfoldIcon">
          <span class="tree-branch-text">{{getCellContent()}}</span>
          <i
            class="unfold-icon ans-icon-arrow-right"
            :class="{'rotate-down':unfold}"
            @click="store.commit('rowUnfoldingChanged', row)"
          ></i>
        </template>
        <template v-else>{{getCellContent()}}</template>
      </div>
    </template>
  </td>
</template>

<script>
import { xCheckbox } from '../../../vue-checkbox/src'
import xCellRenderer from './cellRenderer.js'
import { getValueByPath } from '../../../../src/util'

export default {
  name: 'xTableTd',

  components: { xCheckbox, xCellRenderer },

  props: {
    store: {
      required: true
    },
    row: {
      required: true
    },
    column: {
      required: true
    },
    rowIndex: {
      required: true
    },
    columnIndex: {
      required: true
    },
    first: Boolean,
    fixed: String
  },

  data () {
    return {
      rowspan: 1,
      colspan: 1
    }
  },

  computed: {
    table () {
      return this.store.table
    },

    visible () {
      return this.rowspan || this.colspan
    },

    selection () {
      return this.store.states.selection
    },

    expandRows () {
      return this.store.states.expandRows
    },

    showUnfoldIcon () {
      return this.store.states.treeType && this.store.states.parentRows.includes(this.row) && this.first
    },

    unfold () {
      return this.store.states.unfoldedRows.includes(this.row)
    },

    sortingColumn () {
      return this.store.states.sortingColumn
    },

    contentClasses () {
      let classes = `align-${this.column.align}`
      if (this.store.states.treeType && this.first) {
        classes += ` row-level-${this.row.__level}`
      }
      return classes
    }
  },

  methods: {
    getCellContent () {
      const { row, column, rowIndex, columnIndex } = this
      const value = getValueByPath(row, column.prop)
      if (column.formatter) {
        return column.formatter(row, column, value, rowIndex, columnIndex)
      }
      return value
    }
  },

  created () {
    const cellSpanMethod = this.table.cellSpanMethod
    const { row, column, rowIndex, columnIndex } = this
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
      this.colspan = colspan
      this.rowspan = rowspan
    }
  }
}
</script>
