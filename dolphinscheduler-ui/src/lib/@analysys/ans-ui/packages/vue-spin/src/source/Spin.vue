<template>
  <transition :name="transitionName" @after-leave="handleAfterLeave">
    <div
      v-show="visible"
      :style="{ background }"
      :class="[wrapperClass, customClass, { 'is-fullscreen': fullscreen }]">
      <div class="spin-center-container">
        <div v-if="!iconClass" class="sk-fading-circle">
          <div class="sk-circle1 sk-circle"></div>
          <div class="sk-circle2 sk-circle"></div>
          <div class="sk-circle3 sk-circle"></div>
          <div class="sk-circle4 sk-circle"></div>
          <div class="sk-circle5 sk-circle"></div>
          <div class="sk-circle6 sk-circle"></div>
          <div class="sk-circle7 sk-circle"></div>
          <div class="sk-circle8 sk-circle"></div>
          <div class="sk-circle9 sk-circle"></div>
          <div class="sk-circle10 sk-circle"></div>
          <div class="sk-circle11 sk-circle"></div>
          <div class="sk-circle12 sk-circle"></div>
        </div>
        <i v-else :class="iconClass"></i>
        <div v-if="text" class="loading-text">{{ text }}</div>
      </div>
    </div>
  </transition>
</template>

<script>
import { LIB_NAME, ANIMATION_PREFIX } from '../../../../src/util'

export default {
  name: 'xSpin',
  data () {
    return {
      wrapperClass: `${LIB_NAME}-spin`,
      transitionName: `${ANIMATION_PREFIX}fade`,
      visible: false,
      fullscreen: true,
      text: null,
      background: null,
      iconClass: '',
      customClass: '',
      onAfterLeave: () => {}
    }
  },
  methods: {
    handleAfterLeave () {
      this.onAfterLeave()
      this.$emit('after-leave')
    },
    setText (text) {
      this.text = text
    }
  }
}
</script>
