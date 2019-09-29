<template>
  <div class="task-ctatus-count-model">
    <div v-show="!msg">
      <div class="data-area" v-spin="isSpin" style="height: 430px;">
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
                    <a href="javascript:" @click="searchParams.projectId && _goTask(item.key)" :class="searchParams.projectId ?'links':''">{{item.value}}</a>
                  </span>
                </td>
                <td><span class="ellipsis" style="width: 98%;" :title="item.key">{{item.key}}</span></td>
              </tr>
            </table>
          </div>
        </div>
      </div>
    </div>
    <div v-show="msg">
      <m-no-data :msg="msg" v-if="msg" :height="430"></m-no-data>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import { pie } from './chartConfig'
  import Chart from '~/@analysys/ana-charts'
  import mNoData from '@/module/components/noData/noData'
  import { stateType } from '@/conf/home/pages/projects/pages/_source/instanceConditions/common'

  export default {
    name: 'task-ctatus-count',
    data () {
      return {
        isSpin: true,
        msg: '',
        taskCtatusList: []
      }
    },
    props: {
      searchParams: Object
    },
    methods: {
      ...mapActions('projects', ['getTaskCtatusCount']),
      _goTask (name) {
        this.$router.push({
          name: 'task-instance',
          query: {
            stateType: _.find(stateType, ['label', name])['code'],
            startDate: this.searchParams.startDate,
            endDate: this.searchParams.endDate
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
        if (this.searchParams.projectId) {
          myChart.echart.on('click', e => {
            this._goTask(e.data.name)
          })
        }
      }
    },
    watch: {
      'searchParams': {
        deep: true,
        immediate: true,
        handler (o) {
          this.isSpin = true
          this.getTaskCtatusCount(o).then(res => {
            this.taskCtatusList = []
            this._handleTaskCtatus(res)
            this.isSpin = false
          }).catch(e => {
            this.msg = e.msg || 'error'
            this.isSpin = false
          })
        }
      }
    },
    beforeCreate () {
    },
    created () {
    },
    beforeMount () {
    },
    mounted () {
    },
    beforeUpdate () {
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {},
    components: { mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .task-ctatus-count-model {

  }
</style>
