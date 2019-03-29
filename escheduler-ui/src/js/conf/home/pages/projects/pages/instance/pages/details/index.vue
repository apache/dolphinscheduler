<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-instance-details></m-instance-details>
  </div>
</template>
<script>
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mInstanceDetails from '@/conf/home/pages/dag/instanceDetails.vue'
  export default {
    name: 'instance-details-index',
    data () {
      return {}
    },
    props: {},
    methods: {},
    watch: {},
    created () {
    },
    mounted () {
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {},
    components: { mInstanceDetails, mSecondaryMenu }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
</style>
