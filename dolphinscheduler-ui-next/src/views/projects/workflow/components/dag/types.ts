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
  modifyBy?: any
  warningGroupId: number
}

export interface ProcessTaskRelationList {
  id: number
  name: string
  processDefinitionVersion: number
  projectCode: any
  processDefinitionCode: any
  preTaskCode: number
  preTaskVersion: number
  postTaskCode: any
  postTaskVersion: number
  conditionType: string
  conditionParams: any
  createTime: string
  updateTime: string
}

export interface TaskDefinitionList {
  id: number
  code: any
  name: string
  version: number
  description: string
  projectCode: any
  userId: number
  taskType: string
  taskParams: any
  taskParamList: any[]
  taskParamMap: any
  flag: string
  taskPriority: string
  userName?: any
  projectName?: any
  workerGroup: string
  environmentCode: number
  failRetryTimes: number
  failRetryInterval: number
  timeoutFlag: string
  timeoutNotifyStrategy: string
  timeout: number
  delayTime: number
  resourceIds: string
  createTime: string
  updateTime: string
  modifyBy?: any
  dependence: string
}

export interface WorkflowDefinition {
  processDefinition: ProcessDefinition
  processTaskRelationList: ProcessTaskRelationList[]
  taskDefinitionList: TaskDefinitionList[]
}
