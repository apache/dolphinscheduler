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
  ProjectCodeReq,
  IdReq,
  CodeReq,
  ScheduleReq,
  WorkerGroupIdReq,
  ScheduleListReq,
  CreateScheduleReq,
  DeleteScheduleReq
} from './types'

export function queryScheduleListPaging(
  params: ScheduleListReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/schedules`,
    method: 'get',
    params
  })
}

export function createSchedule(
  data: CreateScheduleReq & WorkerGroupIdReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/schedules`,
    method: 'post',
    data
  })
}

export function queryScheduleList(projectCode: ProjectCodeReq): any {
  return axios({
    url: `/projects/${projectCode}/schedules/list`,
    method: 'post'
  })
}

export function previewSchedule(
  data: ScheduleReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/schedules/preview`,
    method: 'post',
    data
  })
}

export function updateScheduleByProcessDefinitionCode(
  data: CreateScheduleReq,
  projectCode: ProjectCodeReq,
  code: CodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/schedules/update/${code}`,
    method: 'put',
    data
  })
}

export function updateSchedule(
  data: CreateScheduleReq,
  projectCode: ProjectCodeReq,
  id: IdReq
): any {
  return axios({
    url: `/projects/${projectCode}/schedules/${id}`,
    method: 'put',
    data
  })
}

export function deleteScheduleById(
  data: DeleteScheduleReq,
  projectCode: ProjectCodeReq
): any {
  return axios({
    url: `/projects/${projectCode}/schedules/${data.id}`,
    method: 'delete',
    data
  })
}

export function offline(projectCode: ProjectCodeReq, id: IdReq): any {
  return axios({
    url: `/projects/${projectCode}/schedules/${id}/offline`,
    method: 'post'
  })
}

export function online(projectCode: ProjectCodeReq, id: IdReq): any {
  return axios({
    url: `/projects/${projectCode}/schedules/${id}/online`,
    method: 'post'
  })
}
