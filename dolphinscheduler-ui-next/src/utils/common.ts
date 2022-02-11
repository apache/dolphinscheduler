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
  {
    value: 'SUBMITTED_SUCCESS',
    label: `${t('project.workflow.submit_success')}`
  },
  {
    value: 'RUNNING_EXECUTION',
    label: `${t('project.workflow.running')}`
  },
  {
    value: 'READY_PAUSE',
    label: `${t('project.workflow.ready_to_pause')}`
  },
  {
    value: 'PAUSE',
    label: `${t('project.workflow.pause')}`
  },
  {
    value: 'READY_STOP',
    label: `${t('project.workflow.ready_to_stop')}`
  },
  {
    value: 'STOP',
    label: `${t('project.workflow.stop')}`
  },
  {
    value: 'FAILURE',
    label: `${t('project.workflow.failed')}`
  },
  {
    value: 'SUCCESS',
    label: `${t('project.workflow.success')}`
  },
  {
    value: 'NEED_FAULT_TOLERANCE',
    label: `${t('project.workflow.need_fault_tolerance')}`
  },
  {
    value: 'KILL',
    label: `${t('project.workflow.kill')}`
  },
  {
    value: 'WAITING_THREAD',
    label: `${t('project.workflow.waiting_for_thread')}`
  },
  {
    value: 'WAITING_DEPEND',
    label: `${t('project.workflow.waiting_for_dependency_to_complete')}`
  },
  {
    value: 'DELAY_EXECUTION',
    label: `${t('project.workflow.delay_execution')}`
  },
  {
    value: 'FORCED_SUCCESS',
    label: `${t('project.workflow.forced_success')}`
  },
  {
    value: 'SERIAL_WAIT',
    label: `${t('project.workflow.serial_wait')}`
  }
]
