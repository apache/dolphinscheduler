<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-list-construction :title="$t('Process definition')">
      <template slot="conditions">
        <m-conditions @on-conditions="_onConditions">
          <template slot="button-group">
            <x-button type="ghost" size="small"  v-ps="['GENERAL_USER']" @click="() => this.$router.push({name: 'definition-create'})">{{$t('Create process')}}</x-button>
          </template>
        </m-conditions>
      </template>
      <template slot="content">
        <template v-if="processListP.length">
          <m-list :process-list="processListP" @on-update="_onUpdate" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize"></m-list>
          <div class="page-box">
            <x-page :current="parseInt(searchParams.pageNo)" :total="total" show-elevator @on-change="_page"></x-page>
          </div>
        </template>
        <template v-if="!processListP.length">
          <m-no-data></m-no-data>
        </template>
        <m-spin :is-spin="isLoading"></m-spin>
      </template>
    </m-list-construction>
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import localStore from '@/module/util/localStorage'
  import { setUrlParams } from '@/module/util/routerUtil'
  import mNoData from '@/module/components/noData/noData'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'definition-list-index',
    data () {
      return {
        total: null,
        processListP: [],
        isLoading: true,
        searchParams: {
          // 分页条数
          pageSize: 10,
          // 分页
          pageNo: 1,
          // 查询名称
          searchVal: ''
        }
      }
    },
    props: {
    },
    methods: {
      ...mapActions('dag', ['getProcessListP']),
      /**
       * page
       */
      _page (val) {
        this.searchParams.pageNo = val
        setUrlParams({
          pageNo: this.searchParams.pageNo
        })
        this._debounceGET()
      },
      /**
       * conditions
       */
      _onConditions (o) {
        this.searchParams.searchVal = o.searchVal
        this.searchParams.pageNo = 1
        setUrlParams({
          pageNo: this.searchParams.pageNo
        })
        this._debounceGET()
      },
      /**
       * get data list
       */
      _getProcessListP (flag) {
        this.isLoading = !flag
        this.getProcessListP({
          pageSize: this.searchParams.pageSize,
          pageNo: this.searchParams.pageNo,
          searchVal: this.searchParams.searchVal,
          userId: this.$route.query.userId || ''
        }).then(res => {
          setUrlParams({ pageNo: this.pageNo })
          this.processListP = []
          this.processListP = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      },
      _onUpdate () {
        this._debounceGET('false')
      },
      /**
       * Anti-shake request interface
       * @desc Prevent function from being called multiple times
       */
      _debounceGET: _.debounce(function (flag) {
        this._getProcessListP(flag)
      }, 100, {
        'leading': false,
        'trailing': true
      })
    },
    watch: {
      '$route' (a) {
        // url no params get instance list
        if (_.isEmpty(a.query)) {
          this.searchParams.pageNo = 1
        } else {
          this.searchParams.pageNo = a.query.pageNo || 1
        }
      },
      'searchParams': {
        deep: true,
        handler () {
          this._debounceGET()
        }
      }
    },
    created () {
      localStore.removeItem('subProcessId')

      // Routing parameter merging
      if (!_.isEmpty(this.$route.query)) {
        this.searchParams = _.assign(this.searchParams, this.$route.query)
      }
    },
    mounted () {
      this._debounceGET()
    },
    components: { mList, mConditions, mSpin, mListConstruction, mSecondaryMenu, mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
</style>
