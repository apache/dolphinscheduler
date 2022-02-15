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
import type { INodeData, ITaskData } from './types'

export function formatParams(data: INodeData): {
  processDefinitionCode: string
  upstreamCodes: string
  taskDefinitionJsonObj: object
} {
  const params = {
    processDefinitionCode: data.processCode ? String(data.processCode) : '',
    upstreamCodes: '',
    taskDefinitionJsonObj: {
      ...omit(data, [
        'processCode',
        'delayTime',
        'environmentCode',
        'failRetryTimes',
        'failRetryInterval',
        'taskGroupId',
        'localParams',
        'timeoutFlag',
        'timeoutNotifyStrategy',
        'resourceList'
      ]),
      code: data.code,
      delayTime: data.delayTime ? '0' : String(data.delayTime),
      environmentCode: data.environmentCode || -1,
      failRetryTimes: data.failRetryTimes ? String(data.failRetryTimes) : '0',
      failRetryInterval: data.failRetryTimes
        ? String(data.failRetryTimes)
        : '0',
      taskGroupId: data.taskGroupId || '',
      taskParams: {
        localParams: data.localParams,
        rawScript: data.rawScript,
        resourceList: data.resourceList?.length
          ? data.resourceList.map((id: number) => ({ id }))
          : []
      },
      timeoutFlag: data.timeoutFlag ? 'OPEN' : 'CLOSE',
      timeoutNotifyStrategy: data.timeoutNotifyStrategy?.join('')
    }
  } as {
    processDefinitionCode: string
    upstreamCodes: string
    taskDefinitionJsonObj: { timeout: number; timeoutNotifyStrategy: string }
  }
  if (!data.timeoutFlag) {
    params.taskDefinitionJsonObj.timeout = 0
    params.taskDefinitionJsonObj.timeoutNotifyStrategy = ''
  }
  return params
}

export function formatModel(data: ITaskData) {
  const params = {
    name: data.name,
    taskType: data.taskType,
    processName: data.processName,
    flag: data.flag,
    description: data.description,
    taskPriority: data.taskPriority,
    workerGroup: data.workerGroup,
    environmentCode: data.environmentCode === -1 ? null : data.environmentCode,
    taskGroupId: data.taskGroupId,
    taskGroupPriority: data.taskGroupPriority,
    failRetryTimes: data.failRetryTimes,
    failRetryInterval: data.failRetryInterval,
    delayTime: data.delayTime,
    timeoutFlag: data.timeoutFlag === 'OPEN',
    timeoutNotifyStrategy: [data.timeoutNotifyStrategy] || [],
    resourceList: data.taskParams.resourceList,
    timeout: data.timeout,
    rawScript: data.taskParams.rawScript,
    localParams: data.taskParams.localParams,
    preTasks: [],
    id: data.id,
    code: data.code
  } as {
    timeoutNotifyStrategy: string[]
    resourceList: number[]
  }
  if (data.timeoutNotifyStrategy === 'WARNFAILED') {
    params.timeoutNotifyStrategy = ['WARN', 'FAILED']
  }
  if (data.taskParams.resourceList) {
    params.resourceList = data.taskParams.resourceList.map(
      (item: { id: number }) => item.id
    )
  }
  return params
}
