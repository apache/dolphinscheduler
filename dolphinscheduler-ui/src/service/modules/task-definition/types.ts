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

interface PageReq {
  pageNo: number
  pageSize: number
}

interface ListReq extends PageReq {
  searchVal?: string
}

interface ProjectCodeReq {
  projectCode: number
}

interface TaskDefinitionListReq extends ListReq {
  taskType?: string
  userId?: number
}

interface TaskDefinitionJsonReq {
  taskDefinitionJson: string
}

interface CodeReq {
  code: any
}

interface TaskDefinitionJsonObjReq {
  taskDefinitionJsonObj: string
}

interface ReleaseStateReq {
  releaseState: 'OFFLINE' | 'ONLINE'
}

interface VersionReq {
  version: number
}

interface TaskDefinitionItem {
  taskName: string
  taskCode: any
  taskVersion: number
  taskType: string
  taskCreateTime: string
  taskUpdateTime: string
  processDefinitionCode: any
  processDefinitionVersion: number
  processDefinitionName: string
  processReleaseState: string
  upstreamTaskMap: any
  upstreamTaskCode: number
  upstreamTaskName: string
}

interface TaskDefinitionRes {
  totalList: TaskDefinitionItem[]
  total: number
  totalPage: number
  pageSize: number
  currentPage: number
  start: number
}

interface TaskDefinitionVersionItem {
  id: number
  code: number
  name: string
  version: number
  description: string
  projectCode: number
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
  timeoutNotifyStrategy?: any
  timeout: number
  delayTime: number
  resourceIds: string
  createTime: string
  updateTime: string
  modifyBy?: any
  taskGroupId: number
  taskGroupPriority: number
  operator: number
  operateTime: string
  dependence: string
}

interface TaskDefinitionVersionRes {
  totalList: TaskDefinitionVersionItem[]
  total: number
  totalPage: number
  pageSize: number
  currentPage: number
  start: number
}

interface ISingleSaveReq {
  processDefinitionCode?: string
  upstreamCodes: string
  taskDefinitionJsonObj: string
}

export {
  PageReq,
  ListReq,
  ProjectCodeReq,
  TaskDefinitionListReq,
  TaskDefinitionJsonReq,
  CodeReq,
  TaskDefinitionJsonObjReq,
  ReleaseStateReq,
  VersionReq,
  TaskDefinitionItem,
  TaskDefinitionRes,
  TaskDefinitionVersionItem,
  TaskDefinitionVersionRes,
  ISingleSaveReq
}
