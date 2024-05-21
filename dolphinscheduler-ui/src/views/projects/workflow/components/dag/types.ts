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

import type { TaskType } from '@/store/project/types'
export type { ITaskState } from '@/common/types'

export interface ProcessDefinition {
  id: number
  code: number
  name: string
  version: number
  releaseState: string
  projectCode: number
  description: string
  globalParams: string
  globalParamList: any[]
  globalParamMap: any
  createTime: string
  updateTime: string
  flag: string
  userId: number
  userName?: any
  projectName?: any
  locations: string
  scheduleReleaseState?: any
  timeout: number
  tenantId: number
  tenantCode: string
  executionType: string
  modifyBy?: any
  warningGroupId: number
}

export interface Connect {
  id?: number
  name: string
  processDefinitionVersion?: number
  projectCode?: number
  processDefinitionCode?: number
  preTaskCode: number
  preTaskVersion: number
  postTaskCode: number
  postTaskVersion: number
  conditionType: string
  conditionParams: any
  createTime?: string
  updateTime?: string
}

export interface TaskDefinition {
  id: number
  code: number
  name: string
  version: number
  description: string
  projectCode: any
  userId: number
  taskType: TaskType
  taskParams: any
  taskParamList: any[]
  taskParamMap: any
  flag: string
  taskPriority: string
  userName: any
  projectName?: any
  workerGroup: string
  environmentCode: number
  failRetryTimes: number
  failRetryInterval: number
  timeoutFlag: 'OPEN' | 'CLOSE'
  timeoutNotifyStrategy: string
  timeout: number
  delayTime: number
  resourceIds: string
  createTime: string
  updateTime: string
  modifyBy: any
  dependence: string
}

export type NodeData = {
  code: number
  taskType: TaskType
  name: string
} & Partial<TaskDefinition>

export interface WorkflowDefinition {
  processDefinition: ProcessDefinition
  processTaskRelationList: Connect[]
  taskDefinitionList: TaskDefinition[]
}

export interface WorkflowInstance {
  name: string
  state: string
  dagData: WorkflowDefinition
  commandType: string
  commandParam: string
  failureStrategy: string
  processInstancePriority: string
  workerGroup: string
  tenantCode: string
  warningType: string
  warningGroupId: number
}

export interface EditWorkflowDefinition {
  processDefinition: ProcessDefinition
  processTaskRelationList: Connect[]
  taskDefinitionList: NodeData[]
}

export interface Dragged {
  x: number
  y: number
  type: TaskType
}

export interface Coordinate {
  x: number
  y: number
}

export interface GlobalParam {
  key: string
  direct: string
  type: string
  value: string
}

export interface SaveForm {
  name: string
  description: string
  executionType: string
  timeoutFlag: boolean
  timeout: number
  globalParams: GlobalParam[]
  release: boolean
  sync: boolean
}

export interface Location {
  taskCode: number
  x: number
  y: number
}

export interface IStartupParam {
  commandType: string
  commandParam: string
  failureStrategy: string
  processInstancePriority: string
  workerGroup: string
  tenantCode: string
  warningType: string
  warningGroupId: number
}

export interface IWorkflowTaskInstance {
  id: number
  taskCode: number
  taskType: string
}

export { TaskType }
