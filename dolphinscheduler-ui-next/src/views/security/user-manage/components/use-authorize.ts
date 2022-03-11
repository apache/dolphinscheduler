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
import { useI18n } from 'vue-i18n'
import {
  queryAuthorizedProject,
  queryUnauthorizedProject
} from '@/service/modules/projects'
import {
  authedDatasource,
  unAuthDatasource
} from '@/service/modules/data-source'
import {
  authorizedFile,
  authorizeResourceTree,
  authUDFFunc,
  unAuthUDFFunc
} from '@/service/modules/resources'
import {
  grantProject,
  grantResource,
  grantDataSource,
  grantUDFFunc
} from '@/service/modules/users'
import { removeUselessChildren } from '@/utils/tree-format'
import type { TAuthType, IResourceOption, IOption } from '../types'

export function useAuthorize() {
  const { t } = useI18n()

  const state = reactive({
    saving: false,
    loading: false,
    authorizedProjects: [] as number[],
    unauthorizedProjects: [] as IOption[],
    authorizedDatasources: [] as number[],
    unauthorizedDatasources: [] as IOption[],
    authorizedUdfs: [] as number[],
    unauthorizedUdfs: [] as IOption[],
    resourceType: 'file',
    fileResources: [] as IResourceOption[],
    udfResources: [] as IResourceOption[],
    authorizedFileResources: [] as number[],
    authorizedUdfResources: [] as number[]
  })

  const getProjects = async (userId: number) => {
    if (state.loading) return
    state.loading = true
    const projects = await Promise.all([
      queryAuthorizedProject({ userId }),
      queryUnauthorizedProject({ userId })
    ])
    state.loading = false
    state.authorizedProjects = projects[0].map(
      (item: { name: string; id: number }) => item.id
    )
    state.unauthorizedProjects = [...projects[0], ...projects[1]].map(
      (item: { name: string; id: number }) => ({
        label: item.name,
        value: item.id
      })
    )
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

  const getUdfs = async (userId: number) => {
    if (state.loading) return
    state.loading = true
    const udfs = await Promise.all([
      authUDFFunc({ userId }),
      unAuthUDFFunc({ userId })
    ])
    state.loading = false
    state.authorizedUdfs = udfs[0].map(
      (item: { name: string; id: number }) => item.id
    )
    state.unauthorizedUdfs = [...udfs[0], ...udfs[1]].map(
      (item: { name: string; id: number }) => ({
        label: item.name,
        value: item.id
      })
    )
  }

  const getResources = async (userId: number) => {
    if (state.loading) return
    state.loading = true
    const resources = await Promise.all([
      authorizeResourceTree({ userId }),
      authorizedFile({ userId })
    ])
    state.loading = false
    removeUselessChildren(resources[0])
    let udfResources = [] as IResourceOption[]
    let fileResources = [] as IResourceOption[]
    resources[0].forEach((item: IResourceOption) => {
      item.type === 'FILE' ? fileResources.push(item) : udfResources.push(item)
    })
    let udfTargets = [] as number[]
    let fileTargets = [] as number[]
    resources[1].forEach((item: { type: string; id: number }) => {
      item.type === 'FILE'
        ? fileTargets.push(item.id)
        : udfTargets.push(item.id)
    })
    state.fileResources = fileResources
    state.udfResources = udfResources
    console.log(fileResources)
    state.authorizedFileResources = fileTargets
    state.authorizedUdfResources = fileTargets
  }

  const onInit = (type: TAuthType, userId: number) => {
    if (type === 'authorize_project') {
      getProjects(userId)
    }
    if (type === 'authorize_datasource') {
      getDatasources(userId)
    }
    if (type === 'authorize_udf') {
      getUdfs(userId)
    }
    if (type === 'authorize_resource') {
      getResources(userId)
    }
  }

  const onSave = async (type: TAuthType, userId: number) => {
    if (state.saving) return false
    state.saving = true
    if (type === 'authorize_project') {
      await grantProject({
        userId,
        projectIds: state.authorizedProjects.join(',')
      })
    }
    if (type === 'authorize_datasource') {
      await grantDataSource({
        userId,
        datasourceIds: state.authorizedDatasources.join(',')
      })
    }
    if (type === 'authorize_udf') {
      await grantUDFFunc({
        userId,
        udfIds: state.authorizedUdfResources.join(',')
      })
    }
    if (type === 'authorize_resource') {
      await grantResource({
        userId,
        resourceIds:
          state.resourceType === 'file'
            ? state.authorizedFileResources.join(',')
            : state.authorizedUdfResources.join(',')
      })
    }
    state.saving = false
    return true
  }

  return { state, onInit, onSave }
}
