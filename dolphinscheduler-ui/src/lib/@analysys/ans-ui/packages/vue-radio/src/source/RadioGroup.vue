<template>
  <div :class="classes">
    <slot></slot>
  </div>
</template>

<script>
import { LIB_NAME, emitter, findComponentsDownward } from '../../../../src/util'

const prefixCls = `${LIB_NAME}-radio-group`

export default {
  name: 'xRadioGroup',
  mixins: [emitter],
  props: {
    value: {
      type: [String, Number],
      default: ''
    },
    size: {
      validator (value) {
        return ['small', 'default', 'large'].includes(value)
      }
    },
    name: String,
    vertical: Boolean
  },
  data () {
    return {
      currentValue: this.value,
      childrens: []
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-${this.size}`]: !!this.size,
          [`${prefixCls}-vertical`]: this.vertical
        }
      ]
    }
  },
  mounted () {
    this.updateValue()
  },
  methods: {
    updateValue () {
      const value = this.value
      this.childrens = findComponentsDownward(this, 'xRadio')

      if (this.childrens) {
        this.childrens.forEach(child => {
          child.currentValue = value === child.label
          child.group = true
        })
      }
    },
    change (data) {
      this.currentValue = data.value
      this.updateValue()
      this.$emit('input', data.value)
      this.$emit('on-change', data.value)
      this.dispatch('xFormItem', 'on-form-change', data.value)
    }
  },
  watch: {
    value () {
      this.updateValue()
    }
  }
}
</script>
