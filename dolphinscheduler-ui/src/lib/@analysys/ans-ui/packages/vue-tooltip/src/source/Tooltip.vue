<template>
  <transition :name="transitionName">
    <div v-show="visible" :class="[wrapperClass, theme]">
      <div class="tooltip-content" :style="contentStyles" v-html="content"></div>
      <div :class="arrowClass" x-arrow ref="arrow"></div>
    </div>
  </transition>
</template>

<script>
import { LIB_NAME, ANIMATION_PREFIX, Popper } from '../../../../src/util'

export default {
  name: 'xTooltip',
  mixins: [Popper],
  data () {
    return {
      wrapperClass: `${LIB_NAME}-tooltip`,
      arrowClass: `${LIB_NAME}-popper-arrow`,
      transitionName: `${ANIMATION_PREFIX}fade`
    }
  },
  props: {
    // 提示文本
    content: String,
    // 主题
    theme: String,
    // 提示框最大宽度
    maxWidth: String
  },
  computed: {
    contentStyles () {
      return {
        maxWidth: this.maxWidth
      }
    }
  },
  methods: {
    show () {
      this.visible = true
    },
    hide () {
      this.visible = false
    },
    update (options) {
      this.content = options.text
      this.theme = options.theme
      this.maxWidth = options.maxWidth
      if (this.visible) {
        this.updateElementHandler()
      }
    }
  },
  beforeDestroy () {
    // 调用 Popper 方法
    this.destroyPopper()
  }
}
</script>
