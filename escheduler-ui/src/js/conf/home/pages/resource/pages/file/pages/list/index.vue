<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'resource'"></m-secondary-menu>
    <m-list-construction :title="$t('File Management')">
      <template slot="conditions">
        <m-conditions @on-conditions="_onConditions">
          <template slot="button-group">
            <x-button-group size="small" >
              <x-button type="ghost" @click="() => $router.push({name: 'resource-file-create'})" v-ps="['GENERAL_USER']">{{$t('Create File')}}</x-button>
              <x-button type="ghost" @click="_uploading" v-ps="['GENERAL_USER']">{{$t('Upload Files')}}</x-button>
            </x-button-group>
          </template>
        </m-conditions>
      </template>
      <template slot="content">
        <template v-if="fileResourcesList.length">
          <m-list :file-resources-list="fileResourcesList" :page-no="pageNo" :page-size="pageSize">
          </m-list>
          <div class="page-box">
            <x-page :current="pageNo" :total="total" show-elevator @on-change="_page"></x-page>
          </div>
        </template>
        <template v-if="!fileResourcesList.length">
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
    name: 'resource-list-index-FILE',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        total: null,
        searchVal: '',
        isLoading: false,
        fileResourcesList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('resource', ['getResourcesListP']),
      /**
       * File Upload
       */
      _uploading () {
        findComponentDownward(this.$root, 'roof-nav')._fileUpdate('FILE')
      },
      _onConditions (o) {
        this.searchVal = o.searchVal
        this.pageNo = 1
        this._getResourcesList()
      },
      _page (val) {
        this.pageNo = val
        this._getResourcesList()
      },
      _getResourcesList (flag) {
        this.isLoading = !flag
        this.getResourcesListP({
          pageSize: this.pageSize,
          pageNo: this.pageNo,
          searchVal: this.searchVal,
          type: 'FILE'
        }).then(res => {
          this.fileResourcesList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      },
      _updateList () {
        this.pageSize = 10
        this.pageNo = 1
        this.searchVal = ''
        this._getResourcesList()
      },
      _routerView () {
        let name = this.$route.name
        if (name === 'resource-file-details') {
          return true
        } else {
          return false
        }
      }
    },
    watch: {},
    created () {
    },
    mounted () {
      this._getResourcesList()
    },
    components: { mSecondaryMenu, mListConstruction, mConditions, mList, mSpin, mNoData }
  }
</script>