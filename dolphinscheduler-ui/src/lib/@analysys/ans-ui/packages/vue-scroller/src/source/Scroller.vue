<template>
  <div :class="wrapperClass" :style="wrapperStyles" v-mousewheel="handleMouseWheel">
    <div
      ref="content"
      class="scroll-area-wrapper"
      :class="{'scroll-transition':!dragging}"
      @touchstart="startDrag"
      @touchmove="onDrag"
      @touchend="endDrag">
      <slot></slot>
    </div>
    <x-horizontal-scrollbar
      v-if="showScrollbar && scrollX"
      ref="horizontal"
      :class="scrollbarClass"
      :content-width="contentWidth"
      :view-width="wrapperWidth"
      :offset-left="barOffsetLeft"
      :offset-right="barOffsetRight"
      :dragging="dragging"
      :disabled="disabled"
      :active="activeBar"
      @on-start-drag="handleStartDragBar"
      @on-horizontal-drag="handleHorizontalDrag"
      @on-end-drag="handleEndDragBar"
      @mouseenter.native="hoveringBar = true"
      @mouseleave.native="hoveringBar = false">
    </x-horizontal-scrollbar>
    <x-vertical-scrollbar
      v-if="showScrollbar && scrollY"
      ref="vertical"
      :class="scrollbarClass"
      :content-height="contentHeight"
      :view-height="wrapperHeight"
      :offset-top="barOffsetTop"
      :offset-bottom="barOffsetBottom"
      :dragging="dragging"
      :disabled="disabled"
      :active="activeBar"
      @on-start-drag="handleStartDragBar"
      @on-vertical-drag="handleVerticalDrag"
      @on-end-drag="handleEndDragBar"
      @mouseenter.native="hoveringBar = true"
      @mouseleave.native="hoveringBar = false">
    </x-vertical-scrollbar>
  </div>
</template>

<script>
import { LIB_NAME, mousewheel, limitedLoop, formatSize } from '../../../../src/util'
import xVerticalScrollbar from './VerticalScrollbar'
import xHorizontalScrollbar from './HorizontalScrollbar'

export default {
  name: 'xScroller',

  components: { xVerticalScrollbar, xHorizontalScrollbar },

  directives: { mousewheel },

  props: {
    width: [String, Number],

    maxWidth: [String, Number],

    height: [String, Number],

    maxHeight: [String, Number],

    scrollbarClass: String,

    // 是否反转 Y 轴滚轮，当该值为 true 时，滚动 Y 轴将控制水平方向的滚动
    reverseScrollY: {
      type: Boolean,
      default: false
    },

    // 当设置为`active`时，触发滚动或者鼠标移动到滚动条轨迹上时才会显示滚动条
    showScrollbar: {
      type: [Boolean, String],
      default: true
    },

    checkOnMounted: {
      type: Boolean,
      default: false
    },

    disabled: {
      type: Boolean,
      default: false
    },

    barOffsetLeft: {
      type: Number,
      default: 0
    },

    barOffsetRight: {
      type: Number,
      default: 0
    },

    barOffsetTop: {
      type: Number,
      default: 0
    },

    barOffsetBottom: {
      type: Number,
      default: 0
    }
  },

  data () {
    return {
      wrapperClass: `${LIB_NAME}-scroller`,
      contentWidth: 0,
      contentHeight: 0,
      wrapperWidth: 0,
      wrapperHeight: 0,
      scrollX: false,
      scrollY: false,
      barDragging: false,
      bodyDragging: false,
      currentTop: 0,
      currentLeft: 0,
      startClientX: null,
      startClientY: null,
      startTop: null,
      startLeft: null,
      moving: false,
      hoveringBar: false,
      resetMovingTimer: null
    }
  },

  computed: {
    wrapperStyles () {
      return {
        width: formatSize(this.width),
        'max-width': formatSize(this.maxWidth),
        height: formatSize(this.height),
        'max-height': formatSize(this.maxHeight)
      }
    },

    minLeft () {
      return this.wrapperWidth - this.contentWidth
    },

    minTop () {
      return this.wrapperHeight - this.contentHeight
    },

    dragging () {
      return this.bodyDragging || this.barDragging
    },

    showWhenActive () {
      return this.showScrollbar === 'active'
    },

    activeBar () {
      const type = typeof this.showScrollbar
      if (type === 'boolean') return this.showScrollbar
      return this.showWhenActive && (this.moving || this.hoveringBar)
    }
  },

  watch: {
    width () {
      this.checkScrollable()
    },

    maxWidth () {
      this.checkScrollable()
    },

    height () {
      this.checkScrollable()
    },

    maxHeight () {
      this.checkScrollable()
    }
  },

  methods: {
    checkScrollable () {
      const content = this.$refs.content
      const wrapper = this.$el
      this.wrapperWidth = wrapper.clientWidth
      this.wrapperHeight = wrapper.clientHeight
      this.contentWidth = content.offsetWidth
      this.contentHeight = content.offsetHeight
      if (!this.wrapperWidth || !this.contentWidth || !this.wrapperHeight || !this.contentHeight) {
        this.scrollX = false
        this.scrollY = false
        return
      }
      this.scrollX = this.contentWidth - this.wrapperWidth > 1
      this.scrollY = this.contentHeight - this.wrapperHeight > 1
    },

    handleMouseWheel (event, data) {
      if (this.disabled) {
        event.preventDefault()
        return
      }

      const shift = event.shiftKey
      const content = this.$refs.content
      let deltaLeft = this.reverseScrollY || shift ? -data.pixelY : -data.pixelX
      let deltaTop = shift ? -data.pixelX : -data.pixelY
      if (this.scrollX && deltaLeft) {
        let lastLeft = this.currentLeft
        let left = this.getValidNumber(lastLeft + deltaLeft, this.minLeft, 0)
        if (left !== lastLeft) {
          this.currentLeft = left
          content.style.left = left + 'px'
          if (this.$refs.horizontal) {
            this.$refs.horizontal.setLeft(left)
          }
          this.emitEvent(left, false)
          event.preventDefault()
        }
      } else if (this.scrollY && deltaTop) {
        let lastTop = this.currentTop
        let top = this.getValidNumber(lastTop + deltaTop, this.minTop, 0)
        if (top !== lastTop) {
          this.currentTop = top
          content.style.top = top + 'px'
          if (this.$refs.vertical) {
            this.$refs.vertical.setTop(top)
          }
          this.emitEvent(top, true)
          event.preventDefault()
        }
      }
      this.handleAction()
    },

    getValidNumber (source, min, max) {
      source = Math.round(source)
      return Math.max(min, Math.min(max, source))
    },

    handleStartDragBar (vertical) {
      this.barDragging = true
      this.$emit('on-start-drag-bar', vertical)
    },

    handleEndDragBar (vertical) {
      this.barDragging = false
      this.$emit('on-end-drag-bar', vertical)
    },

    handleVerticalDrag (barTopPercentage) {
      const content = this.$refs.content
      let top = this.getValidNumber(-barTopPercentage * this.contentHeight, this.minTop, 0)
      if (top !== this.currentTop) {
        this.currentTop = top
        content.style.top = top + 'px'
        this.emitEvent(top, true)
      }
      this.handleAction()
    },

    handleHorizontalDrag (barLeftPercentage) {
      const content = this.$refs.content

      let left = this.getValidNumber(-barLeftPercentage * this.contentWidth, this.minLeft, 0)
      if (left !== this.currentLeft) {
        this.currentLeft = left
        content.style.left = left + 'px'
        this.emitEvent(left, false)
      }
      this.handleAction()
    },

    startDrag (event) {
      event.preventDefault()
      if (this.disabled) return

      event = event.changedTouches[0]
      this.startTop = this.currentTop
      this.startLeft = this.currentLeft
      this.startClientX = event.clientX
      this.startClientY = event.clientY
      this.bodyDragging = true
    },

    onDrag (e) {
      if (!this.bodyDragging) return

      const content = this.$refs.content
      const event = e.changedTouches[0]
      if (this.scrollX) {
        let left = this.getValidNumber(this.startLeft + event.clientX - this.startClientX, this.minLeft, 0)
        if (left !== this.currentLeft) {
          this.currentLeft = left
          content.style.left = left + 'px'
          if (this.$refs.horizontal) {
            this.$refs.horizontal.setLeft(left)
          }
          this.emitEvent(left, false)
          e.preventDefault()
        }
      }
      if (this.scrollY) {
        let top = this.getValidNumber(this.startTop + event.clientY - this.startClientY, this.minTop, 0)
        if (top !== this.currentTop) {
          this.currentTop = top
          content.style.top = top + 'px'
          if (this.$refs.vertical) {
            this.$refs.vertical.setTop(top)
          }
          this.emitEvent(top, true)
          e.preventDefault()
        }
      }
      this.handleAction()
    },

    endDrag (event) {
      if (this.bodyDragging) {
        this.bodyDragging = false
      }
    },

    emitEvent (amount, vertical) {
      if (vertical) {
        if (amount === 0) {
          this.$nextTick(() => this.$emit('on-y-start'))
        } else if (amount === this.minTop) {
          this.$nextTick(() => this.$emit('on-y-end'))
        }
        this.$emit('on-scroll-y', amount)
      } else {
        if (amount === 0) {
          this.$nextTick(() => this.$emit('on-x-start'))
        } else if (amount === this.minLeft) {
          this.$nextTick(() => this.$emit('on-x-end'))
        }
        this.$emit('on-scroll-x', amount)
      }
    },

    forceCheck () {
      this.checkScrollable()
      if (!this.contentWidth || !this.contentHeight) {
        limitedLoop.nextTick(this.forceCheck, this)
      }
    },

    setContentLeft (left, transition = true) {
      if (this.currentLeft === left) return

      this.currentLeft = left
      if (!transition) {
        this.bodyDragging = true
        setTimeout(() => {
          this.bodyDragging = false
        }, 500)
      }
      this.$refs.content.style.left = left + 'px'
      if (this.$refs.horizontal) {
        this.$refs.horizontal.setLeft(left)
      }
      this.handleAction()
    },

    setContentTop (top, transition = true) {
      if (this.currentTop === top) return

      this.currentTop = top
      if (!transition) {
        this.bodyDragging = true
        setTimeout(() => {
          this.bodyDragging = false
        }, 500)
      }
      this.$refs.content.style.top = top + 'px'
      if (this.$refs.vertical) {
        this.$refs.vertical.setTop(top)
      }
      this.handleAction()
    },

    stickToBoundary (vertical = true, start = true, transition = true) {
      this.checkScrollable()
      const content = this.$refs.content
      if (!transition) {
        this.bodyDragging = true
        setTimeout(() => {
          this.bodyDragging = false
        }, 500)
      }
      if (vertical) {
        this.currentTop = start ? 0 : this.minTop
        content.style.top = start ? 0 : this.minTop + 'px'
        if (this.$refs.vertical) {
          this.$refs.vertical.setTop(this.currentTop)
        }
      } else {
        this.currentLeft = start ? 0 : this.minLeft
        content.style.left = start ? 0 : this.minLeft + 'px'
        if (this.$refs.horizontal) {
          this.$refs.horizontal.setLeft(this.currentLeft)
        }
      }
      this.handleAction()
    },

    handleAction () {
      if (this.showWhenActive) {
        this.moving = true
        if (this.resetMovingTimer) {
          clearTimeout(this.resetMovingTimer)
        }
        this.resetMovingTimer = setTimeout(this.resetMoving, 1000)
      }
    },

    resetMoving () {
      this.moving = false
    },

    scrollToTarget (target) {
      const wrapper = this.$el
      const wrapperRect = wrapper.getBoundingClientRect()
      const targetRect = target.getBoundingClientRect()
      const topDiff = targetRect.top - wrapperRect.top
      const bottomDiff = wrapperRect.top + wrapper.offsetHeight - targetRect.top - target.offsetHeight
      const leftDiff = targetRect.left - wrapperRect.left
      const rightDiff = wrapperRect.left + wrapper.offsetWidth - targetRect.left - target.offsetWidth
      if (topDiff < 0) {
        this.setContentTop(this.currentTop - topDiff)
      } else if (bottomDiff < 0) {
        this.setContentTop(bottomDiff + this.currentTop)
      }
      if (leftDiff < 0) {
        this.setContentLeft(this.currentLeft - leftDiff)
      } else if (rightDiff < 0) {
        this.setContentLeft(rightDiff + this.currentLeft)
      }
    }
  },

  mounted () {
    if (this.checkOnMounted) {
      this.$nextTick(() => {
        this.forceCheck()
      })
    }
  }
}
</script>
