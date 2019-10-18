<template>
  <div :class="wrapperClass" @click="jump" :style="wrapperStyles">
    <transition :name="transitionName">
      <div
        ref="bar"
        v-show="active"
        class="scrollbar-thumb"
        :class="{'scroll-transition':!dragging}"
        :style="{
          width: width ? width + 'px' : 0,
          left: left ? left + 'px' : 0
        }"
        @mousedown="startDrag"
        @touchstart="startDrag"></div>
    </transition>
  </div>
</template>

<script>
import { LIB_NAME, ANIMATION_PREFIX } from '../../../../src/util'

export default {
  name: 'xHorizontalScrollbar',

  props: {
    viewWidth: Number,
    offsetLeft: Number,
    offsetRight: Number,
    contentWidth: Number,
    dragging: Boolean,
    disabled: Boolean,
    active: Boolean
  },

  data () {
    return {
      wrapperClass: `${LIB_NAME}-h-scrollbar`,
      transitionName: `${ANIMATION_PREFIX}fade`,
      left: 0,
      barDragging: false,
      startClientX: null,
      startLeft: null
    }
  },

  computed: {
    wrapperStyles () {
      const styles = {}
      if (this.offsetLeft) {
        styles.left = this.offsetLeft + 'px'
      }
      if (this.offsetLeft || this.offsetRight) {
        styles.width = this.wrapperWidth + 'px'
      }
      return styles
    },

    wrapperWidth () {
      return this.viewWidth - this.offsetLeft - this.offsetRight
    },

    width () {
      return this.viewWidth * this.wrapperWidth / this.contentWidth
    },

    maxLeft () {
      return this.wrapperWidth - this.width
    }
  },

  methods: {
    setLeft (left) {
      if (this.contentWidth) {
        this.left = -left * this.wrapperWidth / this.contentWidth
      }
    },

    startDrag (event) {
      if (this.disabled) return

      this.barDragging = true
      document.onselectstart = () => false
      document.ondragstart = () => false

      event = event.changedTouches ? event.changedTouches[0] : event

      this.startLeft = this.left
      this.startClientX = event.clientX
      this.$emit('on-start-drag', false)
    },

    onDrag (event) {
      if (this.disabled) return

      if (this.barDragging) {
        event = event.changedTouches ? event.changedTouches[0] : event

        const delta = event.clientX - this.startClientX
        let left = this.startLeft + delta
        left = Math.min(this.maxLeft, Math.max(left, 0))
        this.left = left
        this.$emit('on-horizontal-drag', left / this.wrapperWidth)
      }
    },

    endDrag (event) {
      if (this.barDragging) {
        this.barDragging = false
        document.onselectstart = null
        document.ondragstart = null
        this.$emit('on-end-drag', false)
      }
    },

    jump (event) {
      if (this.disabled) return

      let valid = event.target === this.$el

      if (valid) {
        const delta = event.clientX - this.$refs.bar.getBoundingClientRect().left - this.width * 0.5
        let left = this.left + delta
        left = Math.min(this.maxLeft, Math.max(left, 0))
        this.left = left
        this.$emit('on-horizontal-drag', left / this.wrapperWidth)
      }
    }
  },

  mounted () {
    document.addEventListener('mousemove', this.onDrag)
    document.addEventListener('touchmove', this.onDrag)
    document.addEventListener('mouseup', this.endDrag)
    document.addEventListener('touchend', this.endDrag)
  },

  beforeDestroy () {
    document.removeEventListener('mousemove', this.onDrag)
    document.removeEventListener('touchmove', this.onDrag)
    document.removeEventListener('mouseup', this.endDrag)
    document.removeEventListener('touchend', this.endDrag)
  }
}
</script>
