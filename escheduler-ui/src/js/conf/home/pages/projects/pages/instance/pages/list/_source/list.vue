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
          <th width="120">
            <span>{{$t('Run Type')}}</span>
          </th>
          <th width="140">
            <span>{{$t('Start Time')}}</span>
          </th>
          <th width="140">
            <span>{{$t('End Time')}}</span>
          </th>
          <th width="90">
            <span>{{$t('Duration')}}s</span>
          </th>
          <th width="72">
            <span>{{$t('Run Times')}}</span>
          </th>
          <th width="100">
            <span>{{$t('host')}}</span>
          </th>
          <th width="70">
            <span>{{$t('fault-tolerant sign')}}</span>
          </th>
          <th width="50">
            <span>{{$t('State')}}</span>
          </th>
          <th width="260">
            <span>{{$t('Operation')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="item.id">
          <td>
            <span>{{parseInt(pageNo === 1 ? ($index + 1) : (($index + 1) + (pageSize * (pageNo - 1))))}}</span>
          </td>
          <td>
            <span class="ellipsis" style="padding-left: 4px;"><router-link :to="{ path: '/projects/instance/list/' + item.id}" tag="a" class="links">{{item.name}}</router-link></span>
          </td>
          <td><span>{{_rtRunningType(item.commandType)}}</span></td>
          <td><span>{{item.startTime | formatDate}}</span></td>
          <td>
            <span v-if="item.endTime">{{item.endTime | formatDate}}</span>
            <span v-if="!item.endTime">-</span>
          </td>
          <td><span>{{item.duration || '-'}}</span></td>
          <td><span>{{item.runTimes}}</span></td>
          <td>
            <span v-if="item.host">{{item.host}}</span>
            <span v-if="!item.host">-</span>
          </td>
          <td><span>{{item.recovery}}</span></td>

          <td>
            <span v-html="_rtState(item.state)" style="cursor: pointer;"></span>
          </td>
          <td>
            <div v-show="item.disabled">
              <x-button type="info"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Edit')"
                        @click="_reEdit(item)"
                        v-ps="['GENERAL_USER']"
                        icon="iconfont icon-bianjixiugai"
                        :disabled="item.state !== 'SUCCESS' && item.state !== 'PAUSE' && item.state !== 'FAILURE' && item.state !== 'STOP'"></x-button>
              <x-button type="info"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Rerun')"
                        @click="_reRun(item,$index)"
                        v-ps="['GENERAL_USER']"
                        icon="iconfont icon-shuaxin"
                        :disabled="item.state !== 'SUCCESS' && item.state !== 'PAUSE' && item.state !== 'FAILURE' && item.state !== 'STOP'"></x-button>
              <x-button type="success"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Recovery Failed')"
                        @click="_restore(item,$index)"
                        v-ps="['GENERAL_USER']"
                        icon="iconfont icon-cuowuguanbishibai"
                        :disabled="item.state !== 'FAILURE'"></x-button>
              <x-button type="error"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Stop')"
                        @click="_stop(item)"
                        v-ps="['GENERAL_USER']"
                        icon="iconfont icon-zanting1"
                        :disabled="item.state !== 'RUNNING_EXEUTION'"></x-button>
              <x-button type="warning"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="item.state === 'PAUSE' ? $t('Recovery Suspend') : $t('Pause')"
                        @click="_suspend(item,$index)"
                        v-ps="['GENERAL_USER']"
                        :icon="item.state === 'PAUSE' ? 'iconfont icon-ai06' : 'iconfont icon-zanting'"
                        :disabled="item.state !== 'RUNNING_EXEUTION' && item.state !== 'PAUSE'"></x-button>
              <x-poptip
                      :ref="'poptip-delete-' + $index"
                      placement="bottom-end"
                      width="90">
                <p>{{$t('Delete?')}}</p>
                <div style="text-align: right; margin: 0;padding-top: 4px;">
                  <x-button type="text" size="xsmall" shape="circle" @click="_closeDelete($index)">{{$t('Cancel')}}</x-button>
                  <x-button type="primary" size="xsmall" shape="circle" @click="_delete(item,$index)">{{$t('Confirm')}}</x-button>
                </div>
                <template slot="reference">
                  <x-button
                          icon="iconfont icon-shanchu"
                          type="error"
                          shape="circle"
                          size="xsmall"
                          data-toggle="tooltip"
                          :title="$t('delete')"
                          v-ps="['GENERAL_USER']">
                  </x-button>
                </template>
              </x-poptip>

              <x-button type="info"
                        shape="circle"
                        size="xsmall"
                        data-toggle="tooltip"
                        :title="$t('Gantt')"
                        @click="_gantt(item)"
                        icon="iconfont icon-gantt">
              </x-button>

            </div>
            <div v-show="!item.disabled">
              <!--Edit-->
              <x-button
                      type="info"
                      shape="circle"
                      size="xsmall"
                      icon="iconfont icon-bianjixiugai"
                      disabled="true">
              </x-button>

              <!--Rerun-->
              <x-button
                      v-show="buttonType === 'run'"
                      type="info"
                      shape="circle"
                      size="xsmall"
                      disabled="true">
                {{item.count}}s
              </x-button>
              <x-button
                      v-show="buttonType !== 'run'"
                      type="info"
                      shape="circle"
                      size="xsmall"
                      icon="iconfont icon-shuaxin"
                      disabled="true">
              </x-button>

              <!--Recovery Failed-->
              <x-button
                      v-show="buttonType === 'store'"
                      type="success"
                      shape="circle"
                      size="xsmall"
                      disabled="true">
                {{item.count}}s
              </x-button>
              <x-button
                      v-show="buttonType !== 'store'"
                      type="success"
                      shape="circle"
                      size="xsmall"
                      icon="iconfont icon-cuowuguanbishibai"
                      disabled="true">
              </x-button>

              <!--Stop-->
              <x-button
                      type="error"
                      shape="circle"
                      size="xsmall"
                      icon="iconfont icon-zanting1"
                      disabled="true">
              </x-button>

              <!--倒计时 => Recovery Suspend/Pause-->
              <x-button
                      v-show="item.state === 'PAUSE' && buttonType === 'suspend'"
                      type="warning"
                      shape="circle"
                      size="xsmall"
                      disabled="true">
                {{item.count}}s
              </x-button>
              <!--Recovery Suspend-->
              <x-button
                      v-show="item.state === 'PAUSE' && buttonType !== 'suspend'"
                      type="warning"
                      shape="circle"
                      size="xsmall"
                      icon="iconfont icon-ai06"
                      disabled="true">
              </x-button>
              <!--Pause-->
              <x-button
                      v-show="item.state !== 'PAUSE'"
                      type="warning"
                      shape="circle"
                      size="xsmall"
                      icon="iconfont icon-zanting"
                      disabled="true">
              </x-button>

              <!--delete-->
              <x-button
                      type="error"
                      shape="circle"
                      size="xsmall"
                      icon="iconfont icon-shanchu"
                      :disabled="true">
              </x-button>

              <!--Gantt-->
              <x-button
                      type="info"
                      shape="circle"
                      size="xsmall"
                      icon="iconfont icon-gantt"
                      disabled="true">
              </x-button>
            </div>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import '@/module/filter/formatDate'
  import { tasksState, runningType } from '@/conf/home/pages/dag/_source/config'

  export default {
    name: 'list',
    data () {
      return {
        // 数据
        list: [],
        // 按钮类型
        buttonType: ''
      }
    },
    props: {
      processInstanceList: Array,
      pageNo: Number,
      pageSize: Number
    },
    methods: {
      ...mapActions('dag', ['editExecutorsState', 'deleteInstance']),
      /**
       * Return run type
       */
      _rtRunningType (code) {
        return _.filter(runningType, v => v.code === code)[0].desc
      },
      /**
       * Return status
       */
      _rtState (code) {
        let o = tasksState[code]
        return `<em class="iconfont ${o.isSpin ? 'fa fa-spin' : ''}" style="color:${o.color}" data-toggle="tooltip" data-container="body" title="${o.desc}">${o.icoUnicode}</em>`
      },
      /**
       * Close the delete layer
       */
      _closeDelete (i) {
        this.$refs[`poptip-delete-${i}`][0].doClose()
      },
      /**
       * delete
       */
      _delete (item, i) {
        this.deleteInstance({
          processInstanceId: item.id
        }).then(res => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this._onUpdate()
          this.$message.success(res.msg)
        }).catch(e => {
          this.$refs[`poptip-delete-${i}`][0].doClose()
          this.$message.error(e.msg || '')
        })
      },
      /**
       * edit
       */
      _reEdit (item) {
        this.$router.push({ path: `/projects/instance/list/${item.id}` })
      },
      /**
       * Rerun
       * @param REPEAT_RUNNING
       */
      _reRun (item, index) {
        this._countDownFn({
          id: item.id,
          executeType: 'REPEAT_RUNNING',
          index: index,
          buttonType: 'run'
        })
      },
      /**
       * Resume running
       * @param PAUSE => RECOVER_SUSPENDED_PROCESS
       * @param FAILURE => START_FAILURE_TASK_PROCESS
       */
      _restore (item, index) {
        this._countDownFn({
          id: item.id,
          executeType: 'START_FAILURE_TASK_PROCESS',
          index: index,
          buttonType: 'store'
        })
      },
      /**
       * stop
       * @param STOP
       */
      _stop (item) {
        this._upExecutorsState({
          processInstanceId: item.id,
          executeType: 'STOP'
        })
      },
      /**
       * pause
       * @param PAUSE
       */
      _suspend (item, index) {
        if (item.state === 'PAUSE') {
          this._countDownFn({
            id: item.id,
            executeType: 'RECOVER_SUSPENDED_PROCESS',
            index: index,
            buttonType: 'suspend'
          })
        } else {
          this._upExecutorsState({
            processInstanceId: item.id,
            executeType: item.state === 'PAUSE' ? 'RECOVER_SUSPENDED_PROCESS' : 'PAUSE'
          })
        }
      },
      /**
       * operating
       */
      _upExecutorsState (o) {
        this.editExecutorsState(o).then(res => {
          this.$message.success(res.msg)
          $('body').find('.tooltip.fade.top.in').remove()
          this._onUpdate()
        }).catch(e => {
          this.$message.error(e.msg || '')
          this._onUpdate()
        })
      },
      /**
       * Countdown method refresh
       */
      _countDownFn (param) {
        this.buttonType = param.buttonType
        this.editExecutorsState({
          processInstanceId: param.id,
          executeType: param.executeType
        }).then(res => {
          this.list[param.index].disabled = false
          $('body').find('.tooltip.fade.top.in').remove()
          this.$forceUpdate()
          this.$message.success(res.msg)
          // Countdown
          this._countDown(() => {
            this._onUpdate()
          }, param.index)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this._onUpdate()
        })
      },
      /**
       * update
       */
      _onUpdate () {
        this.$emit('on-update')
      },
      /**
       * list data handle
       */
      _listDataHandle (data) {
        if (data.length) {
          _.map(data, v => {
            v.disabled = true
            v.count = 10
          })
        }
        return data
      },
      /**
       * Countdown
       */
      _countDown (fn, index) {
        const TIME_COUNT = 10
        let timer
        let $count
        if (!timer) {
          $count = TIME_COUNT
          timer = setInterval(() => {
            if ($count > 0 && $count <= TIME_COUNT) {
              $count--
              this.list[index].count = $count
              this.$forceUpdate()
            } else {
              fn()
              clearInterval(timer)
              timer = null
            }
          }, 1000)
        }
      },
      _gantt (item) {
        this.$router.push({ path: `/projects/instance/gantt/${item.id}` })
      }
    },
    watch: {
      processInstanceList (a) {
        this.list = []
        setTimeout(() => {
          this.list = this._listDataHandle(a)
        })
      }
    },
    created () {
    },
    mounted () {
      this.list = this._listDataHandle(this.processInstanceList)
    },
    components: { }
  }
</script>