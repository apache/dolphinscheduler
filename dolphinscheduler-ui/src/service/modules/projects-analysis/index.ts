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
import { ListReq, CodeReq, StateReq } from './types'

export function countCommandState(): any {
  return axios({
    url: '/projects/analysis/command-state-count',
    method: 'get'
  })
}

export function countDefinitionByUser(params: CodeReq): any {
  return axios({
    url: '/projects/analysis/define-user-count',
    method: 'get',
    params
  })
}

export function countProcessInstanceState(params: StateReq): any {
  return axios({
    url: '/projects/analysis/process-state-count',
    method: 'get',
    params
  })
}

export function countQueueState(): any {
  return axios({
    url: '/projects/analysis/queue-count',
    method: 'get'
  })
}

export function countTaskState(params: StateReq): any {
  return axios({
    url: '/projects/analysis/task-state-count',
    method: 'get',
    params
  })
}

export function queryListCommandPaging(params: ListReq): any {
  return axios({
    url: '/projects/analysis/listCommand',
    method: 'get',
    params
  })
}

export function queryListErrorCommandPaging(params: ListReq): any {
  return axios({
    url: '/projects/analysis/listErrorCommand',
    method: 'get',
    params
  })
}
