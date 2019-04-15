<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'resource'"></m-secondary-menu>
    <m-list-construction :title="$t('UDF Function')">
      <template slot="conditions">
        <m-conditions @on-conditions="_onConditions">
          <template slot="button-group">
            <x-button type="ghost" @click="_create" v-ps="['GENERAL_USER']" size="small" >{{$t('Create UDF Function')}}</x-button>
          </template>
        </m-conditions>
      </template>
      <template slot="content">
        <template v-if="udfFuncList.length">
          <m-list :udf-func-list="udfFuncList" :page-no="pageNo" :page-size="pageSize" @on-update="_updateList">
          </m-list>
          <div class="page-box">
            <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>
          </div>
        </template>
        <template v-if="!udfFuncList.length">
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
  import mCreateUdf from './_source/createUdf'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  export default {
    name: 'udf-function-index',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        total: 20,
        searchVal: '',
        isLoading: false,
        udfFuncList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('resource', ['getUdfFuncListP']),
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getUdfFuncListP()
      },
      _page (val) {
        this.pageNo = val
        this._getUdfFuncListP()
      },
      _create () {
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          render (h) {
            return h(mCreateUdf, {
              on: {
                onUpdate () {
                  self._updateList()
                  modal.remove()
                },
                close () {
                  modal.remove()
                }
              },
              props: {
              }
            })
          }
        })
      },
      _updateList () {
        this.pageSize = 10
        this.pageNo = 1
        this.searchVal = ''
        this._getUdfFuncListP()
      },
      _getUdfFuncListP (flag) {
        this.isLoading = !flag
        this.getUdfFuncListP({
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          searchVal: this.searchVal
        }).then(res => {
          this.udfFuncList = res.totalList
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
      this._getUdfFuncListP()
    },
    components: { mSecondaryMenu, mListConstruction, mConditions, mList, mSpin, mCreateUdf, mNoData }
  }
</script>
