<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'resource'"></m-secondary-menu>
    <router-view></router-view>
  </div>
</template>
<script>
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  export default {
    name: 'resource-index',
    components: { mSecondaryMenu }
  }
</script>
