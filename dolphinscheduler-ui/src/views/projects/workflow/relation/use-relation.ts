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

import { reactive, ref } from 'vue'
import { useAsyncState } from '@vueuse/core'
import {
  queryWorkFlowList,
  queryLineageByWorkFlowCode,
  queryLineageByWorkFlowName
} from '@/service/modules/lineages'
import type {
  WorkflowRes,
  WorkFlowListRes
} from '@/service/modules/lineages/types'

export function useRelation() {
  const variables = reactive({
    workflowOptions: [],
    workflow: ref(null),
    seriesData: [],
    labelShow: ref(true)
  })

  const formatWorkflow = (obj: Array<WorkFlowListRes>) => {
    variables.seriesData = []

    variables.seriesData = obj.map((item) => {
      return {
        name: item.workFlowName,
        id: item.workFlowCode,
        ...item
      }
    }) as any
  }

  const getWorkflowName = (projectCode: number) => {
    const { state } = useAsyncState(
      queryLineageByWorkFlowName({ projectCode }).then(
        (res: Array<WorkFlowListRes>) => {
          variables.workflowOptions = res.map((item) => {
            return {
              label: item.workFlowName,
              value: item.workFlowCode
            }
          }) as any
        }
      ),
      {}
    )

    return state
  }

  const getOneWorkflow = (workflowCode: number, projectCode: number) => {
    const { state } = useAsyncState(
      queryLineageByWorkFlowCode(
        { workFlowCode: workflowCode },
        { projectCode }
      ).then((res: WorkflowRes) => {
        formatWorkflow(res.workFlowList)
      }),
      {}
    )

    return state
  }

  const getWorkflowList = (projectCode: number) => {
    const { state } = useAsyncState(
      queryWorkFlowList({
        projectCode
      }).then((res: WorkflowRes) => {
        formatWorkflow(res.workFlowList)
      }),
      {}
    )

    return state
  }

  return { variables, getWorkflowName, getOneWorkflow, getWorkflowList }
}
