<template>
  <div style="margin: 20px;">
    <div style="line-height: 40px;">动态列</div>
    <x-table :data="tableData" ref="table" border>
      <template v-for="header in headers">
        <x-table-column
          v-if="header.children"
          :key="header.id"
          :label="header.label"
        >
          <template slot="headerText" slot-scope="scope">
            <div>{{scope.column.label + scope.$index}}</div>
          </template>

          <x-table-column
            v-for="sub in header.children"
            :key="sub.id"
            :width="400"
            :label="sub.label"
            :prop="sub.prop">
          </x-table-column>
        </x-table-column>
        <x-table-column
          v-else
          :fixed="header.id === 1 ? 'left' : header.id === 2 ? 'right' : false"
          :width="300"
          :key="header.id"
          :label="header.label"
          :prop="header.prop">
        </x-table-column>
      </template>
    </x-table>
  </div>
</template>

<script>
import { xTable, xTableColumn } from '../src'

export default {
  name: 'app',
  data () {
    return {
      headers: [],
      multipleHeaders: [
        { id: 1, label: '用户ID', prop: 'name' },
        { id: 2, label: '最近一次使用', prop: 'lastUsedTime' },
        {
          id: 3,
          label: '地址',
          children: [
            { id: 4, label: '省份', prop: 'province' },
            { id: 5, label: '城市', prop: 'city' },
            { id: 6, label: '详细地址', prop: 'address' }
          ]
        }
      ],
      tableHeaders: [
        { id: 1, label: '用户ID', prop: 'name' },
        { id: 5, label: '使用次数', prop: 'usedCount' },
        { id: 6, label: '最近一次使用', prop: 'lastUsedTime' }
      ]
    }
  },
  components: { xTable, xTableColumn },
  computed: {
    tableData () {
      const list = []
      for (let i = 0; i < 10; i++) {
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
  mounted () {
    this.headers = this.multipleHeaders
    // setTimeout(() => {
    //   this.$refs.table.setScrollPosition('left')
    //   setTimeout(() => {
    //     this.headers = this.tableHeaders
    //     setTimeout(() => {
    //       this.$refs.table.setScrollPosition('right')
    //     }, 2000)
    //   }, 2000)
    // }, 2000)
  }
}
</script>
