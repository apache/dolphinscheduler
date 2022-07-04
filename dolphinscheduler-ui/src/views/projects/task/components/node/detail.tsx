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

import { defineComponent, ref, watch, inject, Ref, unref } from 'vue'
import Form from '@/components/form'
import { useTask } from './use-task'
import { useTaskNodeStore } from '@/store/project/task-node'
import type { ITaskData, EditWorkflowDefinition } from './types'

interface IDetailPanel {
  projectCode: number
  data: ITaskData
  readonly: false
  from: number
  detailRef?: Ref
  definition?: EditWorkflowDefinition
}

const NodeDetail = defineComponent({
  name: 'NodeDetail',
  emits: ['taskTypeChange'],
  setup(props, { expose, emit }) {
    const taskStore = useTaskNodeStore()

    const formRef = ref()
    const detailData: IDetailPanel = inject('data') || {
      projectCode: 0,
      data: {
        taskType: 'SHELL'
      },
      readonly: false,
      from: 0
    }
    const { data, projectCode, from, readonly, definition } = unref(detailData)

    const { elementsRef, rulesRef, model } = useTask({
      data,
      projectCode,
      from,
      readonly,
      definition
    })
    watch(
      () => model.taskType,
      async (taskType) => {
        taskStore.updateName(model.name || '')
        emit('taskTypeChange', taskType)
      }
    )

    expose(formRef)

    return () => (
      <Form
        ref={formRef}
        meta={{
          model,
          rules: rulesRef.value,
          elements: elementsRef.value,
          disabled: unref(readonly)
        }}
        layout={{
          xGap: 10
        }}
      />
    )
  }
})

export default NodeDetail
