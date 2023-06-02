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
import utils from '@/utils'
import {
  ResourceTypeReq,
  NameReq,
  FileNameReq,
  FullNameReq,
  TenantCodeReq,
  IdReq,
  ContentReq,
  DescriptionReq,
  CreateReq,
  UserIdReq,
  OnlineCreateReq,
  ProgramTypeReq,
  ListReq,
  ViewResourceReq,
  UdfFuncReq
} from './types'

export function queryResourceListPaging(
  params: ListReq & ResourceTypeReq & FullNameReq & TenantCodeReq
): any {
  return axios({
    url: '/resources',
    method: 'get',
    params
  })
}

export function queryBaseDir(params: ResourceTypeReq): any {
  return axios({
    url: '/resources/base-dir',
    method: 'get',
    params
  })
}

export function queryCurrentResourceByFileName(
  params: ResourceTypeReq & FileNameReq & TenantCodeReq
): any {
  return axios({
    url: '/resources/query-file-name',
    method: 'get',
    params
  })
}

export function queryCurrentResourceByFullName(
  params: ResourceTypeReq & FullNameReq & TenantCodeReq
): any {
  return axios({
    url: '/resources/query-full-name',
    method: 'get',
    params
  })
}

export function createResource(
  data: CreateReq & FileNameReq & NameReq & ResourceTypeReq
): any {
  return axios({
    url: '/resources',
    method: 'post',
    data,
    timeout: 0
  })
}

export function authorizedFile(params: UserIdReq): any {
  return axios({
    url: '/resources/authed-file',
    method: 'get',
    params
  })
}

export function authorizeResourceTree(params: UserIdReq): any {
  return axios({
    url: '/resources/authed-resource-tree',
    method: 'get',
    params
  })
}

export function authUDFFunc(params: UserIdReq): any {
  return axios({
    url: '/resources/authed-udf-func',
    method: 'get',
    params
  })
}

export function createDirectory(
  data: CreateReq & NameReq & ResourceTypeReq
): any {
  return axios({
    url: '/resources/directory',
    method: 'post',
    data
  })
}

export function queryResourceList(params: ResourceTypeReq & FullNameReq): any {
  return axios({
    url: '/resources/list',
    method: 'get',
    params
  })
}

export function onlineCreateResource(
  data: OnlineCreateReq & FileNameReq & ResourceTypeReq
): any {
  return axios({
    url: '/resources/online-create',
    method: 'post',
    data
  })
}

export function queryResourceByProgramType(
  params: ResourceTypeReq & ProgramTypeReq
): any {
  return axios({
    url: '/resources/query-by-type',
    method: 'get',
    params
  })
}

export function queryUdfFuncListPaging(params: ListReq): any {
  return axios({
    url: '/resources/udf-func',
    method: 'get',
    params
  })
}

export function queryUdfFuncList(params: { type: 'HIVE' | 'SPARK' }): any {
  return axios({
    url: '/resources/udf-func/list',
    method: 'get',
    params
  })
}

export function verifyUdfFuncName(params: NameReq): any {
  return axios({
    url: '/resources/udf-func/verify-name',
    method: 'get',
    params
  })
}

export function deleteUdfFunc(id: number, params: FullNameReq): any {
  return axios({
    url: `/resources/udf-func/${id}`,
    method: 'delete',
    params
  })
}

export function unAuthUDFFunc(params: UserIdReq): any {
  return axios({
    url: '/resources/unauth-udf-func',
    method: 'get',
    params
  })
}

export function verifyResourceName(params: FullNameReq & ResourceTypeReq): any {
  return axios({
    url: '/resources/verify-name',
    method: 'get',
    params
  })
}

export function doesResourceExist(params: FullNameReq & ResourceTypeReq): any {
  return axios({
    url: '/resources/verify-name',
    method: 'get',
    params
  })
}

export function updateResource(
  data: NameReq & ResourceTypeReq & DescriptionReq & FullNameReq & TenantCodeReq
): any {
  return axios({
    url: '/resources',
    method: 'put',
    data
  })
}

export function deleteResource(params: FullNameReq & TenantCodeReq): any {
  return axios({
    url: '/resources',
    method: 'delete',
    params
  })
}

export function downloadResource(params: FullNameReq): void {
  utils.downloadFile('resources/download', params)
}

export function viewUIUdfFunction(id: IdReq): any {
  return axios({
    url: `/resources/${id}/udf-func`,
    method: 'get'
  })
}

export function updateResourceContent(
  data: ContentReq & TenantCodeReq & FullNameReq
): any {
  return axios({
    url: '/resources/update-content',
    method: 'put',
    data
  })
}

export function viewResource(
  params: ViewResourceReq & FullNameReq & TenantCodeReq
): any {
  return axios({
    url: '/resources/view',
    method: 'get',
    params
  })
}

export function createUdfFunc(data: UdfFuncReq): any {
  return axios({
    url: '/resources/udf-func',
    method: 'post',
    data
  })
}

export function updateUdfFunc(data: UdfFuncReq, id: number): any {
  return axios({
    url: `/resources/udf-func/${id}`,
    method: 'put',
    data
  })
}
