<template>
  <div>
    <template v-show="!isLoading">
      <div class="perject-home-content" v-show="!msg">
        <div class="time-model">
          <x-datepicker
                  :panel-num="2"
                  placement="bottom-end"
                  @on-change="_datepicker"
                  :value="[searchParams.startDate,searchParams.endDate]"
                  type="daterange"
                  :placeholder="$t('Select date range')"
                  format="YYYY-MM-DD HH:mm:ss">
          </x-datepicker>
        </div>
        <div class="row" >
          <div class="col-md-6">
            <div class="chart-title">
              <span>{{$t('Task status statistics')}}</span>
            </div>
            <div class="row">
              <div class="col-md-7">
                <div id="task-status-pie" style="height:260px;margin-top: 100px;"></div>
              </div>
              <div class="col-md-5">
                <div class="table-small-model">
                  <table>
                    <tr>
                      <th width="40">{{$t('#')}}</th>
                      <th>{{$t('Number')}}</th>
                      <th>{{$t('State')}}</th>
                    </tr>
                    <tr v-for="(item,$index) in taskCtatusList">
                      <td><span>{{$index+1}}</span></td>
                      <td>
                        <span>
                          <a href="javascript:" @click="id && _goTask(item.key)" :class="id ?'links':''">{{item.value}}</a>
                        </span>
                      </td>
                      <td><span class="ellipsis" style="width: 98%;" :title="item.key">{{item.key}}</span></td>
                    </tr>
                  </table>
                </div>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="chart-title">
              <span>{{$t('Process Status Statistics')}}</span>
            </div>
            <div class="row">
              <div class="col-md-7">
                <div id="process-state-pie" style="height:260px;margin-top: 100px;"></div>
              </div>
              <div class="col-md-5">
                <div class="table-small-model">
                  <table>
                    <tr>
                      <th width="40">{{$t('#')}}</th>
                      <th>{{$t('Number')}}</th>
                      <th>{{$t('State')}}</th>
                    </tr>
                    <tr v-for="(item,$index) in processStateList">
                      <td><span>{{$index+1}}</span></td>
                      <td><span><a href="javascript:" @click="id && _goProcess(item.key)" :class="id ?'links':''">{{item.value}}</a></span></td>
                      <td><span class="ellipsis" style="width: 98%;" :title="item.key">{{item.key}}</span></td>
                    </tr>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="row" style="padding-top: 20px;">
          <div class="col-md-6">

          </div>
          <div class="col-md-6">
            <div class="chart-title">
              <span>队列统计</span>
            </div>
            <div class="row">
              <div class="col-md-7">
                <div id="queue-pie" style="height:260px;margin-top: 100px;"></div>
              </div>
              <div class="col-md-5">
                <div class="table-small-model">
                  <table>
                    <tr>
                      <th width="40">{{$t('#')}}</th>
                      <th>等待执行任务</th>
                      <th>等待Kill任务</th>
                    </tr>
                    <tr v-for="(item,$index) in queueList">
                      <td><span>{{$index+1}}</span></td>
                      <td><span><a href="javascript:" >{{item.value}}</a></span></td>
                      <td><span class="ellipsis" style="width: 98%;" :title="item.key">{{item.key}}</span></td>
                    </tr>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-12">
            <div class="chart-title" style="margin-bottom: 20px;margin-top: 30px">
              <span>命令状态统计</span>
            </div>
            <div>
              <div id="command-state-bar" style="height:500px"></div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-12">
            <div class="chart-title" style="margin-bottom: -20px;margin-top: 30px">
              <span>{{$t('Process Definition Statistics')}}</span>
            </div>
            <div>
              <div id="process-definition-bar" style="height:500px"></div>
            </div>
          </div>
        </div>
      </div>
      <m-no-data :msg="msg" v-if="msg"></m-no-data>
    </template>
    <m-spin :is-spin="isLoading" :is-left="id ? true : false">
    </m-spin>
  </div>

</template>
<script>
  import _ from 'lodash'
  import dayjs from 'dayjs'
  import { mapActions } from 'vuex'
  import { pie, bar, simple } from './chartConfig'
  import Chart from '~/@analysys/ana-charts'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import { stateType } from '@/conf/home/pages/projects/pages/_source/instanceConditions/common'


  export default {
    name: 'perject-chart',
    data () {
      return {
        isLoading: true,
        taskCtatusList: [],
        processStateList: [],
        defineUserList: [],
        commandStateList: [],
        queueList: [],
        searchParams: {
          projectId: this.id,
          startDate: '',
          endDate: ''
        },
        msg: ''
      }
    },
    props: {
      id: Number
    },
    methods: {
      ...mapActions('projects', ['getTaskCtatusCount', 'getProcessStateCount', 'getDefineUserCount', 'getCommandStateCount', 'getQueueCount']),
      _datepicker (val) {
        this.searchParams.startDate = val[0]
        this.searchParams.endDate = val[1]
        this._getData(false)
      },
      _goTask (name) {
        this.$router.push({
          name: 'task-instance',
          query: {
            stateType: _.find(stateType, ['label', name])['code'],
            startDate: this.scheduleTime[0],
            endDate: this.scheduleTime[1]
          }
        })
      },
      _goProcess (name) {
        this.$router.push({
          name: 'projects-instance-list',
          query: {
            stateType: _.find(stateType, ['label', name])['code'],
            startDate: this.scheduleTime[0],
            endDate: this.scheduleTime[1]
          }
        })
      },
      _handleTaskCtatus (res) {
        let data = res.data.taskCountDtos
        this.taskCtatusList = _.map(data, v => {
          return {
            key: _.find(stateType, ['code', v.taskStateType])['label'],
            value: v.count,
            type: 'type'
          }
        })
        const myChart = Chart.pie('#task-status-pie', this.taskCtatusList, { title: '' })
        myChart.echart.setOption(pie)

        // 首页不允许跳转
        if (this.id) {
          myChart.echart.on('click', e => {
            this._goTask(e.data.name)
          })
        }
      },
      _handleProcessState (res) {
        let data = res.data.taskCountDtos
        this.processStateList = _.map(data, v => {
          return {
            key: _.find(stateType, ['code', v.taskStateType])['label'],
            value: v.count
          }
        })
        const myChart = Chart.pie('#process-state-pie', this.processStateList, { title: '' })
        myChart.echart.setOption(pie)
        // 首页不允许跳转
        if (this.id) {
          myChart.echart.on('click', e => {
            this._goProcess(e.data.name)
          })
        }
      },
      _handleDefineUser (res) {
        let data = res.data.userList
        this.defineUserList = _.map(data, v => {
          return {
            key: v.userName + ',' + v.userId + ',' + v.count,
            value: v.count
          }
        })
        const myChart = Chart.bar('#process-definition-bar', this.defineUserList, {})
        myChart.echart.setOption(bar)
        // 首页不允许跳转
        if (this.id) {
          myChart.echart.on('click', e => {
            this.$router.push({
              name: 'projects-definition-list',
              query: {
                userId: e.name.split(',')[1]
              }
            })
          })
        }
      },
      _handleCommandState (res) {
        let data = []
        _.forEach(res.data, (v, i) => {
          let key = _.keys(v)
          if (key[0] === 'errorCount') {
            data.push({ typeName: '错误指令数', key: v.commandState, value: v.errorCount })
          }
        })
        _.forEach(res.data, (v, i) => {
          let key = _.keys(v)
          if (key[1] === 'normalCount') {
            data.push({ typeName: '正常指令数', key: v.commandState, value: v.normalCount })
          }
        })
        const myChart = Chart.bar('#command-state-bar', data, {
          title: ''
        })
        myChart.echart.setOption(simple)
      },
      _handleQueue (res) {
        _.forEach(res.data, (v, k) => this.queueList.push({
          key: k === 'taskQueue' ? '等待执行任务' : '等待kill任务',
          value: v
        }))
        const myChart = Chart.pie('#queue-pie', this.queueList, { title: '' })
        myChart.echart.setOption(pie)
      },
      _getData (is = true) {
        this.isLoading = true
        let ioList = [
          this.getTaskCtatusCount(this.searchParams),
          this.getProcessStateCount(this.searchParams),
          this.getCommandStateCount(this.searchParams),
          this.getQueueCount(this.searchParams)
        ]

        if (is) {
          ioList.push(this.getDefineUserCount(_.pick(this.searchParams, ['projectId'])))
        }

        Promise.all(ioList).then(res => {
          this._handleTaskCtatus(res[0])
          this._handleProcessState(res[1])
          this._handleCommandState(res[2])
          this._handleQueue(res[3])
          if (is) {
            this._handleDefineUser(res[4])
          }
          setTimeout(() => {
            this.isLoading = false
          }, 800)
        }).catch(e => {
          this.msg = e.msg || 'error'
          this.isLoading = false
        })
      }
    },
    watch: {
    },
    created () {
      this.searchParams.startDate = dayjs().format('YYYY-MM-DD 00:00:00')
      this.searchParams.endDate = dayjs().format('YYYY-MM-DD HH:mm:ss')
      // init get data
      this._getData()
    },
    mounted () {

    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {},
    components: { mNoData, mSpin }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .perject-home-content {
    padding: 10px 20px;
    position: relative;
    .time-model {
      position: absolute;
      right: 8px;
      top: -40px;
      .ans-input {
        >input {
          width: 344px;
        }
      }
    }
    .chart-title {
      text-align: center;
      height: 60px;
      line-height: 60px;
      span {
        font-size: 22px;
        color: #333;
        font-weight: bold;
      }
    }
  }
  .table-small-model {
    .ellipsis {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space:  nowrap;
      display: block;
    }
  }
</style>
