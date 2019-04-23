<template>
  <m-list-construction :title="'Mysql管理'">
    <template slot="content">
      <div class="servers-wrapper">
        <div class="row">
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>正常与否</span>
              </div>
              <div class="value-p">
                <b style="color: #0098e1;">78</b>
              </div>
              <div class="text-1">
                正常与否
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>最大连接数</span>
              </div>
              <div class="value-p">
                <b style="color: #ffcf3d;">55</b>
              </div>
              <div class="text-1">
                最大连接数
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="gridb-model">
              <div class="title">
                <span>当前活跃连接</span>
              </div>
              <div class="value-p">
                <b style="color: #f07d7d;">32</b>
              </div>
              <div class="text-1">
                当前活跃连接
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
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'servers-mysql',
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
    },
    components: { mList, mListConstruction, mSpin, mNoData }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  @import "./servers";
</style>