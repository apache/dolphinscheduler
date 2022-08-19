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

interface ProjectCodeReq {
  projectCode: number
}

interface IdReq {
  id: number
}

interface TaskListReq {
  pageNo: number
  pageSize: number
  endDate?: string
  executorName?: string
  host?: string
  processInstanceId?: number
  processInstanceName?: string
  processDefinitionName?: string
  searchVal?: string
  startDate?: string
  stateType?: string
  taskName?: string
  taskExecuteType?: 'BATCH' | 'STREAM'
}

interface Dependency {
  localParams?: any
  varPool?: any
  dependTaskList?: any
  relation?: any
  resourceFilesList: any[]
  varPoolMap?: any
  localParametersMap?: any
}

interface SwitchDependency extends Dependency {
  nextNode?: any
  resultConditionLocation: number
  dependTaskList?: any
}

interface TotalList {
  taskComplete: boolean
  firstRun: boolean
  environmentCode: number
  processInstance?: any
  pid: number
  appLink: string
  taskCode: any
  switchTask: boolean
  host: string
  id: number
  state: string
  workerGroup: string
  conditionsTask: boolean
  processInstancePriority?: any
  processInstanceId: number
  dependency: Dependency
  alertFlag: string
  dependentResult?: any
  executePath: string
  switchDependency: SwitchDependency
  maxRetryTimes: number
  executorName: string
  subProcess: boolean
  submitTime: string
  taskGroupId: number
  name: string
  taskDefinitionVersion: number
  processInstanceName: string
  taskGroupPriority: number
  taskDefine?: any
  dryRun: number
  flag: string
  taskParams: string
  duration: string
  processDefine?: any
  taskType: string
  taskInstancePriority: string
  logPath: string
  startTime: string
  environmentConfig?: any
  executorId: number
  firstSubmitTime: string
  resources?: any
  retryTimes: number
  varPool: string
  dependTask: boolean
  delayTime: number
  retryInterval: number
  endTime: string
}

interface TaskInstancesRes {
  totalList: TotalList[]
  total: number
  totalPage: number
  pageSize: number
  currentPage: number
  start: number
}

export {
  ProjectCodeReq,
  IdReq,
  TaskListReq,
  Dependency,
  SwitchDependency,
  TotalList,
  TaskInstancesRes
}
