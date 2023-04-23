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

import {
  ProjectCodeReq,
  ProcessDefinitionCodeReq,
  PreTaskCodesReq,
  PostTaskCodesReq,
  TaskCodeReq,
  SaveReq
} from './types'
import { axios } from '@/service/service'

export function save(data: SaveReq, projectCode: ProjectCodeReq): any {
  return axios({
    url: `/projects/${projectCode}/process-task-relation`,
    method: 'post',
    data
  })
}

export function deleteEdge(data: SaveReq): any {
  return axios({
    url: `/projects/${data.projectCode}/process-task-relation/${data.processDefinitionCode}/${data.preTaskCode}/${data.postTaskCode}`,
    method: 'delete'
  })
}

export function deleteRelation(
  data: ProcessDefinitionCodeReq,
  projectCode: ProjectCodeReq,
  taskCode: TaskCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/process-task-relation/${taskCode}`,
    method: 'delete',
    data
  })
}

export function queryDownstreamRelation(
  projectCode: ProjectCodeReq,
  taskCode: TaskCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/process-task-relation/${taskCode}/downstream`,
    method: 'get'
  })
}

export function deleteDownstreamRelation(
  data: PostTaskCodesReq,
  projectCode: ProjectCodeReq,
  taskCode: TaskCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/process-task-relation/${taskCode}/downstream`,
    method: 'delete',
    data
  })
}

export function queryUpstreamRelation(
  projectCode: ProjectCodeReq,
  taskCode: TaskCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/process-task-relation/${taskCode}/upstream`,
    method: 'get'
  })
}

export function deleteUpstreamRelation(
  data: PreTaskCodesReq,
  projectCode: ProjectCodeReq,
  taskCode: TaskCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/process-task-relation/${taskCode}/upstream`,
    method: 'delete',
    data
  })
}
