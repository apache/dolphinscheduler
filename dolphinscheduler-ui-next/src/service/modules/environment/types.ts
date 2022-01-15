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

interface EnvironmentReq {
  config: string
  name: string
  description?: string
  workerGroups?: string
}

interface EnvironmentCodeReq {
  environmentCode: number
}

interface EnvironmentNameReq {
  environmentName: string
}

interface ListReq {
  pageNo: number
  pageSize: number
  searchVal?: string
}

interface CodeReq {
  code: number
}

interface EnvironmentItem {
  id: number
  code: any
  name: string
  config: string
  description: string
  workerGroups: string[]
  operator: number
  createTime: string
  updateTime: string
}

interface EnvironmentRes {
  totalList: EnvironmentItem[]
  total: number
  totalPage: number
  pageSize: number
  currentPage: number
  start: number
}

export {
  EnvironmentReq,
  EnvironmentCodeReq,
  EnvironmentNameReq,
  ListReq,
  CodeReq,
  EnvironmentItem,
  EnvironmentRes
}
