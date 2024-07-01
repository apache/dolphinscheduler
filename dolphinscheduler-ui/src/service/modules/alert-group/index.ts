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
import { ListReq, GroupNameReq, IdReq, GroupReq } from './types'

export function queryAlertGroupListPaging(params: ListReq): any {
  return axios({
    url: '/alert-groups',
    method: 'get',
    params
  })
}

export function createAlertGroup(data: GroupReq): any {
  return axios({
    url: '/alert-groups',
    method: 'post',
    data
  })
}

export function listAlertGroupById(): any {
  return axios({
    url: '/alert-groups/list',
    method: 'get'
  })
}

export function listNormalAlertGroupById(): any {
  return axios({
    url: '/alert-groups/normal-list',
    method: 'get'
  })
}

export function queryAlertGroupById(data: IdReq): any {
  return axios({
    url: '/alert-groups/query',
    method: 'post',
    data
  })
}

export function verifyGroupName(params: GroupNameReq): any {
  return axios({
    url: '/alert-groups/verify-name',
    method: 'get',
    params
  })
}

export function updateAlertGroup(data: GroupReq, id: IdReq): any {
  return axios({
    url: `/alert-groups/${id.id}`,
    method: 'put',
    data
  })
}

export function delAlertGroupById(id: IdReq): any {
  return axios({
    url: `/alert-groups/${id.id}`,
    method: 'delete',
    params: id
  })
}
