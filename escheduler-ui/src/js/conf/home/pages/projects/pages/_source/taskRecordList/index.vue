<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-list-construction :title="config.title">
      <template slot="conditions">
        <m-conditions @on-query="_onQuery"></m-conditions>
      </template>
      <template slot="content">
        <template v-if="taskRecordList.length">
          <m-list :task-record-list="taskRecordList" @on-update="_onUpdate" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
          </m-list>
          <div class="page-box">
            <x-page :current="parseInt(searchParams.pageNo)" :total="total" show-elevator @on-change="_page"></x-page>
          </div>
        </template>
        <template v-if="!taskRecordList.length">
          <m-no-data></m-no-data>
        </template>
        <m-spin :is-spin="isLoading"></m-spin>
      </template>
    </m-list-construction>
  </div>
</template>
<script>
  import _ from 'lodash'
  import mList from './_source/list'
  import store from '@/conf/home/store'
  import mConditions from './_source/conditions'
  import mSpin from '@/module/components/spin/spin'
  import { setUrlParams } from '@/module/util/routerUtil'
  import mNoData from '@/module/components/noData/noData'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'task-record-list',
    data () {
      return {
        store,
        total: null,
        taskRecordList: [],
        isLoading: true,
        searchParams: {
          taskName: '',
          state: '',
          sourceTable: '',
          destTable: '',
          taskDate: '',
          startDate: '',
          endDate: '',
          pageSize: 10,
          pageNo: 1
        }
      }
    },
    props: {
      config: String
    },
    methods: {
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      _page (val) {
        this.searchParams.pageNo = val
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      /**
       * get list data
       */
      _getList (flag) {
        this.isLoading = !flag
        this.store.dispatch(`dag/${this.config.apiFn}`, this.searchParams).then(res => {
          this.taskRecordList = []
          this.taskRecordList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      },
      _onUpdate () {
        this._debounceGET()
      },
      /**
       * Anti-shake request interface
       * @desc Prevent function from being called multiple times
       */
      _debounceGET: _.debounce(function (flag) {
        this._getList(flag)
      }, 100, {
        'leading': false,
        'trailing': true
      })
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
        if (_.isEmpty(a.query)) {
          this.searchParams.pageNo = 1
          this.searchParams.processInstanceId = ''
        } else {
          this.searchParams.pageNo = a.query.pageNo || 1
        }
      },
      'searchParams.pageNo': {
        deep: true,
        handler () {
          this._debounceGET()
        }
      }
    },
    created () {
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
