<template>
  <m-list-construction :title="$t('Home')">
    <template slot="content">
      <m-project-home :id="0"></m-project-home>
    </template>
  </m-list-construction>
</template>

<script>
import mProjectHome from '@/conf/home/pages/projects/pages/index'
import mListConstruction from '@/module/components/listConstruction/listConstruction'
export default {
  name: 'home',
  components: { mProjectHome, mListConstruction }
}
</script>
