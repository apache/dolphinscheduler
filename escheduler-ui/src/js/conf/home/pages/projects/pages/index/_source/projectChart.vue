<template>
  <div>
    <template v-show="!isLoading">
      <div class="perject-home-content">
        <div class="time-model">
          <x-datepicker
                  :panel-num="2"
                  placement="bottom-end"
                  @on-change="_datepicker"
                  :value="scheduleTime"
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
                    <tr v-for="(item,$index) in taskCountDtosList">
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
                    <tr v-for="(item,$index) in processStateCountList">
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
    </template>
    <m-spin :is-spin="isLoading" :is-left="id ? true : false">
    </m-spin>
  </div>

</template>
<script>
  import _ from 'lodash'
  import dayjs from 'dayjs'
  import { mapActions } from 'vuex'
  import { pie, bar } from './chartConfig'
  import { stateType } from '@/conf/home/pages/projects/pages/_source/instanceConditions/common'
  import Chart from '~/@analysys/ana-charts'
  import mNoData from '@/module/components/noData/noData'
  import mSpin from '@/module/components/spin/spin'


  export default {
    name: 'perject-chart',
    data () {
      return {
        taskCountDtosList: [],
        processStateCountList: [],
        userList: [],
        scheduleTime: ['2018-11-16 00:00:00', '2018-11-16 17:13:11'],
        isLoading: true
      }
    },
    props: {
      id: Number
    },
    methods: {
      ...mapActions('projects', ['getTaskCtatusCount', 'getProcessStateCount', 'getDefineUserCount']),
      _datepicker (val) {
        this.scheduleTime = val
        this._stateTypePie()
        this._processStatePie()
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
      _stateTypePie () {
        this.taskCountDtosList = []
        this.getTaskCtatusCount({
          projectId: this.id,
          startDate: this.scheduleTime[0],
          endDate: this.scheduleTime[1]
        }).then(res => {
          let data = res.data.taskCountDtos
          this.taskCountDtosList = _.map(data, v => {
            return {
              key: _.find(stateType, ['code', v.taskStateType])['label'],
              value: v.count,
              type: 'type'
            }
          })
          const myChart = Chart.pie('#task-status-pie', this.taskCountDtosList, { title: '' })
          myChart.echart.setOption(pie)

          // 首页不允许跳转
          if (this.id) {
            myChart.echart.on('click', e => {
              this._goTask(e.data.name)
            })
          }
        }).catch(e => {})
      },
      _processStatePie () {
        this.processStateCountList = []
        this.getProcessStateCount({
          projectId: this.id,
          startDate: this.scheduleTime[0],
          endDate: this.scheduleTime[1]
        }).then(res => {
          let data = res.data.taskCountDtos
          this.processStateCountList = _.map(data, v => {
            return {
              key: _.find(stateType, ['code', v.taskStateType])['label'],
              value: v.count
            }
          })
          const myChart = Chart.pie('#process-state-pie', this.processStateCountList, { title: '' })
          myChart.echart.setOption(pie)
          // 首页不允许跳转
          if (this.id) {
            myChart.echart.on('click', e => {
              this._goProcess(e.data.name)
            })
          }
        }).catch(e => {})
      },
      _processDefinitionBar () {
        this.getDefineUserCount({
          projectId: this.id
        }).then(res => {
          let data = res.data.userList
          this.userList = _.map(data, v => {
            return {
              key: v.userName + ',' + v.userId + ',' + v.count,
              value: v.count
            }
          })
          const myChart = Chart.bar('#process-definition-bar', this.userList, {})
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
        }).catch(e => {})
      }
    },
    watch: {
    },
    created () {
      this.scheduleTime = [dayjs().format('YYYY-MM-DD 00:00:00'), dayjs().format('YYYY-MM-DD HH:mm:ss')]
      this.isLoading = true
      Promise.all([
        this._stateTypePie(),
        this._processStatePie(),
        this._processDefinitionBar()
      ]).then(res => {
        setTimeout(() => {
          this.isLoading = false
        }, 800)
      }).catch(e => {
        this.isLoading = false
      })
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
