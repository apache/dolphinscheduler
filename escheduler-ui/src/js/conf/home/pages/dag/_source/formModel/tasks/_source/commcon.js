
import i18n from '@/module/i18n'

/**
 * cycle
 */
const cycleList = [
  {
    value: 'month',
    label: `${i18n.$t('月')}`
  },
  {
    value: 'week',
    label: `${i18n.$t('周')}`
  },
  {
    value: 'day',
    label: `${i18n.$t('日')}`
  },
  {
    value: 'hour',
    label: `${i18n.$t('时')}`
  }
]

/**
 * cycle value
 */
const dateValueList = {
  'hour': [
    {
      value: 'last1Hour',
      label: `${i18n.$t('前1小时')}`
    },
    {
      value: 'last2Hours',
      label: `${i18n.$t('前2小时')}`
    },
    {
      value: 'last3Hours',
      label: `${i18n.$t('前3小时')}`
    }
  ],
  'day': [
    {
      value: 'last1Days',
      label: `${i18n.$t('昨天')}`
    },
    {
      value: 'last2Days',
      label: `${i18n.$t('前两天')}`
    },
    {
      value: 'last3Days',
      label: `${i18n.$t('前三天')}`
    },
    {
      value: 'last7Days',
      label: `${i18n.$t('前七天')}`
    }
  ],
  'week': [
    {
      value: 'lastWeek',
      label: `${i18n.$t('上周')}`
    },
    {
      value: 'lastMonday',
      label: `${i18n.$t('上周一')}`
    },
    {
      value: 'lastTuesday',
      label: `${i18n.$t('上周二')}`
    },
    {
      value: 'lastWednesday',
      label: `${i18n.$t('上周三')}`
    },
    {
      value: 'lastThursday',
      label: `${i18n.$t('上周四')}`
    },
    {
      value: 'lastFriday',
      label: `${i18n.$t('上周五')}`
    },
    {
      value: 'lastSaturday',
      label: `${i18n.$t('上周六')}`
    },
    {
      value: 'lastSunday',
      label: `${i18n.$t('上周日')}`
    }
  ],
  'month': [
    {
      value: 'lastMonth',
      label: `${i18n.$t('上月')}`
    },
    {
      value: 'lastMonthBegin',
      label: `${i18n.$t('上月初')}`
    },
    {
      value: 'lastMonthEnd',
      label: `${i18n.$t('上月末')}`
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
    code: `${i18n.$t('查询')}`
  },
  {
    id: 1,
    code: `${i18n.$t('非查询')}`
  }
]

export {
  cycleList,
  dateValueList,
  typeList,
  directList,
  sqlTypeList
}
