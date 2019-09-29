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
    ref="datepickerPoptip"
    @on-hide="pickerPoptipHide"
    @on-show="pickerPoptipShow">
    <div slot="reference">
      <slot name="input" :placeholder="placeholder" :value="text" :suffix-icon="suffixIcon" :prefix-icon="prefixIcon" :disabled="disabled" :size="size" :readonly="readonly">
        <x-input :value="text" :clearable="clearable" :placeholder="placeholder" :suffix-icon="suffixIcon" :prefix-icon="prefixIcon" :size="size" :readonly="readonly" @on-clear="empty"></x-input>
      </slot>
    </div>

    <date
      v-if="type==='date'"
      ref="date"
      :value="dateValue"
      :format="format"
      :disabled-date="disabledDate"
      :confirm="confirm"
      @change="dateChange"
      @cancel="cancel"
      @confirm="$refs.datepickerPoptip.doClose()"><template slot="confirm"><slot name="confirm"></slot></template></date>
    <daterange v-if="type==='daterange'"
               ref="refDaterange"
               :value="dateValue"
               :format="format"
               :confirm="confirm"
               :disabled-date="disabledDate"
               :options="options"
               :options-width="optionsWidth"
               @confirm="$refs.datepickerPoptip.doClose()"
               :multiple="multiple"
               :select-index="selectIndex"
               @change="dateChange"
               @cancel="cancel"
               :panel-num="panelNum"><template slot="confirm"><slot name="confirm"></slot></template></daterange>
    <month v-if="type==='year' || type==='month'"
           :type="type" :value="dateValue" @change="dateChange"></month>

  </x-poptip>
</template>

<script>
import { emitter } from '../../../../src/util'
import { xInput } from '../../../vue-input/src/index'
import { xPoptip } from '../../../vue-poptip/src/index'
import date from './panel/date.vue'
import Daterange from './panel/daterange.vue'
import isType from './util/isType.js'
import isValid from './util/isValid.js'
import ishms from './util/ishms.js'
import moment from 'dayjs'
import month from './panel/month.vue'
import { t } from '../../../../src/locale'

export default {
  name: 'xDatepicker',
  mixins: [emitter],
  components: { date, xPoptip, xInput, Daterange, month },
  props: {

    // 日期面板显示或者隐藏
    show: {
      type: [String, Number, Boolean],
      default: false
    },

    // 初始值
    value: {
      type: [Array, String, Number, Date],
      default () {
        return null
      }
    },

    /*
     * 日期类型
     * 选项值: date || daterange || year || month
    */
    type: {
      type: String,
      default: 'date'
    },

    // 展示的日期格式
    format: {
      type: String,
      default: 'YYYY-MM-DD'
    },

    // 不可选日期
    disabledDate: {
      type: Function
    },

    // 是否显示底部控制栏
    confirm: {
      type: [Number, Boolean],
      default: 0
    },

    // 选项
    options: {
      type: [Array, Object],
      default: null
    },

    // 左边快捷选项宽度
    optionsWidth: {
      type: Number,
      default: 150
    },

    /**
     * 日期面板显示方向
     * 选项值: bottom-left || bottom-right || top-left || top-right
     */
    placement: {
      type: String,
      default: 'bottom-start'

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

    // 文本框描述
    placeholder: {
      type: String,
      default () {
        return t('ans.datepicker.placeholder')
      }
    },

    clearable: Boolean,

    // disabled
    disabled: {
      type: Boolean,
      default: false
    },

    // // 尺寸，可选值为large、small、default或者不设置
    size: {
      type: String,
      default: 'default'
    },

    suffixIcon: {
      type: String,
      default: 'ans-icon-calendar'
    },

    prefixIcon: {
      type: String,
      default: ''
    },

    // 是否只读
    readonly: {
      type: Boolean,
      default: true
    },

    // 日期区间连接符,在daterange模式下有效
    rangeSeparator: {
      default: ' - '
    },

    // 可选择多个日期，默认一个, 0为无限个
    multiple: {
      type: Number,
      default: 1
    },

    // 多选日期时，点选,指定要选的index, 从0开始
    selectIndex: {
      type: Number,
      default: -1
    },

    // 支持面板个数，在daterange模式下有效
    panelNum: {
      type: Number,
      default: 3
    }
  },
  watch: {
    value (v, v1) {
      if (v.toString() !== (v1 && v1.toString()) && v.toString() !== this.dateValueBar.toString()) {
        this.init(true)
      }
    }
  },
  data () {
    return {
      text: '',
      dateValue: null,
      dateValueBar: null,
      display: false,

      isChange: true
    }
  },
  created () {
    this.init()
  },

  methods: {

    init (isChild) {
      let { dataValue } = this.getDateValue()
      this.setText(dataValue)
      this.dateValue = dataValue
      this.dateValueChange = [...dataValue]
      this.dateValueBar = [...dataValue]
      if (isChild) {
        if (this.type === 'daterange') this.$refs.refDaterange.init(this.dateValue, this.display)
        if (this.type === 'date') this.$refs.date.init(this.dateValue)
      }
    },

    // 获取赋值Value
    getDateValue () {
      let value = this.value, dataValue = []
      if (value) {
        if (isType(value) === 'array') {
          // daterange类型的老数据

          if (this.type === 'daterange' && value.length && isType(value[0]) !== 'array') {
            dataValue.push(value)
          } else {
            value.forEach(item => { dataValue.push(item) })
          }
        } else {
          // let isData = isValid(value)

          // 兼容日期区间之前已保存的数据格式 "近xx日" => [['近xx日']]
          this.type === 'daterange' ? dataValue.push([value]) : dataValue.push(value)
        }
      }
      return { dataValue }
    },


    doClose () {
      this.$refs.datepickerPoptip.doClose()
    },

    isConfirm () {
      if (this.confirm || ishms(this.format)) return true
      return false
    },

    dateChange (data, optItem) {
      this.dateValueChange = data
      this.optItem = optItem ? JSON.parse(JSON.stringify(optItem)) : null

      let fmtDate = this.fmtDate(data)

      this.setText(data, optItem)

      if (optItem && this.multiple === 1) {
        this.doClose()
      }

      !this.isConfirm() ? this.doClose() : this.$emit('on-text-change', fmtDate, optItem)
    },

    dateFormat (date) {
      return moment(new Date(date)).format(this.format)
    },

    // 获取文本展示文本
    setText (data) {
      if (data && data.length) {
        let type = this.type

        let fmtDate = this.fmtDate(data)

        if (type === 'date') this.text = fmtDate[0]

        if (type === 'daterange') {
          if (data.length === 1) {
            let dataFirst = fmtDate[0]
            this.text = dataFirst.join(this.rangeSeparator)
          } else {
            let text = []
            fmtDate.forEach(o => { text.push(o.join(this.rangeSeparator)) })
            this.text = text
          }
        }

        if (type === 'year' || type === 'month') this.text = fmtDate[0]
      } else {
        this.text = ''
      }
    },

    // 日期选择面板显示
    pickerPoptipShow () {
      this.$emit('on-open-change')
      this.display = true
    },

    //  日期选择面板关闭
    pickerPoptipHide () {
      if (this.isChange) this.change()
      this.$emit('on-close-change')
      this.display = false
      this.isChange = true
    },

    // 清空
    empty () {
      this.dateValue = []
      this.dateValueChange = []
      this.text = ''
      if (this.type === 'daterange') this.$refs.refDaterange.init(this.dateValue)
      if (this.type === 'date') this.$refs.date.init(this.dateValue)
      this.$emit('on-empty')
    },

    // 取消
    cancel () {
      this.isChange = false
      this.$emit('on-clear')
      this.doClose()
    },

    change () {
      if (this.dateValueChange.length) {
        let date = [...this.dateValueChange]
        let fmtDate = this.fmtDate(date)

        if (this.dateValueBar.toString() !== date.toString()) {
          let changeDate = fmtDate, type = this.type

          if (type === 'date') {
            changeDate = fmtDate[0]
          }

          if (type === 'daterange' && this.multiple === 1) {
            let dataFirst = fmtDate[0]
            changeDate = dataFirst.length === 1 ? dataFirst[0] : dataFirst
          }

          if (type === 'year') {
            changeDate = fmtDate[0]
          }

          if (type === 'month') {
            changeDate = fmtDate[0]
          }

          this.$emit('on-change', changeDate, date, this.optItem)
          this.$emit('input', changeDate)
          this.dispatch('xFormItem', 'on-form-change', changeDate)
          this.dateValueBar = date
        }
      }
    },

    // 格式化数据数组
    fmtDate (date, optItem) {
      if (date && date.length) {
        let fmtDate = []
        date.forEach(o => {
          if (isType(o) === 'array') {
            let arr = []
            for (let i = 0; i < o.length; i++) {
              isValid(o[i]) ? arr.push(moment(o[i]).format(this.format)) : arr.push(o[i])
            }
            fmtDate.push(arr)
          } else {
            isValid(o) ? fmtDate.push(moment(o).format(this.format)) : fmtDate.push(o)
          }
        })
        return fmtDate
      }
    }
  }
}
</script>
