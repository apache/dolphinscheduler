<template>
  <m-list-construction :title="$t('Home')">
    <template slot="content">
      <m-project-chart :id="0"></m-project-chart>
    </template>
  </m-list-construction>
</template>

<script>
import mProjectChart from '@/conf/home/pages/projects/pages/index/_source/projectChart'
import mListConstruction from '@/module/components/listConstruction/listConstruction'
export default {
  name: 'home',
  components: { mProjectChart, mListConstruction }
}
</script>
