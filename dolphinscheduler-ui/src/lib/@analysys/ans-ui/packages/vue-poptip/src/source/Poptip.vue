<template>
  <span>
    <transition name="x-ani-fade" @after-leave="destroyPopper">
      <div
        :class="wrapperClass"
        ref="popper"
        v-show="!hidden"
        role="tooltip"
        :style="{ width: width + 'px' }"
        :id="tooltipId"
        :aria-hidden="hidden ? 'true' : 'false'"
        :tabindex="tabIndex"
      >
        <div :class="arrowClass" x-arrow ref="arrow" v-if="visibleArrow"></div>
        <div :class="prefixCls + '__title'" v-if="title || $slots.title">
          <slot name="title">{{title}}</slot>
        </div>
        <div :class="prefixCls + '__content'" v-if="!confirm">
          <slot>{{ content }}</slot>
        </div>
        <div :class="prefixCls + '__footer'" v-if="confirm">
          <x-button type="text" @click="onCancel" size="small">{{cancelText}}</x-button>
          <x-button type="primary" @click="onOk" size="small">{{okText}}</x-button>
        </div>
      </div>
    </transition>

    <slot name="reference"></slot>
  </span>
</template>
<script>
import { addClass, removeClass, on, off, uuid, LIB_NAME, Popper } from '../../../../src/util/index'
import { xButton } from '../../../vue-button/src'
import { t } from '../../../../src/locale'

const prefixCls = `${LIB_NAME}-poptip`

export default {
  name: 'xPoptip',
  mixins: [Popper],
  components: { xButton },
  data () {
    return {
      tabIndex: 0,
      prefixCls: prefixCls,
      delayTime: 0,
      timer: null,
      arrowClass: `${LIB_NAME}-popper-arrow`
    }
  },
  props: {

    /**
       * 触发方式，可选值为 hover, click, focus, 在 confirm 模式下，只有 click 有效
       */
    trigger: {
      type: String,
      default: 'click',
      validator: value => ['click', 'focus', 'hover', 'manual'].indexOf(value) > -1
    },

    /**
       * 提示框出现的位置，可选值为 top, top-start, top-end, bottom, bottom-start, bottom-end, left, left-start, left-end,
       * right, right-start, right-end
       */
    placement: {
      validator (value) {
        return [
          'top', 'top-start', 'top-end', 'bottom', 'bottom-start', 'bottom-end', 'left', 'left-start', 'left-end',
          'right', 'right-start', 'right-end'
        ].indexOf(value) > -1
      },
      default: 'top'
    },
    title: String,
    content: String,
    disabled: Boolean,
    width: Number,
    visibleArrow: {
      default: true
    },
    confirm: Boolean,
    cancelText: {
      type: String,
      default () {
        return t('ans.poptip.cancel')
      }
    },
    okText: {
      type: String,
      default () {
        return t('ans.poptip.confirm')
      }
    },
    value: Boolean,
    distance: {
      type: Number,
      default: 5
    },
    popperClass: String
  },
  computed: {
    wrapperClass () {
      return [
        `${prefixCls}`, this.popperClass,
        {
          'light': this.visibleArrow,
          [`${prefixCls}--plain`]: this.content
        }
      ]
    },
    tooltipId () {
      return `${prefixCls}-${uuid()}`
    },
    hidden () {
      return this.disabled || !this.visible
    }
  },

  watch: {
    value: {
      immediate: true,
      handler (val) {
        this.$emit('input', this.visible = val)
      }
    },
    visible (val) {
      if (this.disabled) return
      val ? this.$emit('on-show') : this.$emit('on-hide')
      this.$emit('input', val)
    }
  },

  mounted () {
    let reference = this.getReference()
    const popper = this.popper || this.$refs.popper

    // 可访问性
    if (!reference) return

    // reference 加上样式属性
    addClass(reference, `${prefixCls}__reference`)
    reference.setAttribute('aria-describedby', this.tooltipId)
    reference.setAttribute('tabindex', this.tabIndex) // tab 序列

    // 非click时 公共事件处理
    if (this.trigger !== 'click') {
      on(reference, 'focusin', this.handleFocus)
      on(popper, 'focusin', this.handleFocus)
      on(reference, 'focusout', this.handleBlur)
      on(popper, 'focusout', this.handleBlur)
    }
    on(reference, 'keydown', this.handleKeydown)
    on(reference, 'click', this.handleClick)

    // 事件处理
    if (this.trigger === 'click') {
      on(reference, 'click', this.doToggle)
      on(document, 'click', this.handleDocumentClick)
    } else if (this.trigger === 'hover') {
      on(reference, 'mouseenter', this.handleMouseEnter)
      on(popper, 'mouseenter', this.handleMouseEnter)
      on(reference, 'mouseleave', this.handleMouseLeave)
      on(popper, 'mouseleave', this.handleMouseLeave)
    } else if (this.trigger === 'focus' && this.isInput(reference)) {
      on(reference, 'mousedown', this.doShow)
      on(reference, 'mouseup', this.doClose)
    }
  },

  methods: {
    getReference () {
      let reference = this.referenceEl = this.reference || this.$refs.reference
      if (!reference && this.$slots.reference && this.$slots.reference[0]) {
        reference = this.referenceEl = this.$slots.reference[0].elm
      }
      return reference
    },
    doToggle () {
      this.visible = !this.visible
    },
    doShow () {
      this.visible = true
    },
    doClose () {
      this.visible = false
    },
    handleMouseEnter () {
      this.timer && clearTimeout(this.timer)
      this.doShow()
    },
    handleMouseLeave () {
      this.timer && clearTimeout(this.timer)
      this.timer = setTimeout(() => {
        this.doClose()
      }, 100)
    },
    handleFocus () {
      addClass(this.referenceEl, 'focusing')
      this.trigger !== 'manual' && (this.visible = true)
    },
    handleClick () {
      removeClass(this.referenceEl, 'focusing')
    },
    handleBlur () {
      removeClass(this.referenceEl, 'focusing')
      this.trigger !== 'manual' && (this.visible = false)
    },
    handleKeydown (ev) {
      ev.keyCode === 27 && this.trigger !== 'manual' && this.doClose()
    },
    handleDocumentClick (e) {
      let reference = this.getReference()
      const popper = this.popper || this.$refs.popper

      if (!this.$el ||
          !reference ||
          this.$el.contains(e.target) ||
          reference.contains(e.target) ||
          !popper ||
          popper.contains(e.target)) return
      this.visible = false
    },
    onOk () {
      this.doClose()
      this.$emit('on-ok')
    },
    onCancel () {
      this.doClose()
      this.$emit('on-cancel')
    },
    isInput (reference) {
      return !reference.querySelector('input, textarea') &&
        reference.nodeName !== 'INPUT' &&
        reference.nodeName !== 'TEXTAREA'
    }
  },

  destroyed () {
    const reference = this.getReference()

    off(reference, 'click', this.doToggle)
    off(reference, 'mouseup', this.doClose)
    off(reference, 'mousedown', this.doShow)
    off(reference, 'focusin', this.doShow)
    off(reference, 'focusout', this.doClose)
    off(reference, 'mouseleave', this.handleMouseLeave)
    off(reference, 'mouseenter', this.handleMouseEnter)
    off(document, 'click', this.handleDocumentClick)

    this.timer && clearTimeout(this.timer)
  }
}
</script>
