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
import { queryResourceListPaging } from '@/service/modules/resources'
import { IUdfResourceParam, IUdfRes } from './types'

export function useResource() {
  const getResource = (params: IUdfResourceParam) => {
    const { state } = useAsyncState(
      queryResourceListPaging({ ...params }).then((res: any) => {
        const { total } = res
        // setPagination(total)
        const table = res.totalList.map((item: any) => {
          return {
            id: item.id,
            pid: item.pid,
            userId: item.userId,
            fileName: item.fileName,
            fullName: item.fullName,
            alias: item.alias,
            directory: item.directory,
            size: item.size,
            type: item.type,
            description: item.description,
            createTime: item.createTime,
            updateTime: item.updateTime
          }
        })
        return { total, table }
      }),
      { total: 0, table: [] }
    )
    return state
  }

  return { getResource }
}
