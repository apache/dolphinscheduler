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
    id: 0,
    desc: `${i18n.$t('Default')}`,
    code: 0
  },
  {
    id: 1,
    desc: `${i18n.$t('Success')}`,
    code: 1
  },
  {
    id: 2,
    desc: `${i18n.$t('Failure')}`,
    code: 2
  }
]

const operator = [
  {
    id: 0,
    desc: '=',
    code: 0
  },
  {
    id: 1,
    desc: '<',
    code: 1
  },
  {
    id: 2,
    desc: '<=',
    code: 2
  },
  {
    id: 3,
    desc: '>',
    code: 3
  },
  {
    id: 4,
    desc: '>=',
    code: 4
  },
  {
    id: 5,
    desc: '!=',
    code: 5
  }
]

const ruleType = [
  {
    id: 0,
    desc: `${i18n.$t('Single Table')}`,
    code: 0
  },
  {
    id: 1,
    desc: `${i18n.$t('Single Table Custon Sql')}`,
    code: 1
  },
  {
    id: 2,
    desc: `${i18n.$t('Multi Table Accuracy')}`,
    code: 2
  },
  {
    id: 3,
    desc: `${i18n.$t('Multi Table Comparison')}`,
    code: 3
  }
]

const checkType = [
  {
    id: 0,
    desc: `${i18n.$t('Statistics Compare Fixed Value')}`,
    code: 0
  },
  {
    id: 1,
    desc: `${i18n.$t('Statistics Compare Comparsion')}`,
    code: 1
  },
  {
    id: 2,
    desc: `${i18n.$t('Statistics Comparison Percentage')}`,
    code: 2
  }
]

const failureStrategy = [
  {
    id: 0,
    desc: `${i18n.$t('End')}`,
    code: 0
  },
  {
    id: 1,
    desc: `${i18n.$t('Continue')}`,
    code: 1
  },
  {
    id: 2,
    desc: `${i18n.$t('End And Alert')}`,
    code: 2
  },
  {
    id: 3,
    desc: `${i18n.$t('Continue And Alert')}`,
    code: 3
  }
]

export {
  dataQualityTaskState,
  operator,
  ruleType,
  checkType,
  failureStrategy
}
