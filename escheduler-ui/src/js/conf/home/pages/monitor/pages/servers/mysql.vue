<template>
  <m-list-construction :title="'Mysql管理'">
    <template slot="content">
      <div class="servers-wrapper mysql-model">
        <div class="row" v-for="(item,$index) in mysqlList">
          <div class="col-md-2">
            <div class="text-num-model text">
              <div class="title">
                <span>正常与否</span>
              </div>
              <div class="value-p">
                <span class="state">
                  <i class="iconfont success" v-if="item.state">&#xe607;</i>
                  <i class="iconfont error" v-else>&#xe626;</i>
                </span>
              </div>
              <div class="text-1">
                正常与否
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="text-num-model text">
              <div class="title">
                <span>最大连接数</span>
              </div>
              <div class="value-p">
                <b :style="{color:color[0]}">{{item.maxConnections}}</b>
              </div>
              <div class="text-1">
                最大连接数
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="text-num-model text">
              <div class="title">
                <span>当前活跃连接</span>
              </div>
              <div class="value-p">
                <b :style="{color:color[8]}">{{item.threadsConnections}}</b>
              </div>
              <div class="text-1">
                当前活跃连接
              </div>
            </div>
          </div>
          <div class="col-md-2">
            <div class="text-num-model text">
              <div class="title">
                <span>最大连接数</span>
              </div>
              <div class="value-p">
                <b :style="{color:color[2]}">{{item.maxUsedConnections}}</b>
              </div>
              <div class="text-1">
                最大连接数
              </div>
            </div>
          </div>
          <div class="col-md-2">
            <div class="text-num-model text">
              <div class="title">
                <span>线程运行连接</span>
              </div>
              <div class="value-p">
                <b :style="{color:color[4]}">{{item.threadsRunningConnections}}</b>
              </div>
              <div class="text-1">
                线程运行连接
              </div>
            </div>
          </div>
        </div>
      </div>
      <m-spin :is-spin="isLoading" ></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from './_source/zookeeperList'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import themeData from '@/module/echarts/themeData.json'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'servers-mysql',
    data () {
      return {
        isLoading: false,
        mysqlList: [],
        color: themeData.color
      }
    },
    props: {},
    methods: {
      ...mapActions('monitor', ['getDatabaseData'])
    },
    watch: {},
    created () {
      this.isLoading = true
      this.getDatabaseData().then(res => {
        this.mysqlList = res
        this.isLoading = false
      }).catch(() => {
        this.isLoading = false
      })
    },
    mounted () {
    },
    components: { mList, mListConstruction, mSpin, mNoData }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  @import "./servers";
</style>