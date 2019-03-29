<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-list-construction :title="$t('定时管理')">
      <template slot="content">
        <m-list></m-list>
      </template>
    </m-list-construction>
  </div>
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
