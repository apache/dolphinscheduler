<template>
  <label :class="wrapClasses">
    <span :class="radioClasses">
      <span :class="innerClasses"></span>
      <input
        type="radio"
        :name="myName"
        :class="inputClasses"
        :disabled="disabled"
        :checked="currentValue"
        @change="change">
    </span>
    <span class="radio-label" v-if="$slots.default || label">
      <slot></slot><template v-if="!$slots.default">{{label}}</template>
    </span>
  </label>
</template>

<script>
import { LIB_NAME, emitter, findComponentUpward } from '../../../../src/util'

const prefixCls = `${LIB_NAME}-radio`

export default {
  name: 'xRadio',
  mixins: [emitter],
  props: {
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
    label: {
      type: [String, Number]
    },
    disabled: {
      type: Boolean,
      default: false
    },
    name: {
      type: String
    },
    size: {
      type: String,
      validator (value) {
        return ['small', 'default', 'large'].includes(value)
      }
    }
  },
  data () {
    return {
      currentValue: this.value,
      group: false,
      parent: findComponentUpward(this, 'xRadioGroup')
    }
  },
  computed: {
    sizeType () {
      if (this.size) {
        return this.size
      } else if (this.parent && this.parent.size) {
        return this.parent.size
      } else {
        return 'default'
      }
    },
    myName () {
      if (this.name) {
        return this.name
      } else if (this.parent && this.parent.name) {
        return this.parent.name
      } else {
        return ''
      }
    },
    wrapClasses () {
      return [
        `${prefixCls}-wrapper`,
        {
          [`${prefixCls}-wrapper__${this.sizeType}`]: !!this.sizeType,
          [`${prefixCls}-group-item`]: this.group,
          [`${prefixCls}-wrapper-checked`]: this.currentValue,
          [`${prefixCls}-wrapper-disabled`]: this.disabled
        }
      ]
    },
    radioClasses () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-checked`]: this.currentValue,
          [`${prefixCls}-disabled`]: this.disabled
        }
      ]
    },
    innerClasses () {
      return `${prefixCls}-inner`
    },
    inputClasses () {
      return `${prefixCls}-input`
    }
  },
  mounted () {
    if (this.parent) this.group = true
    if (!this.group) {
      this.updateValue()
    } else {
      this.parent.updateValue()
    }
  },
  methods: {
    change (event) {
      if (this.disabled) {
        return false
      }

      const checked = event.target.checked
      this.currentValue = checked

      let value = checked ? this.trueValue : this.falseValue
      this.$emit('input', value)

      if (this.group && this.label !== undefined) {
        this.parent.change({
          value: this.label,
          checked: this.value
        })
      }
      if (!this.group) {
        this.$emit('on-change', value)
        this.dispatch('xFormItem', 'on-form-change', value)
      }
    },
    updateValue () {
      this.currentValue = this.value === this.trueValue
    }
  },
  watch: {
    value (val) {
      if (val !== this.trueValue && val !== this.falseValue) {
        throw new Error('Value should be true-value or false-value.')
      }
      this.updateValue()
    }
  }
}
</script>
