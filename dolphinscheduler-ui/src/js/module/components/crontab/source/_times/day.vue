/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
<template>
  <div class="day-model">
    <div class="v-crontab-from-model">
      <x-radio-group v-model="radioDay" vertical>
        <div class="list-box">
          <x-radio label="everyDay">
            <span class="text">{{$t('每一天')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="WkintervalWeek">
            <span class="text">{{$t('每隔')}}</span>
            <m-input-number :min="0" :max="7" :props-value="parseInt(WkintervalWeekPerformVal)" @on-number="onWkintervalWeekPerform"></m-input-number>
            <span class="text">{{$t('天执行 从')}}</span>
            <x-select :placeholder="$t('请选择具体小时数')" style="width: 200px" v-model="WkintervalWeekStartVal">
              <x-option
                      v-for="item in selectWeekList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
            <span class="text">{{$t('开始')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="intervalDay">
            <span class="text">{{$t('每隔')}}</span>
            <m-input-number :min="0" :max="31" :props-value="parseInt(intervalDayPerformVal)" @on-number="onIntervalDayPerform"></m-input-number>
            <span class="text">{{$t('天执行 从')}}</span>
            <m-input-number :min="1" :max="31" :props-value="parseInt(intervalDayStartVal)" @on-number="onIntervalDayStart"></m-input-number>
            <span class="text">{{$t('天开始')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="WkspecificWeek">
            <!--<span class="text">（周）</span>-->
            <span class="text">{{$t('具体星期几(可多选)')}}</span>
            <x-select multiple :placeholder="$t('请选择具体周几')" v-model="WkspecificWeekVal">
              <x-option
                      v-for="item in selectSpecificWeekList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="specificDay">
            <span class="text">{{$t('具体天数(可多选)')}}</span>
            <x-select multiple :placeholder="$t('请选择具体天数')" v-model="WkspecificDayVal">
              <x-option
                      v-for="item in selectSpecificDayList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="monthLastDays">
            <span class="text">{{$t('在这个月的最后一天')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="monthLastWorkingDays">
            <span class="text">{{$t('在这个月的最后一个工作日')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="monthLastWeeks">
            <span class="text">{{$t('在这个月的最后一个')}}</span>
            <x-select :placeholder="$t('请选择具体周几')" v-model="monthLastWeeksVal">
              <x-option
                      v-for="item in monthLastWeeksList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="monthTailBefore">
            <m-input-number :min="0" :max="31" :props-value="parseInt(monthTailBeforeVal)" @on-number="onMonthTailBefore"></m-input-number>
            <span class="text">{{$t('在本月底前')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="recentlyWorkingDaysMonth">
            <span class="text">{{$t('最近的工作日（周一至周五）至本月')}}</span>
            <m-input-number :min="0" :max="31" :props-value="parseInt(recentlyWorkingDaysMonthVal)" @on-number="onRecentlyWorkingDaysMonth"></m-input-number>
            <span class="text">{{$t('日')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="WkmonthNumWeeks">
            <!--<span class="text">（周）</span>-->
            <span class="text">{{$t('在这个月的第')}}</span>
            <m-input-number :min="0" :max="31" :props-value="parseInt(WkmonthNumWeeksDayVal)" @on-number="onWkmonthNumWeeksDay"></m-input-number>
            <x-select :placeholder="$t('请选择具体周几')" style="width: 200px"  v-model="WkmonthNumWeeksWeekVal">
              <x-option
                      v-for="item in WkmonthNumWeeksWeekList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
      </x-radio-group>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '../_source/i18n'
  import { selectList, isStr, isWeek } from '../util/index'
  import mInputNumber from '../_source/input-number'

  export default {
    name: 'day',
    mixins: [i18n],
    data () {
      return {
        radioDay: 'everyDay',
        dayValue: '*',
        weekValue: '?',
        everyDayVal: '*',
        WkintervalWeekPerformVal: 2, // Every few days
        WkintervalWeekStartVal: 2, // What day of the week
        selectWeekList: _.map(_.cloneDeep(selectList['week']), v => {
          return {
            value: v.value,
            label: `${this.$t(v.label)}`
          }
        }),
        intervalDayPerformVal: 1, // Every other day
        intervalDayStartVal: 1, // From the day
        WkspecificWeekVal: [], // Specific day of the week
        selectSpecificWeekList: selectList['specificWeek'],
        WkspecificDayVal: [], // Specific day of the week
        selectSpecificDayList: selectList['day'],
        monthLastDaysVal: 'L',
        monthLastWorkingDays: 'LW',
        monthLastWeeksVal: '?',
        monthLastWeeksList: _.map(_.cloneDeep(selectList['lastWeeks']), v => {
          return {
            value: v.value,
            label: `${this.$t(v.label)}`
          }
        }),
        monthTailBeforeVal: 1,
        recentlyWorkingDaysMonthVal: 1,
        WkmonthNumWeeksDayVal: 1,
        WkmonthNumWeeksWeekVal: 1,
        WkmonthNumWeeksWeekList: _.map(_.cloneDeep(selectList['week']), v => {
          return {
            value: v.value,
            label: `${this.$t(v.label)}`
          }
        })
      }
    },
    props: {
      dayVal: String,
      weekVal: String
    },
    methods: {
      // Every few weeks
      onWkintervalWeekPerform (val) {
        this.WkintervalWeekPerformVal = val
        if (this.radioDay === 'WkintervalWeek') {
          this.dayValue = `?`
          this.weekValue = `${this.WkintervalWeekStartVal}/${this.WkintervalWeekPerformVal}`
        }
      },
      // Every other day
      onIntervalDayPerform (val) {
        this.intervalDayPerformVal = val
        if (this.radioDay === 'intervalDay') {
          this.dayValue = `${this.intervalDayStartVal}/${this.intervalDayPerformVal}`
        }
      },
      // From week day
      onIntervalDayStart (val) {
        this.intervalDayStartVal = val
        if (this.radioDay === 'intervalDay') {
          this.dayValue = `${this.intervalDayStartVal}/${this.intervalDayPerformVal}`
        }
      },
      // By the end of this month
      onMonthTailBefore (val) {
        this.monthTailBeforeVal = val
        if (this.radioDay === 'monthTailBefore') {
          this.dayValue = `L-${this.monthTailBeforeVal}`
        }
      },
      // Last working day
      onRecentlyWorkingDaysMonth (val) {
        this.recentlyWorkingDaysMonthVal = val
        if (this.radioDay === 'recentlyWorkingDaysMonth') {
          this.dayValue = `${this.recentlyWorkingDaysMonthVal}W`
        }
      },
      // On the day of this month
      onWkmonthNumWeeksDay (val) {
        this.WkmonthNumWeeksDayVal = val
        this.weekValue = `${this.WkmonthNumWeeksWeekVal}#${this.WkmonthNumWeeksDayVal}`
      },

      // Reset every day
      everyDayReset () {
        this.dayValue = _.cloneDeep(this.everyDayVal)
      },
      // Reset interval week starts from *
      WkintervalWeekReset () {
        this.weekValue = `${this.WkintervalWeekStartVal}/${this.WkintervalWeekPerformVal}`
      },
      // Reset interval days from *
      intervalDayReset () {
        this.dayValue = `${this.intervalDayStartVal}/${this.intervalDayPerformVal}`
      },
      // Specific week (multiple choices)
      WkspecificWeekReset () {
        this.weekValue = this.WkspecificWeekVal.length ? this.WkspecificWeekVal.join(',') : '*'
      },
      // Specific days (multiple choices)
      specificDayReset () {
        this.dayValue = this.WkspecificDayVal.length ? this.WkspecificDayVal.join(',') : '*'
      },
      // On the last day of the month
      monthLastDaysReset () {
        this.dayValue = _.cloneDeep(this.monthLastDaysVal)
      },
      // On the last working day of the month
      monthLastWorkingDaysReset () {
        this.dayValue = _.cloneDeep(this.monthLastWorkingDays)
      },
      // At the end of the month*
      monthLastWeeksReset () {
        this.dayValue = _.cloneDeep(this.monthLastWeeksVal)
      },
      // By the end of this month
      monthTailBeforeReset () {
        this.dayValue = `L-${this.monthTailBeforeVal}`
      },
      // Last working day (Monday to Friday) to this month
      recentlyWorkingDaysMonthReset () {
        this.dayValue = `${this.recentlyWorkingDaysMonthVal}W`
      },
      // On the day of this month
      WkmonthNumReset () {
        this.weekValue = `${this.WkmonthNumWeeksWeekVal}#${this.WkmonthNumWeeksDayVal}`
      }
    },
    watch: {
      dayValue (val) {
        this.$emit('on-day-value', val)
        // console.log('dayValue=>  ' + val)
      },
      weekValue (val) {
        this.$emit('on-week-value', val)
        // console.log('weekValue=>  ' + val)
      },
      // Selected type
      radioDay (val) {
        switch (val) {
          case 'everyDay':
            this.weekValue = '?'
            this.everyDayReset()
            break
          case 'WkintervalWeek':
            this.dayValue = '?'
            this.WkintervalWeekReset()
            break
          case 'intervalDay':
            this.weekValue = '?'
            this.intervalDayReset()
            break
          case 'WkspecificWeek':
            this.dayValue = '?'
            this.WkspecificWeekReset()
            break
          case 'specificDay':
            this.weekValue = '?'
            this.specificDayReset()
            break
          case 'monthLastDays':
            this.weekValue = '?'
            this.monthLastDaysReset()
            break
          case 'monthLastWorkingDays':
            this.weekValue = '?'
            this.monthLastWorkingDaysReset()
            break
          case 'monthLastWeeks':
            this.weekValue = '1L'
            this.monthLastWeeksReset()
            break
          case 'monthTailBefore':
            this.weekValue = '?'
            this.monthTailBeforeReset()
            break
          case 'recentlyWorkingDaysMonth':
            this.weekValue = '?'
            this.recentlyWorkingDaysMonthReset()
            break
          case 'WkmonthNumWeeks':
            this.dayValue = '?'
            this.WkmonthNumReset()
            break
        }
      },
      WkintervalWeekStartVal (val) {
        if (this.radioDay === 'WkintervalWeek') {
          this.dayValue = `?`
          this.weekValue = `${val}/${this.WkintervalWeekPerformVal}`
        }
      },
      // Specific day of the week (multiple choice)
      WkspecificWeekVal (val) {
        if (this.radioDay === 'WkspecificWeek') {
          this.dayValue = `?`
          this.weekValue = val.join(',')
        }
      },
      // Specific days (multiple choices)
      WkspecificDayVal (val) {
        if (this.radioDay === 'specificDay') {
          this.weekValue = `?`
          this.dayValue = val.join(',')
        }
      },
      monthLastWeeksVal (val) {
        if (this.radioDay === 'monthLastWeeks') {
          this.weekValue = val
          this.dayValue = `?`
        }
      },
      WkmonthNumWeeksWeekVal (val) {
        if (this.radioDay === 'WkmonthNumWeeks') {
          this.dayValue = `?`
          this.weekValue = `${val}#${this.WkmonthNumWeeksDayVal}`
        }
      }
    },
    beforeCreate () {
    },
    created () {
      let $dayVal = _.cloneDeep(this.dayVal)
      let $weekVal = _.cloneDeep(this.weekVal)
      let isWeek1 = $weekVal.indexOf('/') !== -1
      let isWeek2 = $weekVal.indexOf('#') !== -1

      // Initialization
      if ($dayVal === '*' && $weekVal === '?') {
        console.log('Initialization')
        this.radioDay = 'everyDay'
        return
      }

      // week
      if (isWeek1 || isWeek2 || isWeek($weekVal)) {
        this.dayValue = `?`

        /**
         * Processing by sequence number (excluding days)
         * @param [
         * WkintervalWeek=>(/),
         * WkspecificWeek=>(TUE,WED),
         * WkmonthNumWeeks=>(#)
         * ]
         */
        let hanleWeekOne = () => {
          console.log('1/3')
          let a = isStr($weekVal, '/')
          this.WkintervalWeekStartVal = parseInt(a[0])
          this.WkintervalWeekPerformVal = parseInt(a[1])
          this.dayValue = `?`
          this.weekValue = `${this.WkintervalWeekPerformVal}/${this.WkintervalWeekStartVal}`
          this.radioDay = 'WkintervalWeek'
        }

        let hanleWeekTwo = () => {
          console.log('TUE,WED')
          this.WkspecificWeekVal = $weekVal.split(',')
          this.radioDay = 'WkspecificWeek'
        }

        let hanleWeekThree = () => {
          console.log('6#5')
          let a = isStr($weekVal, '#')
          this.WkmonthNumWeeksWeekVal = parseInt(a[0])
          this.WkmonthNumWeeksDayVal = parseInt(a[1])
          this.radioDay = 'WkmonthNumWeeks'
        }

        // Processing week
        if (isStr($weekVal, '/')) {
          hanleWeekOne()
        } else if (isStr($weekVal, '#')) {
          hanleWeekThree()
        } else if (isWeek($weekVal)) {
          hanleWeekTwo()
        }
      } else {
        this.weekValue = `?`

        /**
         * Processing by sequence number (excluding week)
         * @param [
         * everyDay=>(*),
         * intervalDay=>(1/1),
         * specificDay=>(1,2,5,3,4),
         * monthLastDays=>(L),
         * monthLastWorkingDays=>(LW),
         * monthLastWeeks=>(3L),
         * monthTailBefore=>(L-4),
         * recentlyWorkingDaysMonth=>(6W)
         * ]
         */
        const hanleDayOne = () => {
          console.log('*')
        }
        const hanleDayTwo = () => {
          console.log('1/1')
          let a = isStr($dayVal, '/')
          this.intervalDayStartVal = parseInt(a[0])
          this.intervalDayPerformVal = parseInt(a[1])
          this.radioDay = 'intervalDay'
        }
        const hanleDayThree = () => {
          console.log('1,2,5,3,4')
          this.WkspecificDayVal = $dayVal.split(',')
          this.radioDay = 'specificDay'
        }
        const hanleDayFour = () => {
          console.log('L')
          this.radioDay = 'monthLastDays'
        }
        const hanleDayFive = () => {
          console.log('LW')
          this.radioDay = 'monthLastWorkingDays'
        }
        const hanleDaySix = () => {
          console.log('3L')
          this.monthLastWeeksVal = $dayVal
          this.radioDay = 'monthLastWeeks'
        }
        const hanleDaySeven = () => {
          console.log('L-4')
          let a = isStr($dayVal, '-')
          this.monthTailBeforeVal = parseInt(a[1])
          this.radioDay = 'monthTailBefore'
        }
        const hanleDayEight = () => {
          console.log('6W')
          this.recentlyWorkingDaysMonthVal = parseInt($dayVal.slice(0, $dayVal.length - 1))
          this.radioDay = 'recentlyWorkingDaysMonth'
        }
        if ($dayVal === '*') {
          hanleDayOne()
        } else if (isStr($dayVal, '/')) {
          hanleDayTwo()
        } else if ($dayVal === 'L') {
          hanleDayFour()
        } else if ($dayVal === 'LW') {
          hanleDayFive()
        } else if ($dayVal.charAt($dayVal.length - 1) === 'L') {
          hanleDaySix()
        } else if (isStr($dayVal, '-')) {
          hanleDaySeven()
        } else if ($dayVal.charAt($dayVal.length - 1) === 'W') {
          hanleDayEight()
        } else {
          hanleDayThree()
        }
      }
    },
    beforeMount () {
    },
    mounted () {

    },
    beforeUpdate () {
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {},
    components: { mInputNumber }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .day-model {
    .ans-radio-group-vertical {
      .ans-radio-wrapper {
        margin: 5px 0;
        display: inline-block
      }
    }
  }
</style>
