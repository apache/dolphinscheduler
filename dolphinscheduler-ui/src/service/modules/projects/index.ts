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
import { ListReq, ProjectsReq, UserIdReq, UpdateProjectsReq } from './types'

export function queryProjectListPaging(params: ListReq): any {
  return axios({
    url: '/projects',
    method: 'get',
    params
  })
}

export function createProject(data: ProjectsReq): any {
  return axios({
    url: '/projects',
    method: 'post',
    data
  })
}

export function queryAuthorizedProject(params: UserIdReq): any {
  return axios({
    url: '/projects/authed-project',
    method: 'get',
    params
  })
}

export function queryProjectCreatedAndAuthorizedByUser(): any {
  return axios({
    url: '/projects/created-and-authed',
    method: 'get'
  })
}

export function queryAllProjectList(): any {
  return axios({
    url: '/projects/list',
    method: 'get'
  })
}

export function queryUnauthorizedProject(params: UserIdReq): any {
  return axios({
    url: '/projects/unauth-project',
    method: 'get',
    params
  })
}

export function queryProjectByCode(code: number): any {
  return axios({
    url: `/projects/${code}`,
    method: 'get'
  })
}

export function updateProject(data: UpdateProjectsReq, code: number): any {
  return axios({
    url: `/projects/${code}`,
    method: 'put',
    data
  })
}

export function deleteProject(code: number): any {
  return axios({
    url: `/projects/${code}`,
    method: 'delete'
  })
}
