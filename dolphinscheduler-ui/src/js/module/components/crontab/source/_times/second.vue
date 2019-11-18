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
  <div class="second-model">
    <div class="v-crontab-from-model">
      <x-radio-group v-model="radioSecond" vertical>
        <div class="list-box">
          <x-radio label="everySecond">
            <span class="text">{{$t('每一秒钟')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="intervalSecond">
            <span class="text">{{$t('每隔')}}</span>
            <m-input-number :min="0" :max="59" :props-value="parseInt(intervalPerformVal)" @on-number="onIntervalPerform"></m-input-number>
            <span class="text">{{$t('秒执行 从')}}</span>
            <m-input-number :min="0" :max="59" :props-value="parseInt(intervalStartVal)" @on-number="onIntervalStart"></m-input-number>
            <span class="text">{{$t('秒开始')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="specificSecond">
            <span class="text">{{$t('具体秒数(可多选)')}}</span>
            <x-select multiple :placeholder="$t('请选择具体秒数')" v-model="specificSecondsVal" @on-change="onSpecificSeconds">
              <x-option
                      v-for="item in selectSecondList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="cycleSecond">
            <span class="text">{{$t('周期从')}}</span>
            <m-input-number :min="0" :max="59" :props-value="parseInt(cycleStartVal)" @on-number="onCycleStart"></m-input-number>
            <span class="text">{{$t('到')}}</span>
            <m-input-number :min="0" :max="59" :props-value="parseInt(cycleEndVal)" @on-number="onCycleEnd"></m-input-number>
            <span class="text">{{$t('秒')}}</span>
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
    name: 'second',
    mixins: [i18n],
    data () {
      return {
        secondValue: '*',
        radioSecond: 'everySecond',
        selectSecondList: selectList['60'],
        intervalPerformVal: 5,
        intervalStartVal: 3,
        specificSecondsVal: [],
        cycleStartVal: 1,
        cycleEndVal: 1
      }
    },
    props: {
      secondVal: String,
      value: {
        type: String,
        default: '*'
      }
    },
    model: {
      prop: 'value',
      event: 'secondValueEvent'
    },
    methods: {
      // Interval execution time（1）
      onIntervalPerform (val) {
        console.log(val)
        console.log('++')
        this.intervalPerformVal = val
        if (this.radioSecond === 'intervalSecond') {
          this.secondValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // Interval start time（2）
      onIntervalStart (val) {
        this.intervalStartVal = val
        if (this.radioSecond === 'intervalSecond') {
          this.secondValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // Specific seconds
      onSpecificSeconds (arr) {
      },
      // Cycle start value
      onCycleStart (val) {
        this.cycleStartVal = val
        if (this.radioSecond === 'cycleSecond') {
          this.secondValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // Cycle end value
      onCycleEnd (val) {
        this.cycleEndVal = val
        if (this.radioSecond === 'cycleSecond') {
          this.secondValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // Reset every second
      everyReset () {
        this.secondValue = '*'
      },
      // Reset interval seconds
      intervalReset () {
        this.secondValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
      },
      // Reset specific seconds
      specificReset () {
        if (this.specificSecondsVal.length) {
          this.secondValue = this.specificSecondsVal.join(',')
        } else {
          this.secondValue = '*'
        }
      },
      // Reset cycle seconds
      cycleReset () {
        this.secondValue = `${this.cycleStartVal}-${this.cycleEndVal}`
      },
      /**
       * Parse parameter value
       */
      analyticalValue () {
        return new Promise((resolve, reject) => {
          let $secondVal = _.cloneDeep(this.value)
          // Interval seconds
          let $interval = isStr($secondVal, '/')
          // Specific seconds
          let $specific = isStr($secondVal, ',')
          // Cycle second
          let $cycle = isStr($secondVal, '-')

          // Every second
          if ($secondVal === '*') {
            this.radioSecond = 'everySecond'
            this.secondValue = '*'
            return
          }

          // Positive integer (seconds)
          if ($secondVal.length === 1 && _.isInteger(parseInt($secondVal)) ||
            $secondVal.length === 2 && _.isInteger(parseInt($secondVal))
          ) {
            this.radioSecond = 'specificSecond'
            this.specificSecondsVal = [$secondVal]
            return
          }

          // Interval seconds
          if ($interval) {
            this.radioSecond = 'intervalSecond'
            this.intervalStartVal = parseInt($interval[0])
            this.intervalPerformVal = parseInt($interval[1])
            this.secondValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
            return
          }

          // Specific seconds
          if ($specific) {
            this.radioSecond = 'specificSecond'
            this.specificSecondsVal = $specific
            return
          }

          // Cycle second
          if ($cycle) {
            this.radioSecond = 'cycleSecond'
            this.cycleStartVal = parseInt($cycle[0])
            this.cycleEndVal = parseInt($cycle[1])
            this.secondValue = `${this.cycleStartVal}-${this.cycleEndVal}`
            return
          }
          resolve()
        })
      }
    },
    watch: {
      // Value change reparse structure
      value () {
        this.analyticalValue().then(() => {
          console.log('数据结构解析成功！')
        })
      },
      // Derived value
      secondValue (val) {
        this.$emit('secondValueEvent', val)
      },
      // Selected type
      radioSecond (val) {
        switch (val) {
          case 'everySecond':
            this.everyReset()
            break
          case 'intervalSecond':
            this.intervalReset()
            break
          case 'specificSecond':
            this.specificReset()
            break
          case 'cycleSecond':
            this.cycleReset()
            break
        }
      },
      // Specific seconds
      specificSecondsVal (arr) {
        this.secondValue = arr.join(',')
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
  .second-model {
    .ans-radio-group-vertical {
      .ans-radio-wrapper {
        margin: 5px 0;
        display: inline-block
      }
    }
  }
</style>
