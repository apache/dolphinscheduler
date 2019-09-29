<template>
  <div class="date-packer-daterange-body">
    <div class="date-packer-daterange">
      <div class="date-packer-opt" v-if="options" :style="{width: optionsWidth + 'px'}">
        <div class="picker-panel-shortcut" :style="item.style"
            v-for="item in options"
            @click="optionsClick(item, $event)" 
            @mouseenter="optionsHover(item, $event)"
            @mouseout="optionsHoverOut()"
            :class="{act: getOptItemAct(item)}">{{item.text}}</div>
      </div>
      <div class="date-interval" ref="datePackerDay" :style="{width: 202 * panelNum + 'px', marginLeft: options ? optionsWidth + 'px' : ''}">
        <template v-for="(item, i) in panelNum">
          <day
              type="daterange"
              :format="format"
              :is-hover="isHover"
              :selected-date="selectedDate"
              :hover-start-date="hoverStartDate"
              :hover-end-date="hoverEndDate"
              :disabled-date="disabledDate"
              @change="dayChange"
              @hover="hoverChange"
              @ym-change="ymChange"
              :ref="'dateRange' + i" 
              :class="{'date-start': i === 0, 'date-end': i === panelNum - 1}"
              @switch-date-panel="switchDatePanel" :index="i"></day>
        </template>
      </div>
      
    </div>
    <confirm v-if="isConfirm()" :format="format" @confirm-btn="$emit('confirm')" @cancel="cancel" type="daterange" ref="confirm" @time-change="timeChange">
      <template slot="confirm">
        <slot name="confirm"></slot>
      </template>
    </confirm>
  </div>
	
</template>

<script>

import day from '../base/day.vue'
import confirm from '../base/confirm.vue'
import ishms from '../util/ishms.js'
import isValid from '../util/isValid.js'
import isType from '../util/isType.js'
import moment from 'dayjs'

const deepFlatten = arr => [].concat(...arr.map(v => Array.isArray(v) ? deepFlatten(v) : v))
export default {
  name: 'Daterange',
  components: { day, confirm },

  data () {
    return {
      selectedDate: [],

      hoverStartDate: [],

      hoverEndDate: [],

      isHover: false,

      arrDate: []
    }
  },
  props: {

    // 初始值
    value: {
      type: [Array],
      default () {
        return null
      }
    },

    // 展示的日期格式
    format: String,

    // 不可选日期
    disabledDate: {
      type: Function
    },

    // confirm
    confirm: {
      type: [Number, Boolean],
      default: 0
    },

    // 选项
    options: {
      type: [Array, Object],
      default: null
    },

    optionsWidth: Number,

    // 支持面板个数
    panelNum: {
      type: Number,
      default: 2
    },

    // 可选择多个日期，默认一个, 0为无限个
    multiple: 0,

    // 多选日期时，指定选择的位数
    selectIndex: {
      type: Number,
      default: -1
    }
  },

  mounted () {
    this.init()
  },

  methods: {
    init (date, display) {
      let value = moment(new Date()).subtract(this.panelNum - 1, 'M'), selectedDate = []
      this.hoverStartDate = []
      this.hoverEndDate = []
      this.arrDate = []
      this.isHover = false
      let dateValue = date || this.value
      if (dateValue) {
        selectedDate = deepFlatten(this.value)
        this.selectedDate = dateValue

        // 面板包含当前选中日期
        if (isType(dateValue[0]) === 'array' && isValid(dateValue[0][0])) {
          value = moment(dateValue[0][0]).subtract(this.panelNum - 2, 'M')
        }

        dateValue.forEach((item, i) => {
          if (item.length === 2) {
            this.hoverStartDate.push(item[0])
            this.hoverEndDate.push(item[1])
          } else {
            let options = this.options
            if (options && options.length) {
              for (let i = 0; i < options.length; i++) {
                if (options[i].text === item[0] && options[i].value) {
                  let value = options[i].value()
                  this.hoverStartDate.push(value[0])
                  this.hoverEndDate.push(value[1])
                }
              }
            }
          }
        })
      }

      if (!display) {
        this.switchPanel(value, selectedDate)
      }

      this.timeInit()
    },

    timeInit (isShow = true) {
      if (ishms(this.format)) {
        let selectedDate = this.selectedDate
        let len = selectedDate.length
        if (len) {
          let date = selectedDate[len - 1]
          if (date.length === 2 && isShow) {
            this.$refs.confirm.timsInit(date[0], date[1])
          } else {
            this.$refs.confirm.timsInit()
          }
        }
      }
    },

    // 切换面板到当前选中日期
    switchPanel (value, selectedDate) {
      for (let i = 0; i < this.panelNum; i++) {
        this.$refs['dateRange' + i][0].toDate = value
        value = moment(new Date(value)).add(1, 'M')
        this.$refs['dateRange' + i][0].init(selectedDate)
      }
    },

    dayChange (date) {
      let multiple = this.multiple
      multiple === 1 ? this.radio(date) : this.multiSel(date)
    },

    hoverChange (date) {
      let arrDate = this.arrDate
      date.getTime() > arrDate[0].getTime() ? this.hoverClass([arrDate[0], date]) : this.hoverClass([date, arrDate[0]])
    },

    // 单选
    radio (date) {
      if (this.arrDate.length === 0) {
        this.selectedDate = [[date]]
        this.hoverStartDate = [date]
        this.hoverEndDate = []
        this.arrDate.push(date)
        this.isHover = true
      } else {
        if (date.getTime() > this.selectedDate[0][0].getTime()) {
          this.selectedDate[0].push(date)
          this.hoverEndDate = [date]
        } else {
          let endDate = this.selectedDate[0][0]
          this.selectedDate = []
          this.hoverStartDate = []
          this.selectedDate = [[date, endDate]]
          this.hoverStartDate = [date]
          this.hoverEndDate = [endDate]
        }
        this.isHover = false
        this.arrDate = []
        this.change()
      }
      this.timeInit()
    },

    // 多选
    multiSel (date) {
      let set = () => {
        let arrDate = this.arrDate
        let selectIndex = this.selectIndex
        if (arrDate.length === 0) {
          selectIndex === -1 ? this.hoverStartDate.push(date) : this.hoverStartDate[selectIndex] = date
          arrDate.push(date)
          this.isHover = true
          this.timeInit(false)
        } else {
          if (date.getTime() > arrDate[0].getTime()) {
            if (selectIndex === -1) {
              this.selectedDate.push([arrDate[0], date])
              this.hoverEndDate.push(date)
            } else {
              this.$set(this.selectedDate, selectIndex, [arrDate[0], date])
              this.hoverEndDate[selectIndex] = date
            }
          } else {
            if (selectIndex === -1) {
              this.hoverStartDate.pop()
              this.selectedDate.push([date, arrDate[0]])
              this.hoverStartDate.push(date)
              this.hoverEndDate.push(arrDate[0])
            } else {
              this.$set(this.selectedDate, selectIndex, [date, arrDate[0]])
              this.hoverStartDate[selectIndex] = date
              this.hoverEndDate[selectIndex] = arrDate[0]
            }
          }
          this.isHover = false
          this.arrDate = []
          this.change()
          this.timeInit()
        }
      }

      this.getMultipleFn(set)
    },

    isConfirm () {
      if (this.confirm || ishms(this.format)) return true
      return false
    },

    // 左右切换日期
    switchDatePanel (type, key) {
      if (type === 'add') {
        for (let i = 0; i < this.panelNum - 1; i++) {
          this.$refs['dateRange' + i][0].setToDateInit(type, key)
        }
      } else {
        for (let i = 1; i < this.panelNum; i++) {
          this.$refs['dateRange' + i][0].setToDateInit(type, key)
        }
      }
    },

    // options 选择
    optionsClick (item, e) {
      if (/act/.test(e.currentTarget.className)) return
      item.click && item.click(item)
      let value = item.value()
      let type = item.type
      let dateValue = type === 'text' ? [item.text] : value

      if (value.length === 2 && isValid(value[0]) && isValid(value[1])) {
        let startDate = moment(new Date(value[0])).subtract(this.panelNum - 2, 'M')
        this.switchPanel(startDate, value)
      }

      this.getMultipleFn(() => {
        let selectIndex = this.selectIndex
        if (selectIndex === -1) {
          this.hoverStartDate.push(value[0])
          this.hoverEndDate.push(value[1])
          this.selectedDate.push(dateValue)
        } else {
          this.hoverStartDate[selectIndex] = value[0]
          this.hoverEndDate[selectIndex] = value[1]
          this.$set(this.selectedDate, selectIndex, dateValue)
        }
      }, () => {
        this.selectedDate = [dateValue]
        this.hoverStartDate = [value[0]]
        this.hoverEndDate = [value[1]]
      })
      this.change(item)
      this.reset()
    },

    //
    optionsHover (item, e) {
      if (/act/.test(e.currentTarget.className) || !item.value) return
      this.timeOut = setTimeout(() => {
        this.hoverClass(item.value())
      }, 200)
    },

    optionsHoverOut () {
      if (this.timeOut) {
        clearTimeout(this.timeOut)
        this.timeOut = null
      }
      this.hoverClass([])
    },


    // 根据multiplen单选多选回调
    getMultipleFn (fn, fn1) {
      let num = this.multiple
      if (num === 1) { // 单选
        fn1 && fn1()
      } else { // 多选
        if (num === 0) {
          fn && fn()
        } else {
          let len = this.hoverEndDate.length
          if (len === num && this.selectIndex === -1) {
            this.selectedDate.pop()
            this.hoverStartDate.pop()
            this.hoverEndDate.pop()
          }
          fn && fn()
        }
      }
    },

    // 获取当前opt Item是否选中
    getOptItemAct (item) {
      let value = item.value().toString()
      let selectedDate = this.selectedDate
      return selectedDate.find((n) => n[0] === item.text || n.toString() === value)
    },

    hoverClass (date) {
      let $day = this.$refs.datePackerDay.querySelectorAll('.dataMouseoverAct')
      if (date.length === 2) {
        for (let i = 0, len = $day.length; i < len; i++) {
          let o = $day[i]
          let toDate = moment(o.getAttribute('data-mouseover')).format('YYYYMMDD')
          let startDate = moment(new Date(date[0])).format('YYYYMMDD')
          let endDate = moment(new Date(date[1])).format('YYYYMMDD')

          if (toDate === startDate && toDate === endDate) {
            o.className = 'dataMouseoverAct bg-hover bg-hover-centre'
          } else if (toDate === startDate) {
            o.className = 'dataMouseoverAct bg-hover bg-hover-start'
          } else if (toDate === endDate) {
            o.className = 'dataMouseoverAct bg-hover bg-hover-end'
          } else if (toDate > startDate && toDate < endDate) {
            o.className = 'dataMouseoverAct bg-hover'
          } else {
            o.className = 'dataMouseoverAct'
          }
        }
      } else {
        for (let i = 0, len = $day.length; i < len; i++) {
          let o = $day[i]
          o.className = 'dataMouseoverAct'
        }
      }
    },

    // 监听切换年月
    ymChange (date, ref) {
      let index = ref.getAttribute('index') - 0, value = date, decrease = date
      for (let i = index + 1; i < this.panelNum; i++) {
        let vDateRange = this.$refs['dateRange' + i][0]
        value = moment(value).add(1, 'M')
        vDateRange.toDate = value
        vDateRange.init()
      }

      for (let i = index - 1; i >= 0; i--) {
        let vDateRange = this.$refs['dateRange' + i][0]
        decrease = moment(decrease).subtract(1, 'M')
        vDateRange.toDate = decrease
        vDateRange.init()
      }
    },

    // Reset
    reset () {
      this.isHover = false
      this.arrDate = []
      this.hoverClass([])
    },

    cancel () {
      this.$emit('cancel')
      this.reset()
    },

    timeChange (date) {
      this.selectedDate[this.selectedDate.length - 1] = date
      this.change()
    },

    change (optItem) {
      let date = [...this.selectedDate]
      date.forEach(o => {
        if (o.length === 2) {
          o[0] = new Date(o[0])
          o[1] = new Date(o[1])
        }
      })
      this.$emit('change', date, optItem)
    }
  }
}
</script>