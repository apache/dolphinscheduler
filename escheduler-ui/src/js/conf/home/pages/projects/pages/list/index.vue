<template>
  <m-list-construction :title="$t('Project')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <x-button type="ghost" size="small" @click="_create('')" v-ps="['GENERAL_USER']">{{$t('Create Project')}}</x-button>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="projectsList.length">
        <m-list :projects-list="projectsList" @on-update="_onUpdate" :page-no="pageNo" :page-size="pageSize"></m-list>
        <div class="page-box">
          <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>
        </div>
      </template>
      <template v-if="!projectsList.length">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" :is-left="false"></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import { mapActions } from 'vuex'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mConditions from '@/module/components/conditions/conditions'
  import mList from './_source/list'
  import mCreateProject from './_source/createProject'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'

  export default {
    name: 'projects-list',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        total: null,
        searchVal: '',
        projectsList: [],
        isLoading: true
      }
    },
    props: {},
    methods: {
      ...mapActions('projects', ['getProjectsList']),
      /**
       * Inquire
       */
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getProjectsList()
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
            return h(mCreateProject, {
              on: {
                onUpdate () {
                  self._getProjectsList()
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
      _onUpdate () {
        this._getProjectsList()
      },
      _page (val) {
        this.pageNo = val
        this._getProjectsList()
      },
      _getProjectsList (flag) {
        this.isLoading = !flag
        this.getProjectsList({
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          searchVal: this.searchVal
        }).then(res => {
          this.projectsList = res.totalList
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
      this._getProjectsList()
    },
    components: { mListConstruction, mSpin, mConditions, mList, mCreateProject, mNoData }
  }
</script>