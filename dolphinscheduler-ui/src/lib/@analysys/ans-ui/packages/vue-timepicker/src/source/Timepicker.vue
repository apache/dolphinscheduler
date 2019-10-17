<template>
  <x-poptip
    popper-class="date-poptip"
    class="x-datepicker"
    :placement="placement"
    :append-to-body="appendToBody"
    :position-fixed="positionFixed"
    :viewport="viewport"
    :popper-options="popperOptions"
    ref="timepickerPoptip"
    @on-show="poperShow"
    @on-hide="poperHide"
  >
    <div slot="reference" ref="timepickerPoptipInput">
      <slot
        name="input"
        :placeholder="placeholder"
        :value="text"
        :suffix-icon="suffixIcon"
        :prefix-icon="prefixIcon"
        :disabled="disabled"
        :size="size"
        :readonly="readonly"
      >
        <x-input
          v-model="text"
          :placeholder="placeholder"
          :suffix-icon="suffixIcon"
          :prefix-icon="prefixIcon"
          :size="size"
          :readonly="readonly"
          @on-blur="blur"
          @input="pattern = 'edit'"
        ></x-input>
      </slot>
    </div>
    <div>
      <x-time :format="format" 
              :type="timeType" ref="xTime" 
              :step="step"
              @change="timeChange" @_hoverBlock="hoverBlock"></x-time>
      <div class="x-date-packer-confirm" v-if="confirm">
        <div class="confirm-slot">
          <slot name="confirm"></slot>
        </div>
        <div class="confirm-btn">
          <span class="ck-act" @click="empty">{{t('ans.timepicker.clear')}}</span>
          <button @click="poperDoClose">{{t('ans.timepicker.confirm')}}</button>
        </div>
      </div>
    </div>
  </x-poptip>
</template>

<script>
import { Locale } from '../../../../src/util'
import xTime from '../../../vue-datepicker/src/source/base/time.vue'
import { xInput } from '../../../vue-input/src/index'
import { xPoptip } from '../../../vue-poptip/src/index'
import isValid from '../../../vue-datepicker/src/source/util/isValid.js'
import isType from '../../../vue-datepicker/src/source/util/isType.js'
import moment from 'dayjs'
import { t } from '../../../../src/locale'

export default {
  name: 'xTimepicker',

  components: { xTime, xInput, xPoptip },

  mixins: [Locale],

  props: {
    // 显示类型，可选值为 time、timerange
    type: {
      type: String,
      default: 'time'
    },

    // 可以是 JavaScript 的 Date，例如 new Date()，也可以是标准的时间格式: YYYY-MM-DD HH:mm:ss
    value: {
      type: [Date, String, Number, Array],
      default: ''
    },

    // 展示的时间格式
    format: {
      type: String,
      default: 'HH:mm:ss'
    },

    // 时间选择器出现的位置
    placement: {
      type: String,
      default: 'bottom-start'
    },

    // 占位文本
    placeholder: {
      type: String,
      default () {
        return t('ans.timepicker.placeholder')
      }
    },

    // 是否显示底部控制栏
    confirm: {
      type: [Number, Boolean],
      default: 0
    },

    // 尺寸，可选值为large、small、default或者不设置
    size: {
      type: String,
      default: 'default'
    },

    // 是否只读
    readonly: {
      type: Boolean,
      default: true
    },

    // disabled
    disabled: {
      type: Boolean,
      default: false
    },

    // 是否添加到body
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

    suffixIcon: {
      type: String,
      default: 'ans-icon-calendar'
    },

    prefixIcon: {
      type: String,
      default: ''
    },

    // 步长
    step: {
      type: Array,
      default () {
        return [1, 1, 1]
      }
    }
  },

  data () {
    return {
      text: '',
      timeType: '',
      dateValue: null,
      dateValueBar: '',
      startDate: null,
      endDate: null,

      // 模式 select:选择模式  edit:编辑模式
      pattern: 'select'
    }
  },

  created () {
    this.timeType = this.type === 'timerange' ? 'daterange' : 'date'
  },

  mounted () {
    this.init()
  },

  methods: {
    init () {
      this.setDate(null, true)
      this.setText()
      this.$refs.xTime.init(this.startDate, this.endDate)
    },

    // 设置初始值
    setDate (value, isInit) {
      let date = value || this.value
      if (isType(date) === 'array') {
       
        if (date[0] && isValid(date[0])) {
          this.startDate = date[0]
        }
        if (date[1] && isValid(date[1])) {
          this.endDate = date[1]
        }

        // 初始化
        if(isInit) {
          this.dateValue = [this.startDate, this.endDate]
          this.dateValueBar = [this.startDate, this.endDate]
        }
      } else {
        if (isValid(date)) {
          this.startDate = date

          // 是否初始化
          if(isInit) {
            this.dateValue = new Date(date)
            this.dateValueBar = new Date(date)
          }
        }
      }
    },

    dateFormat (date) {
      if(date) {
        return moment(date).format(this.format)
      }
      return date
    },

    setText () {
      if (this.startDate && this.endDate) {
        this.text = this.dateFormat(this.startDate) + ' - ' + this.dateFormat(this.endDate)
        return
      }
      if (this.startDate) {
        this.text = this.dateFormat(this.startDate)
      }
    },

    // timeChange
    timeChange (date) {
      this.pattern = 'select'
      this.dateValue = date
      this.setDate(date)
      this.setText()
    },

    poperShow () {
      this.$emit('on-show')
      this.$refs.xTime.setScrollTop()
    },

    poperHide () {
      if(this.pattern === 'select') {
        this.change()
      }
      this.$emit('on-hide')
    },

    poperDoClose () {
      this.$refs.timepickerPoptip.doClose()
    },

    // 清空
    empty () {
      this.poperDoClose()
      this.text = ''
      this.startDate = null
      this.endDate = null
      this.dateValue = null
      this.$refs.xTime.init(this.startDate, this.endDate)
      this.$emit('on-clear')
    },

    fmtDateValue (key = 'dateValue') {
      let dateValueFmt = []
      if (isType(this[key]) === 'array') {
        this[key].forEach(o => {
          dateValueFmt.push(this.dateFormat(o))
        })
      } else {
        dateValueFmt = this.dateFormat(this[key])
      }
      return dateValueFmt
    },

    change () {
      if (this.dateValue) {
        // debugger
        if (this.fmtDateValue().toString() !== this.fmtDateValue('dateValueBar').toString()) {
          this.$emit('on-change', this.fmtDateValue(), this.dateValue)
          this.dateValueBar = this.dateValue
          this.$refs.xTime.init(this.startDate, this.endDate)
        }
      }
    },

    // 选中文本框内容
    setSelectionRange (selectionStart, selectionEnd) {
      if (this.readonly || this.disabled) {
        return false
      }
      let input = this.$refs.timepickerPoptipInput.querySelector('input')
      if (input.setSelectionRange) {
        input.focus()
        input.setSelectionRange(selectionStart, selectionEnd)
      }
      else if (input.createTextRange) {
        var range = input.createTextRange();
        range.collapse(true)
        range.moveEnd('character', selectionEnd)
        range.moveStart('character', selectionStart)
        range.select()
      }
    },

    // 失去焦点
    blur () {
      let isDate = (date) => {
        return new Date(date) != 'Invalid Date'
      }
      setTimeout(()=> {
        let input = this.$refs.timepickerPoptipInput.querySelector('input').value

        if(this.readonly || this.disabled || !input || this.pattern !== 'edit') return

        if (this.type === 'time') {
          let date = moment(this.dateValue || new Date()).format('YYYY-MM-DD') + ' ' + input
          if(isDate(date)) {
            this.timeChange(new Date(date))
            this.change()
          } else {
            this.timeChange(this.dateValueBar)
          }
        } else {

          let setDate = (index, input) => {
           return moment(this.dateValue[index] || new Date()).format('YYYY-MM-DD') + ' ' + input
          }
          let inputRange = input.split(' - '), inputDate = setDate(0, inputRange[0]), inputDate1 = setDate(1, inputRange[1])
          if(inputRange.length === 2 && isDate(inputDate) && isDate(inputDate1)) {

            // 结束时间不能小于开始时间
            if(new Date(inputDate).getTime() > new Date(inputDate1).getTime()) {
              this.timeChange(this.dateValueBar)
            } else {
              // debugger
              this.timeChange([new Date(inputDate), new Date(inputDate1)])
              this.change()
            }
            
          } else {
            this.timeChange(this.dateValueBar)
          }
        }
      }, 100)
    },

    hoverBlock (type, index) {

      if(type === 'start') {
        if(index === 0) {
          this.setSelectionRange(0, 2)
        }
        if(index === 1) {
          this.setSelectionRange(3, 5)
        }
        if(index === 2) {
          this.setSelectionRange(6, 8)
        }
      } else {
        let start = this.text.indexOf(' - ') + 3
        if(index === 0) {
          this.setSelectionRange(start, start + 2)
        }
        if(index === 1) {
          this.setSelectionRange(start + 3, start + 5)
        }
        if(index === 2) {
          this.setSelectionRange(start + 6, start + 8)
        }
      }
    }
  }
}
</script>
