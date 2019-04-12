<template>
  <div :class="wrapperClass" v-click-outside="blur">
    <div
      class="tag-container"
      :class="{'tag-container-disabled':disabled}"
      ref="multiple"
      v-if="multiple"
      :style="tagContainerStyles"
      @click="toggleDropdown"
      @mouseenter="inputHovering = true"
      @mouseleave="inputHovering = false"
      @keydown.esc.stop.prevent="blur"
    >
      <slot name="multiple" :selectedList="selectedList">
        <span
          class="tag-wrapper"
          v-for="(o, index) in selectedOptions"
          :key="typeof o.value === 'object' ? index : o.value"
        >
          <span class="tag-text">{{o.label}}</span>
          <i class="remove-tag ans-icon-close" @click.stop="handleRemoveTag(o)"></i>
        </span>
      </slot>
    </div>
    <div
      class="trigger-wrapper"
      @click="toggleDropdown"
      @keydown.esc.stop.prevent="blur"
      @keydown.down.stop.prevent="navigateOptions('next')"
      @keydown.up.stop.prevent="navigateOptions('prev')"
      @keydown.enter.prevent="selectOption"
    >
      <slot name="trigger" :selectedModel="selectedModel">
        <x-input
          ref="input"
          class="inner-input"
          readonly
          :class="{active}"
          :disabled="disabled"
          :placeholder="currentPlaceholder"
          :value="currentLabel"
          v-bind="inputProps"
          @on-click-icon.stop="toggleDropdown"
          @mouseenter.native="inputHovering = true"
          @mouseleave.native="inputHovering = false"
        >
          <template slot="suffix">
            <i v-if="clearable" v-show="showClear" class="ans-icon-fail-solid clear" @click.stop="handleClear"></i>
            <i v-show="!showClear" class="ans-icon-arrow-down arrow-down" :class="{reverse: visible}"></i>
          </template>
        </x-input>
      </slot>
    </div>
    <x-select-dropdown
      ref="dropdown"
      :scrollbar-class="scrollbarClass"
      :custom-header="panelHeader"
      :custom-footer="panelFooter"
      :width="width"
      :height="height"
      :max-height="maxHeight"
      :has-arrow="hasArrow"
      :placement="mergedOptions.placement"
      :append-to-body="appendToBody"
      :position-fixed="positionFixed"
      :viewport="viewport"
      :popper-options="mergedOptions"
      @on-update-width="handleUpdateWidth"
      @click.native="_refocus"
      @keydown.native.esc.stop.prevent="_refocusTrigger"
      @keydown.native.down.stop.prevent="navigateOptions('next')"
      @keydown.native.up.stop.prevent="navigateOptions('prev')"
      @keydown.native.enter.prevent="_selectAndRefocusTrigger"
    >
      <div v-if="filterable" slot="search" class="search-area">
        <x-input
          ref="search"
          v-model="keyword"
          :placeholder="searchPlaceholder"
          prefix-icon="ans-icon-search"
        >
        </x-input>
      </div>
      <ul class="dropdown-container" :style="panelStyles">
        <slot></slot>
      </ul>
      <div v-if="hasEmptySlot" class="empty-hint-erea">
        <slot name="empty"></slot>
      </div>
      <div v-if="!hasEmptySlot && options.length === 0" class="empty-text">
        <i class="ans-icon-no-data"></i>
        <span>{{noDataText}}</span>
      </div>
      <div v-if="!hasEmptySlot && showNoMatchText" class="empty-text">
        <i class="ans-icon-search-no-data"></i>
        <span>{{noMatchText}}</span>
      </div>
    </x-select-dropdown>
  </div>
</template>

<script>
import { LIB_NAME, clickOutside, emitter, getValueByPath, hasClass } from '../../../../src/util'
import { xInput } from '../../../vue-input/src'
import xSelectDropdown from './SelectDropdown.vue'
import { t } from '../../../../src/locale'

export default {
  name: 'xSelect',

  components: { xInput, xSelectDropdown },

  directives: { clickOutside },

  mixins: [emitter],

  provide () {
    return {
      select: this
    }
  },

  data () {
    return {
      wrapperClass: `${LIB_NAME}-select`,
      // 组件是否处于激活状态
      active: false,
      // Popper.js 的可选项
      defalutOptions: {
        placement: 'bottom'
      },
      // 下拉框是否可见
      visible: false,
      // 当前 select 组件中的所有 option 子组件
      options: [],
      // 多选时当前已选项对应的 option 子组件
      selectedOptions: [],
      // 单选时上一次点击选项对应的 option 子组件
      lastOption: null,
      // 鼠标是否悬停在 input 上
      inputHovering: false,
      // 输入框宽度
      inputWidth: 0,
      searchPlaceholder: t('ans.select.search'),
      // 搜索关键字
      keyword: '',
      panelHeader: null,
      panelFooter: null,
      panelStyles: {},
      hasEmptySlot: false,
      focusIndex: -1
    }
  },

  props: {
    // 绑定值
    value: [String, Number, Object, Array],

    // 当绑定值为对象时，通过该属性值判断 Option 是否被选中，否则将比较对象是否相等
    valueKey: String,

    // 原生属性
    name: String,

    // 原生属性
    placeholder: {
      type: String,
      default () {
        return t('ans.select.placeholder')
      }
    },

    inputProps: {
      type: Object,
      default () {
        return {}
      }
    },

    // 下拉框尺寸
    width: [String, Number],

    height: [String, Number],

    maxHeight: {
      type: [String, Number],
      default: 300
    },

    // 选项过长时，是否启用 title 属性
    addTitle: {
      type: Boolean,
      default: false
    },

    // 原生属性
    disabled: Boolean,

    // 单选时是否可清空已选项
    clearable: Boolean,

    // 是否开启多选
    multiple: Boolean,

    // 是否开启搜索功能
    filterable: Boolean,

    filterProps: Array,

    // 选项为空时显示的文字
    noDataText: {
      type: String,
      default () {
        return t('ans.select.noData')
      }
    },

    // 搜索时是否高亮选项中匹配的文字，(仅当未设置 filter-props 时可用)
    highlightMatchedText: {
      type: Boolean,
      default: false
    },

    // 搜索没有任何匹配项时显示的文字
    noMatchText: {
      type: String,
      default () {
        return t('ans.select.noMatch')
      }
    },

    hasArrow: {
      type: Boolean,
      default: false
    },

    // 下拉框是否插入 body
    appendToBody: {
      type: Boolean,
      default: false
    },

    positionFixed: Boolean,

    viewport: Boolean,

    // Popper.js 的可选项
    popperOptions: {
      type: Object,
      default () {
        return {}
      }
    },

    scrollbarClass: String,

    dropAnimation: String
  },

  computed: {
    tagContainerStyles () {
      return {
        'max-width': this.inputWidth - 35 + 'px'
      }
    },

    // select 组件当前绑定的值
    currentValue () {
      return this.multiple
        ? this.selectedOptions.map(o => o.value)
        : this.lastOption ? this.lastOption.value : null
    },

    // select 组件当前显示的选项文本
    currentLabel () {
      return this.multiple
        ? ''
        : this.lastOption ? this.lastOption.label : ''
    },

    // 多选时，选中选项后隐藏占位文本
    currentPlaceholder () {
      return this.multiple && this.selectedOptions.length
        ? ''
        : this.placeholder
    },

    mergedOptions () {
      return Object.assign({}, this.defalutOptions, this.popperOptions)
    },

    selectedList () {
      return this.selectedOptions.map(o => ({ label: o.label, value: o.value }))
    },

    selectedModel () {
      return this.multiple
        ? this.selectedList
        : this.lastOption ? { label: this.lastOption.label, value: this.lastOption.value } : null
    },

    // 是否显示清空图标
    showClear () {
      const value = this.currentValue
      let hasValue = this.multiple
        ? value && value.length > 0
        : value !== undefined && value !== null && value !== ''
      return this.clearable && !this.disabled && this.inputHovering && hasValue
    },

    showNoMatchText () {
      return this.options.length !== 0 && this.options.every(o => !o.visible)
    },

    navigatable () {
      return this.options.some(o => !o.disabled && o.visible)
    }
  },

  watch: {
    keyword (val) {
      this.search(val)
    },

    value (val, oldValue) {
      this.dispatch('xFormItem', 'on-form-change', val)
      this.setSelected(val)
    },

    currentLabel (val) {
      if (this.addTitle && !this.multiple && this.$refs.input && this.$refs.input.$el) {
        this.$refs.input.$el.setAttribute('title', val)
      }
    },

    focusIndex (val) {
      if (val > -1 && val < this.options.length) {
        const target = this._getOptionByElIndex(val)
        this.options.forEach(o => {
          o.focused = target === o
        })
      } else {
        this.options.forEach(o => {
          o.focused = false
        })
      }
    }
  },

  methods: {
    /**
     * 更新下拉框内的滚动条
     */
    updateScrollbar () {
      this.$refs.dropdown.updateLayout()
    },

    /**
     * 将滚动条移回到顶部
     */
    resetScrollbar () {
      this.$refs.dropdown.$refs.scroller.setContentTop(0)
    },

    /**
     * 搜索
     */
    search (keyword) {
      if (!this.visible) return
      this.focusIndex = -1
      this.broadcast('xOption', 'keywordChange', keyword)
      this.broadcast('xOptionGroup', 'keywordChange', keyword)
      this.$nextTick(() => {
        this.updateScrollbar()
      })
    },

    /**
     * 激活组件，显示下拉框
     */
    focus () {
      if (!this.visible) {
        this.toggleDropdown()
      }
    },

    _refocus () {
      if (this.visible) {
        this.filterable && this.$refs.search.focus()
      } else {
        this.$refs.input && this.$refs.input.focus()
      }
    },

    /**
     * 取消组件激活状态
     */
    blur () {
      this.active = false
      this.hideDropdown()
    },

    /**
     * 隐藏下拉框，输入框重新获得焦点
     */
    _refocusTrigger () {
      this.hideDropdown()
      if (this.$refs.input) {
        this.$refs.input.focus()
      }
    },

    /**
     * 切换下拉框显示/隐藏
     */
    toggleDropdown () {
      if (this.disabled) return
      this.active = true
      this.$refs.dropdown.toggle()
      this.visible = !this.visible
      this.$emit('on-visible-change', this.visible)
      if (this.visible) {
        if (this.filterable) {
          this.keyword = ''
          this.$nextTick(() => {
            this.$refs.search.focus()
          })
        } else {
          if (this.$refs.input) {
            this.$refs.input.focus()
          }
        }
        this.resetHoverIndex()
      }
    },

    /**
     * 隐藏下拉框
     */
    hideDropdown () {
      if (this.visible) {
        this.$refs.dropdown.hide()
        this.visible = false
        this.$emit('on-visible-change', this.visible)
      }
    },

    /**
     * 移除多选标签
     */
    handleRemoveTag (option) {
      // 非组件对象，此次为外部调用
      if (!option.$el) {
        option = this.selectedOptions.find(o => o.value === option.value)
      }
      if (!option || this.disabled) return
      this.deselect(option)
      this.setInputHeight()
      this.$emit('input', this.currentValue)
      this.$nextTick(() => {
        this.$refs.dropdown.updateElementHandler()
      })
    },

    /**
     * 清空已选项
     */
    handleClear () {
      this.clearAll()
      this.hideDropdown()
      this.$emit('on-change', this.multiple ? [] : null)
      this.$emit('input', this.currentValue)
    },

    clearAll () {
      if (this.multiple) {
        this.selectedOptions.forEach(o => o.selected = false)
        this.selectedOptions = []
        this.setInputHeight()
      } else {
        if (this.lastOption) {
          this.lastOption.selected = false
        }
        this.lastOption = null
      }
    },

    /**
     * enter 键
     */
    selectOption () {
      if (!this.visible) {
        this.toggleDropdown()
      } else {
        const target = this._getOptionByElIndex(this.focusIndex)
        if (target) {
          this.handleSelect(target)
        }
      }
    },

    _selectAndRefocusTrigger () {
      this.selectOption()
      if (!this.visible && this.$refs.input) {
        this.$refs.input.focus()
      }
    },

    resetHoverIndex () {
      if (!this.multiple) {
        this.focusIndex = this.options.indexOf(this.lastOption)
      } else {
        if (this.selectedOptions.length > 0) {
          this.focusIndex = Math.min.apply(null, this.selectedOptions.map(o => this.options.indexOf(o)))
        } else {
          this.focusIndex = -1
        }
      }
    },

    /**
     * 键盘导航
     */
    navigateOptions (direction) {
      if (!this.visible) {
        this.toggleDropdown()
        return
      }
      if (direction === 'next') {
        this.focusIndex++
        if (this.focusIndex >= this.options.length) {
          this.focusIndex = 0
        }
      } else if (direction === 'prev') {
        this.focusIndex--
        if (this.focusIndex < 0) {
          this.focusIndex = this.options.length - 1
        }
      }
      const option = this._getOptionByElIndex(this.focusIndex)
      if (option.disabled || !option.visible) {
        return this.navigateOptions(direction)
      }
      this.$nextTick(() => this.$refs.dropdown.scrollToTarget(option))
    },

    _getOptionByElIndex (index) {
      if (this.options.length) {
        const els = Array.from(this.options[0].$el.parentNode.children)
          .filter(e => !hasClass('invisible-option'))
        if (els.length < index + 1) return null
        return this.options.find(o => o.$el === els[index])
      } else {
        return null
      }
    },

    /**
     * 点击选项
     */
    handleSelect (option) {
      if (this.multiple) {
        const index = this.selectedOptions.indexOf(option)
        if (index !== -1) {
          this.deselect(option)
        } else {
          this.selectedOptions.push(option)
          option.selected = true
        }
        this.setInputHeight()
        this.$emit('on-change', this.selectedList)
        this.$nextTick(() => {
          this.$refs.dropdown.updateElementHandler()
        })
      } else {
        // 单选时点击已选中的选项
        if (this.lastOption === option) {
          this.hideDropdown()
          return
        }
        if (this.lastOption) {
          this.lastOption.selected = false
        }
        this.lastOption = option
        option.selected = true
        this.hideDropdown()
        this.$emit('on-change', { label: option.label, value: option.value })
      }
      this.$emit('input', this.currentValue)
    },

    /**
     * 多选取消选择
     */
    deselect (option) {
      option.selected = false
      this.selectedOptions.splice(this.selectedOptions.indexOf(option), 1)
    },

    /**
     * 设置绑定选项
     */
    setSelected (value) {
      this.clearAll()
      if (value) {
        if (this.multiple) {
          const list = Array.isArray(value) ? value : [value]
          list.forEach(v => this.selectedOptions.push(this.getOption(v)))
        } else {
          this.lastOption = this.getOption(value)
        }
      }
    },

    /**
     * 根据值查找对应的 option
     */
    getOption (value) {
      const checkByProp = this.valueKey || typeof value === 'object'
      for (const option of this.options) {
        if ((checkByProp && getValueByPath(option.value, this.valueKey) === value) ||
          option.value === value) {
          option.selected = true
          return option
        }
      }
      const label = typeof value === 'object' ? '' : value
      return { value, label }
    },

    /**
     * 处理 option 组件销毁事件
     */
    onOptionDestroy (index) {
      if (index > -1) {
        this.options.splice(index, 1)
      }
      this.focusIndex = -1
    },

    /**
     * 计算多选标签容器高度，并调整输入框高度
     */
    setInputHeight () {
      if (!this.$refs.input) return
      this.$nextTick(() => {
        const calculateHeight = this.$refs.multiple.clientHeight + 4
        this.$refs.input.$refs.input.style.height = Math.max(32, calculateHeight) + 'px'
      })
    },

    registerOption (option) {
      this.options.push(option)
      this.focusIndex = -1
      this.setSelected(this.value)
      this.$refs.dropdown && this.$refs.dropdown.checkScrollable()
    },

    handleUpdateWidth (width) {
      this.panelStyles = { width }
    }
  },

  created () {
    this.$on('click-option', this.handleSelect)
  },

  mounted () {
    this.hasEmptySlot = this.$slots.empty !== undefined
    this.setSelected(this.value)
    if (this.$refs.input) {
      this.inputWidth = this.$refs.input.$el.clientWidth
    }
    if (this.$scopedSlots.header) {
      this.panelHeader = (h, scope) => this.$scopedSlots.header(scope)
    }
    if (this.$scopedSlots.footer) {
      this.panelFooter = (h, scope) => this.$scopedSlots.footer(scope)
    }
  }
}
</script>
