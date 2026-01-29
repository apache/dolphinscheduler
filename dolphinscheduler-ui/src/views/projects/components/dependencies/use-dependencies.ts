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

import { DependentTaskReq } from '@/service/modules/lineages/types'
import { queryDependentTasks } from '@/service/modules/lineages'

export function useDependencies() {
  const getDependentTasksBySingleTask = async (
    projectCode: any,
    workflowCode: any,
    taskCode: any
  ) => {
    const tasks = [] as any
    if (workflowCode && taskCode) {
      const dependentTaskReq = {
        workFlowCode: workflowCode,
        taskCode: taskCode
      } as DependentTaskReq
      await queryDependentTasks(projectCode, dependentTaskReq).then(
        (res: any) => {
          if (res?.data?.length > 0) {
            res.data
              .filter(
                (item: any) => item.workflowDefinitionCode !== workflowCode
              )
              .forEach((item: any) => {
                tasks.push(
                  item.workflowDefinitionName + '->' + item.taskDefinitionName
                )
              })
          }
        }
      )
    }
    return tasks
  }

  const getDependentTasksByWorkflow = async (
    projectCode: any,
    workflowCode: any
  ) => {
    const tasks = [] as any
    if (workflowCode) {
      const dependentTaskReq = {
        workFlowCode: workflowCode
      } as DependentTaskReq
      await queryDependentTasks(projectCode, dependentTaskReq).then(
        (res: any) => {
          if (res?.data?.length > 0) {
            res.data
              .filter(
                (item: any) => item.workflowDefinitionCode !== workflowCode
              )
              .forEach((item: any) => {
                tasks.push(
                  item.workflowDefinitionName + '->' + item.taskDefinitionName
                )
              })
          }
        }
      )
    }
    return tasks
  }

  const getDependentTasksByMultipleTasks = async (
    projectCode: any,
    workflowCode: any,
    taskCodes: any[]
  ) => {
    let tasks = [] as any
    if (workflowCode && taskCodes?.length > 0) {
      for (const taskCode of taskCodes) {
        const res = await getDependentTasksBySingleTask(
          projectCode,
          workflowCode,
          taskCode
        )
        if (res?.length > 0) {
          tasks = tasks.concat(res)
        }
      }
    }
    return tasks
  }

  const getDependentTaskLinksByMultipleTasks = async (
    projectCode: any,
    workflowCode: any,
    taskCodes: any[]
  ) => {
    let dependentTaskLinks = [] as any
    if (workflowCode && projectCode) {
      for (const taskCode of taskCodes) {
        await getDependentTaskLinksByTask(
          projectCode,
          workflowCode,
          taskCode
        ).then((res: any) => {
          dependentTaskLinks = dependentTaskLinks.concat(res)
        })
      }
    }
    return dependentTaskLinks
  }

  const getDependentTaskLinks = async (projectCode: any, workflowCode: any) => {
    const dependentTaskReq = { workFlowCode: workflowCode } as DependentTaskReq
    const dependentTaskLinks = [] as any
    if (workflowCode && projectCode) {
      await queryDependentTasks(projectCode, dependentTaskReq).then(
        (res: any) => {
          if (res?.data?.length > 0) {
            res.data
              .filter((item: any) => {
                if (item.workflowDefinitionCode) {
                  return item.workflowDefinitionCode !== workflowCode
                } else {
                  return false
                }
              })
              .forEach((item: any) => {
                dependentTaskLinks.push({
                  text:
                    item.workflowDefinitionName +
                    '->' +
                    item.taskDefinitionName,
                  show: true,
                  action: () => {
                    const url = `/projects/${item.projectCode}/workflow/definitions/${item.workflowDefinitionCode}`
                    window.open(url, '_blank')
                  }
                })
              })
          }
        }
      )
    }
    return dependentTaskLinks
  }

  const getDependentTaskLinksByTask = async (
    projectCode: any,
    workflowCode: any,
    taskCode: any
  ) => {
    const dependentTaskReq = {
      workFlowCode: workflowCode,
      taskCode: taskCode
    } as DependentTaskReq
    const dependentTaskLinks = [] as any
    if (workflowCode && projectCode) {
      await queryDependentTasks(projectCode, dependentTaskReq).then(
        (res: any) => {
          if (res?.data?.length > 0) {
            res.data
              .filter(
                (item: any) => item.workflowDefinitionCode !== workflowCode
              )
              .forEach((item: any) => {
                dependentTaskLinks.push({
                  text:
                    item.workflowDefinitionName +
                    '->' +
                    item.taskDefinitionName,
                  show: true,
                  action: () => {
                    const url = `/projects/${item.projectCode}/workflow/definitions/${item.workflowDefinitionCode}`
                    window.open(url, '_blank')
                  }
                })
              })
          }
        }
      )
    }
    return dependentTaskLinks
  }

  return {
    getDependentTasksBySingleTask,
    getDependentTasksByMultipleTasks,
    getDependentTaskLinks,
    getDependentTasksByWorkflow,
    getDependentTaskLinksByTask,
    getDependentTaskLinksByMultipleTasks
  }
}
