<template>
  <td
    v-if="visible && !column.destroyed"
    :rowspan="cell.spanModel.rowspan"
    :colspan="cell.spanModel.colspan"
    class="table-cell">
    <x-cell-renderer
      v-if="column.customRender"
      :content="cell.content"
      :row="row"
      :column="column"
      :r-index="rowIndex"
      :c-index="cell.columnIndex"
      :custom-render="column.customRender">
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
        :title="titleText"
        class="table-cell-content"
        :class="contentClasses">
        <template v-if="showUnfoldIcon">
          <i
            class="unfold-icon ans-icon-arrow-right"
            :class="{'rotate-down':unfold}"
            @click="store.commit('rowUnfoldingChanged', row)"
          ></i>
          <span class="tree-branch-text" @click="store.commit('rowUnfoldingChanged', row)">
            <x-cell-renderer
              v-if="column.treeText && first"
              :content="cell.content"
              :row="row"
              :column="column"
              :r-index="rowIndex"
              :c-index="cell.columnIndex"
              :custom-render="column.treeText">
            </x-cell-renderer>
            <template v-else>{{cell.content}}</template>
          </span>
        </template>
        <template v-else>
          <x-cell-renderer
            v-if="column.treeText && first"
            :content="cell.content"
            :row="row"
            :column="column"
            :r-index="rowIndex"
            :c-index="cell.columnIndex"
            :custom-render="column.treeText">
          </x-cell-renderer>
          <template v-else>{{cell.content}}</template>
        </template>
      </div>
    </template>
  </td>
</template>

<script>
import { xCheckbox } from '../../../vue-checkbox/src'
import xCellRenderer from './CellRenderer.vue'

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
    cell: {
      required: true
    },
    first: Boolean
  },

  computed: {
    table () {
      return this.store.table
    },

    visible () {
      return this.cell.spanModel.rowspan || this.cell.spanModel.colspan
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

    titleText () {
      return this.store.states.treeType && this.table.treeTitle ? this.cell.content : undefined
    },

    unfold () {
      return this.store.states.unfoldedRows.includes(this.row)
    },

    contentClasses () {
      let classes = `align-${this.column.align}`
      if (this.store.states.treeType && this.first) {
        classes += ` row-level-${this.row.__level}`
        if (this.showUnfoldIcon) {
          classes += ' flex-wrapper'
        }
        if (this.table.treeTitle) {
          classes += ' ellipsis-text'
        }
      }
      return classes
    }
  }
}
</script>
