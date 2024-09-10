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
import type { IWorkflowExecutionState } from '@/common/types'

interface CodeReq {
  projectCode: number
}

interface WorkflowInstanceListReq {
  pageNo: number
  pageSize: number
  endDate?: string
  executorName?: string
  host?: string
  workflowDefinitionCode?: number
  workflowDefinitionVersion?: number
  searchVal?: string
  startDate?: string
  stateType?: string
}

interface BatchDeleteReq {
  workflowInstanceIds: string
  projectName?: string
  alertGroup?: string
  createTime?: string
  email?: string
  id?: number
  phone?: string
  queue?: string
  queueName?: string
  state?: number
  tenantCode?: string
  tenantId?: number
  updateTime?: string
  userName?: string
  userPassword?: string
  userType?: string
}

interface SubIdReq {
  subId: number
}

interface TaskReq {
  taskCode?: string
  taskId?: number
}

interface LongestReq {
  endTime: string
  size: number
  startTime: string
}

interface IdReq {
  id: number
}

interface WorkflowInstanceReq {
  syncDefine: boolean
  flag?: string
  globalParams?: string
  locations?: string
  scheduleTime?: string
  taskDefinitionJson?: string
  taskRelationJson?: string
  timeout?: number
}

interface IWorkflowInstance {
  id: number
  name: string
  state: IWorkflowExecutionState
  commandType: string
  scheduleTime?: string
  workflowDefinitionCode?: number
  startTime: string
  endTime: string
  duration?: string
  runTimes: number
  recovery: string
  dryRun: number
  executorName: string
  host: string
  count?: number
  disabled?: boolean
  buttonType?: string
  testFlag: number
}

export {
  CodeReq,
  WorkflowInstanceListReq,
  BatchDeleteReq,
  SubIdReq,
  TaskReq,
  LongestReq,
  IdReq,
  WorkflowInstanceReq,
  IWorkflowInstance
}
