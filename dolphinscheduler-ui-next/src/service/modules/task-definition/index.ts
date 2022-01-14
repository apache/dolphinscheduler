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
  GenNumReq,
  CodeReq,
  TaskDefinitionJsonObjReq,
  ReleaseStateReq,
  VersionReq
} from './types'

export function queryTaskDefinitionListPaging(
  params: TaskDefinitionListReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition`,
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

export function genTaskCodeList(
  params: GenNumReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/gen-task-codes`,
    method: 'get',
    params
  })
}

export function queryTaskDefinitionByCode(
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}`,
    method: 'get'
  })
}

export function update(
  data: TaskDefinitionJsonObjReq,
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}`,
    method: 'put',
    data
  })
}

export function deleteTaskDefinition(
  data: TaskDefinitionJsonObjReq,
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}`,
    method: 'put',
    data
  })
}

export function releaseTaskDefinition(
  data: ReleaseStateReq,
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}/release`,
    method: 'post',
    data
  })
}

export function queryVersions(
  params: PageReq,
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}/versions`,
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
    url: `/projects/${projectCode}/task-definition/${code}/versions/${version}`,
    method: 'get'
  })
}

export function deleteVersion(
  version: VersionReq,
  code: CodeReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/task-definition/${code}/versions/${version}`,
    method: 'delete'
  })
}
