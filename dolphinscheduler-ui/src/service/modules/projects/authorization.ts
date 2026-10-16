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

export const projectResourceTypes = [
  'DATASOURCE',
  'WORKER_GROUP',
  'TENANT',
  'QUEUE',
  'ENVIRONMENT',
  'ALERT_GROUP',
  'K8S_NAMESPACE',
  'TASK_GROUP'
] as const

export type ProjectResourceType = (typeof projectResourceTypes)[number]
export interface ResourceAuthorization {
  resourceKey: string
}

export function queryResourceAuthorizations(
  projectCode: number,
  type: ProjectResourceType
): Promise<ResourceAuthorization[]> {
  return axios.get(`/projects/${projectCode}/resource-authorizations/${type}`)
}

export function grantProjectResource(
  projectCode: number,
  type: ProjectResourceType,
  resourceKey: string
) {
  return axios.put(
    `/projects/${projectCode}/resource-authorizations/${type}`,
    undefined,
    { params: { resourceKey } }
  )
}

export function revokeProjectResource(
  projectCode: number,
  type: ProjectResourceType,
  resourceKey: string
) {
  return axios.delete(
    `/projects/${projectCode}/resource-authorizations/${type}`,
    { params: { resourceKey } }
  )
}

export const projectPermissions = [
  'VIEW',
  'DEVELOP',
  'PUBLISH',
  'EXECUTE',
  'MANAGE_SCHEDULES',
  'MANAGE_MEMBERS'
] as const
export type ProjectPermission = (typeof projectPermissions)[number]

export function queryMemberCandidates(
  projectCode: number
): Promise<{ id: number; userName: string }[]> {
  return axios.get(`/projects/${projectCode}/members/candidates`)
}

export function queryMemberPermissions(
  projectCode: number,
  userId: number
): Promise<ProjectPermission[]> {
  return axios.get(`/projects/${projectCode}/members/${userId}/permissions`)
}

export function replaceMemberPermissions(
  projectCode: number,
  userId: number,
  permissions: ProjectPermission[]
) {
  return axios.put(
    `/projects/${projectCode}/members/${userId}/permissions`,
    JSON.stringify(permissions),
    {
      headers: { 'Content-Type': 'application/json' }
    }
  )
}
