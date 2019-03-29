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
 * 状态码表
 */
let stateType = [
  {
    code: '',
    label: `${i18n.$t('无')}`
  }, {
    code: 'SUBMITTED_SUCCESS',
    label: `${i18n.$t('提交成功')}`
  }, {
    code: 'RUNNING_EXEUTION',
    label: `${i18n.$t('正在运行')}`
  }, {
    code: 'READY_PAUSE',
    label: `${i18n.$t('准备暂停')}`
  }, {
    code: 'PAUSE',
    label: `${i18n.$t('暂停')}`
  }, {
    code: 'READY_STOP',
    label: `${i18n.$t('准备停止')}`
  }, {
    code: 'STOP',
    label: `${i18n.$t('停止')}`
  }, {
    code: 'FAILURE',
    label: `${i18n.$t('失败')}`
  }, {
    code: 'SUCCESS',
    label: `${i18n.$t('成功')}`
  }, {
    code: 'NEED_FAULT_TOLERANCE',
    label: `${i18n.$t('需要容错')}`
  }, {
    code: 'KILL',
    label: `${i18n.$t('kill')}`
  }, {
    code: 'WAITTING_THREAD',
    label: `${i18n.$t('等待线程')}`
  }, {
    code: 'WAITTING_DEPEND',
    label: `${i18n.$t('等待依赖完成')}`
  }
]

export {
  stateType
}
