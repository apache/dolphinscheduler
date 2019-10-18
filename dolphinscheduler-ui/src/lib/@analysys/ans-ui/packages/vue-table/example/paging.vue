<template>
  <div style="margin-bottom: 20px;">
    <div style="line-height: 40px;">性能演示</div>
    <x-table affix internal-paging border height="400" :default-column-width="200" row-key="rowId" :data="tableData">
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
      ></x-table-column>
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
    }
  }
}
</script>
