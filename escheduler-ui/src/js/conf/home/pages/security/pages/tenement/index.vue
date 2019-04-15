<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'security'"></m-secondary-menu>
    <template>
      <m-list-construction :title="$t('Tenant Management')">
        <template slot="conditions">
          <m-conditions @on-conditions="_onConditions">
            <template slot="button-group">
              <x-button type="ghost" size="small" @click="_create('')">{{$t('Create Tenant')}}</x-button>
            </template>
          </m-conditions>
        </template>
        <template slot="content">
          <template v-if="tenementList.length">
            <m-list :tenement-list="tenementList" :page-no="pageNo" :page-size="pageSize"></m-list>
            <div class="page-box">
              <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>
            </div>
          </template>
          <template v-if="!tenementList.length">
            <m-no-data></m-no-data>
          </template>
          <m-spin :is-spin="isLoading"></m-spin>
        </template>
      </m-list-construction>
    </template>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mCreateTenement from './_source/createTenement'
  import mNoData from '@/module/components/noData/noData'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'tenement-index',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        total: null,
        searchVal: '',
        isLoading: true,
        tenementList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('security', ['getTenantListP']),
      /**
       * Query
       */
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getTenantListP()
      },
      _page (val) {
        this.pageNo = val
        this._getTenantListP()
      },
      _create (item) {
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          render (h) {
            return h(mCreateTenement, {
              on: {
                onUpdate () {
                  self._getTenantListP('false')
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
      _getTenantListP (flag) {
        this.isLoading = !flag
        this.getTenantListP({
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          searchVal: this.searchVal
        }).then(res => {
          this.tenementList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      }
    },
    watch: {},
    created () {
      this._getTenantListP()
    },
    mounted () {

    },
    components: { mSecondaryMenu, mList, mListConstruction, mConditions, mSpin, mNoData }
  }
</script>
