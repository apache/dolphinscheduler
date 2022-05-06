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

interface UserNameReq {
  userName?: string
}

interface UserNamesReq {
  userNames?: string
}

interface AlertGroupIdReq {
  alertgroupId: string
}

interface UserReq {
  email: string
  tenantId: number | null
  userName: string
  userPassword: string
  phone?: string
  queue?: string
  state?: number
  timeZone?: string
}

interface IdReq {
  id: number
}

interface UserIdReq {
  userId: number
}

interface GrantDataSourceReq extends UserIdReq {
  datasourceIds: string
}

interface GrantResourceReq extends UserIdReq {
  resourceIds: string
}

interface GrantProject extends UserIdReq {
  projectIds: string
}

interface ProjectCodeReq {
  projectCode: string
}

interface GrantUDFReq {
  udfIds: string
}

interface GrantNamespaceReq {
  namespaceIds: string
}

interface ListAllReq extends UserReq {
  alertGroup?: string
  createTime?: string
  id?: number
  queueName?: string
  tenantCode?: string
  updateTime?: string
  userType?: 'ADMIN_USER' | 'GENERAL_USER'
}

interface ListReq {
  pageNo: number
  pageSize: number
  searchVal?: string
}

interface RegisterUserReq {
  email: string
  repeatPassword: string
  userName: string
  userPassword: string
}

interface UserInfoRes extends UserReq, IdReq {
  userType: string
  tenantCode?: any
  queueName?: any
  alertGroup?: any
  createTime: string
  updateTime: string
}

interface UserListRes {
  id: number
  userName: string
  userPassword: string
  email: string
  phone: string
  userType: string
  tenantId: number
  state: number
  tenantCode?: any
  queueName?: any
  alertGroup?: any
  queue: string
  createTime: string
  updateTime: string
}

export {
  UserNameReq,
  UserNamesReq,
  AlertGroupIdReq,
  UserReq,
  IdReq,
  UserIdReq,
  GrantDataSourceReq,
  GrantResourceReq,
  GrantProject,
  ProjectCodeReq,
  GrantUDFReq,
  GrantNamespaceReq,
  ListAllReq,
  ListReq,
  RegisterUserReq,
  UserInfoRes,
  UserListRes
}
