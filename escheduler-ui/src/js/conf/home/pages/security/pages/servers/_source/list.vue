<template>
  <div class="list-model">
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('ip')}}</span>
          </th>
          <th>
            <span>{{$t('进程pid')}}</span>
          </th>
          <th>
            <span>{{$t('zk注册目录')}}</span>
          </th>
          <th>
            <span>{{$t('cpuUsage')}}</span>
          </th>
          <th>
            <span>{{$t('memoryUsage')}}</span>
          </th>
          <th>
            <span>{{$t('创建时间')}}</span>
          </th>
          <th>
            <span>{{$t('最后心跳时间')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index">
          <td>
            <span>{{$index + 1}}</span>
          </td>
          <td>
            <span>
              <a href="javascript:" class="links">{{item.host}}</a>
            </span>
          </td>
          <td><span>{{item.port}}</span></td>
          <td>
            <span>{{item.zkDirectory}}</span>
          </td>
          <td>
            <span>{{_rtResInfo(JSON.parse(item.resInfo)['cpuUsage'])}}</span>
          </td>
          <td>
            <span>{{_rtResInfo(JSON.parse(item.resInfo)['memoryUsage'])}}</span>
          </td>
          <td><span>{{item.createTime | formatDate}}</span></td>
          <td>
            <span>{{item.lastHeartbeatTime | formatDate}}</span>
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
      _rtResInfo (val) {
        return (val * 100).toFixed(2) + ' %'
      }
    }
  }
</script>
