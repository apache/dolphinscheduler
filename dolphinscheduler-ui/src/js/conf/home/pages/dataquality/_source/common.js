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

const dataQualityTaskState = [
  {
    code: '',
    label: `${i18n.$t('All')}`
  },
  {
    code: 0,
    label: `${i18n.$t('Default')}`
  },
  {
    code: 1,
    label: `${i18n.$t('Success')}`
  },
  {
    code: 2,
    label: `${i18n.$t('Failure')}`
  }
]

const operator = [
  {
    label: '=',
    code: 0
  },
  {
    label: '<',
    code: 1
  },
  {
    label: '<=',
    code: 2
  },
  {
    label: '>',
    code: 3
  },
  {
    label: '>=',
    code: 4
  },
  {
    label: '!=',
    code: 5
  }
]

const ruleType = [
  {
    code: -1,
    label: `${i18n.$t('All')}`
  },
  {
    code: 0,
    label: `${i18n.$t('Single Table')}`
  },
  {
    code: 1,
    label: `${i18n.$t('Single Table Custom Sql')}`
  },
  {
    code: 2,
    label: `${i18n.$t('Multi Table Accuracy')}`
  },
  {
    code: 3,
    label: `${i18n.$t('Multi Table Comparison')}`
  }
]

const checkType = [
  {
    label: `${i18n.$t('Expected - Actual')}`,
    code: 0
  },
  {
    label: `${i18n.$t('Actual - Expected')}`,
    code: 1
  },
  {
    label: `${i18n.$t('Actual / Expected')}`,
    code: 2
  },
  {
    label: `${i18n.$t('(Expected - Actual) / Expected')}`,
    code: 3
  }
]

const failureStrategy = [
  {
    label: `${i18n.$t('Alert')}`,
    code: 0
  },
  {
    label: `${i18n.$t('Block')}`,
    code: 1
  }
]

export {
  dataQualityTaskState,
  operator,
  ruleType,
  checkType,
  failureStrategy
}
