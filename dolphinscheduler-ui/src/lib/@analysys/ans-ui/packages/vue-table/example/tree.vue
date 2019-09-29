<template>
  <div style="margin: 20px;">
    <div style="line-height: 40px;">table tree</div>
    <x-table :data="tableData">
      <x-table-column
        v-for="(header, index) in tableHeaders"
        :key="index"
        :label="header.label"
        :prop="header.prop">
      </x-table-column>
    </x-table>
  </div>
</template>

<script>
import { xTable, xTableColumn } from '../src'

export default {
  name: 'app',
  data () {
    return {
      rowCount: 5,
      tableHeaders: [
        { label: '用户ID', prop: 'name' },
        { label: '城市', prop: 'city' },
        { label: '最近一次使用', prop: 'lastUsedTime' }
      ]
    }
  },
  components: { xTable, xTableColumn },
  computed: {
    tableData () {
      const list = this.getListData().slice(0, 1)
      list[0].children = this.getListData()
      list[0].children[2].children = this.getListData()
      list[0].children[2].children[2].children = this.getListData()
      return list
    }
  },
  methods: {
    getListData () {
      const list = []
      for (let i = 0; i < this.rowCount; i++) {
        list.push({
          id: i + 1,
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
  }
}
</script>
