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
        <x-checkbox-group v-model="favorites" @on-change="onChange">
          <x-checkbox v-for="(f, i) in fruits" :label="f" :key="f" :disabled="!i">{{f}}</x-checkbox>
        </x-checkbox-group>
      </div>
    </section>
    <section class="demo-section">
      <h4>indeterminate 状态</h4>
      <div>
        <x-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @on-change="handleCheckAll">全选</x-checkbox>
        <x-checkbox-group v-model="favorites" @on-change="handleCheck">
          <x-checkbox v-for="f in fruits" :label="f" :key="f">{{f}}</x-checkbox>
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
      ck: '真',
      fruits: ['香蕉', '苹果', '橘子'],
      favorites: ['苹果', '橘子'],
      checkAll: false,
      isIndeterminate: true
    }
  },
  components: { xCheckboxGroup, xCheckbox },
  methods: {
    onChange (data) {
      console.log(data)
    },
    clickMe (d) {
      console.log(d)
    },
    handleCheckAll (val) {
      this.favorites = val ? this.fruits : []
      this.isIndeterminate = false
    },
    handleCheck (value) {
      let checkedCount = value.length
      this.checkAll = checkedCount === this.fruits.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.fruits.length
    }
  }
}
</script>

<style lang="scss">

</style>
