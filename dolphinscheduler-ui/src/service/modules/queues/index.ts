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
import { ListReq, QueueReq, IdReq } from './types'

export function queryQueueListPaging(params: ListReq): any {
  return axios({
    url: '/queues',
    method: 'get',
    params
  })
}

export function createQueue(data: QueueReq): any {
  return axios({
    url: '/queues',
    method: 'post',
    data
  })
}

export function queryList(): any {
  return axios({
    url: '/queues/list',
    method: 'get'
  })
}

export function verifyQueue(data: QueueReq): any {
  return axios({
    url: '/queues/verify',
    method: 'post',
    data
  })
}

export function updateQueue(data: QueueReq, idReq: IdReq): any {
  return axios({
    url: `/queues/${idReq.id}`,
    method: 'put',
    data
  })
}

export function deleteQueueById(id: number): any {
  return axios({
    url: `/queues/${id}`,
    method: 'delete'
  })
}
