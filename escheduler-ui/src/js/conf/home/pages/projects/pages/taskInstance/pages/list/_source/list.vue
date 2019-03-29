<template>
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th>
            <span>{{$t('编号')}}</span>
          </th>
          <th>
            <span>{{$t('名称')}}</span>
          </th>
          <th>
            <span>{{$t('工作流实例')}}</span>
          </th>
          <th width="80">
            <span>{{$t('节点类型')}}</span>
          </th>
          <th width="40">
            <span>{{$t('状态')}}</span>
          </th>
          <th width="140">
            <span>{{$t('提交时间')}}</span>
          </th>
          <th width="140">
            <span>{{$t('开始时间')}}</span>
          </th>
          <th width="140">
            <span>{{$t('结束时间')}}</span>
          </th>
          <th width="110">
            <span>{{$t('host')}}</span>
          </th>
          <th width="74">
            <span>{{$t('运行时长')}}(s)</span>
          </th>
          <th width="84">
            <span>{{$t('重试次数')}}</span>
          </th>
          <th width="50">
            <span>{{$t('操作')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span v-if="isAuth" class="ellipsis"><a href="javascript:" class="links">{{item.name}}</a></span>
            <span v-if="!isAuth" class="ellipsis"><a href="javascript:" class="links">{{item.name}}</a></span>
          </td>
          <td><span class="ellipsis">{{item.processInstanceName}}</span></td>
          <td><span>{{item.taskType}}</span></td>
          <td><span v-html="_rtState(item.state)" style="cursor: pointer;"></span></td>
          <td><span>{{item.submitTime | formatDate}}</span></td>
          <td><span>{{item.startTime | formatDate}}</span></td>
          <td>
            <span v-if="item.endTime">{{item.endTime | formatDate}}</span>
            <span v-if="!item.endTime">-</span>
          </td>
          <td><span>{{item.host || '-'}}</span></td>
          <td><span>{{item.duration}}</span></td>
          <td><span>{{item.retryTimes}}</span></td>
          <td>
            <x-button
                    type="info"
                    shape="circle"
                    size="xsmall"
                    data-toggle="tooltip"
                    :title="$t('查看日志')"
                    icon="iconfont icon-xitongcaozuorizhi"
                    @click="_refreshLog(item)">
            </x-button>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  import '@/module/filter/formatDate'
  import Permissions from '@/module/permissions'
  import mLog from '@/conf/home/pages/dag/_source/formModel/log'
  import { tasksState } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'list',
    data () {
      return {
        list: [],
        isAuth: Permissions.getAuth(),
        backfillItem: {}
      }
    },
    props: {
      taskInstanceList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      _rtState (code) {
        let o = tasksState[code]
        return `<em class="iconfont ${o.isSpin ? 'fa fa-spin' : ''}" style="color:${o.color}" data-toggle="tooltip" data-container="body" title="${o.desc}">${o.icoUnicode}</em>`
      },
      _refreshLog (item) {
        let self = this
        let instance = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          render (h) {
            return h(mLog, {
              on: {
                ok () {
                },
                close () {
                  instance.remove()
                }
              },
              props: {
                self: self,
                source: 'list',
                logId: item.id
              }
            })
          }
        })
      }
    },
    watch: {
      taskInstanceList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.taskInstanceList
    },
    components: { }
  }
</script>