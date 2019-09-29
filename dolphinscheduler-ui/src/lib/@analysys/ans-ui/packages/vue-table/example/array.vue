<template>
  <div style="margin: 20px;">
    <div style="line-height: 40px;">列表支持</div>
    <x-table :data="tableData">
      <x-table-column
        prop="a1[0][1].b[2]"
        label="数组">
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
      tableData: [
        {
          a1: [
            [1, { b: ['a', 'b', 'c'] }, 3]
          ]
        }
      ]
    }
  },
  components: { xTable, xTableColumn }
}
</script>
