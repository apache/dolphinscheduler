<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-definition-details></m-definition-details>
  </div>
</template>
<script>
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mDefinitionDetails from '@/conf/home/pages/dag/definitionDetails.vue'
  export default {
    name: 'definition-details-index',
    components: { mDefinitionDetails, mSecondaryMenu }
  }
</script>
