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
import { ref, Ref, unref } from 'vue'
import nodes from './tasks'
import getElementByJson from '@/components/form/get-elements-by-json'
import { useTaskNodeStore } from '@/store/project/task-node'
import type {
  IFormItem,
  IJsonItem,
  INodeData,
  ITaskData,
  FormRules,
  EditWorkflowDefinition
} from './types'

export function useTask({
  data,
  projectCode,
  from,
  readonly,
  definition
}: {
  data: ITaskData
  projectCode: number
  from?: number
  readonly?: boolean
  definition?: EditWorkflowDefinition
}): {
  elementsRef: Ref<IFormItem[]>
  rulesRef: Ref<FormRules>
  model: INodeData
} {
  const taskStore = useTaskNodeStore()
  taskStore.updateDefinition(unref(definition), data?.code)

  const jsonRef = ref([]) as Ref<IJsonItem[]>
  const elementsRef = ref([]) as Ref<IFormItem[]>
  const rulesRef = ref({})

  const params = {
    projectCode,
    from,
    readonly,
    data,
    jsonRef,
    updateElements: () => {
      getElements()
    }
  }

  const { model, json } = nodes[data.taskType || 'SHELL'](params)
  jsonRef.value = json
  model.preTasks = taskStore.getPreTasks
  model.name = taskStore.getName

  const getElements = () => {
    const { rules, elements } = getElementByJson(jsonRef.value, model)
    elementsRef.value = elements
    rulesRef.value = rules
  }

  getElements()

  return { elementsRef, rulesRef, model }
}
