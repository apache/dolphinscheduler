<template>
  <m-list-construction :title="$t('Cron Manage')">
    <template slot="content">
      <m-list></m-list>
    </template>
  </m-list-construction>
</template>
<script>
  import mList from '../definition/timing/_source/list'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  export default {
    name: 'timing-index',
    components: { mList, mListConstruction }
  }
</script>