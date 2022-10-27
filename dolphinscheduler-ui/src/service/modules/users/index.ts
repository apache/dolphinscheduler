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

import { axios } from '@/service/service'
import {
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
  RegisterUserReq
} from './types'

export function activateUser(data: UserNameReq): any {
  return axios({
    url: '/users/activate',
    method: 'post',
    data
  })
}

export function authorizedUser(params: AlertGroupIdReq): any {
  return axios({
    url: '/users/authed-user',
    method: 'get',
    params
  })
}

export function batchActivateUser(data: UserNamesReq): any {
  return axios({
    url: '/users/batch/activate',
    method: 'post',
    data
  })
}

export function createUser(data: UserReq): any {
  return axios({
    url: '/users/create',
    method: 'post',
    data
  })
}

export function delUserById(data: IdReq) {
  return axios({
    url: '/users/delete',
    method: 'post',
    data
  })
}

export function getUserInfo(): any {
  return axios({
    url: '/users/get-user-info',
    method: 'get'
  })
}

export function grantDataSource(data: GrantDataSourceReq) {
  return axios({
    url: '/users/grant-datasource',
    method: 'post',
    data
  })
}

export function grantResource(data: GrantResourceReq) {
  return axios({
    url: '/users/grant-file',
    method: 'post',
    data
  })
}

export function revokeProjectById(data: GrantProject) {
  return axios({
    url: '/users/revoke-project-by-id',
    method: 'post',
    data
  })
}

export function grantProject(data: GrantProject) {
  return axios({
    url: '/users/grant-project',
    method: 'post',
    data
  })
}

export function grantProjectWithReadPerm(data: GrantProject) {
  return axios({
    url: 'users/grant-project-with-read-perm',
    method: 'post',
    data
  })
}

export function grantProjectByCode(data: ProjectCodeReq & UserIdReq): any {
  return axios({
    url: '/users/grant-project-by-code',
    method: 'post',
    data
  })
}

export function grantUDFFunc(data: GrantUDFReq & UserIdReq) {
  return axios({
    url: '/users/grant-udf-func',
    method: 'post',
    data
  })
}

export function grantNamespaceFunc(data: GrantNamespaceReq & UserIdReq) {
  return axios({
    url: '/users/grant-namespace',
    method: 'post',
    data
  })
}

export function listUser(): any {
  return axios({
    url: '/users/list',
    method: 'get'
  })
}

export function listAll(params?: ListAllReq): any {
  return axios({
    url: '/users/list-all',
    method: 'get',
    params
  })
}

export function queryUserList(params: ListReq): any {
  return axios({
    url: '/users/list-paging',
    method: 'get',
    params
  })
}

export function registerUser(data: RegisterUserReq): any {
  return axios({
    url: '/users/register',
    method: 'post',
    data
  })
}

export function revokeProject(data: ProjectCodeReq & UserIdReq): any {
  return axios({
    url: '/users/revoke-project',
    method: 'post',
    data
  })
}

export function unauthorizedUser(params: AlertGroupIdReq): any {
  return axios({
    url: '/users/unauth-user',
    method: 'get',
    params
  })
}

export function updateUser(data: IdReq & UserReq) {
  return axios({
    url: '/users/update',
    method: 'post',
    data
  })
}

export function verifyUserName(params: UserNameReq) {
  return axios({
    url: '/users/verify-user-name',
    method: 'get',
    params
  })
}
