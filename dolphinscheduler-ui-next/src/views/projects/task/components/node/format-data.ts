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
import type { INodeData, ITaskData, ITaskParams } from './types'

export function formatParams(data: INodeData): {
  processDefinitionCode: string
  upstreamCodes: string
  taskDefinitionJsonObj: object
} {
  const taskParams: ITaskParams = {}
  if (data.taskType === 'SPARK') {
    taskParams.programType = data.programType
    taskParams.sparkVersion = data.sparkVersion
    taskParams.mainClass = data.mainClass
    if (data.mainJar) {
      taskParams.mainJar = { id: data.mainJar }
    }
    taskParams.deployMode = data.deployMode
    taskParams.appName = data.appName
    taskParams.driverCores = data.driverCores
    taskParams.driverMemory = data.driverMemory
    taskParams.numExecutors = data.numExecutors
    taskParams.executorMemory = data.executorMemory
    taskParams.executorCores = data.executorCores
    taskParams.mainArgs = data.mainArgs
    taskParams.others = data.others
  }
  if (data.taskType === 'MR') {
    taskParams.programType = data.programType
    taskParams.mainClass = data.mainClass
    if (data.mainJar) {
      taskParams.mainJar = { id: data.mainJar }
    }
    taskParams.appName = data.appName
    taskParams.mainArgs = data.mainArgs
    taskParams.others = data.others
  }

  const params = {
    processDefinitionCode: data.processName ? String(data.processName) : '',
    upstreamCodes: data?.preTasks?.join(','),
    taskDefinitionJsonObj: {
      code: data.code,
      delayTime: data.delayTime ? String(data.delayTime) : '0',
      description: data.description,
      environmentCode: data.environmentCode || -1,
      failRetryInterval: data.failRetryInterval
        ? String(data.failRetryInterval)
        : '0',
      failRetryTimes: data.failRetryTimes ? String(data.failRetryTimes) : '0',
      flag: data.flag,
      name: data.name,
      taskGroupId: data.taskGroupId || 0,
      taskGroupPriority: data.taskGroupPriority,
      taskParams: {
        localParams: data.localParams,
        rawScript: data.rawScript,
        resourceList: data.resourceList?.length
          ? data.resourceList.map((id: number) => ({ id }))
          : [],
        ...taskParams
      },
      taskPriority: data.taskPriority,
      taskType: data.taskType,
      timeout: data.timeout,
      timeoutFlag: data.timeoutFlag ? 'OPEN' : 'CLOSE',
      timeoutNotifyStrategy: data.timeoutNotifyStrategy?.join(''),
      workerGroup: data.workerGroup
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
    ...omit(data, [
      'environmentCode',
      'timeoutFlag',
      'timeoutNotifyStrategy',
      'taskParams'
    ]),
    ...omit(data.taskParams, ['resourceList', 'mainJar', 'localParams']),
    environmentCode: data.environmentCode === -1 ? null : data.environmentCode,
    timeoutFlag: data.timeoutFlag === 'OPEN',
    timeoutNotifyStrategy: [data.timeoutNotifyStrategy] || [],
    localParams: data.taskParams?.localParams || []
  } as INodeData
  if (data.timeoutNotifyStrategy === 'WARNFAILED') {
    params.timeoutNotifyStrategy = ['WARN', 'FAILED']
  }
  if (data.taskParams?.resourceList) {
    params.resourceList = data.taskParams.resourceList.map(
      (item: { id: number }) => item.id
    )
  }
  if (data.taskParams?.mainJar) {
    params.mainJar = data.taskParams?.mainJar.id
  }
  return params
}
