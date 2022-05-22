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

import {
  SettingFilled,
  SettingOutlined,
  CloseCircleOutlined,
  PauseCircleOutlined,
  CheckCircleOutlined,
  EditOutlined,
  MinusCircleOutlined,
  CheckCircleFilled,
  Loading3QuartersOutlined,
  PauseCircleFilled,
  ClockCircleOutlined,
  StopFilled,
  StopOutlined,
  GlobalOutlined,
  IssuesCloseOutlined,
  SendOutlined,
  HistoryOutlined
} from '@vicons/antd'
import { format, parseISO } from 'date-fns'
import _ from 'lodash'
import { ITaskStateConfig } from './types'

/**
 * Intelligent display kb m
 */
export const bytesToSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024 // or 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))

  return parseInt((bytes / Math.pow(k, i)).toPrecision(3)) + ' ' + sizes[i]
}

export const fileTypeArr = [
  'txt',
  'log',
  'sh',
  'bat',
  'conf',
  'cfg',
  'py',
  'java',
  'sql',
  'xml',
  'hql',
  'properties',
  'json',
  'yml',
  'yaml',
  'ini',
  'js'
]

/**
 * Operation type
 * @desc tooltip
 * @code identifier
 */
export const runningType = (t: any) => [
  {
    desc: `${t('project.workflow.start_process')}`,
    code: 'START_PROCESS'
  },
  {
    desc: `${t('project.workflow.execute_from_the_current_node')}`,
    code: 'START_CURRENT_TASK_PROCESS'
  },
  {
    desc: `${t('project.workflow.recover_tolerance_fault_process')}`,
    code: 'RECOVER_TOLERANCE_FAULT_PROCESS'
  },
  {
    desc: `${t('project.workflow.resume_the_suspension_process')}`,
    code: 'RECOVER_SUSPENDED_PROCESS'
  },
  {
    desc: `${t('project.workflow.execute_from_the_failed_nodes')}`,
    code: 'START_FAILURE_TASK_PROCESS'
  },
  {
    desc: `${t('project.workflow.complement_data')}`,
    code: 'COMPLEMENT_DATA'
  },
  {
    desc: `${t('project.workflow.scheduling_execution')}`,
    code: 'SCHEDULER'
  },
  {
    desc: `${t('project.workflow.rerun')}`,
    code: 'REPEAT_RUNNING'
  },
  {
    desc: `${t('project.workflow.pause')}`,
    code: 'PAUSE'
  },
  {
    desc: `${t('project.workflow.stop')}`,
    code: 'STOP'
  },
  {
    desc: `${t('project.workflow.recovery_waiting_thread')}`,
    code: 'RECOVER_WAITING_THREAD'
  },
  {
    desc: `${t('project.workflow.recover_serial_wait')}`,
    code: 'RECOVER_SERIAL_WAIT'
  }
]

/**
 * State code table
 */
export const stateType = (t: any) => [
  {
    value: '',
    label: `${t('project.workflow.all_status')}`
  },
  ...Object.entries(tasksState(t)).map(([key, item]) => ({
    value: key,
    label: item.desc
  }))
]

/**
 * Task status
 * @id id
 * @desc tooltip
 * @color color
 * @icon icon
 * @isSpin is loading (Need to execute the code block to write if judgment)
 */
export const tasksState = (t: any): ITaskStateConfig => ({
  SUBMITTED_SUCCESS: {
    id: 0,
    desc: `${t('project.workflow.submit_success')}`,
    color: '#A9A9A9',
    icon: IssuesCloseOutlined,
    isSpin: false,
    classNames: 'submitted_success'
  },
  RUNNING_EXECUTION: {
    id: 1,
    desc: `${t('project.workflow.executing')}`,
    color: '#0097e0',
    icon: SettingFilled,
    isSpin: true,
    classNames: 'running_execution'
  },
  READY_PAUSE: {
    id: 2,
    desc: `${t('project.workflow.ready_to_pause')}`,
    color: '#07b1a3',
    icon: SettingOutlined,
    isSpin: false,
    classNames: 'ready_pause'
  },
  PAUSE: {
    id: 3,
    desc: `${t('project.workflow.pause')}`,
    color: '#057c72',
    icon: PauseCircleOutlined,
    isSpin: false,
    classNames: 'pause'
  },
  READY_STOP: {
    id: 4,
    desc: `${t('project.workflow.ready_to_stop')}`,
    color: '#FE0402',
    icon: StopFilled,
    isSpin: false,
    classNames: 'ready_stop'
  },
  STOP: {
    id: 5,
    desc: `${t('project.workflow.stop')}`,
    color: '#e90101',
    icon: StopOutlined,
    isSpin: false,
    classNames: 'stop'
  },
  FAILURE: {
    id: 6,
    desc: `${t('project.workflow.failed')}`,
    color: '#000000',
    icon: CloseCircleOutlined,
    isSpin: false,
    classNames: 'failed'
  },
  SUCCESS: {
    id: 7,
    desc: `${t('project.workflow.success')}`,
    color: '#95DF96',
    icon: CheckCircleOutlined,
    isSpin: false,
    classNames: 'success'
  },
  NEED_FAULT_TOLERANCE: {
    id: 8,
    desc: `${t('project.workflow.need_fault_tolerance')}`,
    color: '#FF8C00',
    icon: EditOutlined,
    isSpin: false,
    classNames: 'need_fault_tolerance'
  },
  KILL: {
    id: 9,
    desc: `${t('project.workflow.kill')}`,
    color: '#a70202',
    icon: MinusCircleOutlined,
    isSpin: false,
    classNames: 'kill'
  },
  WAITING_THREAD: {
    id: 10,
    desc: `${t('project.workflow.waiting_for_thread')}`,
    color: '#912eed',
    icon: ClockCircleOutlined,
    isSpin: false,
    classNames: 'waiting_thread'
  },
  WAITING_DEPEND: {
    id: 11,
    desc: `${t('project.workflow.waiting_for_dependence')}`,
    color: '#5101be',
    icon: GlobalOutlined,
    isSpin: false,
    classNames: 'waiting_depend'
  },
  DELAY_EXECUTION: {
    id: 12,
    desc: `${t('project.workflow.delay_execution')}`,
    color: '#5102ce',
    icon: PauseCircleFilled,
    isSpin: false,
    classNames: 'delay_execution'
  },
  FORCED_SUCCESS: {
    id: 13,
    desc: `${t('project.workflow.forced_success')}`,
    color: '#5102ce',
    icon: CheckCircleFilled,
    isSpin: false,
    classNames: 'forced_success'
  },
  SERIAL_WAIT: {
    id: 14,
    desc: `${t('project.workflow.serial_wait')}`,
    color: '#5102ce',
    icon: Loading3QuartersOutlined,
    isSpin: true,
    classNames: 'serial_wait'
  },
  DISPATCH: {
    id: 15,
    desc: `${t('project.workflow.dispatch')}`,
    color: '#5101be',
    icon: SendOutlined,
    isSpin: false,
    classNames: 'dispatch'
  },
  PENDING: {
    id: 18,
    desc: `${t('project.workflow.pending')}`,
    color: '#5101be',
    icon: HistoryOutlined,
    isSpin: false,
    classNames: 'pending'
  }
})

/**
 * A simple uuid generator, support prefix and template pattern.
 *
 * @example
 *
 *  uuid('v-') // -> v-xxx
 *  uuid('v-ani-%{s}-translate')  // -> v-ani-xxx
 */
export function uuid(prefix: string) {
  const id = Math.floor(Math.random() * 10000).toString(36)
  return prefix
    ? ~prefix.indexOf('%{s}')
      ? prefix.replace(/%\{s\}/g, id)
      : prefix + id
    : id
}

export const warningTypeList = [
  {
    id: 'NONE',
    code: 'project.workflow.none_send'
  },
  {
    id: 'SUCCESS',
    code: 'project.workflow.success_send'
  },
  {
    id: 'FAILURE',
    code: 'project.workflow.failure_send'
  },
  {
    id: 'ALL',
    code: 'project.workflow.all_send'
  }
]

export const parseTime = (dateTime: string | number): Date => {
  return _.isString(dateTime) === true
    ? parseISO(dateTime as string)
    : new Date(dateTime)
}

export const renderTableTime = (
  dateTime: string | number | null | undefined
): string => {
  return dateTime ? format(parseTime(dateTime), 'yyyy-MM-dd HH:mm:ss') : '-'
}
