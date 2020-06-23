
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

/**
 * Generate specified interval number
 * @param start Starting value
 * @param end End value
 */
const range = (start, end) => {
  const length = end - start + 1
  let step = start - 1
  return Array.apply(null, { length: length }).map(function (v, i) { step++; return step })
}

const selectList = {
  60: _.map(range(0, 59), v => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  24: _.map(range(0, 23), v => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  12: _.map(range(0, 12), v => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  year: _.map(range(2018, 2030), v => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  week: [
    {
      value: 1,
      label: '星期天'
    },
    {
      value: 2,
      label: '星期一'
    },
    {
      value: 3,
      label: '星期二'
    },
    {
      value: 4,
      label: '星期三'
    },
    {
      value: 5,
      label: '星期四'
    },
    {
      value: 6,
      label: '星期五'
    },
    {
      value: 7,
      label: '星期六'
    }
  ],
  specificWeek: [
    {
      value: 'SUN',
      label: 'SUN'
    },
    {
      value: 'MON',
      label: 'MON'
    },
    {
      value: 'TUE',
      label: 'TUE'
    },
    {
      value: 'WED',
      label: 'WED'
    },
    {
      value: 'THU',
      label: 'THU'
    },
    {
      value: 'FRI',
      label: 'FRI'
    },
    {
      value: 'SAT',
      label: 'SAT'
    }
  ],
  day: _.map(range(1, 31), v => {
    return {
      value: v + '',
      label: v + ''
    }
  }),
  lastWeeks: [
    {
      value: '1L',
      label: '星期天'
    },
    {
      value: '2L',
      label: '星期一'
    },
    {
      value: '3L',
      label: '星期二'
    },
    {
      value: '4L',
      label: '星期三'
    },
    {
      value: '5L',
      label: '星期四'
    },
    {
      value: '6L',
      label: '星期五'
    },
    {
      value: '7L',
      label: '星期六'
    }
  ]
}

const isStr = (str, v) => {
  let flag
  if (str.indexOf(v) !== -1) {
    flag = str.split(v)
  }
  return flag
}

const isWeek = (str) => {
  let flag = false
  const data = str.split(',')
  const isSpecificWeek = (key) => {
    return _.findIndex(selectList.specificWeek, v => v.value === key) !== -1
  }
  _.map(data, v => {
    if (isSpecificWeek(v)) {
      flag = true
    }
  })
  return flag
}

/**
 * template
 *
 * @param {String} string
 * @param {Array} ...args
 * @return {String}
 */
const { hasOwnProperty } = {}
const RE_NARGS = /(%|)\{([0-9a-zA-Z_]+)\}/g
const hasOwn = (o, k) => hasOwnProperty.call(o, k)
const template = (string, ...args) => {
  if (args.length === 1 && typeof args[0] === 'object') {
    args = args[0]
  }
  if (!args || !args.hasOwnProperty) {
    args = {}
  }
  return string.replace(RE_NARGS, (match, prefix, i, index) => {
    let result
    if (string[index - 1] === '{' &&
      string[index + match.length] === '}') {
      return i
    } else {
      result = hasOwn(args, i) ? args[i] : null
      if (result === null || result === undefined) {
        return ''
      }
      return result
    }
  })
}

export {
  selectList,
  isStr,
  isWeek,
  template
}
