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

import { ref, onMounted } from 'vue'
import type { Ref } from 'vue'
import type { Graph } from '@antv/x6'
import type { Coordinate, NodeData } from './types'
import { TaskType } from '@/views/projects/task/constants/task-type'
import { useCellUpdate } from './dag-hooks'

interface Options {
  graph: Ref<Graph | undefined>
}

/**
 * Edit task configuration when dbclick
 * @param {Options} options
 * @returns
 */
export function useTaskEdit(options: Options) {
  const { graph } = options

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

  function openTaskModal(task: NodeData) {
    currTask.value = task
    taskModalVisible.value = true
  }

  /**
   * The confirm event in task config modal
   * @param formRef
   * @param from
   */
  function taskConfirm({ formRef, form }: any) {
    formRef.validate((errors: any) => {
      if (!errors) {
        // override target config
        taskDefinitions.value = taskDefinitions.value.map((task) => {
          if (task.code === currTask.value?.code) {
            setNodeName(task.code + '', form.name)
            console.log(form)
            console.log(JSON.stringify(form))
            return {
              code: task.code,
              ...form
            }
          }
          return task
        })
        taskModalVisible.value = false
      }
    })
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
        const definition = taskDefinitions.value.find((t) => t.code === code)
        if (definition) {
          currTask.value = definition
        }
        taskModalVisible.value = true
      })
    }
  })

  return {
    currTask,
    taskModalVisible,
    taskConfirm,
    taskCancel,
    appendTask,
    taskDefinitions
  }
}
