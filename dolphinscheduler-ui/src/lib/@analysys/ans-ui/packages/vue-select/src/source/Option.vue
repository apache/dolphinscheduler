<template>
  <li
    v-show="visible"
    :class="[wrapperClass, {selected, disabled, multiple}]"
    @mouseenter="hoverItem"
    @click="handleClick">
    <slot :option="this">
      <span
        class="default-option-class"
        :class="{focused, 'invisible-option': !visible}"
        v-html="displayText"
      ></span>
      <i v-if="multiple && selected" class="selected-mark"></i>
    </slot>
  </li>
</template>

<script>
import { LIB_NAME, emitter } from '../../../../src/util'

export default {
  name: 'xOption',
  mixins: [emitter],
  inject: ['select'],
  data () {
    return {
      wrapperClass: `${LIB_NAME}-option`,
      selected: false,
      visible: true,
      focused: false
    }
  },
  props: {
    // 绑定值
    value: {
      required: true
    },
    // 选项文本
    label: {
      type: [String, Number],
      required: true
    },
    // 是否不可选
    disabled: Boolean,
    clickHandler: Function
  },
  computed: {
    multiple () {
      return this.select && this.select.multiple
    },
    displayText () {
      return (this.visible && this.select && this.select.filterable && this.select.highlightMatchedText && this.select.keyword)
        ? this.label.replace(this.select.keyword, `<span class="highlight">${this.select.keyword}</span>`)
        : this.label
    }
  },
  methods: {
    hoverItem () {
      if (!this.disabled) {
        this.select.focusIndex = this.select.options.indexOf(this)
      }
    },
    handleClick () {
      if (this.clickHandler && typeof this.clickHandler === 'function') {
        this.clickHandler(this.value, this.label)
      } else {
        this.defalutHandleClick()
      }
    },
    defalutHandleClick () {
      if (this.disabled) return

      this.dispatch('xSelect', 'click-option', this)
    },
    handleKeywordChange (keyword) {
      if (!keyword || this.match(keyword)) {
        this.visible = true
      } else {
        this.visible = false
      }
    },
    match (keyword) {
      const props = this.select.filterProps
      if (props && props.length && typeof this.value === 'object') {
        const value = this.value
        let matchKeyword = false
        for (const prop of props) {
          if (value && value[prop] && value[prop].toString().includes(keyword)) {
            matchKeyword = true
            break
          }
        }
        return matchKeyword
      } else {
        return this.label.toString().includes(keyword)
      }
    }
  },
  created () {
    // 在 select 组件中注入 option 组件
    this.select.registerOption(this)

    this.$on('keywordChange', this.handleKeywordChange)
  },
  mounted () {
    if (this.select.addTitle) {
      this.$el.setAttribute('title', this.label)
    }
  },
  beforeDestroy () {
    this.select.onOptionDestroy(this.select.options.indexOf(this))
  }
}
</script>
