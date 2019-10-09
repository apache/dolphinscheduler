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
      // 间隔执行时间（1）
      onIntervalPerform (val) {
        console.log(val)
        this.intervalPerformVal = val
        if (this.radioYear === 'intervalYear') {
          this.yearValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // 间隔开始时间（2）
      onIntervalStart (val) {
        this.intervalStartVal = val
        if (this.radioYear === 'intervalYear') {
          this.yearValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // 具体年
      onspecificYears (arr) {
      },
      // 周期开始值
      onCycleStart (val) {
        this.cycleStartVal = val
        if (this.radioYear === 'cycleYear') {
          this.yearValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // 周期结束值
      onCycleEnd (val) {
        this.cycleEndVal = val
        if (this.radioYear === 'cycleYear') {
          this.yearValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // 重置每一年
      everyReset () {
        this.yearValue = '*'
      },
      // 重置间隔年
      intervalReset () {
        this.yearValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
      },
      // 重置具体年数
      specificReset () {
        if (this.specificYearVal.length) {
          this.yearValue = this.specificYearVal.join(',')
        } else {
          this.yearValue = '*'
        }
      },
      // 重置周期年数
      cycleReset () {
        this.yearValue = `${this.cycleStartVal}-${this.cycleEndVal}`
      },
      /**
       * 解析参数值
       */
      analyticalValue () {
        return new Promise((resolve, reject) => {
          let $yearVal = _.cloneDeep(this.value)
          // 间隔年
          let $interval = isStr($yearVal, '/')
          // 具体年
          let $specific = isStr($yearVal, ',')
          // 周期年
          let $cycle = isStr($yearVal, '-')

          // 每一年
          if ($yearVal === '*') {
            this.radioYear = 'everyYear'
            this.yearValue = '*'
            return
          }

          // 正整数(年)
          if ($yearVal.length === 4 && _.isInteger(parseInt($yearVal))) {
            this.radioYear = 'specificYear'
            this.specificYearVal = [$yearVal]
            return
          }

          // 间隔年
          if ($interval) {
            this.radioYear = 'intervalYear'
            this.intervalStartVal = parseInt($interval[0])
            this.intervalPerformVal = parseInt($interval[1])
            this.yearValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
            return
          }

          // 具体年数
          if ($specific) {
            this.radioYear = 'specificYear'
            this.specificYearVal = $specific
            return
          }

          // 周期年
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
      // 导出值
      yearValue (val) {
        this.$emit('yearValueEvent', val)
      },
      // 选中类型
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
      // 具体年数
      specificYearVal (arr) {
        this.yearValue = arr.join(',')
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
  .year-model {

  }


</style>
