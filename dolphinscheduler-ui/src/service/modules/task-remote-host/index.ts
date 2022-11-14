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
  TaskRemoteHostReq,
  TaskRemoteHostCodeReq,
  TaskRemoteHostNameReq,
  ListReq,
  CodeReq
} from './types'

export function createTaskRemoteHost(data: TaskRemoteHostReq): any {
  return axios({
    url: '/remote_host',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    transformRequest: (params) => JSON.stringify(params)
  })
}

export function deleteTaskRemoteHostByCode(code: number): any {
  return axios({
    url: `/remote_host/${code}`,
    method: 'delete',
    params: { code }
  })
}

export function queryTaskRemoteHostListPaging(params: ListReq): any {
  return axios({
    url: '/remote_host/list-paging',
    method: 'get',
    params
  })
}

export function queryTaskRemoteHostByCode(params: TaskRemoteHostCodeReq): any {
  return axios({
    url: '/remote_host/query-by-code',
    method: 'get',
    params
  })
}

export function queryAllTaskRemoteHostList(): any {
  return axios({
    url: '/remote_host/query-remote-host-list',
    method: 'get'
  })
}

export function updateTaskRemoteHost(
  data: TaskRemoteHostReq,
  code: CodeReq
): any {
  return axios({
    url: `/remote_host/${code}`,
    method: 'put',
    data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    transformRequest: (params) => JSON.stringify(params)
  })
}

export function verifyTaskRemoteHost(data: TaskRemoteHostNameReq): any {
  return axios({
    url: '/remote_host/verify-host',
    method: 'post',
    data
  })
}

export function testConnectHost(data: TaskRemoteHostReq): any {
  return axios({
    url: '/remote_host/test-connect',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    },
    transformRequest: (params) => JSON.stringify(params)
  })
}
