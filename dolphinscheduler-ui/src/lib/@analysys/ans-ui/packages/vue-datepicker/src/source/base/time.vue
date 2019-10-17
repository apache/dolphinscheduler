<template>
  <div class="x-date-packer-time" :style="{width: type==='daterange' ? '305px' : 'auto'}">
    <div class="packer-time-body" :class="[getType()]">
      <div class="hd-text" v-if="type==='daterange'">{{t('ans.datepicker.startTime')}}</div>
      <div class="picker-time-div time-hours" ref="hours" >
        <ul @mouseover="hoverBlock('start', 0)">
          <li v-for="(item,index) in setStep(24, step[0])" :key="index"
            :class="{'time-act': item == hours}" 
            @click="change(item, 'hours')">{{numLen(item)}}</li>
        </ul>
      </div>
      <div class="picker-time-div time-minute" ref="minute">
        <ul @mouseover="hoverBlock('start', 1)">
          <li v-for="(item,index) in setStep(60, step[1])" :key="index"
              :class="{'time-act': item == minute}" 
              @click="change(item, 'minute')">{{numLen(item)}}</li>
        </ul>
      </div>
      <div class="picker-time-div time-second"  ref="second">
        <ul @mouseover="hoverBlock('start', 2)">
          <li v-for="(item,index) in setStep(60, step[2])" :key="index"
              :class="{'time-act': item == second}" 
              @click="change(item, 'second')">{{numLen(item)}}</li>
        </ul>
      </div>
    </div>
    <div class="packer-time-body" :class="[getType()]" v-if="type==='daterange'" style="float:right">
      <div class="hd-text" v-if="type==='daterange'">{{t('ans.datepicker.endTime')}}</div>
      <div class="picker-time-div time-hours bd-left" ref="hours1">
        <ul @mouseover="hoverBlock('end', 0)">
          <li v-for="(item,index) in setStep(24, step[0])" :key="index"
          :class="{'time-act': item == hours1}"
          @click="changeLast(item, 'hours1')">{{numLen(item)}}</li>
        </ul>
      </div>
      <div class="picker-time-div time-minute" ref="minute1">
        <ul @mouseover="hoverBlock('end', 1)">
          <li v-for="(item,index) in setStep(60, step[1])" :key="index"
              :class="{'time-act': item == minute1}" 
              @click="changeLast(item, 'minute1')">{{numLen(item)}}</li>
        </ul>
      </div>
      <div class="picker-time-div time-second" ref="second1">
        <ul @mouseover="hoverBlock('end', 2)">
          <li v-for="(item,index) in setStep(60, step[2])" :key="index"
              :class="{'time-act': item == second1}" 
              @click="changeLast(item, 'second1')">{{numLen(item)}}</li>
        </ul>
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
      hours: null,
      minute: null,
      second: null,

      hours1: null,
      minute1: null,
      second1: null
    }
  },
  props: {
    format: String,
    type: String,
    step: {
      type: Array,
      default() {
        return [1, 1, 1]
      }
    }
  },
  mixins: [Locale],
  methods: {
    init (date, date1) {
      this.setDate(date, date1)
      if (date) {
        this.hours = moment(new Date(date)).format('H')
        this.minute = moment(new Date(date)).format('m')
        this.second = moment(new Date(date)).format('s')
      } else {
        this.hours = null
        this.minute = null
        this.second = null
      }

      if (date1) {
        this.hours1 = moment(new Date(date1)).format('H')
        this.minute1 = moment(new Date(date1)).format('m')
        this.second1 = moment(new Date(date1)).format('s')
      } else {
        this.hours1 = null
        this.minute1 = null
        this.second1 = null
      }
    },
    setStep (num, step) {
      if(!step) {
        step = 1
      }
      if (num === 1 || !num) {
        return num
      }
      let n = []
      for (let i = 0; i < num; i++) {
        if(i % step === 0) {
          n.push(i)
        }
      }
      return n
    },

    getStepScrollTop (num, index) {
      let selfStep = this.step[index]
      if(selfStep && selfStep > 1) {
        return num % selfStep === 0 ? (num / selfStep) * 25 : 0
      }
      return num * 25
    },

    setScrollTop () {
      
      // let step = (num, index) => {
      //   let selfStep = this.step[index]
      //   if(selfStep && selfStep > 1) {
      //     return num % selfStep === 0 ? num / selfStep : 0
      //   }
      //   return num
      // }

      setTimeout(() => {
        this.$refs.hours.scrollTop = this.getStepScrollTop(this.hours, 0)
        this.$refs.minute.scrollTop = this.getStepScrollTop(this.minute, 1)
        this.$refs.second.scrollTop = this.getStepScrollTop(this.second, 2)
        if (this.$refs.hours1) this.$refs.hours1.scrollTop = this.getStepScrollTop(this.hours1, 0)
        if (this.$refs.minute1) this.$refs.minute1.scrollTop = this.getStepScrollTop(this.minute1, 1)
        if (this.$refs.second1) this.$refs.second1.scrollTop = this.getStepScrollTop(this.second1, 2)
      }, 10)
    },

    setDate (date, date1) {
      let Dates = new Date(date || new Date())
      this.year = Dates.getFullYear()
      this.month = Dates.getMonth()
      this.day = Dates.getDate()


      let Dates1 = new Date(date1 || new Date())
      this.year1 = Dates1.getFullYear()
      this.month1 = Dates1.getMonth()
      this.day1 = Dates1.getDate()
    },

    setHms (value, type) {
      this[type] = value

      let index = 0

      if(/minute/.test(type)) {
        index = 1
      }

      if(/second/.test(type)) {
        index = 2
      }

      this.$refs[type].scrollTop = this.getStepScrollTop(value, index)

      let key = ['hours', 'minute', 'second', 'hours1', 'minute1', 'second1']
      key.forEach(item => {
        if (this[item] === null) this[item] = 0
      })
    },

    change (value, type) {
      this.setHms(value, type)
      
      if (this.year === this.year1 && this.month === this.month1 && this.day === this.day1) {
        if (type === 'hours') {
          if (this.hours1 <= value && this.minute > this.minute1) {
            this.minute1 = this.minute
            this.$refs.minute1.scrollTop = this.getStepScrollTop(this.minute, 1)
          }
          if (this.hours1 < value) {
            this.hours1 = value
            if (this.$refs.hours1) this.$refs.hours1.scrollTop = this.getStepScrollTop(value, 0)
          }
        }
        if (type === 'minute' && this.hours1 === this.hours && this.minute1 < value) {
          this.minute1 = value
          if (this.$refs.minute1) this.$refs.minute1.scrollTop = this.getStepScrollTop(value, 1)
        }
        if (type === 'second' && this.hours1 === this.hours && this.minute1 === this.minute && this.second1 < value) {
          this.second1 = value
          if (this.$refs.second1) this.$refs.second1.scrollTop = this.getStepScrollTop(value, 2)
        }
      }

      this.onChange()
    },

    changeLast (value, type) {
      if (this.year === this.year1 && this.month === this.month1 && this.day === this.day1) {
        if (type === 'hours1' && this.hours > value) return
        if (type === 'minute1' && this.hours1 === this.hours && this.minute > value) return
        if (type === 'second1' && this.hours1 === this.hours && this.minute1 === this.minute && this.second > value) return
      }

      this.setHms(value, type)
      this.onChange()
    },

    onChange () {
      if (this.type === 'date') {
        this.$emit('change', new Date(this.year, this.month, this.day, this.hours, this.minute, this.second))
      } else {
        this.$emit('change', [new Date(this.year, this.month, this.day, this.hours, this.minute, this.second), new Date(this.year1, this.month1, this.day1, this.hours1, this.minute1, this.second1)])
      }
    },

    numLen (index) {
      return index.toString().length === 1 ? 0 + '' + index : index
    },
    getType () {
      let fmt = this.format
      if (/H|h/.test(fmt) && /m/.test(fmt) && /s/.test(fmt)) return 'hms'
      if (/H|h/.test(fmt) && /m/.test(fmt)) return 'hm'
      if (/H|h/.test(fmt)) return 'h'
    },

    hoverBlock (type, index) {
      this.$emit('_hoverBlock', type, index)
    }
  }
}

</script>
