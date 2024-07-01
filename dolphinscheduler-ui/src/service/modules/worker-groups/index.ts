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
import { ListReq, WorkerGroupReq, IdReq } from './types'

export function queryAllWorkerGroupsPaging(params: ListReq): any {
  return axios({
    url: '/worker-groups',
    method: 'get',
    params
  })
}

export function saveWorkerGroup(data: WorkerGroupReq): any {
  return axios({
    url: '/worker-groups',
    method: 'post',
    data
  })
}

export function queryAllWorkerGroups(): any {
  return axios({
    url: '/worker-groups/all',
    method: 'get'
  })
}

export function queryWorkerAddressList(): any {
  return axios({
    url: '/worker-groups/worker-address-list',
    method: 'get'
  })
}

export function deleteById(id: IdReq): any {
  return axios({
    url: `/worker-groups/${id.id}`,
    method: 'delete',
    params: id
  })
}
