<template>
  <div class="list-model">
    <div class="table-box">
      <table>
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>{{$t('IP')}}</span>
          </th>
          <th>
            <span>{{$t('Process Pid')}}</span>
          </th>
          <th>
            <span>{{$t('zk registration directory')}}</span>
          </th>
          <th>
            <span>{{$t('cpuUsage')}}</span>
          </th>
          <th>
            <span>{{$t('memoryUsage')}}</span>
          </th>
          <th>
            <span>{{$t('Create Time')}}</span>
          </th>
          <th>
            <span>{{$t('Last heartbeat time')}}</span>
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
