<template>
  <button
    v-show="visible"
    :type="htmlType"
    :class="wrapClasses"
    :disabled="disabled"
    @click="handleClick"
  >
    <i v-if="icon && !showSpin" :class="['i', iconPrefix, {[icon]:true}]"></i>
    <i v-if="showSpin" :class="spinClasses"></i>
    <span v-if="showSlot" ref="slot"><slot></slot></span>
  </button>
</template>

<script>
import { LIB_NAME, emitter } from '../../../../src/util'
const prefixCls = `${LIB_NAME}-btn`

export default {
  name: 'xButton',

  data: function () {
    return {
      showSlot: true,
      iconPrefix: `${LIB_NAME}-icon`,
      isFirst: false,
      isLast: false
    }
  },

  mixins: [emitter],

  props: {
    type: {
      type: String,
      default: 'primary',
      validator (v) {
        return ['primary', 'ghost', 'dashed', 'text', 'info', 'success', 'warning', 'error'].includes(v)
      }
    },

    shape: {
      type: String,
      validator (v) {
        return ['circle', ''].includes(v)
      }
    },

    size: {
      type: String,
      default: 'default',
      validator (v) {
        return ['xsmall', 'small', 'large', 'default'].includes(v)
      }
    },

    // 设置按钮为加载中状态
    loading: Boolean,

    disabled: Boolean,

    visible: {
      type: Boolean,
      default: true
    },

    // 设置 button 原生的 type，可选值为 button、submit、reset
    htmlType: {
      type: String,
      default: 'button',
      validator (v) {
        return ['button', 'submit', 'reset'].includes(v)
      }
    },

    // 设置按钮的图标类型
    icon: String,

    // 开启后，按钮的长度为 100%
    long: {
      type: Boolean,
      default: false
    },

    // 用于双向绑定
    value: {
      default: ''
    }
  },

  computed: {
    showSpin () {
      return this.loading
    },

    spinClasses () {
      return [ this.iconPrefix, `x-fa-spin ${LIB_NAME}-icon-spinner` ]
    },

    wrapClasses () {
      return [
        `${prefixCls}`,
        {
          'first-child': this.isFirst,
          'last-child': this.isLast,
          [`${prefixCls}-${this.type}`]: !!this.type,
          [`${prefixCls}-long`]: this.long,
          [`${prefixCls}-${this.shape}`]: !!this.shape,
          [`${prefixCls}-${this.size}`]: !!this.size,
          [`${prefixCls}-loading`]: this.showSpin,
          [`${prefixCls}-icon-only`]: !this.showSlot && (!!this.icon || this.loading)
        }
      ]
    }
  },

  watch: {
    visible (newVal) {
      this.dispatch('xButtonGroup', 'buttonVisibleChanged')
    }
  },

  methods: {
    handleClick (...args) {
      if (this.disabled || this.loading) {
        return
      }
      this.$emit('click', ...args)
    },

    handlePositionChange ({ first, last }) {
      this.isFirst = first === this
      this.isLast = last === this
    }
  },

  mounted () {
    this.showSlot = this.$slots.default !== undefined
  },

  created () {
    this.$on('checkPosition', this.handlePositionChange)
  }
}
</script>
