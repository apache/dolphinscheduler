<template>
  <span :class="wrapClasses" @click.prevent="toggle">
    <input ref="input" type="hidden" :name="name" v-model="currentValue">
    <span :class="['innerClasses',{'is-checked':currentValue},{'is-disabled':disabled}]">
      <span class="switch-inner" v-if="currentValue" v-text="text.on"></span>
      <span class="switch-inner" v-else v-text="text.off"></span>
    </span>
  </span>
</template>

<script>
import { LIB_NAME } from '../../../../src/util'

const prefixCls = `${LIB_NAME}-switch`

export default {
  name: 'xSwitch',
  data () {
    return {
      currentValue: this.value
    }
  },
  props: {
    name: String,
    value: {
      type: [String, Number, Boolean],
      default: false
    },
    trueValue: {
      type: [String, Number, Boolean],
      default: true
    },
    falseValue: {
      type: [String, Number, Boolean],
      default: false
    },
    text: {
      type: Object,
      default () {
        return {
          on: '',
          off: ''
        }
      }
    },
    readonly: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    size: {
      type: String,
      validator (v) {
        return ['small', 'default', 'large'].includes(v)
      },
      default: 'default'
    }
  },
  computed: {
    wrapClasses () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}--small`]: this.size === 'small',
          [`${prefixCls}--default`]: this.size === 'default',
          [`${prefixCls}--large`]: this.size === 'large',
          [`${prefixCls}--disabled`]: !!this.disabled
        }
      ]
    },
    customValue () {
      return this.currentValue ? this.trueValue : this.falseValue
    }
  },
  watch: {
    value (val) {
      if (val !== this.trueValue && val !== this.falseValue) {
        throw new Error('Value should be true-value or false-value.')
      }
      this.updateValue()
    }
  },
  methods: {
    toggle () {
      if (this.disabled) return
      if (this.readonly) {
        this.$emit('on-click', this.customValue)
        return
      }

      this.currentValue = !this.currentValue
      this.$emit('input', this.customValue)
      this.$emit('on-change', this.customValue)
      this.$emit('on-click', this.customValue)
    },
    updateValue () {
      this.currentValue = this.value === this.trueValue
    }
  },
  mounted () {
    this.updateValue()
  }
}
</script>
