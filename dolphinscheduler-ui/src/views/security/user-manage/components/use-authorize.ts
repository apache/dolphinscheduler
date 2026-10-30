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
import { reactive } from 'vue'
import { queryProjectWithAuthorizedLevelListPaging } from '@/service/modules/projects'
import {
  grantProject,
  grantProjectWithReadPerm,
  revokeProjectById
} from '@/service/modules/users'
import type { IOption, IRecord, IResourceOption } from '../types'

export function useAuthorize() {
  const state = reactive({
    saving: false,
    loading: false,
    projectIds: '',
    currentRecord: {} as IRecord | null,
    projectWithAuthorizedLevel: [],
    authorizedProjects: [] as number[],
    unauthorizedProjects: [] as IOption[],
    resourceType: 'file',
    fileResources: [] as IResourceOption[],
    pagination: {
      pageSize: 5,
      page: 1,
      totalPage: 0
    },
    searchVal: '',
    userId: 0
  })

  const getProjects = async (userId: number) => {
    if (state.loading) return
    state.loading = true
    if (userId) {
      state.userId = userId
    }

    const projectsList = await queryProjectWithAuthorizedLevelListPaging({
      userId,
      searchVal: state.searchVal,
      pageSize: state.pagination.pageSize,
      pageNo: state.pagination.page
    })
    state.loading = false
    if (!projectsList) throw Error()
    state.pagination.totalPage = projectsList.totalPage
    state.projectWithAuthorizedLevel = projectsList.totalList
    return state.projectWithAuthorizedLevel
  }

  const requestData = async (page: number) => {
    state.pagination.page = page
    await getProjects(state.userId)
  }

  const handleChangePageSize = async (pageSize: number) => {
    state.pagination.page = 1
    state.pagination.pageSize = pageSize
    await getProjects(state.userId)
  }

  const revokeProjectByIdRequest = async (
    userId: number,
    projectIds: string
  ) => {
    await revokeProjectById({
      userId,
      projectIds: projectIds
    })
    await getProjects(userId)
  }

  const grantProjectRequest = async (userId: number, projectIds: string) => {
    await grantProject({
      userId,
      projectIds: projectIds
    })
    await getProjects(userId)
  }

  const grantProjectWithReadPermRequest = async (
    userId: number,
    projectIds: string
  ) => {
    await grantProjectWithReadPerm({
      userId,
      projectIds: projectIds
    })
    await getProjects(userId)
  }

  const onInit = (userId: number) => {
    getProjects(userId)
  }

  return {
    state,
    onInit,
    getProjects,
    revokeProjectByIdRequest,
    grantProjectRequest,
    grantProjectWithReadPermRequest,
    requestData,
    handleChangePageSize
  }
}
