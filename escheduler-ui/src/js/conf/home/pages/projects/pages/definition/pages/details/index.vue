<template>
  <m-definition-details></m-definition-details>
</template>
<script>
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mDefinitionDetails from '@/conf/home/pages/dag/definitionDetails.vue'
  export default {
    name: 'definition-details-index',
    components: { mDefinitionDetails, mSecondaryMenu }
  }
</script>
