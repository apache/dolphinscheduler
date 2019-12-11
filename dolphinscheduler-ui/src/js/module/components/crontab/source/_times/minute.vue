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
  <div class="minute-model">
    <div class="v-crontab-from-model">
      <x-radio-group v-model="radioMinute" vertical>
        <div class="list-box">
          <x-radio label="everyMinute">
            <span class="text">{{$t('每一分钟')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="intervalMinute">
            <span class="text">{{$t('每隔')}}</span>
            <m-input-number :min="0" :max="59" :props-value="parseInt(intervalPerformVal)" @on-number="onIntervalPerform"></m-input-number>
            <span class="text">{{$t('分执行 从')}}</span>
            <m-input-number :min="0" :max="59" :props-value="parseInt(intervalStartVal)" @on-number="onIntervalStart"></m-input-number>
            <span class="text">{{$t('分开始')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="specificMinute">
            <span class="text">{{$t('具体分钟数(可多选)')}}</span>
            <x-select multiple :placeholder="$t('请选择具体分钟数')" v-model="specificMinutesVal" @on-change="onspecificMinutes">
              <x-option
                      v-for="item in selectMinuteList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="cycleMinute">
            <span class="text">{{$t('周期从')}}</span>
            <m-input-number :min="0" :max="59" :props-value="parseInt(cycleStartVal)" @on-number="onCycleStart"></m-input-number>
            <span class="text">{{$t('到')}}</span>
            <m-input-number :min="0" :max="59" :props-value="parseInt(cycleEndVal)" @on-number="onCycleEnd"></m-input-number>
            <span class="text">{{$t('分')}}</span>
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
    name: 'minute',
    mixins: [i18n],
    data () {
      return {
        minuteValue: '*',
        radioMinute: 'everyMinute',
        selectMinuteList: selectList['60'],
        intervalPerformVal: 5,
        intervalStartVal: 3,
        specificMinutesVal: [],
        cycleStartVal: 1,
        cycleEndVal: 1
      }
    },
    props: {
      minuteVal: String,
      value: {
        type: String,
        default: '*'
      }
    },
    model: {
      prop: 'value',
      event: 'minuteValueEvent'
    },
    methods: {
      // Interval execution time（1）
      onIntervalPerform (val) {
        console.log(val)
        this.intervalPerformVal = val
        if (this.radioMinute === 'intervalMinute') {
          this.minuteValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // Interval start time（2）
      onIntervalStart (val) {
        this.intervalStartVal = val
        if (this.radioMinute === 'intervalMinute') {
          this.minuteValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // Specific points
      onspecificMinutes (arr) {
      },
      // Cycle start value
      onCycleStart (val) {
        this.cycleStartVal = val
        if (this.radioMinute === 'cycleMinute') {
          this.minuteValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // Cycle end value
      onCycleEnd (val) {
        this.cycleEndVal = val
        if (this.radioMinute === 'cycleMinute') {
          this.minuteValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // Reset every point
      everyReset () {
        this.minuteValue = '*'
      },
      // Reset interval minute
      intervalReset () {
        this.minuteValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
      },
      // Reset specific minutes
      specificReset () {
        if (this.specificMinutesVal.length) {
          this.minuteValue = this.specificMinutesVal.join(',')
        } else {
          this.minuteValue = '*'
        }
      },
      // Reset cycle minutes
      cycleReset () {
        this.minuteValue = `${this.cycleStartVal}-${this.cycleEndVal}`
      },
      /**
       * Parse parameter value
       */
      analyticalValue () {
        return new Promise((resolve, reject) => {
          let $minuteVal = _.cloneDeep(this.value)
          // Interval score
          let $interval = isStr($minuteVal, '/')
          // Specific points
          let $specific = isStr($minuteVal, ',')
          // Periodic Division
          let $cycle = isStr($minuteVal, '-')

          // Every point
          if ($minuteVal === '*') {
            this.radioMinute = 'everyMinute'
            this.minuteValue = '*'
            return
          }

          // Positive integer (min)
          if ($minuteVal.length === 1 && _.isInteger(parseInt($minuteVal)) ||
            $minuteVal.length === 2 && _.isInteger(parseInt($minuteVal))
          ) {
            this.radioMinute = 'specificMinute'
            this.specificMinutesVal = [$minuteVal]
            return
          }

          // nterval score
          if ($interval) {
            this.radioMinute = 'intervalMinute'
            this.intervalStartVal = parseInt($interval[0])
            this.intervalPerformVal = parseInt($interval[1])
            this.minuteValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
            return
          }

          // Specific minutes
          if ($specific) {
            this.radioMinute = 'specificMinute'
            this.specificMinutesVal = $specific
            return
          }

          // Periodic Division
          if ($cycle) {
            this.radioMinute = 'cycleMinute'
            this.cycleStartVal = parseInt($cycle[0])
            this.cycleEndVal = parseInt($cycle[1])
            this.minuteValue = `${this.cycleStartVal}/${this.cycleEndVal}`
            return
          }
          resolve()
        })
      }
    },
    watch: {
      // Derived value
      minuteValue (val) {
        this.$emit('minuteValueEvent', val)
      },
      // Selected type
      radioMinute (val) {
        switch (val) {
          case 'everyMinute':
            this.everyReset()
            break
          case 'intervalMinute':
            this.intervalReset()
            break
          case 'specificMinute':
            this.specificReset()
            break
          case 'cycleMinute':
            this.cycleReset()
            break
        }
      },
      // pecific minutes
      specificMinutesVal (arr) {
        this.minuteValue = arr.join(',')
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
  .minute-model {
    .ans-radio-group-vertical {
      .ans-radio-wrapper {
        margin: 5px 0;
        display: inline-block
      }
    }
  }
</style>
