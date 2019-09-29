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
      // 间隔执行时间（1）
      onIntervalPerform (val) {
        console.log(val)
        console.log('++')
        this.intervalPerformVal = val
        if (this.radioSecond === 'intervalSecond') {
          this.secondValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // 间隔开始时间（2）
      onIntervalStart (val) {
        this.intervalStartVal = val
        if (this.radioSecond === 'intervalSecond') {
          this.secondValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
        }
      },
      // 具体秒
      onSpecificSeconds (arr) {
      },
      // 周期开始值
      onCycleStart (val) {
        this.cycleStartVal = val
        if (this.radioSecond === 'cycleSecond') {
          this.secondValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // 周期结束值
      onCycleEnd (val) {
        this.cycleEndVal = val
        if (this.radioSecond === 'cycleSecond') {
          this.secondValue = `${this.cycleStartVal}-${this.cycleEndVal}`
        }
      },
      // 重置每一秒
      everyReset () {
        this.secondValue = '*'
      },
      // 重置间隔秒
      intervalReset () {
        this.secondValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
      },
      // 重置具体秒数
      specificReset () {
        if (this.specificSecondsVal.length) {
          this.secondValue = this.specificSecondsVal.join(',')
        } else {
          this.secondValue = '*'
        }
      },
      // 重置周期秒数
      cycleReset () {
        this.secondValue = `${this.cycleStartVal}-${this.cycleEndVal}`
      },
      /**
       * 解析参数值
       */
      analyticalValue () {
        return new Promise((resolve, reject) => {
          let $secondVal = _.cloneDeep(this.value)
          // 间隔秒
          let $interval = isStr($secondVal, '/')
          // 具体秒
          let $specific = isStr($secondVal, ',')
          // 周期秒
          let $cycle = isStr($secondVal, '-')

          // 每一秒
          if ($secondVal === '*') {
            this.radioSecond = 'everySecond'
            this.secondValue = '*'
            return
          }

          // 正整数(秒)
          if ($secondVal.length === 1 && _.isInteger(parseInt($secondVal)) ||
            $secondVal.length === 2 && _.isInteger(parseInt($secondVal))
          ) {
            this.radioSecond = 'specificSecond'
            this.specificSecondsVal = [$secondVal]
            return
          }

          // 间隔秒
          if ($interval) {
            this.radioSecond = 'intervalSecond'
            this.intervalStartVal = parseInt($interval[0])
            this.intervalPerformVal = parseInt($interval[1])
            this.secondValue = `${this.intervalStartVal}/${this.intervalPerformVal}`
            return
          }

          // 具体秒数
          if ($specific) {
            this.radioSecond = 'specificSecond'
            this.specificSecondsVal = $specific
            return
          }

          // 周期秒
          if ($cycle) {
            this.radioSecond = 'cycleSecond'
            this.cycleStartVal = parseInt($cycle[0])
            this.cycleEndVal = parseInt($cycle[1])
            this.secondValue = `${this.cycleStartVal}/${this.cycleEndVal}`
            return
          }
          resolve()
        })
      }
    },
    watch: {
      // value变化重新解析结构
      value () {
        this.analyticalValue().then(() => {
          console.log('数据结构解析成功！')
        })
      },
      // 导出值
      secondValue (val) {
        this.$emit('secondValueEvent', val)
      },
      // 选中类型
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
      // 具体秒数
      specificSecondsVal (arr) {
        this.secondValue = arr.join(',')
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
  .second-model {

  }


</style>
