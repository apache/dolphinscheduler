<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'resource'"></m-secondary-menu>
    <m-list-construction :title="$t('UDF Resources')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <x-button type="ghost" size="small"  @click="_uploading" v-ps="['GENERAL_USER']">{{$t('Upload UDF Resources')}}</x-button>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="udfResourcesList.length">
        <m-list :udf-resources-list="udfResourcesList" :page-no="pageNo" :page-size="pageSize">
        </m-list>
        <div class="page-box">
          <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>
        </div>
      </template>
      <template v-if="!udfResourcesList.length">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading">
      </m-spin>
    </template>
  </m-list-construction>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import { findComponentDownward } from '@/module/util/'
  import mNoData from '@/module/components/noData/noData'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'resource-list-index-UDF',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        total: null,
        searchVal: '',
        isLoading: false,
        udfResourcesList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('resource', ['getResourcesListP']),
      /**
       * File Upload
       */
      _uploading () {
        findComponentDownward(this.$root, 'roof-nav')._fileUpdate('UDF')
      },
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getResourcesListP()
      },
      _page (val) {
        this.pageNo = val
        this._getResourcesListP()
      },
      _updateList () {
        this.pageSize = 10
        this.pageNo = 1
        this.searchVal = ''
        this._getResourcesListP()
      },
      _getResourcesListP (flag) {
        this.isLoading = !flag
        this.getResourcesListP({
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          searchVal: this.searchVal,
          type: 'UDF'
        }).then(res => {
          this.udfResourcesList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      }
    },
    watch: {},
    created () {
    },
    mounted () {
      this._getResourcesListP()
    },
    components: { mSecondaryMenu, mListConstruction, mConditions, mList, mSpin, mNoData }
  }
</script>
