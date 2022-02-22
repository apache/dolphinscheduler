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

import _ from 'lodash'
import { ref, onMounted, watch } from 'vue'
import type { Ref } from 'vue'
import type { Graph } from '@antv/x6'
import type { Coordinate, NodeData } from './types'
import { TaskType } from '@/views/projects/task/constants/task-type'
import { formatParams } from '@/views/projects/task/components/node/format-data'
import { useCellUpdate } from './dag-hooks'
import { WorkflowDefinition } from './types'

interface Options {
  graph: Ref<Graph | undefined>
  definition: Ref<WorkflowDefinition | undefined>
}

/**
 * Edit task configuration when dbclick
 * @param {Options} options
 * @returns
 */
export function useTaskEdit(options: Options) {
  const { graph, definition } = options

  const { addNode, setNodeName } = useCellUpdate({ graph })

  const taskDefinitions = ref<NodeData[]>([])
  const currTask = ref<NodeData>({
    taskType: 'SHELL',
    code: 0,
    name: ''
  })
  const taskModalVisible = ref(false)

  /**
   * Append a new task
   */
  function appendTask(code: number, type: TaskType, coordinate: Coordinate) {
    addNode(code + '', type, '', coordinate)
    taskDefinitions.value.push({
      code,
      taskType: type,
      name: ''
    })
    openTaskModal({ code, taskType: type, name: '' })
  }

  /**
   * Copy a task
   */
  function copyTask(
    name: string,
    code: number,
    targetCode: number,
    type: TaskType,
    coordinate: Coordinate
  ) {
    addNode(code + '', type, name, coordinate)
    const definition = taskDefinitions.value.find((t) => t.code === targetCode)

    const newDefinition = {
      ...definition,
      code,
      name
    } as NodeData

    taskDefinitions.value.push(newDefinition)
  }

  /**
   * Remove task
   * @param {number} code
   */
  function removeTasks(codes: number[]) {
    taskDefinitions.value = taskDefinitions.value.filter(
      (task) => !codes.includes(task.code)
    )
  }

  function openTaskModal(task: NodeData) {
    currTask.value = task
    taskModalVisible.value = true
  }

  /**
   * Edit task
   * @param {number} code
   */
  function editTask(code: number) {
    const definition = taskDefinitions.value.find((t) => t.code === code)
    if (definition) {
      currTask.value = definition
    }
    taskModalVisible.value = true
  }

  /**
   * The confirm event in task config modal
   * @param formRef
   * @param from
   */
  function taskConfirm({ data }: any) {
    const taskDef = formatParams(data).taskDefinitionJsonObj as NodeData
    // override target config
    taskDefinitions.value = taskDefinitions.value.map((task) => {
      if (task.code === currTask.value?.code) {
        setNodeName(task.code + '', taskDef.name)
        return {
          ...taskDef,
          code: task.code,
          taskType: currTask.value.taskType
        }
      }
      return task
    })
    taskModalVisible.value = false
  }

  /**
   * The cancel event in task config modal
   */
  function taskCancel() {
    taskModalVisible.value = false
  }

  onMounted(() => {
    if (graph.value) {
      graph.value.on('cell:dblclick', ({ cell }) => {
        const code = Number(cell.id)
        editTask(code)
      })
    }
  })

  watch(definition, () => {
    taskDefinitions.value = definition.value?.taskDefinitionList || []
  })

  return {
    currTask,
    taskModalVisible,
    taskConfirm,
    taskCancel,
    appendTask,
    editTask,
    copyTask,
    taskDefinitions,
    removeTasks
  }
}
