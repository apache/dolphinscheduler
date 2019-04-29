<template>
  <m-list-construction :title="$t('Edit Password')">
    <template slot="content">
      <m-info></m-info>
    </template>
  </m-list-construction>
</template>
<script>
  import mInfo from './_source/info'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'password-index',
    components: { mListConstruction, mInfo }
  }
</script>