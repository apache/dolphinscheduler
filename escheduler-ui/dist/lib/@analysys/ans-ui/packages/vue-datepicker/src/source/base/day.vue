<template>
  <div class="x-date-packer-panel" ref="dateDay">
    <ym v-show="ymShow" ref="dateYm" @change="ymChange" @hide="ymShow = false"></ym>
    <div class="x-date-packer-hd">
      <span class="hd-icon hd-icon-left"><i class="ans-icon-arrow-to-left" @click="setToDate('subtract', 'y')"></i> <i class="ans-icon-arrow-left ans-icon-arrow-left" @click="setToDate('subtract', 'M')"></i></span>
      <span class="hd-txt" @click="showYm('y')">{{year + t('ans.datepicker.year')}}</span>
      <span class="hd-txt" @click="showYm('m')">{{month && t('ans.datepicker.month' + month)}}</span>
      <span class="hd-icon hd-icon-right"><i class="ans-icon-arrow-right" @click="setToDate('add', 'M')"></i> <i class="ans-icon-arrow-to-right" @click="setToDate('add', 'y')"></i></span>
    </div>
    <div class="x-date-packer-day">
      <div class="x-date-packer-day-week">
        <span v-for="item in week" class="lattice">{{item}}</span>
      </div>
      <div class="x-date-packer-day-day">
        <label class="lattice" v-for="i in weeks"></label><span class="lattice em" v-for="d in days" v-attr="hoverDate(d)" @click="selected(d, $event)"
                                                                :key="uuId()" @mouseover="hover(d, $event)" :class="{'picker-disabled': getDisabledDate(d)}">
          <label :data-mouseover="year + '-' + month + '-' + d" class="dataMouseoverAct"><em :data-today="today(d)" :data-select="selectDay(d)">{{d}}</em></label>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
import moment from 'dayjs'
import toDate from '../util/toDate'
import isValid from '../util/isValid.js'
import ym from './years.vue'
import { uuid, Locale } from '../../../../../src/util'
import { t } from '../../../../../src/locale'

let toDateCache = toDate()

const colorAlls = ['#0098e1', '#ffcf3d', '#7281c2', '#f2ac6f', '#f07d7d', '#e84d80', '#a463b0', '#7a56b8', '#625ad1', '#8ba8d6']

for (let i = 10; i < 100; i++) {
  colorAlls[i] = '#' + Math.floor(Math.random() * 0xffffff).toString(16)
}

const WEEKS = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat']
const daysInWeek = WEEKS.map(w => t(`ans.datepicker.weeks.${w}`))

export default {
  components: { ym },
  data () {
    return {
      week: daysInWeek,

      // 本月一号星期几
      weeks: 0,

      // 本月多少天
      days: 0,

      year: 0,

      month: 0,

      // 当前面板日期
      toDate: new Date(),

      ymShow: false
    }
  },

  mixins: [Locale],

  props: {

    type: {
      type: String,
      default: 'date'
    },

    // 展示的日期格式
    format: {
      type: String,
      default: 'YYYY/MM/DD'
    },

    // 选中日期
    selectedDate: {
      type: [Array],
      default () {
        // return [new Date()]
        return null
      }
    },

    // 是否
    isHover: {
      type: [Number, String, Boolean],
      default: false
    },

    // hover start time
    hoverStartDate: {
      type: [Array],
      default: null
      // default () {
      //   return ['2018-06-03', '2018-06-10', '2018-06-15']
      // }
    },

    // hover end time
    hoverEndDate: {
      type: [Array],
      default: null
      // default () {
      //   return ['2018-06-07', '2018-06-14', '2018-07-23']
      // }
    },

    // 禁用日期
    disabledDate: {
      type: Function
    }
  },

  methods: {
    init (date) {
      let { year, month, week, day } = this.thisDate()
      this.year = year
      this.month = month
      this.weeks = week
      this.days = day
    },

    // 获取当前面板信息
    thisDate () {
      let dates = moment(new Date(this.toDate)).toDate(), year = dates.getFullYear(), month = dates.getMonth() + 1
      return {
        year: year, // 年份
        month: month, // 月份
        day: new Date(year, month, 0).getDate(), // 每月多少天
        week: new Date(year, month - 1, 1).getDay(), // 每月第一天星期几
        hours: dates.getHours(),
        minutes: dates.getMinutes(),
        seconds: dates.getSeconds()
      }
    },

    // 日期格式化
    dateFmt (date, fmt) {
      return moment(date).format(fmt)
    },

    // 给今天添加样式
    today (d) {
      return new Date(this.year, this.month, d).getTime() === new Date(toDateCache.year, toDateCache.month, toDateCache.today).getTime()
    },

    //
    hoverDate (d) {
      let toDate = moment(new Date(this.year, this.month - 1, d)).format('YYYYMMDD')
      let hStart = this.hoverStartDate, hEnd = this.hoverEndDate
      return { toDate, hStart, hEnd }
    },

    // 当前选中日期
    selectDay (d) {
      let selDate = this.selectedDate

      if (!selDate || this.type !== 'date') return false
      if (selDate.length > 0) {
        let toDate = moment(new Date(this.year, this.month - 1, d)).format('YYYYMMDD')
        for (let i = 0, len = selDate.length; i < len; i++) {
          if (isValid(selDate[i])) {
            let thisDate = moment(selDate[i]).format('YYYYMMDD')
            if (toDate === thisDate) return true
          }
        }
        return false
      }
    },

    setToDateInit (type, key) {
      this.toDate = moment(new Date(this.toDate))[type](1, key).format('YYYY-MM-DD')
      this.init()
    },

    // 加减日期
    setToDate (type, key) {
      this.setToDateInit(type, key)
      this.$emit('switch-date-panel', type, key)
    },

    // 选中
    selected (d, e) {
      if (/picker-disabled/.test(e.currentTarget.className)) return
      let date = new Date(this.year, this.month - 1, d)
      this.$emit('change', date)
    },

    // hover
    hover (d, e) {
      if (/picker-disabled/.test(e.currentTarget.className)) return
      if (this.isHover) {
        this.$emit('hover', new Date(this.year, this.month - 1, d))
      }
    },

    // 显示选择年月面板
    showYm (type) {
      this.ymShow = true
      this.$refs.dateYm.init(this.toDate, type)
    },

    ymChange (date) {
      this.toDate = date
      this.ymShow = false
      this.init([date])
      this.$emit('ym-change', date, this.$refs.dateDay)
    },

    // 设置不可选日期
    getDisabledDate (d) {
      return this.disabledDate ? this.disabledDate(moment(new Date(this.year, this.month - 1, d)).toDate()) : false
    },

    uuId () {
      return uuid() + uuid() + uuid()
    }
  },
  directives: {
    attr: {
      bind: (el, binding) => {
        let colors = colorAlls
        let { toDate, hStart, hEnd } = binding.value
        if (hStart && hEnd) {
          for (let i = 0, len = hStart.length; i < len; i++) {
            if (hStart[i] && hEnd[i]) {
              let startDate = moment(new Date(hStart[i])).format('YYYYMMDD')
              let endDate = moment(new Date(hEnd[i])).format('YYYYMMDD')
              let style = el.getAttribute('style')
              let b = document.createElement('b')
              if (toDate === startDate) {
                !style ? el.setAttribute('data-hover-start', 'true') : b.setAttribute('data-hover-start', 'true')
              }
              if (toDate === endDate) {
                !style ? el.setAttribute('data-hover-end', 'true') : b.setAttribute('data-hover-end', 'true')
              }
              if (toDate >= startDate && toDate <= endDate) {
                if (!style) {
                  el.style.background = colors[i] || '#0098e1'
                } else {
                  b.style.background = colors[i] || '#0098e1'
                  el.appendChild(b)
                }
                el.setAttribute('data-hover', 'true')
                el.setAttribute('data-hover-' + i, 'true')
              }
            }
          }
        }
      }
    }
  }
}
</script>
