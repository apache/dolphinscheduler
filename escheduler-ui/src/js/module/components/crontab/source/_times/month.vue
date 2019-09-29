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
      // 间隔执行时间（1）
      onIntervalPerform (val) {
        this.intervalPerformVal = val
        if (this.radioMonth === 'intervalMonth') {
          this.monthValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // 间隔开始时间（2）
      onIntervalStart (val) {
        this.intervalStartVal = val
        if (this.radioMonth === 'intervalMonth') {
          this.monthValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // 具体月
      onspecificlMonths (arr) {
      },
      // 周期开始值
      onCycleStart (val) {
        this.cycleStartVal = val
        if (this.radioMonth === 'cycleMonth') {
          this.monthValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // 周期结束值
      onCycleEnd (val) {
        this.cycleEndVal = val
        if (this.radioMonth === 'cycleMonth') {
          this.monthValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // 重置每一月
      everyReset () {
        this.monthValue = '*'
      },
      // 重置间隔月
      intervalReset () {
        this.monthValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
      },
      // 重置具体月数
      specificReset () {
        if (this.specificMonthVal.length) {
          this.monthValue = this.specificMonthVal.join(',')
        } else {
          this.monthValue = '*'
        }
      },
      // 重置周期月数
      cycleReset () {
        this.monthValue = `${this.cycleStartVal}-${this.cycleEndVal}`
      },
      /**
       * 解析参数值
       */
      analyticalValue () {
        return new Promise((resolve, reject) => {
          let $monthVal = _.cloneDeep(this.value)
          // 间隔月
          let $interval = isStr($monthVal, '/')
          // 具体月
          let $specific = isStr($monthVal, ',')
          // 周期月
          let $cycle = isStr($monthVal, '-')

          // 每一月
          if ($monthVal === '*') {
            this.radioMonth = 'everyMonth'
            this.monthValue = '*'
            return
          }

          // 正整数(月)
          if ($monthVal.length === 1 && _.isInteger(parseInt($monthVal)) ||
            $monthVal.length === 2 && _.isInteger(parseInt($monthVal))
          ) {
            this.radioMonth = 'specificlMonth'
            this.specificMonthVal = [$monthVal]
            return
          }

          // 间隔月
          if ($interval) {
            this.radioMonth = 'intervalMonth'
            this.intervalStartVal = parseInt($interval[0])
            this.intervalPerformVal = parseInt($interval[1])
            this.monthValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
            return
          }

          // 具体月数
          if ($specific) {
            this.radioMonth = 'specificlMonth'
            this.specificMonthVal = $specific
            return
          }

          // 周期月
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
      // 导出值
      monthValue (val) {
        this.$emit('monthValueEvent', val)
      },
      // 选中类型
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
      // 具体月数
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

  }


</style>
