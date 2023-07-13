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

interface CodeReq {
  code: number
}

interface ListReq {
  pageNo: number
  pageSize: number
  searchVal?: string
}

interface ProcessDefinitionCodeReq {
  processDefinitionCode?: number
}

interface ScheduleReq {
  schedule?: string
}

interface WorkerGroupIdReq {
  workerGroupId?: number
}

interface ScheduleListReq extends ListReq, ProcessDefinitionCodeReq {
  processDefinitionId: number
}

interface CreateScheduleReq extends ScheduleReq, ProcessDefinitionCodeReq {
  environmentCode?: number
  failureStrategy?: 'END' | 'CONTINUE'
  processInstancePriority?: 'HIGHEST' | 'HIGH' | 'MEDIUM' | 'LOW' | 'LOWEST'
  warningGroupId?: number
  warningType?: 'NONE' | 'SUCCESS' | 'FAILURE' | 'ALL'
  workerGroup?: string
}

interface DeleteScheduleReq extends IdReq {
  alertGroup?: string
  createTime?: string
  email?: string
  phone?: string
  queue?: string
  queueName?: string
  state?: number
  tenantCode?: string
  tenantId?: number
  updateTime?: string
  userName?: string
  userPassword?: string
  userType?: 'ADMIN_USER' | 'GENERAL_USER'
}

export {
  ProjectCodeReq,
  IdReq,
  CodeReq,
  ListReq,
  ProcessDefinitionCodeReq,
  ScheduleReq,
  WorkerGroupIdReq,
  ScheduleListReq,
  CreateScheduleReq,
  DeleteScheduleReq
}
