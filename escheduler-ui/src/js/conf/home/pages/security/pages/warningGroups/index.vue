<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'security'"></m-secondary-menu>
    <template>
      <m-list-construction :title="'Warning group management'">
        <template slot="conditions">
          <m-conditions @on-conditions="_onConditions">
            <template slot="button-group">
              <x-button type="ghost" size="small" @click="_create('')">{{$t('Create alarm group')}}</x-button>
            </template>
          </m-conditions>
        </template>
        <template slot="content">
          <template v-if="alertgroupList.length">
            <m-list :alertgroup-list="alertgroupList" :page-no="pageNo" :page-size="pageSize"></m-list>
            <div class="page-box">
              <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>

            </div>
          </template>
          <template v-if="!alertgroupList.length">
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
  import mCreateWarning from './_source/createWarning'
  import mNoData from '@/module/components/noData/noData'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'warning-groups-index',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        total: null,
        searchVal: '',
        isLoading: false,
        alertgroupList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('security', ['getAlertgroupP']),
      /**
       * Inquire
       */
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getAlertgroupP()
      },
      _page (val) {
        this.pageNo = val
        this._getAlertgroupP()
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
            return h(mCreateWarning, {
              on: {
                onUpdate () {
                  self._getAlertgroupP('false')
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
      _getAlertgroupP (flag) {
        this.isLoading = !flag
        this.getAlertgroupP({
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          searchVal: this.searchVal
        }).then(res => {
          this.alertgroupList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      }
    },
    watch: {},
    created () {
      this._getAlertgroupP()
    },
    mounted () {
    },
    components: { mSecondaryMenu, mList, mListConstruction, mConditions, mSpin, mNoData }
  }
</script>
