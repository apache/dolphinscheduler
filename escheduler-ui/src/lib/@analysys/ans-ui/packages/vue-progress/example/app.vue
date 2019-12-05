<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-progress :percentage="0"></x-progress>
        <x-progress :percentage="30"></x-progress>
      </div>
    </section>
    <section class="demo-section">
      <h4>颜色和状态</h4>
      <div>
        <x-progress :percentage="40" color="#8a31d2"></x-progress>
        <x-progress :percentage="20" status="success"></x-progress>
        <x-progress :percentage="50" status="exception"></x-progress>
        <x-progress :percentage="20" color="#8a31d2" status="success"></x-progress>
        <x-progress :percentage="50" color="#8a31d2" status="exception"></x-progress>
      </div>
    </section>
    <section class="demo-section">
      <h4>宽度和文字</h4>
      <div>
        <x-progress :percentage="60" :stroke-width="20" show-inline-text></x-progress>
        <x-progress :percentage="20" :stroke-width="20" status="success" show-inline-text></x-progress>
        <x-progress :percentage="50" :stroke-width="20" status="exception" show-inline-text></x-progress>
      </div>
    </section>
    <section class="demo-section">
      <h4>环形进度条</h4>
      <div>
        <x-progress :percentage="0" type="circle"></x-progress>
        <x-progress :percentage="30" type="circle"></x-progress>
        <x-progress :percentage="40" type="circle" color="#8a31d2"></x-progress>
        <x-progress :percentage="20" type="circle" status="success"></x-progress>
        <x-progress :percentage="50" type="circle" status="exception"></x-progress>
        <x-progress :percentage="50" type="circle" status="success" :width="200" :stroke-width="20"></x-progress>
      </div>
    </section>
    <section class="demo-section">
      <h4>过渡效果</h4>
      <div>
        <x-progress :percentage="value" :status="status"></x-progress>
        <x-progress :percentage="value" :status="status" type="circle"></x-progress>
      </div>
    </section>
    <section class="demo-section">
      <h4>插槽</h4>
      <div>
        <x-progress :percentage="70">
          <div slot="inline">inline插槽</div>
        </x-progress>
      </div>
      <div>
        <x-progress :percentage="40">
          <div slot="outside">outside插槽</div>
        </x-progress>
      </div>
      <div>
        <x-progress :percentage="50" type="circle">
          <div slot="circle">circle插槽</div>
        </x-progress>
      </div>
    </section>
  </div>
</template>

<script>
import { xProgress } from '../src'

export default {
  name: 'app',
  components: { xProgress },
  data () {
    return {
      reverse: false,
      value: 0
    }
  },
  computed: {
    status () {
      return this.value >= 100 ? 'success' : 'exception'
    }
  },
  methods: {
    timer () {
      setInterval(() => {
        this.value = this.reverse ? this.value - 10 : this.value + 10
        if (this.value >= 100) {
          this.value = 100
          this.reverse = true
        } else if (this.value <= 0) {
          this.value = 0
          this.reverse = false
        }
      }, 1000)
    }
  },
  mounted () {
    this.timer()
  }
}
</script>

<style lang="scss"></style>
