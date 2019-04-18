<template>
  <m-list-construction :title="$t('Project Home')">
    <template slot="content">
      <m-project-chart :id="id"></m-project-chart>
    </template>
  </m-list-construction>
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
    components: { mSecondaryMenu, mListConstruction, mProjectChart }
  }
</script>
