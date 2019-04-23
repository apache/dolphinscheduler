<template>
  <m-list-construction :title="'Worker管理'">
    <template slot="content">
      <div class="servers-wrapper">
        <div class="row">
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>Cpu详细信息</span>
              </div>
              <div class="gauge-echart">
                <div id="a1" style="height: 380px;"></div>
              </div>
              <div class="text-1" style="margin-top: -126px">
                cpu
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>内存详细信息</span>
              </div>
              <div class="gauge-echart">
                <div id="a2" style="height: 380px;"></div>
              </div>
              <div class="text-1" style="margin-top: -126px">
                cpu
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>内存详细信息</span>
              </div>
              <div class="value-p">
                <b style="color: #0098e1;">83</b>
              </div>
              <div class="text-1">
                cpu
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>Cpu详细信息</span>
              </div>
              <div class="gauge-echart">
                <div id="a3" style="height: 380px;"></div>
              </div>
              <div class="text-1" style="margin-top: -126px">
                cpu
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>内存详细信息</span>
              </div>
              <div class="gauge-echart">
                <div id="a4" style="height: 380px;"></div>
              </div>
              <div class="text-1" style="margin-top: -126px">
                cpu
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>内存详细信息</span>
              </div>
              <div class="value-p">
                <b  style="color: #7281c2;">18</b>
              </div>
              <div class="text-1">
                cpu
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>Cpu详细信息</span>
              </div>
              <div class="gauge-echart">
                <div id="a5" style="height: 380px;"></div>
              </div>
              <div class="text-1" style="margin-top: -126px">
                cpu
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>内存详细信息</span>
              </div>
              <div class="gauge-echart">
                <div id="a6" style="height: 380px;"></div>
              </div>
              <div class="text-1" style="margin-top: -126px">
                cpu
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>内存详细信息</span>
              </div>
              <div class="value-p">
                <b  style="color: #f2ac6f;">15</b>
              </div>
              <div class="text-1">
                cpu
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </m-list-construction>
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import gaugeOption from './_source/gaugeOption'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'servers-worker',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        totalPage: null,
        searchVal: '',
        isLoading: false,
        masterList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('security', ['getProcessMasterList'])
    },
    watch: {},
    created () {

    },
    mounted () {
      let b = {}
      let a = ['a1', 'a2', 'a3', 'a4', 'a5', 'a6']
      a.forEach((v, i) => {
        b[v] = echarts.init(document.getElementById(v)) // eslint-disable-line
        b[v].setOption(gaugeOption, true)
      })
    },
    components: { mList, mListConstruction, mSpin, mNoData }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  @import "./servers";
</style>