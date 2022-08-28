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
import type { ISpecialSelect } from './types'

const timeI18n = {
  second: {
    everyTime: 'crontab.every_second',
    every: 'crontab.every',
    timeCarriedOut: 'crontab.second_carried_out',
    timeStart: 'crontab.second_start',
    cycleFrom: 'crontab.cycle_from',
    specificTime: 'crontab.specific_second',
    specificTimeTip: 'crontab.specific_second_tip',
    to: 'crontab.to',
    time: 'crontab.second'
  },
  minute: {
    everyTime: 'crontab.every_minute',
    every: 'crontab.every',
    timeCarriedOut: 'crontab.minute_carried_out',
    timeStart: 'crontab.minute_start',
    cycleFrom: 'crontab.cycle_from',
    specificTime: 'crontab.specific_minute',
    specificTimeTip: 'crontab.specific_minute_tip',
    to: 'crontab.to',
    time: 'crontab.minute'
  },
  hour: {
    everyTime: 'crontab.every_hour',
    every: 'crontab.every',
    timeCarriedOut: 'crontab.hour_carried_out',
    timeStart: 'crontab.hour_start',
    cycleFrom: 'crontab.cycle_from',
    specificTime: 'crontab.specific_hour',
    specificTimeTip: 'crontab.specific_hour_tip',
    to: 'crontab.to',
    time: 'crontab.hour'
  },
  month: {
    everyTime: 'crontab.every_month',
    every: 'crontab.every',
    timeCarriedOut: 'crontab.month_carried_out',
    timeStart: 'crontab.month_start',
    cycleFrom: 'crontab.cycle_from',
    specificTime: 'crontab.specific_month',
    specificTimeTip: 'crontab.specific_month_tip',
    to: 'crontab.to',
    time: 'crontab.month'
  },
  year: {
    everyTime: 'crontab.every_year',
    every: 'crontab.every',
    timeCarriedOut: 'crontab.year_carried_out',
    timeStart: 'crontab.year_start',
    cycleFrom: 'crontab.cycle_from',
    specificTime: 'crontab.specific_year',
    specificTimeTip: 'crontab.specific_year_tip',
    to: 'crontab.to',
    time: 'crontab.year'
  }
}

const week = [
  {
    label: 'crontab.sunday',
    value: 1
  },
  {
    label: 'crontab.monday',
    value: 2
  },
  {
    label: 'crontab.tuesday',
    value: 3
  },
  {
    label: 'crontab.wednesday',
    value: 4
  },
  {
    label: 'crontab.thursday',
    value: 5
  },
  {
    label: 'crontab.friday',
    value: 6
  },
  {
    label: 'crontab.saturday',
    value: 7
  }
]

const specificWeek = [
  {
    label: 'SUN',
    value: 'SUN'
  },
  {
    label: 'MON',
    value: 'MON'
  },
  {
    label: 'TUE',
    value: 'TUE'
  },
  {
    label: 'WED',
    value: 'WED'
  },
  {
    label: 'THU',
    value: 'THU'
  },
  {
    label: 'FRI',
    value: 'FRI'
  },
  {
    label: 'SAT',
    value: 'SAT'
  }
]

const lastWeeks = [
  {
    label: 'crontab.sunday',
    value: '?'
  },
  {
    label: 'crontab.monday',
    value: '2L'
  },
  {
    label: 'crontab.tuesday',
    value: '3L'
  },
  {
    label: 'crontab.wednesday',
    value: '4L'
  },
  {
    label: 'crontab.thursday',
    value: '5L'
  },
  {
    label: 'crontab.friday',
    value: '6L'
  },
  {
    label: 'crontab.saturday',
    value: '7L'
  }
]

const isStr = (str: string, v: string) => {
  let flag
  if (str.indexOf(v) !== -1) {
    flag = str.split(v)
  }
  return flag
}

const isWeek = (str: string) => {
  let flag = false
  const data = str.split(',')
  const isSpecificWeek = (key: string) => {
    return _.findIndex(specificWeek, (v) => v.value === key) !== -1
  }
  _.map(data, (v) => {
    if (isSpecificWeek(v)) {
      flag = true
    }
  })
  return flag
}

const range = (start: number, stop: number, step = 1) =>
  Array.from({ length: (stop - start) / step + 1 }, (_, i) => start + i * step)

const specificList: ISpecialSelect = {
  60: _.map(range(0, 59), (v) => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  24: _.map(range(0, 23), (v) => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  12: _.map(range(1, 12), (v) => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  year: _.map(range(2018, 2030), (v) => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  day: _.map(range(1, 31), (v) => {
    return {
      value: v + '',
      label: v + ''
    }
  })
}

export { isStr, isWeek, timeI18n, week, specificWeek, lastWeeks, specificList }
