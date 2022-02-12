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

import { omit } from 'lodash'
import type { IDataNode, ITask } from './types'

export function useData({
  nodeData,
  type,
  taskDefinition
}: {
  nodeData: IDataNode
  type: string
  taskDefinition?: ITask
}) {
  const data = {
    backfill: {},
    isCreate: false
  }

  if (type === 'task-definition') {
    if (taskDefinition) {
      data.backfill = formatBackfill(taskDefinition, nodeData.taskType)
      data.isCreate = false
    }
  }

  return {
    code: nodeData.id
  }
}

export function formatBackfill(task: ITask, taskType: string) {
  let strategy: string | undefined = task.timeoutNotifyStrategy
  if (taskType === 'DEPENDENT' && task.timeoutNotifyStrategy === 'WARNFAILED') {
    strategy = 'WARN,FAILED'
  }
  return {
    code: task.code,
    conditionResult: task.taskParams.conditionResult,
    switchResult: task.taskParams.switchResult,
    delayTime: task.delayTime,
    dependence: task.taskParams.dependence,
    desc: task.description,
    id: task.id,
    maxRetryTimes: task.failRetryTimes,
    name: task.name,
    params: omit(task.taskParams, [
      'conditionResult',
      'dependence',
      'waitStartTimeout',
      'switchResult'
    ]),
    retryInterval: task.failRetryInterval,
    runFlag: task.flag,
    taskInstancePriority: task.taskPriority,
    timeout: {
      interval: task.timeout,
      strategy,
      enable: task.timeoutFlag === 'OPEN'
    },
    type: task.taskType,
    waitStartTimeout: task.taskParams.waitStartTimeout,
    workerGroup: task.workerGroup,
    environmentCode: task.environmentCode,
    taskGroupId: task.taskGroupId,
    taskGroupPriority: task.taskGroupPriority
  }
}
