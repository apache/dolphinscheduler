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

import i18n from '@/module/i18n'

/**
 * cycle
 */
const cycleList = [
  {
    value: 'month',
    label: `${i18n.$t('month')}`
  },
  {
    value: 'week',
    label: `${i18n.$t('week')}`
  },
  {
    value: 'day',
    label: `${i18n.$t('day')}`
  },
  {
    value: 'hour',
    label: `${i18n.$t('hour')}`
  }
]

/**
 * cycle value
 */
const dateValueList = {
  hour: [
    {
      value: 'currentHour',
      label: `${i18n.$t('CurrentHour')}`
    },
    {
      value: 'last1Hour',
      label: `${i18n.$t('Last1Hour')}`
    },
    {
      value: 'last2Hours',
      label: `${i18n.$t('Last2Hours')}`
    },
    {
      value: 'last3Hours',
      label: `${i18n.$t('Last3Hours')}`
    },
    {
      value: 'last24Hours',
      label: `${i18n.$t('Last24Hours')}`
    }
  ],
  day: [
    {
      value: 'today',
      label: `${i18n.$t('today')}`
    },
    {
      value: 'last1Days',
      label: `${i18n.$t('Last1Days')}`
    },
    {
      value: 'last2Days',
      label: `${i18n.$t('Last2Days')}`
    },
    {
      value: 'last3Days',
      label: `${i18n.$t('Last3Days')}`
    },
    {
      value: 'last7Days',
      label: `${i18n.$t('Last7Days')}`
    }
  ],
  week: [
    {
      value: 'thisWeek',
      label: `${i18n.$t('ThisWeek')}`
    },
    {
      value: 'lastWeek',
      label: `${i18n.$t('LastWeek')}`
    },
    {
      value: 'lastMonday',
      label: `${i18n.$t('LastMonday')}`
    },
    {
      value: 'lastTuesday',
      label: `${i18n.$t('LastTuesday')}`
    },
    {
      value: 'lastWednesday',
      label: `${i18n.$t('LastWednesday')}`
    },
    {
      value: 'lastThursday',
      label: `${i18n.$t('LastThursday')}`
    },
    {
      value: 'lastFriday',
      label: `${i18n.$t('LastFriday')}`
    },
    {
      value: 'lastSaturday',
      label: `${i18n.$t('LastSaturday')}`
    },
    {
      value: 'lastSunday',
      label: `${i18n.$t('LastSunday')}`
    }
  ],
  month: [
    {
      value: 'thisMonth',
      label: `${i18n.$t('ThisMonth')}`
    },
    {
      value: 'lastMonth',
      label: `${i18n.$t('LastMonth')}`
    },
    {
      value: 'lastMonthBegin',
      label: `${i18n.$t('LastMonthBegin')}`
    },
    {
      value: 'lastMonthEnd',
      label: `${i18n.$t('LastMonthEnd')}`
    }
  ]
}

/**
 * direct
 */
const directList = [
  {
    id: 1,
    code: 'IN',
    disabled: false
  },
  {
    id: 2,
    code: 'OUT',
    disabled: false
  }
]

/**
 * type
 */
const typeList = [
  {
    id: 1,
    code: 'VARCHAR',
    disabled: false
  },
  {
    id: 2,
    code: 'INTEGER',
    disabled: false
  },
  {
    id: 3,
    code: 'LONG',
    disabled: false
  },
  {
    id: 4,
    code: 'FLOAT',
    disabled: false
  },
  {
    id: 5,
    code: 'DOUBLE',
    disabled: false
  },
  {
    id: 6,
    code: 'DATE',
    disabled: false
  },
  {
    id: 7,
    code: 'TIME',
    disabled: false
  },
  {
    id: 8,
    code: 'TIMESTAMP',
    disabled: false
  },
  {
    id: 9,
    code: 'BOOLEAN',
    disabled: false
  }
]

/**
 * sqlType
 */
const sqlTypeList = [
  {
    id: '0',
    code: `${i18n.$t('Query')}`
  },
  {
    id: '1',
    code: `${i18n.$t('Non Query')}`
  }
]

const positionList = [
  {
    id: 'PARAMETER',
    code: 'Parameter'
  },
  {
    id: 'BODY',
    code: 'Body'
  },
  {
    id: 'HEADERS',
    code: 'Headers'
  }
]
const nodeStatusList = [
  {
    value: 'SUCCESS',
    label: `${i18n.$t('success')}`
  },
  {
    value: 'FAILURE',
    label: `${i18n.$t('failed')}`
  }
]

export {
  cycleList,
  dateValueList,
  typeList,
  directList,
  sqlTypeList,
  positionList,
  nodeStatusList
}
