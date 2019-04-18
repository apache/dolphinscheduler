<template>
  <m-list-construction :title="$t('Task Instance')">
    <template slot="conditions">
      <m-instance-conditions @on-query="_onQuery"></m-instance-conditions>
    </template>
    <template slot="content">
      <template v-if="taskInstanceList.length">
        <m-list :task-instance-list="taskInstanceList" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
        </m-list>
        <div class="page-box">
          <x-page :current="parseInt(searchParams.pageNo)" :total="total" show-elevator @on-change="_page"></x-page>
        </div>
      </template>
      <template v-if="!taskInstanceList.length">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading"></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mInstanceConditions from '@/conf/home/pages/projects/pages/_source/instanceConditions'

  export default {
    name: 'task-instance-list-index',
    data () {
      return {
        isLoading: true,
        total: null,
        taskInstanceList: [],
        searchParams: {
          // page size
          pageSize: 10,
          // page index
          pageNo: 1,
          // Query name
          searchVal: '',
          // Process instance id
          processInstanceId: '',
          // host
          host: '',
          // state
          stateType: '',
          // start date
          startDate: '',
          // end date
          endDate: ''
        }
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('dag', ['getTaskInstanceList']),
      /**
       * click query
       */
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
        if (this.searchParams.taskName) {
          this.searchParams.taskName = ''
        }
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
        this.getTaskInstanceList(this.searchParams).then(res => {
          this.taskInstanceList = []
          this.taskInstanceList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
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
    components: { mList, mInstanceConditions, mSpin, mListConstruction, mSecondaryMenu, mNoData }
  }
</script>