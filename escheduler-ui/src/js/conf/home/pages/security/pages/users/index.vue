<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'security'"></m-secondary-menu>
    <template>
      <m-list-construction :title="$t('用户管理')">
        <template slot="conditions">
          <m-conditions @on-conditions="_onConditions">
            <template slot="button-group">
              <x-button type="ghost" size="small" @click="_create('')">{{$t('创建用户')}}</x-button>
            </template>
          </m-conditions>
        </template>
        <template slot="content">
          <template v-if="userList.length">
            <m-list :user-list="userList" :page-no="pageNo" :page-size="pageSize"></m-list>
            <div class="page-box">
              <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>
            </div>
          </template>
          <template v-if="!userList.length">
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
  import mCreateUser from './_source/createUser'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'users-index',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        total: null,
        searchVal: '',
        isLoading: true,
        userList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('security', ['getUsersList']),
      /**
       * 查询
       */
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getUsersListP()
      },
      _page (val) {
        this.pageNo = val
        this._getUsersListP()
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
            return h(mCreateUser, {
              on: {
                onUpdate () {
                  self._getUsersListP('false')
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
      _getUsersListP (flag) {
        this.isLoading = !flag
        this.getUsersList({
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          searchVal: this.searchVal
        }).then(res => {
          this.userList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      }
    },
    watch: {},
    created () {
      this._getUsersListP()
    },
    mounted () {
    },
    components: { mSecondaryMenu, mList, mListConstruction, mConditions, mSpin, mNoData }
  }
</script>
