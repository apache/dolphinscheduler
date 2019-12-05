<template>
  <div
    :class="[
      `${wrapperClass}`,
      {
        [`${wrapperClass}--${size}`]: isNotTextarea
      },
      {
        [`${wrapperClass}--prepend`]: !!$slots.prepend,
        [`${wrapperClass}--append`]: !!$slots.append
      }
    ]"
    @mouseenter="hovering = true"
    @mouseleave="hovering = false"
  >
    <template v-if="isNotTextarea">
      <!-- 前置元素 -->
      <div ref="prepend" class="prepend-area" v-if="$slots.prepend">
        <slot name="prepend"></slot>
      </div>
      <!-- 输入框 -->
      <input
        ref="input"
        class="input-element"
        :class="{
          'no-border': noBorder,
          prefix: hasPrefix,
          suffix: hasSuffix,
          active: focused,
          disabled
        }"
        v-model="currentValue"
        :name="name"
        :type="type"
        :placeholder="placeholder"
        :disabled="disabled"
        :readonly="readonly"
        :autofocus="autofocus"
        :autocomplete="autocomplete"
        :maxlength="maxlength"
        :tabindex="tabindex"
        @focus="handleFocus"
        @blur="handleBlur"
        @keyup.enter="handleEnter"
        @change="handleChange"
        @input="hanldleInput"
        @click="handleClick"
        @compositionstart="handleComposition"
        @compositionupdate="handleComposition"
        @compositionend="handleComposition"
        :aria-label="label"
      />
      <!-- 前置图标 -->
      <span class="prefix-area" :style="prefixStyles" v-if="hasPrefix" @click="handleIconClick">
        <slot v-if="$slots.prefix" name="prefix"></slot>
        <i v-else class="icon" :class="prefixIcon"></i>
      </span>
      <!-- 后置图标 -->
      <span class="suffix-area" :style="suffixStyles" v-if="hasSuffix" @click="handleIconClick">
        <slot v-if="$slots.suffix" name="suffix"></slot>
        <template v-else>
          <i v-if="clearable" v-show="showClear" class="icon ans-icon-fail-solid close"></i>
          <i v-else class="icon" :class="suffixIcon"></i>
        </template>
      </span>
      <!-- 后置元素 -->
      <div ref="append" class="append-area" v-if="$slots.append">
        <slot name="append"></slot>
      </div>
      <div class="no-border-shadow" v-if="noBorder" v-show="focused"></div>
    </template>
    <textarea
      v-else
      ref="textarea"
      class="input-element input-textarea"
      :class="{
        active: focused,
        disabled
      }"
      :style="textareaStyle"
      v-model="currentValue"
      :name="name"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :autofocus="autofocus"
      :autocomplete="autocomplete"
      :maxlength="maxlength"
      :minlength="minlength"
      :tabindex="tabindex"
      :rows="rows"
      @keyup.enter="handleEnter"
      @focus="handleFocus"
      @blur="handleBlur"
      @change="handleChange"
      @input="hanldleInput"
      @click="handleClick"
      @compositionstart="handleComposition"
      @compositionupdate="handleComposition"
      @compositionend="handleComposition"
      :aria-label="label"
    >
    </textarea>
  </div>
</template>
<script>
import { LIB_NAME, findComponentUpward, emitter } from '../../../../src/util'
import calcTextareaHeight from './util/calcTextareaHeight.js'
import { t } from '../../../../src/locale'

const wrapperClass = `${LIB_NAME}-input`

export default {
  name: 'xInput',
  mixins: [ emitter ],
  data () {
    return {
      // 组件容器样式名称
      wrapperClass,
      // 当前输入
      currentValue: this.value,
      // 文本域样式
      textareaCalcStyle: {},
      // 前置图标定位
      prefixStyles: {},
      // 后置图标定位
      suffixStyles: {},
      // 是否处于鼠标悬停状态
      hovering: false,
      // 是否处于聚焦状态
      focused: false,
      // 是否处于中文输入中
      isOnComposition: false
    }
  },
  props: {
    // 输入框类型，可选值为 text、textarea 和其他原生 input 的 type 值
    type: {
      default: 'text'
    },
    // 输入框尺寸，可选值为 large、default、small
    size: {
      validator (value) {
        return ['small', 'default', 'large'].includes(value)
      },
      default: 'default'
    },
    // 绑定的值，可使用 v-model 双向绑定
    value: [String, Number],
    // 是否可清空
    clearable: {
      type: Boolean,
      default: false
    },
    // 输入框前置图标
    suffixIcon: String,
    // 输入框后置图标
    prefixIcon: String,
    // aria-label 对应值
    label: String,
    // 是否无边框，该值为 true 时只有底部边框
    noBorder: {
      type: Boolean,
      default: false
    },

    // 原生属性
    name: String,
    placeholder: {
      type: String,
      default () {
        return t('ans.input.placeholder')
      }
    },
    disabled: Boolean,
    readonly: Boolean,
    autofocus: Boolean,
    autocomplete: {
      type: String,
      default: 'off'
    },
    maxlength: Number,
    tabindex: Number,

    // 文本域是否可以拉伸
    resize: String,
    // 文本域默认行数，仅在 textarea 类型下有效
    rows: {
      type: Number,
      default: 2
    },
    // 自适应内容高度，只当 type='textarea' 时有效，可传入对象，如: {minRows: 2, maxRows: 6}
    autosize: {
      type: [Boolean, Object],
      default: false
    },
    // textarea 原生属性，与 submit 相关
    minlength: Number
  },
  computed: {
    // 当前文本域样式
    textareaStyle () {
      return Object.assign({}, this.textareaCalcStyle, { resize: this.resize })
    },
    // 是否显示清除图标
    showClear () {
      return !this.disabled && this.clearable &&
              this.currentValue && (this.focused || this.hovering)
    },
    // 是否存在前置图标
    hasPrefix () {
      return this.prefixIcon || this.$slots.prefix
    },
    // 是否存在后置图标
    hasSuffix () {
      return this.suffixIcon || this.clearable || this.$slots.suffix
    },
    isNotTextarea () {
      return this.type !== 'textarea'
    }
  },
  methods: {
    /**
     * 处理中文输入状态
     */
    handleComposition (event) {
      if (event.type === 'compositionend') {
        this.isOnComposition = false
      } else {
        this.isOnComposition = true
      }
    },
    /**
     * 按下回车键时触发
     */
    handleEnter (e) {
      this.$emit('on-enterkey', e)
    },
    /**
     * 点击触发
     */
    handleClick (e) {
      this.$emit('on-click', e)
    },
    /**
     * 输入框失去焦点时触发
     */
    handleBlur (e) {
      this.focused = false
      this.$emit('on-blur', e)
      if (!findComponentUpward(this, ['xDatepicker', 'xTimePicker', 'xCascader', 'xSelect'])) {
        this.dispatch('xFormItem', 'on-form-blur', this.currentValue)
      }
    },
    /**
     * 输入框聚焦时触发
     */
    handleFocus (e) {
      this.focused = true
      this.$emit('on-focus', e)
    },
    /**
     * 用户输入时触发
     */
    hanldleInput (e) {
      if (this.isOnComposition) return
      this.$emit('input', e.target.value)
    },
    /**
     * 数据改变时触发
     */
    handleChange (e) {
      this.$emit('on-change', e.target.value)
    },
    /**
     * 点击前置/后置图标时触发
     */
    handleIconClick (e) {
      if (this.showClear) {
        this.clear()
        this.$emit('on-clear')
      }
      this.$emit('on-click-icon', e)
    },
    /**
     * 获得焦点
     */
    focus () {
      (this.$refs.input || this.$refs.textarea).focus()
    },
    /**
     * 失去焦点
     */
    blur () {
      (this.$refs.input || this.$refs.textarea).blur()
    },
    /**
     * 清空输入
     */
    clear () {
      this.setCurrentValue('')
      this.focus()
    },
    /**
     * 文本域自适应
     */
    resizeTextarea () {
      var { autosize, isNotTextarea } = this
      if (isNotTextarea) return
      if (!autosize) {
        this.textareaCalcStyle = {
          height: 'auto',
          minHeight: calcTextareaHeight(this.$refs.textarea).minHeight
        }
        return
      }
      const { minRows, maxRows } = autosize
      this.textareaCalcStyle = calcTextareaHeight(this.$refs.textarea, minRows, maxRows)
    },
    /**
     * 设置当前输入
     */
    setCurrentValue (value) {
      if (value === this.currentValue) return
      this.$nextTick(_ => {
        this.resizeTextarea()
      })
      this.currentValue = value
    }
  },
  watch: {
    value (val, oldValue) {
      this.setCurrentValue(val)
    },
    currentValue () {
      this.resizeTextarea()
    }
  },
  mounted () {
    if (this.autofocus) {
      this.focus()
    }
    this.resizeTextarea()
    if (this.hasPrefix) {
      const left = this.$slots.prepend ? `${this.$refs.prepend.clientWidth + 10}px` : '10px'
      this.prefixStyles = { left }
    }
    if (this.hasSuffix) {
      const right = this.$slots.append ? `${this.$refs.append.clientWidth + 10}px` : '10px'
      this.suffixStyles = { right }
    }
  }
}
</script>
