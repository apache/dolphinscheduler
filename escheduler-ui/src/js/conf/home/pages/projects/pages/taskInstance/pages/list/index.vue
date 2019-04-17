<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-list-construction :title="$t('Task Instance')">
      <template slot="conditions">
        <m-conditions @on-query="_onQuery"></m-conditions>
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
  </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import { setUrlParams } from '@/module/util/routerUtil'
  import mSpin from '@/module/components/spin/spin'
  import mConditions from '@/conf/home/pages/projects/pages/instance/pages/list/_source/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mNoData from '@/module/components/noData/noData'

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
          setUrlParams({
            taskName: ''
          })
        }
        this._debounceGET()
      },
      _page (val) {
        this.searchParams.pageNo = val
        setUrlParams({
          pageNo: this.searchParams.pageNo
        })
        this._debounceGET()
      },
      /**
       * get list data
       */
      _getTaskInstanceList (flag) {
        this.isLoading = !flag
        this.getTaskInstanceList(this.searchParams).then(res => {
          this.taskInstanceList = []
          this.taskInstanceList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      },
      /**
       * Anti-shake request interface
       * @desc Prevent function from being called multiple times
       */
      _debounceGET: _.debounce(function (flag) {
        this._getTaskInstanceList(flag)
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
      'searchParams': {
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