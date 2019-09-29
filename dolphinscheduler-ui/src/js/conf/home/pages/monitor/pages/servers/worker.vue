<template>
  <m-list-construction :title="'Worker ' + $t('Manage')">
    <template slot="content">
      <div class="servers-wrapper" v-show="workerList.length">
        <div class="row-box" v-for="(item,$index) in workerList">
          <div class="row-title">
            <div class="left">
              <span class="sp">IP: {{item.host}}</span>
              <span class="sp">{{$t('Process Pid')}}: {{item.port}}</span>
              <span class="sp">{{$t('Zk registration directory')}}: {{item.zkDirectory}}</span>
            </div>
            <div class="right">
              <span class="sp">{{$t('Create Time')}}: {{item.createTime | formatDate}}</span>
              <span class="sp">{{$t('Last heartbeat time')}}: {{item.lastHeartbeatTime | formatDate}}</span>
            </div>
          </div>
          <div class="row-cont">
            <div class="col-md-4">
              <m-gauge
                      :value="(item.resInfo.cpuUsage * 100).toFixed(2)"
                      :name="'cpuUsage'"
                      :id="'gauge-cpu-' + item.id">
              </m-gauge>
            </div>
            <div class="col-md-4">
              <m-gauge
                      :value="(item.resInfo.memoryUsage * 100).toFixed(2)"
                      :name="'memoryUsage'"
                      :id="'gauge-memory-' + item.id">
              </m-gauge>
            </div>
            <div class="col-md-4">
              <div class="text-num-model">
                <div class="value-p">
                  <b :style="{color:color[$index]}">{{item.resInfo.loadAverage}}</b>
                </div>
                <div class="text-1">
                  loadAverage
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="!workerList.length">
        <m-no-data></m-no-data>
      </div>
      <m-spin :is-spin="isLoading"></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mGauge from './_source/gauge'
  import mList from './_source/zookeeperList'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import themeData from '@/module/echarts/themeData.json'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'servers-worker',
    data () {
      return {
        isLoading: false,
        workerList: [],
        color: themeData.color
      }
    },
    props: {},
    methods: {
      ...mapActions('monitor', ['getWorkerData'])
    },
    watch: {},
    created () {

    },
    mounted () {
      this.isLoading = true
      this.getWorkerData().then(res => {
        this.workerList = _.map(res, (v, i) => {
          return _.assign(v, {
            resInfo: JSON.parse(v.resInfo)
          })
        })
        this.isLoading = false
      }).catch(() => {
        this.isLoading = true
      })
    },
    components: { mList, mListConstruction, mSpin, mNoData, mGauge }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  @import "./servers";
</style>
