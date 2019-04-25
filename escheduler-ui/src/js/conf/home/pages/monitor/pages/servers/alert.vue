<template>
  <m-list-construction title="Alert服务">
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
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from './_source/zookeeperList'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'servers-alert',
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
    components: { mList, mListConstruction, mSpin, mNoData }
  }
</script>
