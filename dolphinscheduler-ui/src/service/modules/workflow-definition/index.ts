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
  WorkflowDefinitionReq,
  TargetCodeReq
} from './types'

export function queryListPaging(params: PageReq & ListReq, code: number): any {
  return axios({
    url: `/projects/${code}/workflow-definition`,
    method: 'get',
    params
  })
}

export function createWorkflowDefinition(
  data: WorkflowDefinitionReq,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/workflow-definition`,
    method: 'post',
    data
  })
}

export function queryWorkflowDefinitionList(projectCode: number): any {
  return axios({
    url: `/projects/${projectCode}/workflow-definition/query-workflow-definition-list`,
    method: 'get'
  })
}

export function batchCopyByCodes(
  data: TargetCodeReq & CodesReq,
  code: number
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/batch-copy`,
    method: 'post',
    data
  })
}

export function batchDeleteByCodes(data: CodesReq, code: number): any {
  return axios({
    url: `/projects/${code}/workflow-definition/batch-delete`,
    method: 'post',
    data
  })
}

export function batchExportByCodes(data: CodesReq, code: number): any {
  return axios({
    url: `/projects/${code}/workflow-definition/batch-export`,
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
    url: `/projects/${code}/workflow-definition/batch-move`,
    method: 'post',
    data
  })
}

export function getTaskListByDefinitionCodes(
  params: CodesReq,
  code: number
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/batch-query-tasks`,
    method: 'get',
    params
  })
}

export function importWorkflowDefinition(data: FormData, code: number): any {
  return axios({
    url: `/projects/${code}/workflow-definition/import`,
    method: 'post',
    data
  })
}

export function queryList(code: CodeReq): any {
  return axios({
    url: `/projects/${code}/workflow-definition/list`,
    method: 'get'
  })
}

export function queryWorkflowDefinitionByName(
  params: NameReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/query-by-name`,
    method: 'get',
    params
  })
}

export function querySimpleList(code: number): any {
  return axios({
    url: `/projects/${code}/workflow-definition/simple-list`,
    method: 'get'
  })
}

export function verifyName(
  params: { name: string; workflowCode?: number },
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/workflow-definition/verify-name`,
    method: 'get',
    params
  })
}

export function queryWorkflowDefinitionByCode(
  code: number,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/workflow-definition/${code}`,
    method: 'get'
  })
}

export function updateWorkflowDefinition(
  data: WorkflowDefinitionReq & ReleaseStateReq,
  code: number,
  projectCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/workflow-definition/${code}`,
    method: 'put',
    data
  })
}

export function deleteByCode(
  code: number,
  workflowDefinitionCode: number
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/${workflowDefinitionCode}`,
    method: 'delete'
  })
}

export function release(
  data: NameReq & ReleaseStateReq,
  code: number,
  workflowDefinitionCode: number
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/${workflowDefinitionCode}/release`,
    method: 'post',
    data
  })
}

export function getTasksByDefinitionList(
  projectCode: number,
  workflowDefinitionCode: number
): any {
  return axios({
    url: `/projects/${projectCode}/workflow-definition/query-task-definition-list`,
    method: 'get',
    params: {
      workflowDefinitionCode: workflowDefinitionCode
    }
  })
}

export function queryVersions(
  params: PageReq,
  code: number,
  workflowDefinitionCode: number
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/${workflowDefinitionCode}/versions`,
    method: 'get',
    params
  })
}

export function switchVersion(
  code: number,
  workflowDefinitionCode: number,
  version: number
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/${workflowDefinitionCode}/versions/${version}`,
    method: 'get'
  })
}

export function deleteVersion(
  code: number,
  workflowDefinitionCode: number,
  version: number
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/${workflowDefinitionCode}/versions/${version}`,
    method: 'delete'
  })
}

export function viewTree(
  code: number,
  workflowDefinitionCode: number,
  params: LimitReq
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/${workflowDefinitionCode}/view-tree`,
    method: 'get',
    params
  })
}

export function viewWorkflowDefinitionVariables(
  code: number,
  workflowDefinitionCode: number
): any {
  return axios({
    url: `/projects/${code}/workflow-definition/${workflowDefinitionCode}/view-variables`,
    method: 'get'
  })
}
