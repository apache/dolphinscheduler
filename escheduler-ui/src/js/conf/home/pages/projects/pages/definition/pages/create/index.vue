<template>
  <div class="main-layout-box">
    <m-secondary-menu :type="'projects'"></m-secondary-menu>
    <m-create-dag></m-create-dag>
  </div>
</template>
<script>
  import mCreateDag from '@/conf/home/pages/dag/index'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  export default {
    name: 'definition-create-index',
    components: { mCreateDag, mSecondaryMenu }
  }
</script>
