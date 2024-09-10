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
  ContentReq,
  DescriptionReq,
  CreateReq,
  OnlineCreateReq,
  ProgramTypeReq,
  ListReq,
  ViewResourceReq
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
