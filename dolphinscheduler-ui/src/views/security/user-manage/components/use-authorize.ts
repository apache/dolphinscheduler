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
import {
  queryAuthorizedProject,
  queryUnauthorizedProject,
  queryProjectWithAuthorizedLevelListPaging
} from '@/service/modules/projects'
import {
  authedDatasource,
  unAuthDatasource
} from '@/service/modules/data-source'
import {
  authorizedFile,
  authorizedFileWithReadPerm,
  authorizeResourceTree,
  authUDFFunc,
  unAuthUDFFunc
} from '@/service/modules/resources'
import {
  authNamespaceFunc,
  unAuthNamespaceFunc
} from '@/service/modules/k8s-namespace'
import {
  grantProject,
  grantProjectWithReadPerm,
  grantResource,
  grantDataSource,
  grantUDFFunc,
  grantNamespaceFunc,
  revokeProjectById,
  grantResourceWithPermLevel,
} from '@/service/modules/users'
import utils from '@/utils'
import type { TAuthType, IResourceOption, IOption, IRecord } from '../types'
import type {ProjectList} from '../../../../service/modules/projects/types'
import { parseTime } from '@/common/common'
import { format } from 'date-fns'

export function useAuthorize() {
  const state = reactive({
    saving: false,
    loading: false,
    currentRecord: {} as IRecord | null,
    projectWithAuthorizedLevel: [],
    authorizedProjects: [] as number[],
    unauthorizedProjects: [] as IOption[],
    authorizedDatasources: [] as number[],
    unauthorizedDatasources: [] as IOption[],
    authorizedUdfs: [] as number[],
    unauthorizedUdfs: [] as IOption[],
    authorizedNamespaces: [] as number[],
    unauthorizedNamespaces: [] as IOption[],
    resourceType: 'file',
    authorizationType: 'read',
    fileResources: [] as IResourceOption[],
    udfResources: [] as IResourceOption[],
    authorizedFileResources: [] as number[],
    authorizedUdfResources: [] as number[],
    authorizedFileResourcesWithReadPerm: [] as number[],
    authorizedUdfResourcesWithReadPerm: [] as number[],
    pagination: {
      pageSize: 5,
      page: 1,
      totalPage: 0
    },
    searchVal: '',
    userId: 0
  })

  const onOperationClick = (
    data: { rowData: IRecord; key?: TAuthType },
    type: 'authorize' | 'edit' | 'delete'
  ) => {
    // state.currentRecord = data.rowData
    // if (type === 'edit') {
    //   state.detailModalShow = true
    // }
    // if (type === 'authorize' && data.key) {
    //   state.authorizeModalShow = true
    //   state.authorizeType = data.key
    // }
  }

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
    // state.projectWithAuthorizedLevel = projectsList.map((record: ProjectList) => {
    //   record.createTime = record.createTime
    //     ? format(parseTime(record.createTime), 'yyyy-MM-dd HH:mm:ss')
    //     : ''
    //   record.updateTime = record.updateTime
    //     ? format(parseTime(record.updateTime), 'yyyy-MM-dd HH:mm:ss')
    //     : ''
    //   return record
    // })
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

  const revokeProjectByIdRequest = async (userId: number, projectIds: string) => {
    const res = await revokeProjectById({
      userId,
      projectIds: projectIds
    })
    // if (!res) throw Error()
    console.log('res', res)
    await getProjects(userId)
  }

  const grantProjectRequest = async (userId: number, projectIds: string) => {
    // if (state.loading) return
    // state.loading = true
    const res = await grantProject({
      userId,
      projectIds: projectIds
    })
    // if (!res) throw Error()
    console.log('res', res)
    await getProjects(userId)
  }

  const grantProjectWithReadPermRequest = async (userId: number, projectIds: string) => {
    // if (state.loading) return
    // state.loading = true
    const res = await grantProjectWithReadPerm({
      userId,
      projectIds: projectIds
    })
    // if (!res) throw Error()
    console.log('res', res)
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

  const getUdfs = async (userId: number) => {
    if (state.loading) return
    state.loading = true
    const udfs = await Promise.all([
      authUDFFunc({ userId }),
      unAuthUDFFunc({ userId })
    ])
    state.loading = false
    state.authorizedUdfs = udfs[0].map(
      (item: { funcName: string; id: number }) => item.id
    )
    state.unauthorizedUdfs = [...udfs[0], ...udfs[1]].map(
      (item: { funcName: string; id: number }) => ({
        label: item.funcName,
        value: item.id
      })
    )
  }

  const getResources = async (userId: number) => {
    if (state.loading) return
    state.loading = true
    const resources = await Promise.all([
      authorizeResourceTree({ userId }),
      authorizedFile({ userId }),
      authorizedFileWithReadPerm({ userId })
    ])
    state.loading = false
    utils.removeUselessChildren(resources[0])
    const udfResources = [] as IResourceOption[]
    const fileResources = [] as IResourceOption[]
    resources[0].forEach((item: IResourceOption) => {
      item.type === 'FILE' ? fileResources.push(item) : udfResources.push(item)
    })
    const udfTargets = [] as number[]
    const fileTargets = [] as number[]
    resources[1].forEach((item: { type: string; id: number }) => {
      item.type === 'FILE'
        ? fileTargets.push(item.id)
        : udfTargets.push(item.id)
    })
    const udfTargetsWithReadPerm = [] as number[]
    const fileTargetsWithReadPerm = [] as number[]
    resources[2].forEach((item: { type: string; id: number }) => {
      item.type === 'FILE'
        ? fileTargetsWithReadPerm.push(item.id)
        : udfTargetsWithReadPerm.push(item.id)
    })
    state.fileResources = fileResources
    state.udfResources = udfResources
    state.authorizedFileResources = fileTargets
    state.authorizedUdfResources = udfTargets
    state.authorizedFileResourcesWithReadPerm = fileTargetsWithReadPerm
    state.authorizedUdfResourcesWithReadPerm = udfTargetsWithReadPerm
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
    if (type === 'authorize_udf') {
      getUdfs(userId)
    }
    if (type === 'authorize_resource') {
      getResources(userId)
    }
    if (type === 'authorize_namespace') {
      getNamespaces(userId)
    }
  }

  /*
    getParent
  */
  const getParent = (data2: Array<number>, nodeId2: number) => {
    let arrRes: Array<any> = []
    if (data2.length === 0) {
      if (nodeId2) {
        arrRes.unshift(data2)
      }
      return arrRes
    }
    const rev = (data: Array<any>, nodeId: number) => {
      for (let i = 0, length = data.length; i < length; i++) {
        const node = data[i]
        if (node.id === nodeId) {
          arrRes.unshift(node)
          rev(data2, node.pid)
          break
        } else {
          if (node.children) {
            rev(node.children, nodeId)
          }
        }
      }
      return arrRes
    }
    arrRes = rev(data2, nodeId2)
    return arrRes
  }

  const onSave = async (type: TAuthType, userId: number) => {
    if (state.saving) return false
    state.saving = true
    if (type === 'authorize_project') {
      // await grantProject({
      //   userId,
      //   projectIds: state.authorizedProjects.join(',')
      // })
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
        udfIds: state.authorizedUdfs.join(',')
      })
    }
    if (type === 'authorize_resource') {
      let fullPathFileId = []
      const pathFileId: Array<string> = []
      state.authorizedFileResources.forEach((v: number) => {
        state.fileResources.forEach((v1: any) => {
          const arr = []
          arr[0] = v1
          if (getParent(arr, v).length > 0) {
            fullPathFileId = getParent(arr, v).map((v2: any) => {
              return v2.id
            })
            pathFileId.push(fullPathFileId.join('-'))
          }
        })
      })

      let fullPathUdfId = []
      const pathUdfId: Array<string> = []
      state.authorizedUdfResources.forEach((v: number) => {
        state.udfResources.forEach((v1: any) => {
          const arr = []
          arr[0] = v1
          if (getParent(arr, v).length > 0) {
            fullPathUdfId = getParent(arr, v).map((v2: any) => {
              return v2.id
            })
            pathUdfId.push(fullPathUdfId.join('-'))
          }
        })
      })

      let fullPathFileIdWithReadPerm = []
      const pathFileIdWithReadPerm: Array<string> = []
      state.authorizedFileResourcesWithReadPerm.forEach((v: number) => {
        state.fileResources.forEach((v1: any) => {
          const arr = []
          arr[0] = v1
          if (getParent(arr, v).length > 0) {
            fullPathFileIdWithReadPerm = getParent(arr, v).map((v2: any) => {
              return v2.id
            })
            pathFileIdWithReadPerm.push(fullPathFileIdWithReadPerm.join('-'))
          }
        })
      })

      let fullPathUdfIdWithReadPerm = []
      const pathUdfIdWithReadPerm: Array<string> = []
      state.authorizedUdfResourcesWithReadPerm.forEach((v: number) => {
        state.udfResources.forEach((v1: any) => {
          const arr = []
          arr[0] = v1
          if (getParent(arr, v).length > 0) {
            fullPathUdfIdWithReadPerm = getParent(arr, v).map((v2: any) => {
              return v2.id
            })
            pathUdfIdWithReadPerm.push(fullPathUdfIdWithReadPerm.join('-'))
          }
        })
      })

      const allPathId = pathFileId.concat(pathUdfId)
      const allPathIdWithReadPerm = pathFileIdWithReadPerm.concat(pathUdfIdWithReadPerm)
      await grantResourceWithPermLevel({
        userId,
        allPermResourceIds: allPathId.join(','),
        readPermResourceIds: allPathIdWithReadPerm.join(',')
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

  return { state, onInit, onSave, onOperationClick, getProjects, revokeProjectByIdRequest, grantProjectRequest, grantProjectWithReadPermRequest, requestData, handleChangePageSize }
}
