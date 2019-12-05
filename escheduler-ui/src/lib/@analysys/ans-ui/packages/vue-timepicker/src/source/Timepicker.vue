<template>
  <x-poptip
    popper-class="date-poptip"
    class="x-datepicker"
    :placement="placement"
    transition="datepicker-animation"
    :append-to-body="appendToBody"
    :position-fixed="positionFixed"
    :viewport="viewport"
    :popper-options="popperOptions"
    ref="timepickerPoptip"
    @show="poperShow"
    @hide="poperHide"
  >
    <div slot="reference">
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
          :value="text"
          :placeholder="placeholder"
          :suffix-icon="suffixIcon"
          :prefix-icon="prefixIcon"
          :size="size"
          :readonly="readonly"
        ></x-input>
      </slot>
    </div>
    <div>
      <x-time :format="format" :type="timeType" ref="xTime" @change="timeChange"></x-time>
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
    }
  },

  data () {
    return {
      text: '',
      timeType: '',
      dateValue: null,
      dateValueBar: '',
      startDate: null,
      endDate: null
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
      this.setDate()
      this.setText()
      this.$refs.xTime.init(this.startDate, this.endDate)
    },

    // 设置初始值
    setDate (value) {
      let date = value || this.value
      if (isType(date) === 'array') {
        if (date[0] && isValid(date[0])) {
          this.startDate = date[0]
        }
        if (date[1] && isValid(date[1])) {
          this.endDate = date[1]
        }
      } else {
        if (isValid(date)) this.startDate = date
      }
    },

    dateFormat (date) {
      return moment(date).format(this.format)
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
      this.dateValue = date
      this.setDate(date)
      this.setText()
    },

    poperShow () {
      this.$emit('on-show')
      this.$refs.xTime.setScrollTop()
    },

    poperHide () {
      this.change()
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

    fmtDateValue () {
      let dateValueFmt = []
      if (isType(this.dateValue) === 'array') {
        this.dateValue.forEach(o => {
          dateValueFmt.push(this.dateFormat(o))
        })
      } else {
        dateValueFmt = this.dateFormat(this.dateValue)
      }
      return dateValueFmt
    },

    change () {
      if (this.dateValue) {
        if (this.dateValue.toString() !== this.dateValueBar.toString()) {
          this.$emit('on-change', this.fmtDateValue(), this.dateValue)
          this.dateValueBar = this.dateValue
        }
      }
    }
  }
}
</script>
