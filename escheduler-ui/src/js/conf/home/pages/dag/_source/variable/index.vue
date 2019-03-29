<template>
  <div class="assist-dag-model">
    <template v-if="isView">
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
        isView: false
      }
    },
    methods: {
      _toggleView () {
        this.isView = !this.isView
      }
    },
    components: { mVariablesView }
  }
</script>