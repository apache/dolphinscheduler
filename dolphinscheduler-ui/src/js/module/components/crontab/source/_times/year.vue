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
  <div class="year-model">
    <div class="v-crontab-from-model">
      <x-radio-group v-model="radioYear" vertical>
        <div class="list-box">
          <x-radio label="everyYear">
            <span class="text">{{$t('每一年')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="intervalYear">
            <span class="text">{{$t('每隔')}}</span>
            <m-input-number :min="2019" :max="2030" :props-value="parseInt(intervalPerformVal)" @on-number="onIntervalPerform"></m-input-number>
            <span class="text">{{$t('年执行 从')}}</span>
            <m-input-number :min="2019" :max="2030" :props-value="parseInt(intervalStartVal)" @on-number="onIntervalStart"></m-input-number>
            <span class="text">{{$t('年开始')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="specificYear">
            <span class="text">{{$t('具体年数(可多选)')}}</span>
            <x-select multiple :placeholder="$t('请选择具体年数')" v-model="specificYearVal" @on-change="onspecificYears">
              <x-option
                      v-for="item in selectYearList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="cycleYear">
            <span class="text">{{$t('周期从')}}</span>
            <m-input-number :min="2019" :max="2030" :props-value="parseInt(cycleStartVal)" @on-number="onCycleStart"></m-input-number>
            <span class="text">{{$t('到')}}</span>
            <m-input-number :min="2019" :max="2030" :props-value="parseInt(cycleEndVal)" @on-number="onCycleEnd"></m-input-number>
            <span class="text">{{$t('年')}}</span>
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
    name: 'year',
    mixins: [i18n],
    data () {
      return {
        yearValue: '*',
        radioYear: 'everyYear',
        selectYearList: selectList['year'],
        intervalPerformVal: 1,
        intervalStartVal: 2019,
        specificYearVal: [],
        cycleStartVal: 2019,
        cycleEndVal: 2019
      }
    },
    props: {
      yearVal: String,
      value: {
        type: String,
        default: '*'
      }
    },
    model: {
      prop: 'value',
      event: 'yearValueEvent'
    },
    methods: {
      // Interval execution time（1）
      onIntervalPerform (val) {
        console.log(val)
        this.intervalPerformVal = val
        if (this.radioYear === 'intervalYear') {
          this.yearValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // Interval start time（2）
      onIntervalStart (val) {
        this.intervalStartVal = val
        if (this.radioYear === 'intervalYear') {
          this.yearValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // Specific year
      onspecificYears (arr) {
      },
      // Cycle start value
      onCycleStart (val) {
        this.cycleStartVal = val
        if (this.radioYear === 'cycleYear') {
          this.yearValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // Cycle end value
      onCycleEnd (val) {
        this.cycleEndVal = val
        if (this.radioYear === 'cycleYear') {
          this.yearValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // Reset every year
      everyReset () {
        this.yearValue = '*'
      },
      // Reset every other year
      intervalReset () {
        this.yearValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
      },
      // Reset specific years
      specificReset () {
        if (this.specificYearVal.length) {
          this.yearValue = this.specificYearVal.join(',')
        } else {
          this.yearValue = '*'
        }
      },
      // Reset cycle years
      cycleReset () {
        this.yearValue = `${this.cycleStartVal}-${this.cycleEndVal}`
      },
      /**
       * Parse parameter value
       */
      analyticalValue () {
        return new Promise((resolve, reject) => {
          let $yearVal = _.cloneDeep(this.value)
          // Interval year
          let $interval = isStr($yearVal, '/')
          // Specific year
          let $specific = isStr($yearVal, ',')
          // Cycle year
          let $cycle = isStr($yearVal, '-')

          // Every year
          if ($yearVal === '*') {
            this.radioYear = 'everyYear'
            this.yearValue = '*'
            return
          }

          // Positive integer (year)
          if ($yearVal.length === 4 && _.isInteger(parseInt($yearVal))) {
            this.radioYear = 'specificYear'
            this.specificYearVal = [$yearVal]
            return
          }

          // Interval year
          if ($interval) {
            this.radioYear = 'intervalYear'
            this.intervalStartVal = parseInt($interval[0])
            this.intervalPerformVal = parseInt($interval[1])
            this.yearValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
            return
          }

          // Specific years
          if ($specific) {
            this.radioYear = 'specificYear'
            this.specificYearVal = $specific
            return
          }

          // Cycle year
          if ($cycle) {
            this.radioYear = 'cycleYear'
            this.cycleStartVal = parseInt($cycle[0])
            this.cycleEndVal = parseInt($cycle[1])
            this.yearValue = `${this.cycleStartVal}/${this.cycleEndVal}`
            return
          }
          resolve()
        })
      }
    },
    watch: {
      // Derived value
      yearValue (val) {
        this.$emit('yearValueEvent', val)
      },
      // Selected type
      radioYear (val) {
        switch (val) {
          case 'everyYear':
            this.everyReset()
            break
          case 'intervalYear':
            this.intervalReset()
            break
          case 'specificYear':
            this.specificReset()
            break
          case 'cycleYear':
            this.cycleReset()
            break
        }
      },
      // Specific years
      specificYearVal (arr) {
        this.yearValue = arr.join(',')
      }
    },
    beforeCreate () {
    },
    created () {
      this.analyticalValue().then(() => {
        console.log('Data structure parsing succeeded!')
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
  .year-model {
    .ans-radio-group-vertical {
      .ans-radio-wrapper {
        margin: 5px 0;
        display: inline-block
      }
    }
  }
</style>
