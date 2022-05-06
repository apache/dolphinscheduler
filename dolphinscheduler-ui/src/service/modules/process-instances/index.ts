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
  CodeReq,
  ProcessInstanceListReq,
  BatchDeleteReq,
  SubIdReq,
  TaskReq,
  LongestReq,
  ProcessInstanceReq
} from './types'

export function queryProcessInstanceListPaging(
  params: ProcessInstanceListReq,
  code: number
): any {
  return axios({
    url: `/projects/${code}/process-instances`,
    method: 'get',
    params
  })
}

export function batchDeleteProcessInstanceByIds(
  data: BatchDeleteReq,
  code: number
): any {
  return axios({
    url: `/projects/${code}/process-instances/batch-delete`,
    method: 'post',
    data
  })
}

export function queryParentInstanceBySubId(
  params: SubIdReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code}/process-instances/query-parent-by-sub`,
    method: 'get',
    params
  })
}

export function querySubProcessInstanceByTaskCode(
  params: TaskReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code.projectCode}/process-instances/query-sub-by-parent`,
    method: 'get',
    params
  })
}

export function queryTopNLongestRunningProcessInstance(
  params: LongestReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code}/process-instances/top-n`,
    method: 'get',
    params
  })
}

export function queryProcessInstanceById(
  instanceId: number,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/process-instances/${instanceId}`,
    method: 'get'
  })
}

export function updateProcessInstance(
  data: ProcessInstanceReq,
  id: number,
  code: number
): any {
  return axios({
    url: `/projects/${code}/process-instances/${id}`,
    method: 'put',
    data
  })
}

export function deleteProcessInstanceById(id: number, code: number): any {
  return axios({
    url: `/projects/${code}/process-instances/${id}`,
    method: 'delete'
  })
}

export function queryTaskListByProcessId(id: number, code: number): any {
  return axios({
    url: `/projects/${code}/process-instances/${id}/tasks`,
    method: 'get'
  })
}

export function viewGanttTree(id: number, code: number): any {
  return axios({
    url: `/projects/${code}/process-instances/${id}/view-gantt`,
    method: 'get'
  })
}

export function viewVariables(id: number, code: number): any {
  return axios({
    url: `/projects/${code}/process-instances/${id}/view-variables`,
    method: 'get'
  })
}
