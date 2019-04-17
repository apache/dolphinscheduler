<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'user'"></m-secondary-menu>
    <m-list-construction :title="$t('User Information')">
      <template slot="content">
        <m-info></m-info>
      </template>
    </m-list-construction>
  </div>
</template>
<script>
  import mInfo from './_source/info'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'account-index',
    components: { mSecondaryMenu, mListConstruction, mInfo }
  }
</script>