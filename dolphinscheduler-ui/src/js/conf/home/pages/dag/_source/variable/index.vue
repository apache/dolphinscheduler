<template>
  <div class="assist-dag-model">
    <template v-if="isView && isActive">
      <m-variables-view></m-variables-view>
    </template>
  </div>
</template>
<script>
  import mVariablesView from './variablesView'
  export default {
    name: 'assist-dag-index',
    data () {
      return {
        isView: false,
        isActive: true
      }
    },
    methods: {
      _toggleView () {
        this.isView = !this.isView
      }
    },
    watch: {
      '$route': {
        deep: true,
        handler () {
          this.isActive = false
          this.$nextTick(() => (this.isActive = true))
        }
      }
    },
    components: { mVariablesView }
  }
</script>
