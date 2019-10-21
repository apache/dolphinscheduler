<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-switch v-model="checked"></x-switch>
        <span>{{checked}}</span>
      </div>
    </section>
    <section class="demo-section">
      <h4>禁用状态</h4>
      <div>
        <x-switch disabled></x-switch>
      </div>
    </section>
    <section class="demo-section">
      <h4>文本</h4>
      <div>
        <x-switch v-model="checked" :text="text"></x-switch>
        <span>{{checked}}</span>
      </div>
    </section>
    <section class="demo-section">
      <h4>大小</h4>
      <div>
        <x-switch v-model="checked" size="small"></x-switch>
        <x-switch v-model="checked"></x-switch>
        <x-switch v-model="checked" size="large"></x-switch>
        <span>{{checked}}</span>
      </div>
    </section>
  </div>
</template>

<script>
import { xSwitch } from '../src'

export default {
  name: 'app',
  data () {
    return {
      checked: true,
      text: {
        on: '是',
        off: '否'
      }
    }
  },
  components: { xSwitch }
}
</script>

<style lang="scss"></style>
