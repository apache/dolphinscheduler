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

import { useAsyncState } from '@vueuse/core'
import {
  queryResourceListPaging,
  viewResource
} from '@/service/modules/resources'
import type { ResourceListRes } from '@/service/modules/resources/types'
import { IResourceListState, ISetPagination, ResourceType } from './types'

export function useFileState(
  setPagination: ISetPagination = {} as ISetPagination
) {
  const getResourceListState: IResourceListState = (
    type: ResourceType,
    fullName = '',
    tenantCode = '',
    searchVal = '',
    pageNo = 1,
    pageSize = 10
  ) => {
    const { state } = useAsyncState(
      queryResourceListPaging({
        fullName,
        tenantCode,
        type: type,
        searchVal,
        pageNo,
        pageSize
      }).then((res: ResourceListRes): any => {
        const { total } = res
        setPagination(total)
        const table = res.totalList.map((item) => {
          return {
            id: item.id,
            name: item.alias,
            alias: item.alias,
            fullName: item.fullName,
            type: item.type,
            user_name: item.userName,
            directory: item.directory,
            file_name: item.fileName,
            size: item.size,
            create_time: item.createTime,
            update_time: item.updateTime
          }
        })

        return { total, table }
      }),
      { total: 0, table: [] }
    )

    return state
  }

  const getResourceView = (fullName: string, tenantCode: string) => {
    const params = {
      skipLineNum: 0,
      limit: 3000,
      fullName: fullName,
      tenantCode: tenantCode
    }
    const { state } = useAsyncState(viewResource(params), {})
    return state
  }

  return { getResourceListState, getResourceView }
}
