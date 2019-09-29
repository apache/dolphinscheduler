<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-button @click="loading1 = !loading1">{{loading1 ? '取消加载' : '开始加载'}}</x-button>
        <div class="target" v-spin="loading1">v-spin</div>
      </div>
    </section>
    <section class="demo-section">
      <h4>自定义样式</h4>
      <div>
        <x-button @click="loading2 = !loading2">{{loading2 ? '取消加载' : '开始加载'}}</x-button>
        <div
          class="target"
          spin-text="正在加载..."
          spin-background="rgba(238, 238, 238, 0.85)"
          spin-icon-class="circle"
          spin-custom-class="custom"
          v-spin="loading2">
          <div>spin-text | 加载图标下方文字</div>
          <div>spin-background | 遮罩背景色</div>
          <div>spin-icon-class | 自定义加载图标类名</div>
          <div>spin-custom-class | spin 的自定义类名</div>
        </div>
      </div>
    </section>
    <section class="demo-section">
      <h4>整页加载</h4>
      <div>
        <x-button @click="timer3">{{loading3 ? '取消加载' : '开始加载'}}</x-button>
        <div class="target" v-spin.fullscreen="loading3">v-spin.fullscreen</div>
      </div>
    </section>
    <section class="demo-section">
      <h4>隐藏滚动条</h4>
      <div>
        <x-button @click="timer4">{{loading4 ? '取消加载' : '开始加载'}}</x-button>
        <div class="target" v-spin.fullscreen.lock="loading4">v-spin.fullscreen.lock</div>
      </div>
    </section>
    <section class="demo-section">
      <h4>服务</h4>
      <div>
        <x-button @click="startService">{{loading5 ? '取消加载' : '开始加载'}}</x-button>
        <div class="target" id="service"></div>
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
      loading1: false,
      loading2: false,
      loading3: false,
      loading4: false,
      loading5: false
    }
  },
  methods: {
    timer3 () {
      this.loading3 = true
      setTimeout(() => {
        this.loading3 = false
      }, 3000)
    },
    timer4 () {
      this.loading4 = true
      setTimeout(() => {
        this.loading4 = false
      }, 3000)
    },
    startService () {
      const spin = this.$spin({
        target: '#service',
        body: true,
        text: 'Loading...',
        background: '#bed4df94',
        iconClass: 'circle',
        customClass: 'custom'
      })
      setTimeout(() => {
        spin.close()
      }, 3000)
    }
  }
}
</script>

<style lang="scss">
.target {
  margin: 20px 0;
  width: 300px;
  height: 200px;
  border: 1px solid #ddd;
}
.circle {
  display: inline-block;
  border-radius: 50%;
  background: #9792df;
  width: 20px;
  height: 20px;
  animation: circle-bounce 1s 0s ease-in-out infinite;
}
.custom {
  .loading-text {
    color: #747474;
  }
}
@keyframes circle-bounce {
  0% {
    transform: scale(0);
  }
  100% {
    transform: scale(1);
    opacity: 0;
  }
}
</style>
