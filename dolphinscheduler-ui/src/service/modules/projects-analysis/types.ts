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

interface CodeReq {
  projectCode?: number
}

interface StateReq extends CodeReq {
  endDate?: string
  startDate?: string
}

interface UserList {
  userName: string
  userId: number
  count: number
}

interface ProcessDefinitionRes {
  count: number
  userList: UserList[]
}

interface WorkflowInstanceCountVo {
  totalCount: number
  workflowInstanceStatusCounts: WorkflowInstanceStatusCountDto[]
}

interface WorkflowInstanceStatusCountDto {
  count: number
  state: string
}

interface TaskInstanceCountVo {
  totalCount: number
  taskInstanceStatusCounts: TaskInstanceStatusCountDto[]
}

interface TaskInstanceStatusCountDto {
  count: number
  state: string
}

interface TaskQueueRes {
  taskKill: number
  taskQueue: number
}

interface CommandStateRes {
  errorCount: number
  normalCount: number
  commandState: string
}

interface ListReq {
  pageNo: number
  pageSize: number
  searchVal?: string
}

export {
  CodeReq,
  StateReq,
  ProcessDefinitionRes,
  WorkflowInstanceCountVo,
  TaskInstanceCountVo,
  TaskQueueRes,
  CommandStateRes,
  ListReq
}
