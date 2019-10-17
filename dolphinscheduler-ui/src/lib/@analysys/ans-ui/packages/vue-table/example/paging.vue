<template>
  <div style="margin-bottom: 20px;">
    <div style="line-height: 40px;">性能演示</div>
    <x-table affix border virtual-scroll height="300" :default-column-width="200" row-key="rowId" :data="tableData" :cellSpanMethod="cellSpanMethod">
      <x-table-column type="selection"></x-table-column>
      <x-table-column
        v-for="(header, i) in headers"
        :key="i"
        :fixed="i < 2"
        :formatter="formatter"
        :prop="header.prop"
        :label="header.label"
        sortable
        :sort-method="sortMethod"
      >
        <template slot="prepend" slot-scope="scope">
          <div :style="{margin:i === 2 ? '0 0 0 10px' : '0 10px 0 0'}">这是前置插槽{{scope.$index}}</div>
        </template>
        <template slot="append" slot-scope="scope">
          <div>这是后置插槽{{scope.$index}}</div>
        </template>
        <template slot="headerText" slot-scope="scope">
          <div>这是表头文本插槽</div>
          <div>{{scope.column.label}}</div>
        </template>
      </x-table-column>
    </x-table>
  </div>
</template>

<script>
import { xTable, xTableColumn } from '../src'
import jsonData from './indexs.json'

export default {
  name: 'app',
  data () {
    return {
      headers: jsonData.datas.table.heads
    }
  },
  components: { xTable, xTableColumn },
  computed: {
    tableData () {
      let id = 1
      jsonData.datas.table.bodys.forEach(row => {
        row.rowId = id++
      })
      return jsonData.datas.table.bodys.slice(0, 201)
    }
  },
  methods: {
    formatter (row, column, content, ri, ci) {
      const value = parseFloat(content)
      if (isNaN(value)) {
        return content || '—'
      }
      return value.toFixed(2)
    },
    sortMethod (a, b) {
      return a - b
    },
    cellSpanMethod ({ row, column, rowIndex, columnIndex }) {
      // console.log('span')
      if (rowIndex % 2 === 0) {
        if (columnIndex === 1) {
          return [1, 2]
        } else if (columnIndex === 2) {
          return [0, 0]
        }
      }
      if (columnIndex === 3) {
        if (rowIndex === 10) {
          return [5, 1]
        } else if ([11, 12, 13, 14].includes(rowIndex)) {
          return [0, 0]
        }
      }
      return [1, 1]
    }
  }
}
</script>
