<template>
  <m-list-construction :title="'Zookeeper ' + $t('Manage')">
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
  import mList from './_source/zookeeperList'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'servers-zookeeper',
    data () {
      return {
        isLoading: false,
        zookeeperList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('monitor', ['getZookeeperData'])
    },
    watch: {},
    created () {
      this.isLoading = true
      this.getZookeeperData().then(res => {
        this.zookeeperList = res
        this.isLoading = false
      }).catch(() => {
        this.isLoading = false
      })
    },
    mounted () {

    },
    components: { mList, mListConstruction, mSpin, mNoData }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  @import "./servers";
</style>
