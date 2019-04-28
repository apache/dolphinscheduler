<template>
  <div class="list-model">
    <div class="table-box">
      <table class="fixed">
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>{{$t('Process Name')}}</span>
          </th>
          <th width="50">
            <span>{{$t('State')}}</span>
          </th>
          <th width="140">
            <span>{{$t('Create Time')}}</span>
          </th>
          <th width="140">
            <span>{{$t('Update Time')}}</span>
          </th>
          <th>
            <span>{{$t('Description')}}</span>
          </th>
          <th width="90">
            <span>{{$t('Timing state')}}</span>
          </th>
          <th width="220">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span class="ellipsis">
              <router-link :to="{ path: '/projects/definition/list/' + item.id}" tag="a" class="links">
                {{item.name}}
              </router-link>
            </span>
          </td>
          <td><span>{{_rtPublishStatus(item.releaseState)}}</span></td>
          <td>
            <span v-if="item.createTime">{{item.createTime | formatDate}}</span>
            <span v-if="!item.createTime">-</span>
          </td>
          <td>
            <span>{{item.updateTime | formatDate}}</span>
          </td>
          <td><span class="ellipsis">{{item.desc}}</span></td>
          <td>
            <span v-if="item.scheduleReleaseState === 'OFFLINE'">{{$t('offline')}}</span>
            <span v-if="item.scheduleReleaseState === 'ONLINE'">{{$t('online')}}</span>
            <span v-if="!item.scheduleReleaseState">-</span>
          </td>
          <td>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Edit')" @click="_edit(item)" :disabled="item.releaseState === 'ONLINE'" v-ps="['GENERAL_USER']" icon="iconfont icon-bianji"><!--{{$t('编辑')}}--></x-button>
            <x-button type="success" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Start')" @click="_start(item)" :disabled="item.releaseState !== 'ONLINE'" v-ps="['GENERAL_USER']" icon="iconfont icon-qidong"><!--{{$t('启动')}}--></x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Timing')" @click="_timing(item)" :disabled="item.releaseState !== 'ONLINE' || item.scheduleReleaseState !== null" v-ps="['GENERAL_USER']" icon="iconfont icon-timer"><!--{{$t('定时')}}--></x-button>
            <x-button type="error" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('offline')" @click="_downline(item)" v-if="item.releaseState === 'ONLINE'" v-ps="['GENERAL_USER']" icon="iconfont icon-erji-xiaxianjilu"><!--{{$t('下线')}}--></x-button>
            <x-button type="warning" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('online')" @click="_poponline(item)" v-if="item.releaseState === 'OFFLINE'" v-ps="['GENERAL_USER']" icon="iconfont icon-erji-xiaxianjilu-copy"><!--{{$t('上线')}}--></x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('Cron Management')" @click="_timingManage(item)" :disabled="item.releaseState !== 'ONLINE'" v-ps="['GENERAL_USER']" icon="iconfont icon-paibanguanli"><!--{{$t('定时管理')}}--></x-button>
            <x-button type="info" shape="circle" size="xsmall" data-toggle="tooltip" :title="$t('TreeView')" @click="_treeView(item)"  icon="iconfont icon-juxingkaobei"><!--{{$t('树形图')}}--></x-button>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import mStart from './start'
  import mTiming from './timing'
  import { mapActions } from 'vuex'
  import '@/module/filter/formatDate'
  import { publishStatus } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'definition-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      processList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('dag', ['editProcessState', 'getStartCheck', 'getReceiver']),
      _rtPublishStatus (code) {
        return _.filter(publishStatus, v => v.code === code)[0].desc
      },
      _treeView (item) {
        this.$router.push({ path: `/projects/definition/tree/${item.id}` })
      },
      /**
       * Start
       */
      _start (item) {
        this.getStartCheck({ processDefinitionId: item.id }).then(res => {
          let self = this
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mStart, {
                on: {
                  onUpdate () {
                    self._onUpdate()
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  item: item
                }
              })
            }
          })
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      /**
       * get emial
       */
      _getReceiver (id) {
        return new Promise((resolve, reject) => {
          this.getReceiver({ processDefinitionId: id }).then(res => {
            resolve({
              receivers: res.receivers && res.receivers.split(',') || [],
              receiversCc: res.receiversCc && res.receiversCc.split(',') || []
            })
          })
        })
      },
      /**
       * timing
       */
      _timing (item) {
        let self = this
        this._getReceiver(item.id).then(res => {
          let modal = this.$modal.dialog({
            closable: false,
            showMask: true,
            escClose: true,
            className: 'v-modal-custom',
            transitionName: 'opacityp',
            render (h) {
              return h(mTiming, {
                on: {
                  onUpdate () {
                    self._onUpdate()
                    modal.remove()
                  },
                  close () {
                    modal.remove()
                  }
                },
                props: {
                  item: item,
                  receiversD: res.receivers,
                  receiversCcD: res.receiversCc
                }
              })
            }
          })
        })
      },
      /**
       * Timing manage
       */
      _timingManage (item) {
        this.$router.push({ path: `/projects/definition/list/timing/${item.id}` })
      },
      /**
       * edit
       */
      _edit (item) {
        this.$router.push({ path: `/projects/definition/list/${item.id}` })
      },
      /**
       * Offline
       */
      _downline (item) {
        this._upProcessState({
          processId: item.id,
          releaseState: 0
        })
      },
      /**
       * online
       */
      _poponline (item) {
        this._upProcessState({
          processId: item.id,
          releaseState: 1
        })
      },
      /**
       * Edit state
       */
      _upProcessState (o) {
        this.editProcessState(o).then(res => {
          this.$message.success(res.msg)
          $('body').find('.tooltip.fade.top.in').remove()
          this._onUpdate()
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      _onUpdate () {
        this.$emit('on-update')
      }
    },
    watch: {
      processList (a) {
        this.list = []
        setTimeout(() => {
          this.list = a
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this.processList
    },
    components: { }
  }
</script>
