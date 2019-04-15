<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-list-construction :title="$t('Project Home')">
      <template slot="content">
        <m-project-chart :id="id"></m-project-chart>
      </template>
    </m-list-construction>
  </div>
</template>
<script>
  import mProjectChart from './_source/projectChart'
  import localStore from '@/module/util/localStorage'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'projects-index-index',
    data () {
      return {
        id: localStore.getItem('projectId') || null
      }
    },
    props: {},
    methods: {

    },
    watch: {},
    created () {

    },
    mounted () {
    },
    components: { mSecondaryMenu, mListConstruction, mProjectChart }
  }
</script>

<style lang="scss" rel="stylesheet/scss">

</style>
