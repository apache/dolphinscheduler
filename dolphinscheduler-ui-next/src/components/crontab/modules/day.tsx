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

import { defineComponent, onMounted, PropType, ref, watch } from 'vue'
import { NInputNumber, NRadio, NRadioGroup, NSelect } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { isStr, isWeek, week, specificWeek, lastWeeks } from '../common'
import styles from '../index.module.scss'

const props = {
  dayValue: {
    type: String as PropType<string>,
    default: '*'
  },
  weekValue: {
    type: String as PropType<string>,
    default: '?'
  }
}

export default defineComponent({
  name: 'CrontabDay',
  props,
  emits: ['update:dayValue', 'update:weekValue'],
  setup(props, ctx) {
    const { t } = useI18n()

    const options = Array.from({ length: 60 }, (x, i) => ({
      label: i.toString(),
      value: i
    }))

    const weekOptions = week.map((v) => ({
      label: t(v.label),
      value: v.value
    }))

    const lastWeekOptions = lastWeeks.map((v) => ({
      label: t(v.label),
      value: v.value
    }))

    const radioRef = ref()
    const dayRef = ref()
    const weekRef = ref()
    const WkintervalWeekStartRef = ref(2)
    const WkintervalWeekPerformRef = ref(2)
    const intervalDayStartRef = ref(1)
    const intervalDayPerformRef = ref(1)
    const WkspecificDayRef = ref<Array<number>>([])
    const WkspecificWeekRef = ref<Array<number>>([])
    const monthLastDaysRef = ref('L')
    const monthLastWorkingDaysRef = ref('LW')
    const monthLastWeeksRef = ref('?')
    const monthTailBeforeRef = ref(1)
    const recentlyWorkingDaysMonthRef = ref(1)
    const WkmonthNumWeeksDayRef = ref(1)
    const WkmonthNumWeeksWeekRef = ref(1)

    /**
     * Parse parameter value
     */
    const analyticalValue = () => {
      const $dayVal = props.dayValue
      const $weekVal = props.weekValue
      const isWeek1 = $weekVal.indexOf('/') !== -1
      const isWeek2 = $weekVal.indexOf('#') !== -1

      // Initialization
      if ($dayVal === '*' && $weekVal === '?') {
        radioRef.value = 'everyDay'
        return
      }

      // week
      if (isWeek1 || isWeek2 || isWeek($weekVal)) {
        dayRef.value = '?'

        /**
         * Processing by sequence number (excluding days)
         * @param [
         * WkintervalWeek=>(/),
         * WkspecificWeek=>(TUE,WED),
         * WkmonthNumWeeks=>(#)
         * ]
         */
        const hanleWeekOne = () => {
          const a = isStr($weekVal, '/') as string[]
          WkintervalWeekStartRef.value = parseInt(a[0])
          WkintervalWeekPerformRef.value = parseInt(a[1])
          dayRef.value = '?'
          weekRef.value = `${WkintervalWeekPerformRef.value}/${WkintervalWeekStartRef.value}`
          radioRef.value = 'WkintervalWeek'
        }

        const hanleWeekTwo = () => {
          WkspecificWeekRef.value = $weekVal
            .split(',')
            .map((item) => parseInt(item))
          radioRef.value = 'WkspecificWeek'
        }

        const hanleWeekThree = () => {
          const a = isStr($weekVal, '#') as string[]
          WkmonthNumWeeksDayRef.value = parseInt(a[0])
          WkmonthNumWeeksDayRef.value = parseInt(a[1])
          radioRef.value = 'WkmonthNumWeeks'
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
        weekRef.value = '?'

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
          radioRef.value = 'everyDay'
        }

        const hanleDayTwo = () => {
          const a = isStr($dayVal, '/') as string[]
          intervalDayStartRef.value = parseInt(a[0])
          intervalDayPerformRef.value = parseInt(a[1])
          radioRef.value = 'intervalDay'
        }

        const hanleDayThree = () => {
          WkspecificDayRef.value = $dayVal
            .split(',')
            .map((item) => parseInt(item))
          radioRef.value = 'specificDay'
        }

        const hanleDayFour = () => {
          radioRef.value = 'monthLastDays'
        }

        const hanleDayFive = () => {
          radioRef.value = 'monthLastWorkingDays'
        }

        const hanleDaySix = () => {
          monthLastWeeksRef.value = $dayVal
          radioRef.value = 'monthLastWeeks'
        }

        const hanleDaySeven = () => {
          const a = isStr($dayVal, '-') as string[]
          monthTailBeforeRef.value = parseInt(a[1])
          radioRef.value = 'monthTailBefore'
        }

        const hanleDayEight = () => {
          recentlyWorkingDaysMonthRef.value = parseInt(
            $dayVal.slice(0, $dayVal.length - 1)
          )
          radioRef.value = 'recentlyWorkingDaysMonth'
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
    }

    // Every few weeks
    const onWkintervalWeekPerform = (value: number | null) => {
      WkintervalWeekPerformRef.value = value || 0
      if (radioRef.value === 'WkintervalWeek') {
        dayRef.value = '?'
        weekRef.value = `${WkintervalWeekStartRef.value}/${WkintervalWeekPerformRef.value}`
      }
    }

    // Every few weeks
    const onWkintervalWeekStart = (value: number | null) => {
      WkintervalWeekStartRef.value = value || 0
      if (radioRef.value === 'WkintervalWeek') {
        dayRef.value = '?'
        weekRef.value = `${WkintervalWeekStartRef.value}/${WkintervalWeekPerformRef.value}`
      }
    }

    // Interval start time（1）
    const onIntervalDayStart = (value: number | null) => {
      intervalDayStartRef.value = value || 0
      if (radioRef.value === 'intervalDay') {
        intervalDaySet()
      }
    }

    // Interval execution time（2）
    const onIntervalDayPerform = (value: number | null) => {
      intervalDayPerformRef.value = value || 0
      if (radioRef.value === 'intervalDay') {
        intervalDaySet()
      }
    }

    // Specific day of the week (multiple choice)
    const onWkspecificWeek = (arr: Array<number>) => {
      WkspecificWeekRef.value = arr
      if (radioRef.value === 'WkspecificWeek') {
        dayRef.value = '?'
        weekRef.value = arr.join(',')
      }
    }

    // Specific days (multiple choices)
    const onWkspecificDay = (arr: Array<number>) => {
      WkspecificDayRef.value = arr
      if (radioRef.value === 'specificDay') {
        weekRef.value = '?'
        dayRef.value = arr.join(',')
      }
    }

    const onMonthLastWeeks = (value: string | null) => {
      monthLastWeeksRef.value = value || '?'
      if (radioRef.value === 'monthLastWeeks') {
        weekRef.value = value
        dayRef.value = '?'
      }
    }

    // Specific days
    const onSpecificDays = (arr: Array<number>) => {
      WkspecificDayRef.value = arr
      if (radioRef.value === 'specificDay') {
        specificSet()
      }
    }

    // By the end of this month
    const onMonthTailBefore = (value: number | null) => {
      monthTailBeforeRef.value = value || 0
      if (radioRef.value === 'monthTailBefore') {
        dayRef.value = `L-${monthTailBeforeRef.value}`
      }
    }

    // Last working day
    const onRecentlyWorkingDaysMonth = (value: number | null) => {
      recentlyWorkingDaysMonthRef.value = value || 0
      if (radioRef.value === 'recentlyWorkingDaysMonth') {
        dayRef.value = `${recentlyWorkingDaysMonthRef.value}W`
      }
    }

    // On the day of this month
    const onWkmonthNumWeeksDay = (value: number | null) => {
      WkmonthNumWeeksDayRef.value = value || 0
      if (radioRef.value === 'WkmonthNumWeeks') {
        weekRef.value = `${WkmonthNumWeeksWeekRef.value}#${WkmonthNumWeeksDayRef.value}`
      }
    }

    // On the week of this month
    const onWkmonthNumWeeksWeek = (value: number | null) => {
      if (radioRef.value === 'WkmonthNumWeeks') {
        dayRef.value = '?'
        weekRef.value = `${value}#${WkmonthNumWeeksDayRef.value}`
      }
    }

    // Reset every day
    const everyDaySet = () => {
      dayRef.value = '*'
    }

    // Reset interval week starts from *
    const WkintervalWeekReset = () => {
      weekRef.value = `${WkintervalWeekStartRef.value}/${WkintervalWeekPerformRef.value}`
    }

    // Reset interval days
    const intervalDaySet = () => {
      dayRef.value = `${intervalDayStartRef.value}/${intervalDayPerformRef.value}`
    }

    // Specific week (multiple choices)
    const WkspecificWeekReset = () => {
      weekRef.value = WkspecificWeekRef.value.length
        ? WkspecificWeekRef.value.join(',')
        : '*'
    }

    // Reset specific days
    const specificSet = () => {
      if (WkspecificDayRef.value.length) {
        dayRef.value = WkspecificDayRef.value.join(',')
      } else {
        dayRef.value = '*'
      }
    }

    // On the last day of the month
    const monthLastDaysReset = () => {
      dayRef.value = monthLastDaysRef.value
    }

    // On the last working day of the month
    const monthLastWorkingDaysReset = () => {
      dayRef.value = monthLastWorkingDaysRef.value
    }

    // At the end of the month*
    const monthLastWeeksReset = () => {
      dayRef.value = monthLastWeeksRef.value
    }

    // By the end of this month
    const monthTailBeforeReset = () => {
      dayRef.value = `L-${monthTailBeforeRef.value}`
    }

    // Last working day (Monday to Friday) to this month
    const recentlyWorkingDaysMonthReset = () => {
      dayRef.value = `${recentlyWorkingDaysMonthRef.value}W`
    }

    // On the day of this month
    const WkmonthNumReset = () => {
      weekRef.value = `${WkmonthNumWeeksWeekRef.value}#${WkmonthNumWeeksDayRef.value}`
    }

    const updateRadioDay = (value: string) => {
      switch (value) {
        case 'everyDay':
          weekRef.value = '?'
          everyDaySet()
          break
        case 'WkintervalWeek':
          dayRef.value = '?'
          WkintervalWeekReset()
          break
        case 'intervalDay':
          weekRef.value = '?'
          intervalDaySet()
          break
        case 'WkspecificWeek':
          dayRef.value = '?'
          WkspecificWeekReset()
          break
        case 'specificDay':
          weekRef.value = '?'
          specificSet()
          break
        case 'monthLastDays':
          weekRef.value = '?'
          monthLastDaysReset()
          break
        case 'monthLastWorkingDays':
          weekRef.value = '?'
          monthLastWorkingDaysReset()
          break
        case 'monthLastWeeks':
          weekRef.value = '1L'
          monthLastWeeksReset()
          break
        case 'monthTailBefore':
          weekRef.value = '?'
          monthTailBeforeReset()
          break
        case 'recentlyWorkingDaysMonth':
          weekRef.value = '?'
          recentlyWorkingDaysMonthReset()
          break
        case 'WkmonthNumWeeks':
          dayRef.value = '?'
          WkmonthNumReset()
          break
      }
    }

    watch(
      () => dayRef.value,
      () => ctx.emit('update:dayValue', dayRef.value.toString())
    )

    watch(
      () => weekRef.value,
      () => ctx.emit('update:weekValue', weekRef.value.toString())
    )

    onMounted(() => analyticalValue())

    return {
      options,
      weekOptions,
      lastWeekOptions,
      radioRef,
      WkintervalWeekStartRef,
      WkintervalWeekPerformRef,
      intervalDayStartRef,
      intervalDayPerformRef,
      WkspecificWeekRef,
      WkspecificDayRef,
      monthLastWeeksRef,
      monthTailBeforeRef,
      recentlyWorkingDaysMonthRef,
      WkmonthNumWeeksDayRef,
      WkmonthNumWeeksWeekRef,
      updateRadioDay,
      onWkintervalWeekStart,
      onWkintervalWeekPerform,
      onIntervalDayStart,
      onIntervalDayPerform,
      onSpecificDays,
      onWkspecificWeek,
      onWkspecificDay,
      onMonthLastWeeks,
      onMonthTailBefore,
      onRecentlyWorkingDaysMonth,
      onWkmonthNumWeeksDay,
      onWkmonthNumWeeksWeek
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <NRadioGroup
        v-model:value={this.radioRef}
        onUpdateValue={this.updateRadioDay}
      >
        <div class={styles['crontab-list']}>
          <NRadio value={'everyDay'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.every_day')}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'WkintervalWeek'} />
          <div class={styles['crontab-list-item']}>
            <div class={styles['item-text']}>{t('crontab.every')}</div>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={0}
                min={0}
                max={7}
                v-model:value={this.WkintervalWeekPerformRef}
                onUpdateValue={this.onWkintervalWeekPerform}
              />
            </div>
            <div class={styles['item-text']}>
              {t('crontab.day_carried_out')}
            </div>
            <div>
              <NSelect
                options={this.weekOptions}
                defaultValue={this.WkintervalWeekStartRef}
                v-model:value={this.WkintervalWeekStartRef}
                onUpdateValue={this.onWkintervalWeekStart}
              />
            </div>
            <div>{t('crontab.start')}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'intervalDay'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.every')}</div>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={0}
                min={0}
                max={31}
                v-model:value={this.intervalDayPerformRef}
                onUpdateValue={this.onIntervalDayPerform}
              />
            </div>
            <div>{t('crontab.day_carried_out')}</div>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={0}
                min={1}
                max={31}
                v-model:value={this.intervalDayStartRef}
                onUpdateValue={this.onIntervalDayStart}
              />
            </div>
            <div>{t('crontab.day_start')}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'WkspecificWeek'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.specific_week')}</div>
            <div>
              <NSelect
                style={{ width: '300px' }}
                multiple
                options={specificWeek}
                placeholder={t('crontab.specific_week_tip')}
                v-model:value={this.WkspecificWeekRef}
                onUpdateValue={this.onWkspecificWeek}
              />
            </div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'specificDay'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.specific_day')}</div>
            <div>
              <NSelect
                style={{ width: '300px' }}
                multiple
                options={this.options}
                placeholder={t('crontab.specific_day_tip')}
                v-model:value={this.WkspecificDayRef}
                onUpdateValue={this.onWkspecificDay}
              />
            </div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'monthLastDays'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.last_day_of_month')}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'monthLastWorkingDays'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.last_work_day_of_month')}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'monthLastWeeks'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.last_of_month')}</div>
            <div>
              <NSelect
                style={{ width: '150px' }}
                options={this.lastWeekOptions}
                defaultValue={this.monthLastWeeksRef}
                v-model:value={this.monthLastWeeksRef}
                onUpdateValue={this.onMonthLastWeeks}
              />
            </div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'monthTailBefore'} />
          <div class={styles['crontab-list-item']}>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={0}
                min={0}
                max={31}
                v-model:value={this.monthTailBeforeRef}
                onUpdateValue={this.onMonthTailBefore}
              />
            </div>
            <div>{t('crontab.before_end_of_month')}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'recentlyWorkingDaysMonth'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.recent_business_day_to_month')}</div>
            <div class={styles['number-input']}>
              <NInputNumber
                style={{ width: '100px' }}
                defaultValue={0}
                min={0}
                max={31}
                v-model:value={this.recentlyWorkingDaysMonthRef}
                onUpdateValue={this.onRecentlyWorkingDaysMonth}
              />
            </div>
            <div style={{ width: '50px' }}>{t('crontab.one_day')}</div>
          </div>
        </div>
        <div class={styles['crontab-list']}>
          <NRadio value={'WkmonthNumWeeks'} />
          <div class={styles['crontab-list-item']}>
            <div>{t('crontab.in_this_months')}</div>
            <div class={styles['number-input']}>
              <NInputNumber
                defaultValue={0}
                min={0}
                max={31}
                v-model:value={this.WkmonthNumWeeksDayRef}
                onUpdateValue={this.onWkmonthNumWeeksDay}
              />
            </div>
            <div>
              <NSelect
                style={{ width: '150px' }}
                options={this.weekOptions}
                defaultValue={this.WkmonthNumWeeksWeekRef}
                v-model:value={this.WkmonthNumWeeksWeekRef}
                onUpdateValue={this.onWkmonthNumWeeksWeek}
              />
            </div>
          </div>
        </div>
      </NRadioGroup>
    )
  }
})
