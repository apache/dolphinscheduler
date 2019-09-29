<template>
  <div class="hour-model">
    <div class="v-crontab-from-model">
      <x-radio-group v-model="radioHour" vertical>
        <div class="list-box">
          <x-radio label="everyHour">
            <span class="text">{{$t('每一小时')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="intervalHour">
            <span class="text">{{$t('每隔')}}</span>
            <m-input-number :min="0" :max="23" :props-value="parseInt(intervalPerformVal)" @on-number="onIntervalPerform"></m-input-number>
            <span class="text">{{$t('小时执行 从')}}</span>
            <m-input-number :min="0" :max="23" :props-value="parseInt(intervalStartVal)" @on-number="onIntervalStart"></m-input-number>
            <span class="text">{{$t('小时开始')}}</span>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="specificHour">
            <span class="text">{{$t('具体小时数(可多选)')}}</span>
            <x-select multiple :placeholder="$t('请选择具体小时数')" v-model="specificHoursVal" @on-change="onspecificHours">
              <x-option
                      v-for="item in selectHourList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </x-radio>
        </div>
        <div class="list-box">
          <x-radio label="cycleHour">
            <span class="text">{{$t('周期从')}}</span>
            <m-input-number :min="0" :max="23" :props-value="parseInt(cycleStartVal)" @on-number="onCycleStart"></m-input-number>
            <span class="text">{{$t('到')}}</span>
            <m-input-number :min="0" :max="23" :props-value="parseInt(cycleEndVal)" @on-number="onCycleEnd"></m-input-number>
            <span class="text">{{$t('小时')}}</span>
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
    name: 'hour',
    mixins: [i18n],
    data () {
      return {
        hourValue: '*',
        radioHour: 'everyHour',
        selectHourList: selectList['24'],
        intervalPerformVal: 5,
        intervalStartVal: 3,
        specificHoursVal: [],
        cycleStartVal: 1,
        cycleEndVal: 1
      }
    },
    props: {
      hourVal: String,
      value: {
        type: String,
        default: '*'
      }
    },
    model: {
      prop: 'value',
      event: 'hourValueEvent'
    },
    methods: {
      // 间隔执行时间（1）
      onIntervalPerform (val) {
        this.intervalPerformVal = val
        if (this.radioHour === 'intervalHour') {
          this.hourValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // 间隔开始时间（2）
      onIntervalStart (val) {
        this.intervalStartVal = val
        if (this.radioHour === 'intervalHour') {
          this.hourValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // 具体小时
      onspecificHours (arr) {
      },
      // 周期开始值
      onCycleStart (val) {
        this.cycleStartVal = val
        if (this.radioHour === 'cycleHour') {
          this.hourValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // 周期结束值
      onCycleEnd (val) {
        this.cycleEndVal = val
        if (this.radioHour === 'cycleHour') {
          this.hourValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // 重置每一小时
      everyReset () {
        this.hourValue = '*'
      },
      // 重置间隔小时
      intervalReset () {
        this.hourValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
      },
      // 重置具体小时数
      specificReset () {
        if (this.specificHoursVal.length) {
          this.hourValue = this.specificHoursVal.join(',')
        } else {
          this.hourValue = '*'
        }
      },
      // 重置周期小时数
      cycleReset () {
        this.hourValue = `${this.cycleStartVal}-${this.cycleEndVal}`
      },
      /**
       * 解析参数值
       */
      analyticalValue () {
        return new Promise((resolve, reject) => {
          let $hourVal = _.cloneDeep(this.value)
          // 间隔小时
          let $interval = isStr($hourVal, '/')
          // 具体小时
          let $specific = isStr($hourVal, ',')
          // 周期小时
          let $cycle = isStr($hourVal, '-')

          // 每一小时
          if ($hourVal === '*') {
            this.radioHour = 'everyHour'
            this.hourValue = '*'
            return
          }

          // 正整数(时)
          if ($hourVal.length === 1 && _.isInteger(parseInt($hourVal)) ||
            $hourVal.length === 2 && _.isInteger(parseInt($hourVal))
          ) {
            this.radioHour = 'specificHour'
            this.specificHoursVal = [$hourVal]
            return
          }

          // 间隔小时
          if ($interval) {
            this.radioHour = 'intervalHour'
            this.intervalStartVal = parseInt($interval[0])
            this.intervalPerformVal = parseInt($interval[1])
            this.hourValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
            return
          }

          // 具体小时数
          if ($specific) {
            this.radioHour = 'specificHour'
            this.specificHoursVal = $specific
            return
          }

          // 周期小时
          if ($cycle) {
            this.radioHour = 'cycleHour'
            this.cycleStartVal = parseInt($cycle[0])
            this.cycleEndVal = parseInt($cycle[1])
            this.hourValue = `${this.cycleStartVal}/${this.cycleEndVal}`
            return
          }
          resolve()
        })
      }
    },
    watch: {
      // 导出值
      hourValue (val) {
        this.$emit('hourValueEvent', val)
      },
      // 选中类型
      radioHour (val) {
        switch (val) {
          case 'everyHour':
            this.everyReset()
            break
          case 'intervalHour':
            this.intervalReset()
            break
          case 'specificHour':
            this.specificReset()
            break
          case 'cycleHour':
            this.cycleReset()
            break
        }
      },
      // 具体小时数
      specificHoursVal (arr) {
        this.hourValue = arr.join(',')
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
</style>
