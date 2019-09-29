<template>
  <div class="home-main list-construction-model">
    <div class="content-title">
      <span>{{title}}</span>
    </div>
    <div class="conditions-box">
      <slot name="conditions">
      </slot>
    </div>
    <div class="list-box">
      <slot name="content">
      </slot>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'list-construction',
    data () {
      return {}
    },
    props: {
      title: String
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
</style>
