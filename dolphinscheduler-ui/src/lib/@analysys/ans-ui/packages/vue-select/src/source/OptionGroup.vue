<template>
  <ul v-show="visible" :class="wrapperClass">
    <li>
      <slot name="content">
        <span class="group-title">{{label}}</span>
      </slot>
    </li>
    <li class="group-container">
      <ul class="group-wrapper">
        <slot></slot>
      </ul>
    </li>
  </ul>
</template>

<script>
import { LIB_NAME } from '../../../../src/util'

export default {
  name: 'xOptionGroup',
  data () {
    return {
      wrapperClass: `${LIB_NAME}-option-group`,
      visible: true
    }
  },
  props: {
    // 分组名称
    label: String
  },
  methods: {
    handleKeywordChange (val) {
      this.visible = !this.$children.every(o => !o.visible)
    }
  },
  created () {
    this.$on('keywordChange', this.handleKeywordChange)
  }
}
</script>
