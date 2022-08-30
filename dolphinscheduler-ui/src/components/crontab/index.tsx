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

import { computed, defineComponent, ref, watch, PropType } from 'vue'
import { NTabPane, NTabs } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import CrontabTime from './modules/time'
import CrontabDay from './modules/day'
import { timeI18n } from './common'

const props = {
  value: {
    type: String as PropType<String>,
    default: '* * * * * ? *'
  }
}

export default defineComponent({
  name: 'Crontab',
  props,
  emits: ['update:value'],
  setup(props, ctx) {
    const cron = props.value.split(' ')
    const secondRef = ref(cron[0])
    const minuteRef = ref(cron[1])
    const hourRef = ref(cron[2])
    const dayRef = ref(cron[3])
    const monthRef = ref(cron[4])
    const weekRef = ref(cron[5])
    const yearRef = ref(cron[6])

    const crontabValue = computed(
      () =>
        `${secondRef.value} ${minuteRef.value} ${hourRef.value} ${dayRef.value} ${monthRef.value} ${weekRef.value} ${yearRef.value}`
    )

    const reset = () => {
      const cron = props.value.split(' ')
      secondRef.value = cron[0]
      minuteRef.value = cron[1]
      hourRef.value = cron[2]
      dayRef.value = cron[3]
      monthRef.value = cron[4]
      weekRef.value = cron[5]
      yearRef.value = cron[6]
    }

    watch(
      () => crontabValue.value,
      () => {
        ctx.emit('update:value', crontabValue.value)
      }
    )

    watch(
      () => props.value,
      () => {
        reset()
      }
    )

    return {
      secondRef,
      minuteRef,
      hourRef,
      dayRef,
      weekRef,
      monthRef,
      yearRef,
      crontabValue
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <NTabs type='line'>
        <NTabPane name='seconde' tab={t('crontab.second')}>
          <CrontabTime
            v-model:timeValue={this.secondRef}
            timeI18n={timeI18n.second}
            timeSpecial={60}
            timeMin={0}
            timeMax={59}
          />
        </NTabPane>
        <NTabPane name='minute' tab={t('crontab.minute')}>
          <CrontabTime
            v-model:timeValue={this.minuteRef}
            timeI18n={timeI18n.minute}
            timeSpecial={60}
            timeMin={0}
            timeMax={59}
          />
        </NTabPane>
        <NTabPane name='hour' tab={t('crontab.hour')}>
          <CrontabTime
            v-model:timeValue={this.hourRef}
            timeI18n={timeI18n.hour}
            timeSpecial={24}
            timeMin={0}
            timeMax={23}
          />
        </NTabPane>
        <NTabPane name='day' tab={t('crontab.day')}>
          <CrontabDay
            v-model:dayValue={this.dayRef}
            v-model:weekValue={this.weekRef}
          />
        </NTabPane>
        <NTabPane name='month' tab={t('crontab.month')}>
          <CrontabTime
            v-model:timeValue={this.monthRef}
            timeI18n={timeI18n.month}
            timeSpecial={12}
            timeMin={0}
            timeMax={12}
          />
        </NTabPane>
        <NTabPane name='year' tab={t('crontab.year')}>
          <CrontabTime
            v-model:timeValue={this.yearRef}
            timeI18n={timeI18n.year}
            timeSpecial={'year'}
            timeMin={0}
            timeMax={2030}
            intervalPerform={1}
            intervalStart={2019}
            cycleStart={2019}
            cycleEnd={2019}
          />
        </NTabPane>
      </NTabs>
    )
  }
})
