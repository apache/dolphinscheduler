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
import { defineStore } from 'pinia'
import { uniqBy } from 'lodash'
import type {
  TaskNodeState,
  EditWorkflowDefinition,
  IOption,
  IResource,
  ProgramType,
  IMainJar,
  DependentResultType,
  BDependentResultType
} from './types'

export const useTaskNodeStore = defineStore({
  id: 'project-task',
  state: (): TaskNodeState => ({
    preTaskOptions: [],
    postTaskOptions: [],
    preTasks: [],
    resources: [],
    mainJars: {},
    name: '',
    dependentResult: {}
  }),
  persist: true,
  getters: {
    getPreTaskOptions(): IOption[] {
      return this.preTaskOptions
    },
    getPostTaskOptions(): IOption[] {
      return this.postTaskOptions
    },
    getPreTasks(): number[] {
      return this.preTasks
    },
    getResources(): IResource[] {
      return this.resources
    },
    getMainJar(state) {
      return (type: ProgramType): IMainJar[] | undefined => state.mainJars[type]
    },
    getName(): string {
      return this.name
    },
    getDependentResult(): DependentResultType {
      return this.dependentResult
    }
  },
  actions: {
    updateDefinition(definition?: EditWorkflowDefinition, code?: number) {
      if (!definition) return
      const { processTaskRelationList = [], taskDefinitionList = [] } =
        definition

      const preTaskOptions: { value: number; label: string }[] = []
      const tasks: { [field: number]: string } = {}
      taskDefinitionList.forEach(
        (task: { code: number; taskType: string; name: string }) => {
          tasks[task.code] = task.name
          if (task.code === code) return
          if (
            task.taskType === 'CONDITIONS' &&
            processTaskRelationList.filter(
              (relation: { preTaskCode: number }) =>
                relation.preTaskCode === task.code
            ).length >= 2
          ) {
            return
          }
          preTaskOptions.push({
            value: task.code,
            label: task.name
          })
        }
      )

      this.preTaskOptions = uniqBy(preTaskOptions, 'value')
      if (!code) return
      const preTasks: number[] = []
      const postTaskOptions: { value: number; label: string }[] = []
      processTaskRelationList.forEach(
        (relation: { preTaskCode: number; postTaskCode: number }) => {
          if (relation.preTaskCode === code) {
            postTaskOptions.push({
              value: relation.postTaskCode,
              label: tasks[relation.postTaskCode]
            })
          }
          if (relation.postTaskCode === code && relation.preTaskCode !== 0) {
            preTasks.push(relation.preTaskCode)
            if (
              !this.preTaskOptions.find(
                (item) => item.value === relation.preTaskCode
              )
            ) {
              this.preTaskOptions.push({
                value: relation.preTaskCode,
                label: tasks[relation.preTaskCode]
              })
            }
          }
        }
      )
      this.preTasks = preTasks
      this.postTaskOptions = postTaskOptions
    },
    updateResource(resources: IResource[]) {
      this.resources = resources
    },
    updateMainJar(type: ProgramType, mainJar: IMainJar[]) {
      this.mainJars[type] = mainJar
    },
    updateName(name: string) {
      this.name = name
    },
    updateDependentResult(dependentResult: BDependentResultType) {
      const result = {} as DependentResultType
      for (const [key, value] of Object.entries(dependentResult)) {
        result[key] = value === 'FAILED' ? 'FAILURE' : value
      }
      this.dependentResult = result
    },
    init() {
      this.preTaskOptions = []
      this.postTaskOptions = []
      this.preTasks = []
      this.resources = []
      this.mainJars = {}
      this.name = ''
    }
  }
})
