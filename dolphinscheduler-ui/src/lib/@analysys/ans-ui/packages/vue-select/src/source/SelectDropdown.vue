<template>
  <transition :name="transitionName" @before-enter="beforeEnter">
    <div v-show="visible" :class="wrapperClasses" :style="wrapperStyles">
      <div v-if="hasArrow" :class="arrowClass" x-arrow ref="arrow"></div>
      <x-cuctom-render v-if="customHeader" :render="customHeader"></x-cuctom-render>
      <slot name="search"></slot>
      <div ref="wrapper" class="inner-wrapper" :style="innerStyles">
        <x-scroller
          ref="scroller"
          :scrollbar-class="scrollbarClass"
          :style="innerStyles"
          width="100%"
          @start-drag="handleStartDrag"
          @dragging="handleDragging"
        >
          <slot></slot>
        </x-scroller>
      </div>
      <x-cuctom-render v-if="customFooter" :render="customFooter"></x-cuctom-render>
    </div>
  </transition>
</template>
<script>
import { LIB_NAME, ANIMATION_PREFIX, Popper, formatSize, xCuctomRender } from '../../../../src/util'
import { xScroller } from '../../../vue-scroller/src'

export default {
  name: 'xSelectDropdown',
  components: { xCuctomRender, xScroller },
  mixins: [Popper],
  props: {
    width: [String, Number],
    height: [String, Number],
    maxHeight: [String, Number],
    hasArrow: Boolean,
    customHeader: Function,
    customFooter: Function,
    customClass: String,
    scrollbarClass: String
  },
  data () {
    return {
      arrowClass: `${LIB_NAME}-popper-arrow large`,
      wrapperStyles: {},
      innerStyles: {}
    }
  },
  computed: {
    transitionName () {
      return this.$parent.dropAnimation || `${ANIMATION_PREFIX}drop`
    },
    wrapperClasses () {
      return [
        `${LIB_NAME}-select-dropdown`,
        { light: this.hasArrow },
        { 'no-box-shadow': this.noBoxShadow },
        this.customClass
      ]
    }
  },
  methods: {
    handleStartDrag () {
      this.$parent.dragging = false
    },
    handleDragging () {
      this.$parent.dragging = true
    },
    toggle () {
      if (this.visible) {
        this.hide()
      } else {
        this.show()
      }
    },
    show () {
      const width = formatSize(this.width) || this.$parent.$el.clientWidth + 'px'
      this.$emit('on-update-width', width)
      this.wrapperStyles = { width }
      this.innerStyles = {
        height: formatSize(this.height),
        maxHeight: formatSize(this.maxHeight)
      }
      this.visible = true
    },
    hide () {
      this.visible = false
    },
    beforeEnter () {
      this.checkScrollable()
    },
    checkScrollable () {
      this.$nextTick(() => {
        this.$refs.scroller.checkScrollable()
      })
    },
    updateLayout () {
      this.$refs.scroller.stickToBoundary(true, true, false)
      this.updateElementHandler()
    },
    scrollToTarget (option) {
      if (option && option.$el) {
        this.$refs.scroller.scrollToTarget(option.$el)
      }
    },
    setReference (reference) {
      this.destroyPopper()
      this.referenceEl = reference
    }
  },
  mounted () {
    this.$refs.reference = this.$parent.$el
  }
}
</script>
