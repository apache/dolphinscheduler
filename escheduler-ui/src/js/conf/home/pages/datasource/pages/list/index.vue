<template>
  <m-list-construction :title="$t('Datasource')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <x-button type="ghost" size="small" @click="_create('')" v-ps="['GENERAL_USER']">{{$t('Create Datasource')}}</x-button>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="datasourcesList.length">
        <m-list :datasources-list="datasourcesList" :page-no="pageNo" :page-size="pageSize"></m-list>
        <div class="page-box">
          <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>
        </div>
      </template>
      <template v-if="!datasourcesList.length">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" :is-left="false">
      </m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mCreateDataSource from './_source/createDataSource'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'datasource-indexP',
    data () {
      return {
        // loading
        isLoading: true,
        // Number of pages per page
        pageSize: 10,
        // Number of pages
        pageNo: 1,
        // Total number of articles
        total: 20,
        // Search value
        searchVal: '',
        // data sources(List)
        datasourcesList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('datasource', ['getDatasourcesListP']),
      /**
       * create data source
       */
      _create (item) {
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          render (h) {
            return h(mCreateDataSource, {
              on: {
                onUpdate () {
                  self._getDatasourcesListP('false')
                  modal.remove()
                },
                close () {
                  modal.remove()
                }
              },
              props: {
                item: item
              }
            })
          }
        })
      },
      /**
       * page
       */
      _page (val) {
        this.pageNo = val
        this._getDatasourcesListP()
      },
      /**
       * conditions event
       */
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getDatasourcesListP('false')
      },
      /**
       * get data(List)
       */
      _getDatasourcesListP (flag) {
        this.isLoading = !flag
        this.getDatasourcesListP({
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          searchVal: this.searchVal
        }).then(res => {
          this.datasourcesList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      }
    },
    watch: {},
    created () {
      this._getDatasourcesListP()
    },
    mounted () {
    },
    components: { mList, mConditions, mSpin, mListConstruction, mNoData }
  }
</script>
