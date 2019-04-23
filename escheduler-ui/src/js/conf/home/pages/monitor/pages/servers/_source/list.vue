<template>
  <div class="list-model">
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>node</span>
          </th>
          <th>
            <span>ip</span>
          </th>
          <th>
            <span>模式</span>
          </th>
          <th>
            <span>连接数</span>
          </th>
          <th>
            <span>watch数</span>
          </th>
          <th>
            <span>数据量</span>
          </th>
          <th>
            <span>Sent/Received</span>
          </th>
          <th>
            <span>节点自检状态</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{$index + 1}}</span>
          </td>
          <td>
            <span>
              <a href="javascript:" class="links">task1</a>
            </span>
          </td>
          <td><span>192.11.1.1</span></td>
          <td>
            <span>2222</span>
          </td>
          <td>
            <span>3333</span>
          </td>
          <td>
            <span>4444</span>
          </td>
          <td><span>5555</span></td>
          <td>
            <span>6666</span>
          </td>
          <td>
            <span>7777</span>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  import '@/module/filter/formatDate'

  export default {
    name: 'tenement-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      list: Array
    },
    methods: {
    }
  }
</script>
