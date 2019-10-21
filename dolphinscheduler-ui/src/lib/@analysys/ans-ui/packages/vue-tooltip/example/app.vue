<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-button @click="changeText" v-tooltip="text">v-tooltip="text"</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>指定方向</h4>
      <div>
        <p>
          <x-button @click="changeText" v-tooltip.top="text">v-tooltip.top="text"</x-button>
          <x-button @click="changeText" v-tooltip.bottom="text">v-tooltip.bottom="text"</x-button>
          <x-button @click="changeText" v-tooltip.left="text">v-tooltip.left="text"</x-button>
          <x-button @click="changeText" v-tooltip.right="text">v-tooltip.right="text"</x-button>
        </p>
        <p>
          <x-button @click="changeText" v-tooltip.top.start="text">v-tooltip.top.start="text"</x-button>
          <x-button @click="changeText" v-tooltip.bottom.start="text">v-tooltip.bottom.start="text"</x-button>
          <x-button @click="changeText" v-tooltip.left.start="text">v-tooltip.left.start="text"</x-button>
          <x-button @click="changeText" v-tooltip.right.start="text">v-tooltip.right.start="text"</x-button>
        </p>
        <p>
          <x-button @click="changeText" v-tooltip.top.end="text">v-tooltip.top.end="text"</x-button>
          <x-button @click="changeText" v-tooltip.bottom.end="text">v-tooltip.bottom.end="text"</x-button>
          <x-button @click="changeText" v-tooltip.left.end="text">v-tooltip.left.end="text"</x-button>
          <x-button @click="changeText" v-tooltip.right.end="text">v-tooltip.right.end="text"</x-button>
        </p>
      </div>
    </section>
    <section class="demo-section">
      <h4>指定主题</h4>
      <div>
        <x-button @click="changeText" v-tooltip.right.start.light="text">v-tooltip.right.start.light="text"</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>指定触发事件</h4>
      <div>
        <x-button @click="changeText" v-tooltip.right.light.click="text">v-tooltip.right.light.click="text"</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>绑定详细配置</h4>
      <div>
        <x-button @click="changeOption" v-tooltip="options">v-tooltip="options"</x-button>
      </div>
    </section>
  </div>
</template>

<script>
import { xButton } from '../../vue-button/src'

export default {
  name: 'app',
  components: { xButton },
  data () {
    return {
      text: 'abc',
      options: {
        text: '',
        placement: 'top',
        maxWidth: '100px',
        theme: 'dark',
        triggerEvent: 'mouseenter',
        containerID: 'temp'
      }
    }
  },
  methods: {
    changeText () {
      this.text = '这是一段非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常非常长的文字'
    },
    changeOption () {
      this.options.maxWidth = '120px'
      this.options.theme = 'light'
      this.options.text = this.optionText()
    },
    optionText () {
      const copy = Object.assign({}, this.options)
      copy.text = 'options text'
      let result = ''
      for (const key in copy) {
        if (copy.hasOwnProperty(key)) {
          const element = copy[key]
          result += `--${key}: ${element}--`
        }
      }
      return result
    }
  },
  mounted () {
    this.options.text = this.optionText()
  }
}
</script>

<style lang="scss"></style>
