<template>
  <label :class="className">
    <span  :class="checkedClass">
      <span :class="prefixCls+'-inner'"></span>
      <input type="checkbox" :class="prefixCls+'-input'"
             v-if="group"
             :value="label"
             v-model="model"
             :disabled="disabled"
             @change="change"/>
      <input type="checkbox" :class="prefixCls+'-input'"
             v-if="!group"
             :checked="currentValue"
             :disabled="disabled"
             @change="change"/>
    </span>
    <span class="checkbox-label" v-if="$slots.default || label">
      <slot></slot><template v-if="!$slots.default">{{label}}</template>
    </span>
  </label>
</template>
<script>
import { findComponentUpward, emitter } from '../../../../src/util'
import { LIB_NAME } from '../../../../src/util/constants'

const prefixCls = `${LIB_NAME}-checkbox`

export default {
  name: 'xCheckbox',
  mixins: [emitter],
  data () {
    return {
      prefixCls: prefixCls,
      currentValue: this.value === this.trueValue,
      // 是否在组中
      group: false,
      // 组中用到，保存组中所有的状态
      model: [],
      // 是否显示子组件
      showSlot: true
    }
  },
  props: {
    // 只在组中有效，判断当前是否选中
    label: {
      type: [String, Number, Boolean],
      default: ''
    },
    // 只在单独时有效，控制是否选中
    value: [String, Number, Boolean],
    // 是否无效
    disabled: {
      type: Boolean,
      default: false
    },
    trueValue: {
      type: [Boolean, String, Number],
      default: true
    },
    falseValue: {
      type: [Boolean, String, Number],
      default: false
    }
  },
  computed: {
    className () {
      return [{
        [`${prefixCls}-wrapper`]: true,
        [`${prefixCls}-wrapper-checked`]: !!this.currentValue,
        [`${prefixCls}-wrapper-disabled`]: !!this.disabled
      }]
    },
    checkedClass () {
      return [{
        [`${prefixCls}`]: true,
        [`${prefixCls}-checked`]: !!this.currentValue
      }]
    }
  },

  mounted () {
    this.parent = findComponentUpward(this, 'xCheckboxGroup')
    if (this.parent) this.group = true
    if (!this.group) {
      this.updateModel()
    } else {
      this.parent.updateModel(true)
    }
    this.showSlot = this.$slots.default !== undefined
  },
  methods: {
    change (event) {
      if (this.disabled) {
        return
      }
      const checked = event.target.checked
      this.currentValue = checked
      let current = checked ? this.trueValue : this.falseValue
      this.$emit('input', current)
      if (this.group) {
        this.$parent.change(this.model)
      } else {
        this.$emit('on-change', current)
        this.dispatch('xFormItem', 'on-form-change', current)
      }
    },
    updateModel () {
      this.currentValue = this.value === this.trueValue
    }
  },
  watch: {
    value () {
      this.updateModel()
    }
  }
}
</script>
