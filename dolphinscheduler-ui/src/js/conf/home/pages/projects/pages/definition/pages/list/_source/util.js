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

const warningTypeList = [
  {
    id: 'NONE',
    code: `${i18n.$t('none_1')}`
  },
  {
    id: 'SUCCESS',
    code: `${i18n.$t('success_1')}`
  },
  {
    id: 'FAILURE',
    code: `${i18n.$t('failure_1')}`
  },
  {
    id: 'ALL',
    code: `${i18n.$t('All_1')}`
  }
]

const isEmial = (val) => {
  let regEmail = /^([a-zA-Z0-9]+[_|\-|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\-|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,}$/ // eslint-disable-line
  return regEmail.test(val)
}

const fuzzyQuery = (list, keyWord) => {
  const len = list.length
  const arr = []
  const reg = new RegExp(keyWord)
  for (let i = 0; i < len; i++) {
    if (list[i].match(reg)) {
      arr.push(list[i])
    }
  }
  return arr
}

export {
  warningTypeList,
  isEmial,
  fuzzyQuery
}
