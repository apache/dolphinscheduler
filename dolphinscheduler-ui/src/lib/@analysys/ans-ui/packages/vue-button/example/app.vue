<template>
  <div>
    <section class="demo-section">
      <h4>类型</h4>
      <div>
        <x-button type="info" @click="info">Info</x-button>
        <x-button type="success" @click="success">Success</x-button>
        <x-button type="warning" @click="warning">Warning</x-button>
        <x-button type="error" @click="error">Error</x-button>
        <br><br>
        <x-button>Default</x-button>
        <x-button type="ghost">Ghost</x-button>
        <x-button type="dashed">Dashed</x-button>
        <x-button type="text">Text</x-button>
        <x-button type="ghost" disabled>Ghost(Disabled)</x-button>
        <x-button type="dashed" disabled>Dashed(Disabled)</x-button>
        <x-button type="text" disabled>Text(Disabled)</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>形状和图标</h4>
      <div>
        <x-button type="primary" shape="circle" icon="ans-icon-warn-empty" size="small"></x-button>
        <x-button type="primary" icon="ans-icon-search">Plugin</x-button>
        <x-button type="primary" shape="circle" icon="ans-icon-search">Plugin</x-button>
        <x-button type="primary" shape="circle">Circle</x-button>
        <br><br>
        <x-button type="ghost" shape="circle" icon="ans-icon-warn-empty"></x-button>
        <x-button type="ghost" icon="ans-icon-search">Plugin</x-button>
        <x-button type="ghost" shape="circle" icon="ans-icon-search">Plugin</x-button>
        <x-button type="ghost" shape="circle">Circle</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>大小</h4>
      <div>
        <x-button type="primary" size="large">Large</x-button>
        <x-button type="primary">Default</x-button>
        <x-button type="primary" size="small">Small</x-button>
        <x-button type="primary" size="xsmall">xSmall</x-button>
        <br><br>
        <x-button type="primary" shape="circle" size="large">Large</x-button>
        <x-button type="primary" shape="circle">Default</x-button>
        <x-button type="primary" shape="circle" size="small">Small</x-button>
        <x-button type="primary" shape="circle" size="xsmall">xSmall</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>状态</h4>
      <div>
        <x-button type="primary" disabled @click="info">Primary (Disabled)</x-button>
        <x-button type="dashed" disabled>Dashed (Disabled)</x-button>
        <x-button type="ghost" shape="circle" disabled>Ghost With Circle (Disabled)</x-button>
        <x-button type="primary" icon="ans-icon-search" :loading="spinnerLoading" ref="spinnerBtn" @click="handleLoadClick">Loading...</x-button>
      </div>
    </section>
    <section class="demo-section">
      <h4>基本按钮组</h4>
      <div>
        <x-button-group v-model="checkedValue">
          <x-button value="1">Cancel</x-button>
          <x-button value="2">Confirm</x-button>
        </x-button-group>
        <x-button-group size="small">
          <x-button type="ghost">Yesterday</x-button>
          <x-button disabled type="ghost">Today</x-button>
          <x-button type="ghost">Tomorrow</x-button>
        </x-button-group>
      </div>
    </section>
    <section class="demo-section">
      <h4>图标类型</h4>
      <div>
        <x-button-group :size="'large'" shape="circle">
          <x-button type="primary" icon="ans-icon-search"></x-button>
          <x-button type="primary" icon="ans-icon-more"></x-button>
          <x-button type="primary" icon="ans-icon-close"></x-button>
          <x-button type="primary" icon="ans-icon-more"></x-button>
        </x-button-group>
        <x-button-group shape="circle">
          <x-button type="primary" icon="ans-icon-arrow-to-left"></x-button>
          <x-button type="primary" icon="ans-icon-arrow-to-right"></x-button>
        </x-button-group>
      </div>
    </section>
    <section class="demo-section">
      <h4>垂直排列</h4>
      <div>
        <x-button-group :vertical="true">
          <x-button type="ghost" icon="ans-icon-arrow-up"></x-button>
          <x-button type="ghost" icon="ans-icon-arrow-right"></x-button>
          <x-button type="ghost" icon="ans-icon-arrow-down"></x-button>
          <x-button type="ghost" icon="ans-icon-arrow-left"></x-button>
        </x-button-group>
      </div>
    </section>
    <section class="demo-section">
      <h4>圆角</h4>
      <div>
        <x-button-group shape="circle" value="open">
          <x-button type="primary" value="open">开启</x-button>
          <x-button type="primary" value="close">关闭</x-button>
        </x-button-group>
        <x-button-group shape="circle" value="2">
          <x-button :visible="false" type="ghost" icon="fa-github">苹果</x-button>
          <x-button type="ghost" icon="fa-html5" value="2">李子</x-button>
          <x-button type="ghost" icon="fa-firefox">橘子</x-button>
          <x-button :visible="false" type="ghost" icon="fa-chrome">香蕉</x-button>
        </x-button-group>
      </div>
    </section>
    <section class="demo-section">
      <h4>大小</h4>
      <div>
        <x-button-group size="large">
          <x-button type="ghost">Large</x-button>
          <x-button type="ghost">Large</x-button>
        </x-button-group>
        <x-button-group>
          <x-button type="ghost">Default</x-button>
          <x-button type="ghost">Default</x-button>
        </x-button-group>
        <x-button-group size="small">
          <x-button type="ghost">Small</x-button>
          <x-button type="ghost">Small</x-button>
        </x-button-group>
        <x-button-group size="xsmall">
          <x-button type="ghost">xSmall</x-button>
          <x-button type="ghost">xSmall</x-button>
        </x-button-group>
      </div>
      <div>
        <x-button-group size="large" shape="circle">
          <x-button type="ghost">Large</x-button>
          <x-button type="ghost">Large</x-button>
        </x-button-group>
        <x-button-group shape="circle">
          <x-button type="ghost">Default</x-button>
          <x-button type="ghost">Default</x-button>
        </x-button-group>
        <x-button-group size="small" shape="circle">
          <x-button type="ghost">Small</x-button>
          <x-button type="ghost">Small</x-button>
        </x-button-group>
        <x-button-group size="xsmall" shape="circle">
          <x-button type="ghost">xSmall</x-button>
          <x-button type="ghost">xSmall</x-button>
        </x-button-group>
      </div>
    </section>
  </div>
</template>

<script>
import { xButton, xButtonGroup } from '../src'

export default {
  name: 'app',
  data: function () {
    return {
      name: 1,
      timeout: 0,
      spinnerLoading: false,
      checkedValue: '2' // for button-group two-way binding.
    }
  },
  watch: {
    // Watcher for pipe the button-group value to console
    checkedValue (newValue) {
      console.log(newValue)
    }
  },
  methods: {
    handleLoadClick () {
      // toggle button `loading` manually
      this.spinnerLoading = !this.spinnerLoading
      var timeout = 10
      // Simulate a sync progress
      let loop = () => {
        timeout -= 1
        if (timeout <= 0) {
          // release the async locker
          this.spinnerLoading = false
          return
        }
        setTimeout(loop, 1000)
      }
      setTimeout(loop, 500)
    },
    handleLockerClick (e, next) {
      this.timeout = 10
      // Simulate a sync progress
      let loop = () => {
        this.timeout -= 1
        if (this.timeout <= 0) {
          // release the async locker
          next()
          return
        }
        setTimeout(loop, 1000)
      }
      setTimeout(loop, 500)
    },
    handleBtnGroupSelected (value) {
      console.log(value)
    },
    info () {
      // vMessage.info({ content: 'hello word' + (this.name++), duration: 2, onClose: function () {}, closable: false })
    },
    success () {
      // message.success({ content: 'hello word' + (this.name++), duration: 2, onClose: function () {}, closable: false })
    },
    error () {
      // message.error({ content: 'hello wordhello wordhello word' + (this.name++), duration: 0, onClose: function () {}, closable: true })
    },
    loading () {
      // message.loading({ content: 'hello word' + (this.name++), duration: 2, onClose: function () {}, closable: true })
    },
    warning () {
      // message.warning({ content: 'hello word' + (this.name++), duration: 2, onClose: function () {}, closable: false })
    }
  },
  components: { xButtonGroup, xButton }
}
</script>

<style lang="scss"></style>
