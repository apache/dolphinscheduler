<template>
  <m-list-construction :title="'Zookeeper管理'">
    <template slot="content">
      <template v-if="zookeeperList.length">
        <m-list :list="zookeeperList"></m-list>
      </template>
      <template v-if="!zookeeperList.length">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" ></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'servers-zookeeper',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        totalPage: null,
        searchVal: '',
        isLoading: false,
        zookeeperList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('security', ['getProcessMasterList'])
    },
    watch: {},
    created () {
      this.zookeeperList = [{ id: 1 }, { id: 1 }, { id: 1 }, { id: 1 }]
    },
    mounted () {
    },
    components: { mList, mListConstruction, mSpin, mNoData }
  }
</script>
