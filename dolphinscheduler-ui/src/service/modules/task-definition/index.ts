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
  PageReq,
  ProjectCodeReq,
  TaskDefinitionListReq,
  TaskDefinitionJsonReq,
  CodeReq,
  TaskDefinitionJsonObjReq,
  ReleaseStateReq,
  VersionReq,
  ISingleSaveReq,
  TaskDefinitionReq
} from './types'

export function queryTaskDefinitionListPaging(
  params: TaskDefinitionListReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode.projectCode}/task-definition`,
    method: 'get',
    params
  })
}

export function save(
  data: TaskDefinitionJsonReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition`,
    method: 'post',
    data
  })
}

export function genTaskCodeList(num: number, projectCode: number) {
  return axios.request<unknown, number[]>({
    url: `/projects/${projectCode}/task-definition/gen-task-codes`,
    method: 'get',
    params: {
      genNum: num
    }
  })
}

export function queryTaskDefinitionByCode(
  code: number,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}`,
    method: 'get'
  })
}

export function updateTask(
  projectCode: number,
  code: number,
  data: TaskDefinitionJsonObjReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}`,
    method: 'put',
    data
  })
}

export function deleteTaskDefinition(
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode.projectCode}/task-definition/${code.code}`,
    method: 'delete'
  })
}

export function releaseTaskDefinition(
  data: ReleaseStateReq,
  code: number,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}/release`,
    method: 'post',
    data
  })
}

export function queryTaskVersions(
  params: PageReq,
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode.projectCode}/task-definition/${code.code}/versions`,
    method: 'get',
    params
  })
}

export function switchVersion(
  version: VersionReq,
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode.projectCode}/task-definition/${code.code}/versions/${version.version}`,
    method: 'get'
  })
}

export function deleteVersion(
  version: VersionReq,
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode.projectCode}/task-definition/${code.code}/versions/${version.version}`,
    method: 'delete'
  })
}

export function saveSingle(projectCode: number, data: ISingleSaveReq) {
  return axios({
    url: `/projects/${projectCode}/task-definition/save-single`,
    method: 'post',
    data
  })
}

export function updateWithUpstream(
  projectCode: number,
  code: number,
  data: ISingleSaveReq
) {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}/with-upstream`,
    method: 'put',
    data
  })
}

export function startTaskDefinition(
  projectCode: number,
  code: number,
  data: TaskDefinitionReq
) {
  return axios({
    url: `projects/${projectCode}/executors/task-instance/${code}/start`,
    method: 'post',
    data
  })
}
