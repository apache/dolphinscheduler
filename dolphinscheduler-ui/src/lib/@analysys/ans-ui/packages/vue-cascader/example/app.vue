<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-cascader
          :options="phones"
          v-model="selectedOptions"
          @on-change="handleChange">
        </x-cascader>
      </div>
      <br>
      <div>
        <x-cascader
          expand-trigger="hover"
          :options="components"
          v-model="selectedOptions2"
          @on-change="handleChange">
        </x-cascader>
      </div>
      <br>
      <div>
        <x-cascader
          multiple
          expand-trigger="click"
          :options="components"
          @on-change="handleChange">
        </x-cascader>
      </div>
    </section>
    <section class="demo-section">
      <h4>允许选择任意一级的选项</h4>
      <div>
        <x-cascader
          change-on-select
          :options="phones"
          :show-all-levels="false"
          @on-change="handleChange">
        </x-cascader>
      </div>
    </section>
    <section class="demo-section">
      <h4>搜索</h4>
      <div>
        <x-cascader
          filterable
          clearable
          :options="phones"
          @on-change="handleChange">
        </x-cascader>
      </div>
    </section>
  </div>
</template>

<script>
import { xCascader } from '../src/index'
import { phones, components } from './data'

export default {
  data () {
    return {
      phones,
      components,
      selectedOptions: ['xiaomi', 'mi6', ['12878']],
      selectedOptions2: []
    }
  },
  components: { xCascader },
  methods: {
    handleChange (value) {
      console.log(value)
    }
  }
}
</script>

<style lang="scss">
.ans-cascader-drop__menu .highlight {
  color: red;
  font-weight: normal;
}
</style>
