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
import Permissions from '@/module/permissions'

/**
 * Operation bar config
 * @code code
 * @icon icon
 * @disable disable
 * @desc tooltip
 */
const toolOper = (dagThis) => {
  let disabled = Permissions.getAuth() === false ? false : !dagThis.$store.state.dag.isDetails
  return [
    {
      code: 'pointer',
      icon: '&#xe781;',
      disable: disabled,
      desc: `${i18n.$t('拖动节点和选中项')}`
    },
    {
      code: 'line',
      icon: '&#xe61c;',
      disable: disabled,
      desc: `${i18n.$t('选择线条连接')}`
    },
    {
      code: 'remove',
      icon: '&#xe611;',
      disable: disabled,
      desc: `${i18n.$t('删除选中的线或节点')}`
    },
    {
      code: 'download',
      icon: '&#xe628;',
      disable: !!dagThis.type,
      desc: `${i18n.$t('下载')}`
    },
    {
      code: 'screen',
      icon: '&#xe6e0;',
      disable: disabled,
      desc: `${i18n.$t('全屏')}`
    }
  ]
}

/**
 * Post status
 * @id Front end definition id
 * @desc tooltip
 * @code Backend definition identifier
 */
let publishStatus = [
  {
    id: 0,
    desc: `${i18n.$t('未发布')}`,
    code: 'NOT_RELEASE'
  },
  {
    id: 1,
    desc: `${i18n.$t('上线')}`,
    code: 'ONLINE'
  },
  {
    id: 2,
    desc: `${i18n.$t('下线')}`,
    code: 'OFFLINE'
  }
]

/**
 * Operation type
 * @desc tooltip
 * @code identifier
 */
let runningType = [
  {
    desc: `${i18n.$t('启动工作流')}`,
    code: 'START_PROCESS'
  },
  {
    desc: `${i18n.$t('从当前节点开始执行')}`,
    code: 'START_CURRENT_TASK_PROCESS'
  },
  {
    desc: `${i18n.$t('恢复被容错的工作流')}`,
    code: 'RECOVER_TOLERANCE_FAULT_PROCESS'
  },
  {
    desc: `${i18n.$t('恢复暂停流程')}`,
    code: 'RECOVER_SUSPENDED_PROCESS'
  },
  {
    desc: `${i18n.$t('从失败节点开始执行')}`,
    code: 'START_FAILURE_TASK_PROCESS'
  },
  {
    desc: `${i18n.$t('补数')}`,
    code: 'COMPLEMENT_DATA'
  },
  {
    desc: `${i18n.$t('调度执行')}`,
    code: 'SCHEDULER'
  },
  {
    desc: `${i18n.$t('重跑')}`,
    code: 'REPEAT_RUNNING'
  },
  {
    desc: `${i18n.$t('暂停')}`,
    code: 'PAUSE'
  },
  {
    desc: `${i18n.$t('停止')}`,
    code: 'STOP'
  },
  {
    desc: `${i18n.$t('恢复等待线程')}`,
    code: 'RECOVER_WAITTING_THREAD'
  }
]

/**
 * Task status
 * @key key
 * @id id
 * @desc tooltip
 * @color color
 * @icoUnicode iconfont
 * @isSpin is loading (Need to execute the code block to write if judgment)
 */
let tasksState = {
  'SUBMITTED_SUCCESS': {
    id: 0,
    desc: `${i18n.$t('提交成功')}`,
    color: '#A9A9A9',
    icoUnicode: '&#xe7c2;',
    isSpin: false
  },
  'RUNNING_EXEUTION': {
    id: 1,
    desc: `${i18n.$t('正在执行')}`,
    color: '#0097e0',
    icoUnicode: '&#xe80f;',
    isSpin: true
  },
  'READY_PAUSE': {
    id: 2,
    desc: `${i18n.$t('准备暂停')}`,
    color: '#07b1a3',
    icoUnicode: '&#xe677;',
    isSpin: false
  },
  'PAUSE': {
    id: 3,
    desc: `${i18n.$t('暂停')}`,
    color: '#057c72',
    icoUnicode: '&#xe679;',
    isSpin: false
  },
  'READY_STOP': {
    id: 4,
    desc: `${i18n.$t('准备停止')}`,
    color: '#FE0402',
    icoUnicode: '&#xe6e6;',
    isSpin: false
  },
  'STOP': {
    id: 5,
    desc: `${i18n.$t('停止')}`,
    color: '#e90101',
    icoUnicode: '&#xe6ae;',
    isSpin: false
  },
  'FAILURE': {
    id: 6,
    desc: `${i18n.$t('失败')}`,
    color: '#000000',
    icoUnicode: '&#xe75d;',
    isSpin: false
  },
  'SUCCESS': {
    id: 7,
    desc: `${i18n.$t('成功')}`,
    color: '#33cc00',
    icoUnicode: '&#xe6d4;',
    isSpin: false
  },
  'NEED_FAULT_TOLERANCE': {
    id: 8,
    desc: `${i18n.$t('需要容错')}`,
    color: '#FF8C00',
    icoUnicode: '&#xe60d;',
    isSpin: false
  },
  'KILL': {
    id: 9,
    desc: `${i18n.$t('kill')}`,
    color: '#a70202',
    icoUnicode: '&#xe6ce;',
    isSpin: false
  },
  'WAITTING_THREAD': {
    id: 10,
    desc: `${i18n.$t('等待线程')}`,
    color: '#912eed',
    icoUnicode: '&#xe62e;',
    isSpin: false
  },
  'WAITTING_DEPEND': {
    id: 11,
    desc: `${i18n.$t('等待依赖')}`,
    color: '#5101be',
    icoUnicode: '&#xe68c;',
    isSpin: false
  }
}

/**
 * Node type
 * @key key
 * @desc tooltip
 * @color color (tree and gantt)
 */
let tasksType = {
  'SHELL': {
    desc: 'SHELL',
    color: '#646464'
  },
  'SUB_PROCESS': {
    desc: 'SUB_PROCESS',
    color: '#0097e0'
  },
  'PROCEDURE': {
    desc: 'PROCEDURE',
    color: '#525CCD'
  },
  'SQL': {
    desc: 'SQL',
    color: '#7A98A1'
  },
  'SPARK': {
    desc: 'SPARK',
    color: '#E46F13'
  },
  'MR': {
    desc: 'MapReduce',
    color: '#A0A5CC'
  },
  'PYTHON': {
    desc: 'PYTHON',
    color: '#FED52D'
  },
  'DEPENDENT': {
    desc: 'DEPENDENT',
    color: '#2FBFD8'
  }
}


export {
  toolOper,
  publishStatus,
  runningType,
  tasksState,
  tasksType
}
