<template>
  <div style="margin: 20px;">
    <div style="line-height: 40px;">排序测试</div>
    <x-table :data="tbody" @on-sort-change="handleSort">
      <x-table-column
        v-for="header in tableHeaders"
        :key="header.id"
        :label="header.label"
        :prop="header.prop"
        sortable="custom"
      ></x-table-column>
    </x-table>
  </div>
</template>

<script>
import { xTable, xTableColumn } from '../src'

export default {
  name: 'app',
  data () {
    return {
      tableHeaders: [
        { id: 1, label: '用户ID', prop: 'name' },
        { id: 2, label: '城市', prop: 'city' },
        { id: 3, label: '最近一次使用', prop: 'lastUsedTime' }
      ],
      tbody: []
    }
  },
  components: { xTable, xTableColumn },
  computed: {
    tableData () {
      const list = []
      for (let i = 0; i < 10; i++) {
        list.push({
          id: i + 2,
          name: '易小宝',
          city: '长沙',
          lastUsedTime: `2018-09-${i + 1 < 10 ? '0' + (i + 1) : (i + 1)} 12:30:33`,
          province: '湖南',
          address: '岳麓区岳麓山下',
          age: '22',
          gender: '男',
          occupation: '工程师',
          cellphone: '18888888888',
          usedCount: Math.floor(Math.random() * 10)
        })
      }
      return list
    }
  },
  methods: {
    handleSort (column, prop, order) {
      this.tbody.sort((a, b) => {
        return Math.random() - 0.5
      })
      console.log(this.tbody)
    }
  },
  mounted () {
    this.tbody = this.tableData
  }
}
</script>
