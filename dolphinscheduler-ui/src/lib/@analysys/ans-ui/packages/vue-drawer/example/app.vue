<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-button @click="$left">left</x-button>
        <x-button @click="$right">right</x-button>
        <x-button @click="$top">top</x-button>
        <x-button @click="$bottom">bottom</x-button>
      </div>
    </section>
  </div>
</template>

<script>
  import { xButton } from '../../vue-button/src'
  import { xDrawer } from '../src'
  import Test from './test.vue'


  export default {
    name: 'app',
    methods: {
      $left () {
        xDrawer({
          direction: 'left',
          className: 'hhh',
          render (h) {
            return h(Test)
          }
        })
      },
      $right () {
        xDrawer({
          className: 'hhh',
          render (h) {
            return h(Test)
          }
        })
      },
      $top () {
        xDrawer({
          className: 'hhh',
          direction: 'top',
          render (h) {
            return h(Test)
          }
        })
      },
      $bottom () {
        xDrawer({
          className: 'hhh',
          direction: 'bottom',
          render (h) {
            return h(Test)
          }
        })
      }
    },
    components: { xButton },
    created () {
    }
  }
</script>

<style lang="scss">

</style>
