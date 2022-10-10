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
  UdfFuncReq
} from './types'

export function queryResourceListPaging(
  params: ListReq & IdReq & ResourceTypeReq
): any {
  return axios({
    url: '/resources',
    method: 'get',
    params
  })
}

export function queryResourceById(
  params: ResourceTypeReq & FullNameReq & IdReq,
  id: number
): any {
  return axios({
    url: `/resources/${id}`,
    method: 'get',
    params
  })
}

export function queryCurrentResourceById(id: number): any {
  return axios({
    url: `/resources/${id}/query`,
    method: 'get'
  })
}

export function createResource(
  data: CreateReq & FileNameReq & NameReq & ResourceTypeReq
): any {
  return axios({
    url: '/resources',
    method: 'post',
    data
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

export function queryResourceList(params: ResourceTypeReq): any {
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

export function deleteUdfFunc(id: number): any {
  return axios({
    url: `/resources/udf-func/${id}`,
    method: 'delete'
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

export function queryResource(
  params: FullNameReq & ResourceTypeReq,
  id: IdReq
): any {
  return axios({
    url: `/resources/verify-name/${id}`,
    method: 'get',
    params
  })
}

export function updateResource(
  data: NameReq & ResourceTypeReq & IdReq & DescriptionReq,
  id: number
): any {
  return axios({
    url: `/resources/${id}`,
    method: 'put',
    data
  })
}

export function deleteResource(id: number): any {
  return axios({
    url: `/resources/${id}`,
    method: 'delete'
  })
}

export function downloadResource(id: number): void {
  utils.downloadFile(`resources/${id}/download`)
}

export function viewUIUdfFunction(id: IdReq): any {
  return axios({
    url: `/resources/${id}/udf-func`,
    method: 'get'
  })
}

export function updateResourceContent(data: ContentReq, id: number): any {
  return axios({
    url: `/resources/${id}/update-content`,
    method: 'put',
    data
  })
}

export function viewResource(params: ViewResourceReq, id: number): any {
  return axios({
    url: `/resources/${id}/view`,
    method: 'get',
    params
  })
}

export function createUdfFunc(
  data: UdfFuncReq,
  resourceId: ResourceIdReq
): any {
  return axios({
    url: `/resources/${resourceId}/udf-func`,
    method: 'post',
    data
  })
}

export function updateUdfFunc(
  data: UdfFuncReq,
  resourceId: ResourceIdReq,
  id: number
): any {
  return axios({
    url: `/resources/${resourceId}/udf-func/${id}`,
    method: 'put',
    data
  })
}
