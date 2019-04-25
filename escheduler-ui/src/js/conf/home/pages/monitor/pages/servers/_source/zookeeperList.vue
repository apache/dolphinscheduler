<template>
  <div class="list-model zookeeper-list">
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>host</span>
          </th>
          <th>
            <span>连接数</span>
          </th>
          <th>
            <span>watches数量</span>
          </th>
          <th>
            <span>发送量</span>
          </th>
          <th>
            <span>接收量</span>
          </th>
          <th>
            <span>leader/follower</span>
          </th>
          <th>
            <span>最低延时</span>
          </th>
          <th>
            <span>平均延时</span>
          </th>
          <th>
            <span>最大延时</span>
          </th>
          <th>
            <span>节点数</span>
          </th>
          <th>
            <span>当前查询时间戳</span>
          </th>
          <th style="text-align: center">
            <span>节点自检状态</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{$index + 1}}</span>
          </td>
          <td>
            <span>
              <a href="javascript:" class="links">{{item.hostname}}</a>
            </span>
          </td>
          <td><span>{{item.connections}}</span></td>
          <td>
            <span>{{item.watches}}</span>
          </td>
          <td>
            <span>{{item.sent}}</span>
          </td>
          <td>
            <span>{{item.received}}</span>
          </td>
          <td><span>{{item.mode}}</span></td>
          <td>
            <span>{{item.minLatency}}</span>
          </td>
          <td>
            <span>{{item.avgLatency}}</span>
          </td>
          <td>
            <span>{{item.maxLatency}}</span>
          </td>
          <td>
            <span>{{item.nodeCount}}</span>
          </td>
          <td>
            <span>{{item.date | formatDate}}</span>
          </td>
          <td>
            <span class="state">
              <i class="iconfont success" v-if="item.state">&#xe607;</i>
              <i class="iconfont error" v-else>&#xe626;</i>
            </span>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'zookeeper-list',
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

<style lang="scss" rel="stylesheet/scss">
  .zookeeper-list {
    .state {
      text-align: center;
      display: block;
      >i {
        font-size: 18px;
      }
      .success {
        color: #33cc00;
      }
      .error {
        color: #ff0000;
      }
    }
  }
</style>

