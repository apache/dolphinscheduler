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
import { useAsyncState } from '@vueuse/core'
import { viewGanttTree } from '@/service/modules/process-instances'
import { IGanttRes } from './type'

export function useGantt() {
  const variables = reactive({
    seriesData: [],
    taskList: [] as Array<string>
  })

  const formatGantt = (obj: IGanttRes) => {
    variables.seriesData = []
    variables.taskList = []

    variables.seriesData = obj.tasks.map((item) => {
      variables.taskList.push(item.taskName)
      return {
        name: item.taskName,
        ...item
      }
    }) as any
  }

  const getGantt = (id: number, code: number) => {
    const { state } = useAsyncState(
      viewGanttTree(id, code).then((res: IGanttRes) => {
        formatGantt(res)
      }),
      {}
    )
    return state
  }

  return { variables, getGantt }
}
