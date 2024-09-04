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
  WorkflowInstanceListReq,
  BatchDeleteReq,
  SubIdReq,
  TaskReq,
  LongestReq,
  WorkflowInstanceReq
} from './types'

export function queryWorkflowInstanceListPaging(
  params: WorkflowInstanceListReq,
  code: number
): any {
  return axios({
    url: `/projects/${code}/workflow-instances`,
    method: 'get',
    params
  })
}

export function batchDeleteWorkflowInstanceByIds(
  data: BatchDeleteReq,
  code: number
): any {
  return axios({
    url: `/projects/${code}/workflow-instances/batch-delete`,
    method: 'post',
    data
  })
}

export function queryParentInstanceBySubId(
  params: SubIdReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code}/workflow-instances/query-parent-by-sub`,
    method: 'get',
    params
  })
}

export function querySubWorkflowInstanceByTaskCode(
  params: TaskReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code.projectCode}/workflow-instances/query-sub-by-parent`,
    method: 'get',
    params
  })
}

export function queryTopNLongestRunningWorkflowInstance(
  params: LongestReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code}/workflow-instances/top-n`,
    method: 'get',
    params
  })
}

export function queryWorkflowInstanceById(
  instanceId: number,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/workflow-instances/${instanceId}`,
    method: 'get'
  })
}

export function updateWorkflowInstance(
  data: WorkflowInstanceReq,
  id: number,
  code: number
): any {
  return axios({
    url: `/projects/${code}/workflow-instances/${id}`,
    method: 'put',
    data
  })
}

export function deleteWorkflowInstanceById(id: number, code: number): any {
  return axios({
    url: `/projects/${code}/workflow-instances/${id}`,
    method: 'delete'
  })
}

export function queryTaskListByWorkflowId(id: number, code: number): any {
  return axios({
    url: `/projects/${code}/workflow-instances/${id}/tasks`,
    method: 'get'
  })
}

export function viewGanttTree(id: number, code: number): any {
  return axios({
    url: `/projects/${code}/workflow-instances/${id}/view-gantt`,
    method: 'get'
  })
}

export function viewVariables(id: number, code: number): any {
  return axios({
    url: `/projects/${code}/workflow-instances/${id}/view-variables`,
    method: 'get'
  })
}
