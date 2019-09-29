<template>
  <m-list-construction :title="$t('Cron Manage')">
    <template slot="content">
      <m-list></m-list>
    </template>
  </m-list-construction>
</template>
<script>
  import mList from './_source/list'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  export default {
    name: 'definition-timing-index',
    components: { mList, mListConstruction, mSecondaryMenu }
  }
</script>
