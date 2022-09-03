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

import _ from 'lodash'
import { defineComponent, onMounted, PropType, ref, toRefs, watch } from 'vue'
import { NInputNumber, NRadio, NRadioGroup, NSelect } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { ICrontabI18n } from '../types'
import { isStr, specificList } from '../common'
import styles from '../index.module.scss'

const props = {
  timeMin: {
    type: Number as PropType<number>,
    default: 0
  },
  timeMax: {
    type: Number as PropType<number>,
    default: 60
  },
  intervalPerform: {
    type: Number as PropType<number>,
    default: 5
  },
  intervalStart: {
    type: Number as PropType<number>,
    default: 3
  },
  cycleStart: {
    type: Number as PropType<number>,
    default: 1
  },
  cycleEnd: {
    type: Number as PropType<number>,
    default: 1
  },
  timeSpecial: {
    type: Number as PropType<number | string>,
    default: 60
  },
  timeValue: {
    type: String as PropType<string>,
    default: '*'
  },
  timeI18n: {
    type: Object as PropType<ICrontabI18n>,
    require: true
  }
}

export default defineComponent({
  name: 'CrontabTime',
  props,
  emits: ['update:timeValue'],
  setup(props, ctx) {
    const options = Array.from({ length: 60 }, (x, i) => ({
      label: i.toString(),
      value: i
    }))

    const timeRef = ref()
    const radioRef = ref()
    const intervalStartRef = ref(props.intervalStart)
    const intervalPerformRef = ref(props.intervalPerform)
    const specificTimesRef = ref<Array<number>>([])
    const cycleStartRef = ref(props.cycleStart)
    const cycleEndRef = ref(props.cycleEnd)

    /**
     * Parse parameter value
     */
    const analyticalValue = () => {
      const $timeVal = props.timeValue
      // Interval time
      const $interval = isStr($timeVal, '/')
      // Specific time
      const $specific = isStr($timeVal, ',')
      // Cycle time
      const $cycle = isStr($timeVal, '-')

      // Every time
      if ($timeVal === '*') {
        radioRef.value = 'everyTime'
        timeRef.value = '*'
        return
      }

      // Positive integer (times)
      if (
        ($timeVal.length === 1 ||
          $timeVal.length === 2 ||
          $timeVal.length === 4) &&
        _.isInteger(parseInt($timeVal))
      ) {
        radioRef.value = 'specificTime'
        specificTimesRef.value = [parseInt($timeVal)]
        return
      }

      // Interval times
      if ($interval) {
        radioRef.value = 'intervalTime'
        intervalStartRef.value = parseInt($interval[0])
        intervalPerformRef.value = parseInt($interval[1])
        timeRef.value = `${intervalStartRef.value}/${intervalPerformRef.value}`
        return
      }

      // Specific times
      if ($specific) {
        radioRef.value = 'specificTime'
        specificTimesRef.value = $specific.map((item) => parseInt(item))
        return
      }

      // Cycle time
      if ($cycle) {
        radioRef.value = 'cycleTime'
        cycleStartRef.value = parseInt($cycle[0])
        cycleEndRef.value = parseInt($cycle[1])
        timeRef.value = `${cycleStartRef.value}-${cycleEndRef.value}`
        return
      }
    }

    // Interval start time（1）
    const onIntervalStart = (value: number | null) => {
      intervalStartRef.value = value || 0
      if (radioRef.value === 'intervalTime') {
        timeRef.value = `${intervalStartRef.value}/${intervalPerformRef.value}`
      }
    }

    // Interval execution time（2）
    const onIntervalPerform = (value: number | null) => {
      intervalPerformRef.value = value || 0
      if (radioRef.value === 'intervalTime') {
        timeRef.value = `${intervalStartRef.value}/${intervalPerformRef.value}`
      }
    }

    // Specific time
    const onSpecificTimes = (arr: Array<number>) => {
      specificTimesRef.value = arr
      if (radioRef.value === 'specificTime') {
        specificReset()
      }
    }

    // Cycle start value
    const onCycleStart = (value: number | null) => {
      cycleStartRef.value = value || 0
      if (radioRef.value === 'cycleTime') {
        timeRef.value = `${cycleStartRef.value}-${cycleEndRef.value}`
      }
    }

    // Cycle end value
    const onCycleEnd = (value: number | null) => {
      cycleEndRef.value = value || 0
      if (radioRef.value === 'cycleTime') {
        timeRef.value = `${cycleStartRef.value}-${cycleEndRef.value}`
      }
    }

    // Reset every time
    const everyReset = () => {
      timeRef.value = '*'
    }

    // Reset interval time
    const intervalReset = () => {
      timeRef.value = `${intervalStartRef.value}/${intervalPerformRef.value}`
    }

    // Reset specific time
    const specificReset = () => {
      let timeValue = '*'
      if (specificTimesRef.value.length) {
        timeValue = specificTimesRef.value.join(',')
      }
      timeRef.value = timeValue
    }

    // Reset cycle time
    const cycleReset = () => {
      timeRef.value = `${cycleStartRef.value}-${cycleEndRef.value}`
    }

    const updateRadioTime = (value: string) => {
      switch (value) {
        case 'everyTime':
          everyReset()
          break
        case 'intervalTime':
          intervalReset()
          break
        case 'specificTime':
          specificReset()
          break
        case 'cycleTime':
          cycleReset()
          break
      }
    }

    watch(
      () => timeRef.value,
      () => ctx.emit('update:timeValue', timeRef.value.toString())
    )

    onMounted(() => analyticalValue())

    return {
      options,
      radioRef,
      intervalStartRef,
      intervalPerformRef,
      specificTimesRef,
      cycleStartRef,
      cycleEndRef,
      updateRadioTime,
      onIntervalStart,
      onIntervalPerform,
      onSpecificTimes,
      onCycleStart,
      onCycleEnd,
      ...toRefs(props)
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <NRadioGroup
        v-model:value={this.radioRef}
        onUpdateValue={this.updateRadioTime}
      >
        <div class={styles['crontab-list']}>
          <NRadio value={'everyTime'} />
          <div class={styles['crontab-list-item']}>
            <div>{t(this.timeI18n!.everyTime)}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'intervalTime'} />
          <div class={styles['crontab-list-item']}>
            <div class={styles['item-text']}>{t(this.timeI18n!.every)}</div>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={5}
                min={this.timeMin}
                max={this.timeMax}
                v-model:value={this.intervalPerformRef}
                onUpdateValue={this.onIntervalPerform}
              />
            </div>
            <div class={styles['item-text']}>
              {t(this.timeI18n!.timeCarriedOut)}
            </div>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={3}
                min={this.timeMin}
                max={this.timeMax}
                v-model:value={this.intervalStartRef}
                onUpdateValue={this.onIntervalStart}
              />
            </div>
            <div class={styles['item-text']}>{t(this.timeI18n!.timeStart)}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'specificTime'} />
          <div class={styles['crontab-list-item']}>
            <div>{t(this.timeI18n!.specificTime)}</div>
            <div class={styles['select-input']}>
              <NSelect
                multiple
                options={specificList[this.timeSpecial]}
                placeholder={t(this.timeI18n!.specificTimeTip)}
                v-model:value={this.specificTimesRef}
                onUpdateValue={this.onSpecificTimes}
              />
            </div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'cycleTime'} />
          <div class={styles['crontab-list-item']}>
            <div>{t(this.timeI18n!.cycleFrom)}</div>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={1}
                min={this.timeMin}
                max={this.timeMax}
                v-model:value={this.cycleStartRef}
                onUpdateValue={this.onCycleStart}
              />
            </div>
            <div>{t(this.timeI18n!.to)}</div>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={1}
                min={this.timeMin}
                max={this.timeMax}
                v-model:value={this.cycleEndRef}
                onUpdateValue={this.onCycleEnd}
              />
            </div>
            <div>{t(this.timeI18n!.time)}</div>
          </div>
        </div>
      </NRadioGroup>
    )
  }
})
