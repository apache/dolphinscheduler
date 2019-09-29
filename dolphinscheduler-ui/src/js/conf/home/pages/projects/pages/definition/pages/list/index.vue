<template>
  <m-list-construction :title="$t('Process definition')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <x-button type="ghost" size="small"  @click="() => this.$router.push({name: 'definition-create'})">{{$t('Create process')}}</x-button>
          <x-button type="ghost" size="small"  @click="_uploading">{{$t('Import process')}}</x-button>

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
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import localStore from '@/module/util/localStorage'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import { findComponentDownward } from '@/module/util/'

  export default {
    name: 'definition-list-index',
    data () {
      return {
        total: null,
        processListP: [],
        isLoading: true,
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          searchVal: '',
          userId: ''
        }
      }
    },
    mixins: [listUrlParamHandle],
    props: {
    },
    methods: {
      ...mapActions('dag', ['getProcessListP']),
      /**
       * File Upload
       */
      _uploading () {
        findComponentDownward(this.$root, 'roof-nav')._fileUpdate('DEFINITION')
      },
      /**
       * page
       */
      _page (val) {
        this.searchParams.pageNo = val
      },
      /**
       * conditions
       */
      _onConditions (o) {
        this.searchParams.searchVal = o.searchVal
        this.searchParams.pageNo = 1
      },
      /**
       * get data list
       */
      _getList (flag) {
        this.isLoading = !flag
        this.getProcessListP(this.searchParams).then(res => {
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
      _updateList () {
        this.searchParams.pageNo = 1
        this.searchParams.searchVal = ''
        this._debounceGET()
      }
    },
    watch: {
      '$route' (a) {
        // url no params get instance list
        this.searchParams.pageNo = _.isEmpty(a.query) ? 1 : a.query.pageNo
      }
    },
    created () {
      localStore.removeItem('subProcessId')
    },
    mounted () {
    },
    components: { mList, mConditions, mSpin, mListConstruction, mSecondaryMenu, mNoData }
  }
</script>

