<template>
  <div class="x-date-packer-ym">
    <div class="x-date-packer-hd">
      <span class="hd-icon hd-icon-left" style="display:block"><i class="ans-icon-arrow-left" @click="switchYear('left')"></i></span>
      <span class="hd-txt" @click="selectText">{{year + t('ans.datepicker.year')}}</span>
      <span class="hd-icon hd-icon-right" style="display:block"><i class="ans-icon-arrow-right" @click="switchYear('right')"></i></span>
    </div>
    <div class="x-date-packer-item">
      <div class="x-date-packer-year" v-if="type === 'y'">
        <div class="year-item" v-for="item in yearList"><span :class="{act: isYear(item)}" @click="selectYear(item)">{{item}}</span></div>
      </div>
      <div class="x-date-packer-month" v-else>
        <div class="year-item" v-for="item in 12"><span :class="{act: isMonth(item)}" @click="selectMonth(item)">{{t('ans.datepicker.month' + item)}}</span></div>
      </div>
    </div>
  </div>
</template>

<script>

import moment from 'dayjs'
import { Locale } from '../../../../../src/util'

export default {
  data () {
    return {
      date: null,

      year: 0,

      month: 0,

      type: 'y',

      yearList: []
    }
  },

  mixins: [Locale],

  props: {
    types: String
  },

  methods: {
    init (date, type) {
      this.date = date
      this.type = type
      this.year = moment(date).format('YYYY') - 0
      this.setYearList()
    },

    setYearList () {
      let list = []
      for (let i = this.year; i <= (this.year - 0) + 10; i++) {
        list.push(i)
      }
      this.yearList = list
    },

    isYear (y) {
      return y === moment(this.date).format('YYYY') - 0
    },

    isMonth (m) {
      return this.year === (moment(this.date).format('YYYY') - 0) && m === (moment(this.date).format('M') - 0)
    },

    // 选择年份
    selectYear (y) {
      this.year = y
      if (this.types === 'year') {
        let date = new Date(this.year, 0, 1)
        this.date = date
        this.$emit('change', date)
        return
      }
      this.type = 'm'
    },

    // 选择月份
    selectMonth (m) {
      this.month = m
      let date = new Date(this.year, this.month - 1, 1)
      this.$emit('change', date)
      this.date = date
    },

    // 点击年份文本
    selectText () {
      if (this.type === 'y') {
        this.$emit('hide')
      } else {
        this.type = 'y'
        this.year = moment(this.date).format('YYYY')
      }
    },

    //  左右切换年份
    switchYear (type) {
      if (type === 'left') {
        if (this.type === 'y') {
          this.year = this.year - 10
          this.setYearList()
        } else {
          this.year = this.year - 1
        }
      } else {
        if (this.type === 'y') {
          this.year = (this.year - 0) + 10
          this.setYearList()
        } else {
          this.year = this.year + 1
        }
      }
    }
  }
}

</script>
