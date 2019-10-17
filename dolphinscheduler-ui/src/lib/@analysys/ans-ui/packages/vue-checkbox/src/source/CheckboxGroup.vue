<template>
  <div :class="classes">
    <slot></slot>
  </div>
</template>
<script>
import { findComponentsDownward, emitter } from '../../../../src/util'
import { LIB_NAME } from '../../../../src/util/constants'

const prefixCls = `${LIB_NAME}-checkbox-group`

export default {
  name: 'xCheckboxGroup',
  mixins: [emitter],
  props: {
    value: {
      type: Array,
      default () {
        return []
      }
    }
  },
  data () {
    return {
      currentValue: this.value,
      children: []
    }
  },
  computed: {
    classes () {
      return `${prefixCls}`
    }
  },
  mounted () {
    this.updateModel(true)
  },
  methods: {
    updateModel (update) {
      const value = this.value
      this.children = findComponentsDownward(this, 'xCheckbox')
      if (this.children) {
        this.children.forEach(child => {
          child.model = value
          if (update) {
            child.currentValue = value.indexOf(child.label) >= 0
            child.group = true
          }
        })
      }
    },
    change (data) {
      this.currentValue = data
      this.$emit('input', data)
      this.$emit('on-change', data)
      this.dispatch('xFormItem', 'on-form-change', data)
    }
  },
  watch: {
    value () {
      this.updateModel(true)
    }
  }
}
</script>
