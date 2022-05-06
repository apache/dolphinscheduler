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
  CodesReq,
  NameReq,
  ReleaseStateReq,
  LimitReq,
  PageReq,
  ListReq,
  ProcessDefinitionReq,
  TargetCodeReq
} from './types'

export function queryListPaging(params: PageReq & ListReq, code: number): any {
  return axios({
    url: `/projects/${code}/process-definition`,
    method: 'get',
    params
  })
}

export function createProcessDefinition(
  data: ProcessDefinitionReq,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/process-definition`,
    method: 'post',
    data
  })
}

export function queryProcessDefinitionList(projectCode: number): any {
  return axios({
    url: `/projects/${projectCode}/process-definition/query-process-definition-list`,
    method: 'get'
  })
}

export function batchCopyByCodes(
  data: TargetCodeReq & CodesReq,
  code: number
): any {
  return axios({
    url: `/projects/${code}/process-definition/batch-copy`,
    method: 'post',
    data
  })
}

export function batchDeleteByCodes(data: CodesReq, code: number): any {
  return axios({
    url: `/projects/${code}/process-definition/batch-delete`,
    method: 'post',
    data
  })
}

export function batchExportByCodes(data: CodesReq, code: number): any {
  return axios({
    url: `/projects/${code}/process-definition/batch-export`,
    method: 'post',
    responseType: 'blob',
    data
  })
}

export function batchMoveByCodes(
  data: TargetCodeReq & CodesReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code}/process-definition/batch-move`,
    method: 'post',
    data
  })
}

export function getTaskListByDefinitionCodes(
  params: CodesReq,
  code: number
): any {
  return axios({
    url: `/projects/${code}/process-definition/batch-query-tasks`,
    method: 'get',
    params
  })
}

export function importProcessDefinition(data: FormData, code: number): any {
  return axios({
    url: `/projects/${code}/process-definition/import`,
    method: 'post',
    data
  })
}

export function queryList(code: CodeReq): any {
  return axios({
    url: `/projects/${code}/process-definition/list`,
    method: 'get'
  })
}

export function queryProcessDefinitionByName(
  params: NameReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code}/process-definition/query-by-name`,
    method: 'get',
    params
  })
}

export function querySimpleList(code: number): any {
  return axios({
    url: `/projects/${code}/process-definition/simple-list`,
    method: 'get'
  })
}

export function verifyName(params: NameReq, code: number): any {
  return axios({
    url: `/projects/${code}/process-definition/verify-name`,
    method: 'get',
    params
  })
}

export function queryProcessDefinitionByCode(
  code: number,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/process-definition/${code}`,
    method: 'get'
  })
}

export function updateProcessDefinition(
  data: ProcessDefinitionReq & ReleaseStateReq,
  code: number,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/process-definition/${code}`,
    method: 'put',
    data
  })
}

export function deleteByCode(code: number, processCode: number): any {
  return axios({
    url: `/projects/${code}/process-definition/${processCode}`,
    method: 'delete'
  })
}

export function release(
  data: NameReq & ReleaseStateReq,
  code: number,
  processCode: number
): any {
  return axios({
    url: `/projects/${code}/process-definition/${processCode}/release`,
    method: 'post',
    data
  })
}

export function getTasksByDefinitionList(
  projectCode: number,
  processCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/process-definition/query-task-definition-list`,
    method: 'get',
    params: {
      processDefinitionCode: processCode
    }
  })
}

export function queryVersions(
  params: PageReq,
  code: number,
  processCode: number
): any {
  return axios({
    url: `/projects/${code}/process-definition/${processCode}/versions`,
    method: 'get',
    params
  })
}

export function switchVersion(
  code: number,
  processCode: number,
  version: number
): any {
  return axios({
    url: `/projects/${code}/process-definition/${processCode}/versions/${version}`,
    method: 'get'
  })
}

export function deleteVersion(
  code: number,
  processCode: number,
  version: number
): any {
  return axios({
    url: `/projects/${code}/process-definition/${processCode}/versions/${version}`,
    method: 'delete'
  })
}

export function viewTree(
  code: number,
  processCode: number,
  params: LimitReq
): any {
  return axios({
    url: `/projects/${code}/process-definition/${processCode}/view-tree`,
    method: 'get',
    params
  })
}
