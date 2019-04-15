<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'security'"></m-secondary-menu>
    <m-list-construction :title="$t('Service-Worker')">
      <template slot="content">
        <template v-if="workerList.length">
          <m-list :list="workerList"></m-list>
        </template>
        <template v-if="!workerList.length">
          <m-no-data></m-no-data>
        </template>
        <m-spin :is-spin="isLoading" ></m-spin>
      </template>
    </m-list-construction>
  </div>
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from '../../_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'worker-index',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        totalPage: null,
        searchVal: '',
        isLoading: false,
        workerList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('security', ['getProcessWorkerList'])
    },
    watch: {},
    created () {
      this.isLoading = true
      this.getProcessWorkerList().then(res => {
        this.workerList = res.data
        this.isLoading = false
      })
    },
    mounted () {
    },
    components: { mList, mListConstruction, mSpin, mNoData, mSecondaryMenu }
  }
</script>
