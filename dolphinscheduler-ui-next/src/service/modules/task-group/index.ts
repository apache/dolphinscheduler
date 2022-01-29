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
  ListReq,
  TaskGroupIdReq,
  TaskGroupQueueIdReq,
  TaskGroupQueuePriorityUpdateReq,
  TaskGroupReq,
  TaskGroupUpdateReq
} from './types'

export function queryTaskGroupListPaging(params: ListReq): any {
  return axios({
    url: '/task-group/list-paging',
    method: 'get',
    params
  })
}

export function queryTaskGroupListPagingByProjectCode(params: ListReq): any {
  return axios({
    url: '/task-group/query-list-by-projectCode',
    method: 'get',
    params
  })
}

export function createTaskGroup(data: TaskGroupReq): any {
  return axios({
    url: '/task-group/create',
    method: 'post',
    data
  })
}

export function updateTaskGroup(data: TaskGroupUpdateReq): any {
  return axios({
    url: '/task-group/update',
    method: 'post',
    data
  })
}

export function closeTaskGroup(data: TaskGroupIdReq): any {
  return axios({
    url: '/task-group/close-task-group',
    method: 'post',
    data
  })
}

export function startTaskGroup(data: TaskGroupIdReq): any {
  return axios({
    url: '/task-group/start-task-group',
    method: 'post',
    data
  })
}

export function queryTaskListInTaskGroupQueueById(params: ListReq): any {
  return axios({
    url: '/task-group/query-list-by-group-id',
    method: 'get',
    params
  })
}

export function modifyTaskGroupQueuePriority(
  data: TaskGroupQueuePriorityUpdateReq
): any {
  return axios({
    url: '/task-group/modifyPriority',
    method: 'post',
    data
  })
}

export function forceStartTaskInQueue(data: TaskGroupQueueIdReq): any {
  return axios({
    url: '/task-group/forceStart',
    method: 'post',
    data
  })
}
