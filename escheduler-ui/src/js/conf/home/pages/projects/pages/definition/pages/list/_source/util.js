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

let warningTypeList = [
  {
    id: 'NONE',
    code: `${i18n.$t('都不发')}`
  },
  {
    id: 'SUCCESS',
    code: `${i18n.$t('成功发')}`
  },
  {
    id: 'FAILURE',
    code: `${i18n.$t('失败发')}`
  },
  {
    id: 'ALL',
    code: `${i18n.$t('成功或失败都发')}`
  }
]

const isEmial = (val) => {
  let regEmail = /^([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/ // eslint-disable-line
  return regEmail.test(val)
}

const fuzzyQuery = (list, keyWord) => {
  let len = list.length
  let arr = []
  let reg = new RegExp(keyWord)
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
