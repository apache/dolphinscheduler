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
  projectCode: number
}

interface CodesReq {
  codes: string
}

interface FileReq {
  file: any
}

interface NameReq {
  name: string
}

interface ReleaseStateReq {
  releaseState: 'OFFLINE' | 'ONLINE'
}

interface VersionReq {
  version: number
}

interface LimitReq {
  limit: number
}

interface PageReq {
  pageNo: number
  pageSize: number
}

interface ListReq extends PageReq {
  searchVal?: string
  userId?: number
}

interface ProcessDefinitionReq {
  name: string
  locations: string
  taskDefinitionJson: string
  taskRelationJson: string
  tenantCode: string
  executionType: string
  description?: string
  globalParams?: string
  timeout?: number
}

interface TargetCodeReq {
  targetProjectCode: number
}

interface SimpleListRes {
  id: number
  code: any
  name: string
  projectCode: any
}

export {
  CodeReq,
  CodesReq,
  FileReq,
  NameReq,
  ReleaseStateReq,
  VersionReq,
  LimitReq,
  PageReq,
  ListReq,
  ProcessDefinitionReq,
  TargetCodeReq,
  SimpleListRes
}
