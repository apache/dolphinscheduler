
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
  'hour': [
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
    }
  ],
  'day': [
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
  'week': [
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
  'month': [
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
    id: 0,
    code: `${i18n.$t('Query')}`
  },
  {
    id: 1,
    code: `${i18n.$t('Non Query')}`
  }
]

export {
  cycleList,
  dateValueList,
  typeList,
  directList,
  sqlTypeList
}
