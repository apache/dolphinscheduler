<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'user'"></m-secondary-menu>
    <router-view></router-view>
  </div>

</template>
<script>
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  export default {
    name: 'user-index',
    components: { mSecondaryMenu }
  }
</script>