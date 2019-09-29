<template>
  <m-instance-details></m-instance-details>
</template>
<script>
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mInstanceDetails from '@/conf/home/pages/dag/instanceDetails.vue'
  export default {
    name: 'instance-details-index',
    data () {
      return {}
    },
    components: { mInstanceDetails, mSecondaryMenu }
  }
</script>