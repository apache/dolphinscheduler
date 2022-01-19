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

import { useRoute } from 'vue-router'
import { useAsyncState } from '@vueuse/core'
import { countDefinitionByUser } from '@/service/modules/projects-analysis'
import type { ProcessDefinitionRes } from '@/service/modules/projects-analysis/types'
import type { DefinitionChartData } from './types'

export function useProcessDefinition() {
  const route = useRoute()

  const getProcessDefinition = () => {
    const { state } = useAsyncState(
      countDefinitionByUser({
        projectCode: Number(route.params.projectCode)
      }).then((res: ProcessDefinitionRes): DefinitionChartData => {
        const xAxisData = res.userList.map((item) => item.userName)
        const seriesData = res.userList.map((item) => item.count)

        return { xAxisData, seriesData }
      }),
      { xAxisData: [], seriesData: [] }
    )
    return state
  }

  return { getProcessDefinition }
}
