<template>
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
</template>
<script>
  import _ from 'lodash'
  import mList from './_source/list'
  import store from '@/conf/home/store'
  import mConditions from './_source/conditions'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
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
    mixins: [listUrlParamHandle],
    props: {
      config: String
    },
    methods: {
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.pageNo = 1
      },
      _page (val) {
        this.searchParams.pageNo = val
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
      }
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
        if (_.isEmpty(a.query)) {
          this.searchParams.processInstanceId = ''
        }
        this.searchParams.pageNo = _.isEmpty(a.query) ? 1 : a.query.pageNo
      }
    },
    created () {
    },
    mounted () {
    },
    components: { mList, mConditions, mSpin, mListConstruction, mSecondaryMenu, mNoData }
  }
</script>
