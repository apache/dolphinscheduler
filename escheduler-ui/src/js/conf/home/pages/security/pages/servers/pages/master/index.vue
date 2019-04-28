<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'security'"></m-secondary-menu>
    <m-list-construction :title="$t('Service-Master')">
      <template slot="content">
        <template v-if="masterList.length">
          <m-list :list="masterList"></m-list>
        </template>
        <template v-if="!masterList.length">
          <m-no-data></m-no-data>
        </template>
        <m-spin :is-spin="isLoading" ></m-spin>
      </template>
    </m-list-construction>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mSpin from '@/module/components/spin/spin'
  import mList from '../../_source/list'
  import mNoData from '@/module/components/noData/noData'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'

  export default {
    name: 'servers-master',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        totalPage: null,
        searchVal: '',
        isLoading: false,
        masterList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('security', ['getProcessMasterList'])
    },
    watch: {},
    created () {
      this.isLoading = true
      this.getProcessMasterList().then(res => {
        this.masterList = res.data
        this.isLoading = false
      })
    },
    mounted () {
    },
    components: { mList, mListConstruction, mSpin, mNoData, mSecondaryMenu }
  }
</script>
