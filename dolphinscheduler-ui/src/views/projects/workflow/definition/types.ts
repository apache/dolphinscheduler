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

export interface IDefinitionParam {
  pageSize: number
  pageNo: number
  searchVal: string | undefined
}

export interface IDefinitionData {
  code: number
  createTime: string
  description: string
  executionType: string
  flag: 'YES' | 'NO'
  globalParamList: any
  globalParamMap: Object
  globalParams: string
  id: number
  locations: any
  modifyBy: string
  name: string
  projectCode: number
  projectName: any
  releaseState: string
  scheduleReleaseState: any
  tenantCode: any
  tenantId: number
  timeout: number
  updateTime: string
  userId: number
  userName: string
  version: number
  warningGroupId: number
}

export interface ICrontabData {
  id: number
  crontab: string
  definitionDescription: string
  endTime: string
  environmentCode: number
  failureStrategy: string
  processDefinitionCode: number
  processDefinitionName: string
  processInstancePriority: string
  projectName: string
  releaseState: 'ONLINE' | 'OFFLINE'
  startTime: string
  timezoneId: string
  createTime: string
  updateTime: string
  userId: 1
  userName: string
  warningGroupId: number
  warningType: string
  workerGroup: string
}
