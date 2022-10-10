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

interface FileReq {
  file: any
}

interface ResourceTypeReq {
  type: 'FILE' | 'UDF'
  programType?: string
}

interface UdfTypeReq {
  type: 'HIVE' | 'SPARK'
}

interface NameReq {
  name: string
}

interface FileNameReq {
  fileName: string
}

interface FullNameReq {
  fullName: string
}

interface IdReq {
  id: number
}

interface ContentReq {
  content: string
}

interface DescriptionReq {
  description?: string
}

interface CreateReq extends ResourceTypeReq, DescriptionReq {
  currentDir: string
  pid: number
}

interface UserIdReq {
  userId: number
}

interface OnlineCreateReq extends CreateReq, ContentReq {
  suffix: string
}

interface ProgramTypeReq {
  programType: 'JAVA' | 'SCALA' | 'PYTHON' | 'SQL'
}

interface ListReq {
  pageNo: number
  pageSize: number
  searchVal?: string
}

interface ViewResourceReq {
  limit: number
  skipLineNum: number
}

interface ResourceIdReq {
  resourceId: number
}

interface UdfFuncReq extends UdfTypeReq, DescriptionReq, ResourceIdReq {
  className: string
  funcName: string
  argTypes?: string
  database?: string
}

interface ResourceFile {
  id: number
  pid: number
  alias: string
  userName: string
  userId: number
  type: string
  directory: boolean
  fileName: string
  fullName: string
  description: string
  size: number
  updateTime: string
}

interface ResourceListRes {
  currentPage: number
  pageSize: number
  start: number
  total: number
  totalList: ResourceFile[]
}

interface ResourceViewRes {
  alias: string
  content: string
}

export {
  FileReq,
  ResourceTypeReq,
  UdfTypeReq,
  NameReq,
  FileNameReq,
  FullNameReq,
  IdReq,
  ContentReq,
  DescriptionReq,
  CreateReq,
  UserIdReq,
  OnlineCreateReq,
  ProgramTypeReq,
  ListReq,
  ViewResourceReq,
  ResourceIdReq,
  UdfFuncReq,
  ResourceListRes,
  ResourceViewRes,
  ResourceFile
}
