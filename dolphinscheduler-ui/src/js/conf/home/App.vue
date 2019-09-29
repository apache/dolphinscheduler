<template>
  <m-layout>
    <m-nav slot="top"></m-nav>
    <router-view slot="bottom"></router-view>
  </m-layout>
</template>

<script>
  import mLayout from '@/module/components/layout/layout'
  import mNav from '@/module/components/nav/nav'
  export default {
    name: 'app',
    components: { mLayout, mNav }
  }
</script>
