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
  authedDatasource,
  unAuthDatasource
} from '@/service/modules/data-source'
import {
  authNamespaceFunc,
  unAuthNamespaceFunc
} from '@/service/modules/k8s-namespace'
import {
  grantDataSource,
  grantNamespaceFunc,
  grantProject,
  grantProjectWithReadPerm,
  revokeProjectById
} from '@/service/modules/users'
import type { IOption, IRecord, IResourceOption, TAuthType } from '../types'

export function useAuthorize() {
  const state = reactive({
    saving: false,
    loading: false,
    projectIds: '',
    currentRecord: {} as IRecord | null,
    projectWithAuthorizedLevel: [],
    authorizedProjects: [] as number[],
    unauthorizedProjects: [] as IOption[],
    authorizedDatasources: [] as number[],
    unauthorizedDatasources: [] as IOption[],
    authorizedNamespaces: [] as number[],
    unauthorizedNamespaces: [] as IOption[],
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

  const getDatasources = async (userId: number) => {
    if (state.loading) return
    state.loading = true
    const datasources = await Promise.all([
      authedDatasource({ userId }),
      unAuthDatasource({ userId })
    ])
    state.loading = false
    state.authorizedDatasources = datasources[0].map(
      (item: { name: string; id: number }) => item.id
    )
    state.unauthorizedDatasources = [...datasources[0], ...datasources[1]].map(
      (item: { name: string; id: number }) => ({
        label: item.name,
        value: item.id
      })
    )
  }

  const getNamespaces = async (userId: number) => {
    if (state.loading) return
    state.loading = true
    const namespaces = await Promise.all([
      authNamespaceFunc({ userId }),
      unAuthNamespaceFunc({ userId })
    ])
    state.loading = false
    state.authorizedNamespaces = namespaces[0].map(
      (item: { id: number }) => item.id
    )
    state.unauthorizedNamespaces = [...namespaces[0], ...namespaces[1]].map(
      (item: { namespace: string; id: number }) => ({
        label: item.namespace,
        value: item.id
      })
    )
  }

  const onInit = (type: TAuthType, userId: number) => {
    if (type === 'authorize_project') {
      getProjects(userId)
    }
    if (type === 'authorize_datasource') {
      getDatasources(userId)
    }
    if (type === 'authorize_namespace') {
      getNamespaces(userId)
    }
  }

  /*
    getParent
  */
  const onSave = async (type: TAuthType, userId: number) => {
    if (state.saving) return false
    state.saving = true
    if (type === 'authorize_datasource') {
      await grantDataSource({
        userId,
        datasourceIds: state.authorizedDatasources.join(',')
      })
    }
    if (type === 'authorize_namespace') {
      await grantNamespaceFunc({
        userId,
        namespaceIds: state.authorizedNamespaces.join(',')
      })
    }
    state.saving = false
    return true
  }

  return {
    state,
    onInit,
    onSave,
    getProjects,
    revokeProjectByIdRequest,
    grantProjectRequest,
    grantProjectWithReadPermRequest,
    requestData,
    handleChangePageSize
  }
}
