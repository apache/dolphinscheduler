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
  <div class="month-model">
    <div class="v-crontab-from-model">
      <x-radio-group v-model="radioMonth" vertical>
        <div class="list-box">
          <x-radio label="everyMonth">
            <span class="text">{{$t('每一月')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="intervalMonth">
            <span class="text">{{$t('每隔')}}</span>
            <m-input-number :min="0" :max="12" :props-value="parseInt(intervalPerformVal)" @on-number="onIntervalPerform"></m-input-number>
            <span class="text">{{$t('月执行 从')}}</span>
            <m-input-number :min="0" :max="12" :props-value="parseInt(intervalStartVal)" @on-number="onIntervalStart"></m-input-number>
            <span class="text">{{$t('月开始')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="specificlMonth">
            <span class="text">{{$t('具体月数(可多选)')}}</span>
            <x-select multiple :placeholder="$t('请选择具体月数')" v-model="specificMonthVal" @on-change="onspecificlMonths">
              <x-option
                      v-for="item in selectMonthList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="cycleMonth">
            <span class="text">{{$t('周期从')}}</span>
            <m-input-number :min="0" :max="12" :props-value="parseInt(cycleStartVal)" @on-number="onCycleStart"></m-input-number>
            <span class="text">{{$t('到')}}</span>
            <m-input-number :min="0" :max="12" :props-value="parseInt(cycleEndVal)" @on-number="onCycleEnd"></m-input-number>
            <span class="text">{{$t('月')}}</span>
          </x-radio>
        </div>
      </x-radio-group>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '../_source/i18n'
  import { selectList, isStr } from '../util/index'
  import mInputNumber from '../_source/input-number'

  export default {
    name: 'month',
    mixins: [i18n],
    data () {
      return {
        monthValue: '*',
        radioMonth: 'everyMonth',
        selectMonthList: selectList['12'],
        intervalPerformVal: 5,
        intervalStartVal: 3,
        specificMonthVal: [],
        cycleStartVal: 1,
        cycleEndVal: 1
      }
    },
    props: {
      monthVal: String,
      value: {
        type: String,
        default: '*'
      }
    },
    model: {
      prop: 'value',
      event: 'monthValueEvent'
    },
    methods: {
      // Interval execution time（1）
      onIntervalPerform (val) {
        this.intervalPerformVal = val
        if (this.radioMonth === 'intervalMonth') {
          this.monthValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // Interval start time（2）
      onIntervalStart (val) {
        this.intervalStartVal = val
        if (this.radioMonth === 'intervalMonth') {
          this.monthValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // Specific months
      onspecificlMonths (arr) {
      },
      // Cycle start value
      onCycleStart (val) {
        this.cycleStartVal = val
        if (this.radioMonth === 'cycleMonth') {
          this.monthValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // Cycle end value
      onCycleEnd (val) {
        this.cycleEndVal = val
        if (this.radioMonth === 'cycleMonth') {
          this.monthValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // Reset every month
      everyReset () {
        this.monthValue = '*'
      },
      // Reset every month
      intervalReset () {
        this.monthValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
      },
      // Reset specific months
      specificReset () {
        if (this.specificMonthVal.length) {
          this.monthValue = this.specificMonthVal.join(',')
        } else {
          this.monthValue = '*'
        }
      },
      // Months of reset cycle
      cycleReset () {
        this.monthValue = `${this.cycleStartVal}-${this.cycleEndVal}`
      },
      /**
       * Parse parameter value
       */
      analyticalValue () {
        return new Promise((resolve, reject) => {
          let $monthVal = _.cloneDeep(this.value)
          // Interval month
          let $interval = isStr($monthVal, '/')
          // Specific months
          let $specific = isStr($monthVal, ',')
          // Cycle month
          let $cycle = isStr($monthVal, '-')

          // Every month
          if ($monthVal === '*') {
            this.radioMonth = 'everyMonth'
            this.monthValue = '*'
            return
          }

          // Positive integer (month)
          if ($monthVal.length === 1 && _.isInteger(parseInt($monthVal)) ||
            $monthVal.length === 2 && _.isInteger(parseInt($monthVal))
          ) {
            this.radioMonth = 'specificlMonth'
            this.specificMonthVal = [$monthVal]
            return
          }

          // Interval month
          if ($interval) {
            this.radioMonth = 'intervalMonth'
            this.intervalStartVal = parseInt($interval[0])
            this.intervalPerformVal = parseInt($interval[1])
            this.monthValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
            return
          }

          // Specific months
          if ($specific) {
            this.radioMonth = 'specificlMonth'
            this.specificMonthVal = $specific
            return
          }

          // Cycle month
          if ($cycle) {
            this.radioMonth = 'cycleMonth'
            this.cycleStartVal = parseInt($cycle[0])
            this.cycleEndVal = parseInt($cycle[1])
            this.monthValue = `${this.cycleStartVal}/${this.cycleEndVal}`
            return
          }
          resolve()
        })
      }
    },
    watch: {
      // Derived value
      monthValue (val) {
        this.$emit('monthValueEvent', val)
      },
      // Selected type
      radioMonth (val) {
        switch (val) {
          case 'everyMonth':
            this.everyReset()
            break
          case 'intervalMonth':
            this.intervalReset()
            break
          case 'specificlMonth':
            this.specificReset()
            break
          case 'cycleMonth':
            this.cycleReset()
            break
        }
      },
      // Specific months
      specificMonthVal (arr) {
        this.monthValue = arr.join(',')
      }
    },
    beforeCreate () {
    },
    created () {
      this.analyticalValue().then(() => {
        console.log('数据结构解析成功！')
      })
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
  .month-model {
    .ans-radio-group-vertical {
      .ans-radio-wrapper {
        margin: 5px 0;
        display: inline-block
      }
    }
  }
</style>
