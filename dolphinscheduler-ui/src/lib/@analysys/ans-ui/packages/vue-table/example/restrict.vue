<template>
  <div style="margin: 20px;">
    <div style="line-height: 40px;">restrict 属性</div>
    <button @click="changeHeight">改变高度</button>
    <div style="width:800px;overflow:hidden;" :style="{height:height + 'px'}">
      <x-table :data="tableData" ref="table" restrict>
        <x-table-column
          v-for="header in tableHeaders"
          :fixed="header.id === 1 ? 'left' : header.id === 2 ? 'right' : false"
          :width="200"
          :key="header.id"
          :label="header.label"
          :prop="header.prop">
        </x-table-column>
      </x-table>
    </div>
  </div>
</template>

<script>
import { xTable, xTableColumn } from '../src'

export default {
  name: 'app',
  data () {
    return {
      height: 200,
      tableHeaders: [
        { id: 1, label: '用户ID', prop: 'name' },
        { id: 2, label: '城市', prop: 'city' },
        { id: 3, label: '职业', prop: 'occupation' },
        { id: 4, label: '电话', prop: 'cellphone' },
        { id: 5, label: '使用次数', prop: 'usedCount' },
        { id: 6, label: '最近一次使用', prop: 'lastUsedTime' }
      ]
    }
  },
  components: { xTable, xTableColumn },
  computed: {
    tableData () {
      const list = []
      for (let i = 0; i < 6; i++) {
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
  },
  methods: {
    changeHeight () {
      this.height = this.height > 240 ? 200 : 400
      setTimeout(() => this.$refs.table.doLayout(), 200)
    }
  }
}
</script>
