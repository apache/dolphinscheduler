<template>
  <m-list-construction :title="$t('User Information')">
    <template slot="content">
      <m-info></m-info>
    </template>
  </m-list-construction>
</template>
<script>
  import mInfo from './_source/info'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'account-index',
    components: { mListConstruction, mInfo }
  }
</script>