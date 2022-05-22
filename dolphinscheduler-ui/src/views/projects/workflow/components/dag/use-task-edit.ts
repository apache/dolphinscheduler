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

import { ref, onMounted, watch } from 'vue'
import { remove } from 'lodash'
import { TaskType } from '@/views/projects/task/constants/task-type'
import { formatParams } from '@/views/projects/task/components/node/format-data'
import { useCellUpdate } from './dag-hooks'
import type { Ref } from 'vue'
import type { Graph } from '@antv/x6'
import type {
  Coordinate,
  NodeData,
  WorkflowDefinition,
  EditWorkflowDefinition
} from './types'

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
  const {
    addNode,
    removeNode,
    getSources,
    getTargets,
    setNodeName,
    setNodeEdge
  } = useCellUpdate({
    graph
  })
  const processDefinition = ref(
    definition?.value || {
      processDefinition: {},
      processTaskRelationList: [],
      taskDefinitionList: []
    }
  ) as Ref<EditWorkflowDefinition>

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
    addNode(code + '', type, '', 'YES', coordinate)
    processDefinition.value.taskDefinitionList.push({
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
    flag: string,
    coordinate: Coordinate
  ) {
    addNode(code + '', type, name, flag, coordinate)
    const definition = processDefinition.value.taskDefinitionList.find(
      (t) => t.code === targetCode
    )

    const newDefinition = {
      ...definition,
      code,
      name
    } as NodeData

    processDefinition.value.taskDefinitionList.push(newDefinition)
  }

  /**
   * Remove task
   * @param {number} codes
   */
  function removeTasks(codes: number[], cells: any[]) {
    processDefinition.value.taskDefinitionList =
      processDefinition.value.taskDefinitionList.filter(
        (task) => !codes.includes(task.code)
      )
    codes.forEach((code: number) => {
      remove(
        processDefinition.value.processTaskRelationList,
        (process) =>
          process.postTaskCode === code || process.preTaskCode === code
      )
    })
    cells?.forEach((cell) => {
      if (cell.isEdge()) {
        const preTaskCode = cell.getSourceCellId()
        const postTaskCode = cell.getTargetCellId()
        remove(
          processDefinition.value.processTaskRelationList,
          (process) =>
            String(process.postTaskCode) === postTaskCode &&
            String(process.preTaskCode) === preTaskCode
        )
      }
    })
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
    const definition = processDefinition.value.taskDefinitionList.find(
      (t) => t.code === code
    )
    if (definition) {
      currTask.value = definition
    }
    updatePreTasks(getSources(String(code)), code)
    updatePostTasks(code)
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
    processDefinition.value.taskDefinitionList =
      processDefinition.value.taskDefinitionList.map((task) => {
        if (task.code === currTask.value?.code) {
          setNodeName(task.code + '', taskDef.name)

          setNodeEdge(String(task.code), data.preTasks)
          updatePreTasks(data.preTasks, task.code)
          return {
            ...taskDef,
            version: task.version,
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
    if (!currTask.value.name) {
      removeNode(String(currTask.value.code))
      remove(
        processDefinition.value.taskDefinitionList,
        (task) => task.code === currTask.value.code
      )
    }
  }

  function updatePreTasks(preTasks: number[], code: number) {
    if (processDefinition.value?.processTaskRelationList?.length) {
      remove(
        processDefinition.value.processTaskRelationList,
        (process) => process.postTaskCode === code
      )
    }
    if (!preTasks?.length) return
    preTasks.forEach((task) => {
      processDefinition.value?.processTaskRelationList.push({
        postTaskCode: code,
        preTaskCode: task,
        name: '',
        preTaskVersion: 1,
        postTaskVersion: 1,
        conditionType: 'NONE',
        conditionParams: {}
      })
    })
  }

  function updatePostTasks(code: number) {
    const targets = getTargets(String(code))
    targets.forEach((target: number) => {
      if (
        !processDefinition.value?.processTaskRelationList.find(
          (relation) =>
            relation.postTaskCode === target && relation.preTaskCode === code
        )
      ) {
        processDefinition.value?.processTaskRelationList.push({
          postTaskCode: target,
          preTaskCode: code,
          name: '',
          preTaskVersion: 1,
          postTaskVersion: 1,
          conditionType: 'NONE',
          conditionParams: {}
        })
      }
    })
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
    if (definition.value) processDefinition.value = definition.value
  })

  return {
    currTask,
    taskModalVisible,
    processDefinition,
    taskConfirm,
    taskCancel,
    appendTask,
    editTask,
    copyTask,
    removeTasks
  }
}
