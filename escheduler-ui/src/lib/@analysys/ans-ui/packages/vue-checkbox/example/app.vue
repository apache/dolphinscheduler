<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <p><x-checkbox @on-change="clickMe">普通</x-checkbox></p>
        <p><x-checkbox v-model="ck" true-value="真" false-value="假"  @on-change="clickMe">{{ck}}</x-checkbox></p>
      </div>
    </section>
    <section class="demo-section">
      <h4>组合</h4>
      <div>
        <x-checkbox-group v-model="md" @on-change="onChange">
          <x-checkbox :label="'香蕉'" disabled>香蕉</x-checkbox>
          <x-checkbox :label="'苹果'">苹果</x-checkbox>
          <x-checkbox :label="'橘子'">橘子</x-checkbox>
        </x-checkbox-group>
      </div>
    </section>
  </div>
</template>

<script>
import { xCheckboxGroup, xCheckbox } from '../src'

export default {
  name: 'app',
  data () {
    return {
      md: ['香蕉', '苹果', '橘子'],
      ck: '真'
    }
  },
  components: { xCheckboxGroup, xCheckbox },
  methods: {
    onChange (data) {
      console.log(data)
    },
    clickMe (d) {
      console.log(d)
    }
  }
}
</script>

<style lang="scss">

</style>
