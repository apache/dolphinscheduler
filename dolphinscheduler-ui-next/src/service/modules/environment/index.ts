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
  EnvironmentReq,
  EnvironmentCodeReq,
  EnvironmentNameReq,
  ListReq,
  CodeReq
} from './types'

export function createEnvironment(data: EnvironmentReq): any {
  return axios({
    url: '/environment/create',
    method: 'post',
    data
  })
}

export function deleteEnvironmentByCode(data: EnvironmentCodeReq): any {
  return axios({
    url: '/environment/delete',
    method: 'post',
    data
  })
}

export function queryEnvironmentListPaging(params: ListReq): any {
  return axios({
    url: '/environment/list-paging',
    method: 'get',
    params
  })
}

export function queryEnvironmentByCode(params: EnvironmentCodeReq): any {
  return axios({
    url: '/environment/query-by-code',
    method: 'get',
    params
  })
}

export function queryAllEnvironmentList(): any {
  return axios({
    url: '/environment/query-environment-list',
    method: 'get'
  })
}

export function updateEnvironment(data: EnvironmentReq & CodeReq): any {
  return axios({
    url: '/environment/update',
    method: 'post',
    data
  })
}

export function verifyEnvironment(data: EnvironmentNameReq): any {
  return axios({
    url: '/environment/verify-environment',
    method: 'post',
    data
  })
}
