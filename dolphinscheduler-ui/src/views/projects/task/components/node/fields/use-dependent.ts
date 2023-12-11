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

import { ref, onMounted, watch, h, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { NEllipsis, NIcon } from 'naive-ui'
import { useRelationCustomParams, useDependentTimeout } from '.'
import { useTaskNodeStore } from '@/store/project/task-node'
import { queryAllProjectListForDependent } from '@/service/modules/projects'
import { tasksState } from '@/common/common'
import {
  queryProcessDefinitionList,
  getTasksByDefinitionList
} from '@/service/modules/process-definition'
import { Router, useRouter } from 'vue-router'
import type {
  IJsonItem,
  IDependentItem,
  IDependentItemOptions,
  IDependTaskOptions,
  IDependTask,
  ITaskState,
  IDateType
} from '../types'
import { IRenderOption } from '../types'

export function useDependent(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const router: Router = useRouter()
  const nodeStore = useTaskNodeStore()

  const dependentFailurePolicyOptions = computed(() => {
    return [
      {
        label: t('project.node.dependent_failure_policy_failure'),
        value: 'DEPENDENT_FAILURE_FAILURE'
      },
      {
        label: t('project.node.dependent_failure_policy_waiting'),
        value: 'DEPENDENT_FAILURE_WAITING'
      }
    ]
  })
  const failureWaitingTimeSpan = computed(() =>
    model.failurePolicy === 'DEPENDENT_FAILURE_WAITING' ? 12 : 0
  )
  const dependentResult = nodeStore.getDependentResult
  const TasksStateConfig = tasksState(t)
  const projectList = ref([] as IRenderOption[])
  const processCache = {} as {
    [key: number]: IRenderOption[]
  }
  const taskCache = {} as {
    [key: number]: IRenderOption[]
  }
  const selectOptions = ref([] as IDependTaskOptions[])

  const DependentTypeOptions = [
    {
      value: 'DEPENDENT_ON_WORKFLOW',
      label: t('project.node.dependent_on_workflow')
    },
    {
      value: 'DEPENDENT_ON_TASK',
      label: t('project.node.dependent_on_task')
    }
  ]

  const CYCLE_LIST = [
    {
      value: 'month',
      label: t('project.node.month')
    },
    {
      value: 'week',
      label: t('project.node.week')
    },
    {
      value: 'day',
      label: t('project.node.day')
    },
    {
      value: 'hour',
      label: t('project.node.hour')
    }
  ]
  const DATE_LIST = {
    hour: [
      {
        value: 'currentHour',
        label: t('project.node.current_hour')
      },
      {
        value: 'last1Hour',
        label: t('project.node.last_1_hour')
      },
      {
        value: 'last2Hours',
        label: t('project.node.last_2_hour')
      },
      {
        value: 'last3Hours',
        label: t('project.node.last_3_hour')
      },
      {
        value: 'last24Hours',
        label: t('project.node.last_24_hour')
      }
    ],
    day: [
      {
        value: 'today',
        label: t('project.node.today')
      },
      {
        value: 'last1Days',
        label: t('project.node.last_1_days')
      },
      {
        value: 'last2Days',
        label: t('project.node.last_2_days')
      },
      {
        value: 'last3Days',
        label: t('project.node.last_3_days')
      },
      {
        value: 'last7Days',
        label: t('project.node.last_7_days')
      }
    ],
    week: [
      {
        value: 'thisWeek',
        label: t('project.node.this_week')
      },
      {
        value: 'lastWeek',
        label: t('project.node.last_week')
      },
      {
        value: 'lastMonday',
        label: t('project.node.last_monday')
      },
      {
        value: 'lastTuesday',
        label: t('project.node.last_tuesday')
      },
      {
        value: 'lastWednesday',
        label: t('project.node.last_wednesday')
      },
      {
        value: 'lastThursday',
        label: t('project.node.last_thursday')
      },
      {
        value: 'lastFriday',
        label: t('project.node.last_friday')
      },
      {
        value: 'lastSaturday',
        label: t('project.node.last_saturday')
      },
      {
        value: 'lastSunday',
        label: t('project.node.last_sunday')
      }
    ],
    month: [
      {
        value: 'thisMonth',
        label: t('project.node.this_month')
      },
      {
        value: 'thisMonthBegin',
        label: t('project.node.this_month_begin')
      },
      {
        value: 'lastMonth',
        label: t('project.node.last_month')
      },
      {
        value: 'lastMonthBegin',
        label: t('project.node.last_month_begin')
      },
      {
        value: 'lastMonthEnd',
        label: t('project.node.last_month_end')
      }
    ]
  } as { [key in IDateType]: { value: string; label: string }[] }

  const getProjectList = async () => {
    const result = await queryAllProjectListForDependent()
    projectList.value = result.map((item: { code: number; name: string }) => ({
      value: item.code,
      label: () => h(NEllipsis, null, item.name),
      filterLabel: item.name
    }))
    return projectList
  }
  const getProcessList = async (code: number) => {
    if (processCache[code]) {
      return processCache[code]
    }
    const result = await queryProcessDefinitionList(code)
    const processList = result.map((item: { code: number; name: string }) => ({
      value: item.code,
      label: () => h(NEllipsis, null, item.name),
      filterLabel: item.name
    }))
    processCache[code] = processList

    return processList
  }

  const getTaskList = async (code: number, processCode: number) => {
    if (taskCache[processCode]) {
      return taskCache[processCode]
    }
    const result = await getTasksByDefinitionList(code, processCode)
    const taskList = result.map((item: { code: number; name: string }) => ({
      value: item.code,
      label: () => h(NEllipsis, null, item.name),
      filterLabel: item.name
    }))
    taskList.unshift({
      value: -1,
      label: 'ALL',
      filterLabel: 'ALL'
    })
    taskCache[processCode] = taskList
    return taskList
  }

  const renderState = (item: {
    definitionCode: number
    depTaskCode: number
    cycle: string
    dateValue: string
  }) => {
    if (!item || router.currentRoute.value.name !== 'workflow-instance-detail')
      return null
    const key = `${item.definitionCode}-${item.depTaskCode}-${item.cycle}-${item.dateValue}`
    const state: ITaskState = dependentResult[key]
    return h(NIcon, { size: 24, color: TasksStateConfig[state]?.color }, () =>
      h(TasksStateConfig[state]?.icon)
    )
  }

  onMounted(() => {
    getProjectList()
  })

  watch(
    () => model.dependTaskList,
    (value) => {
      selectOptions.value = []
      value.forEach((item: IDependTask, taskIndex: number) => {
        if (!item.dependItemList?.length) return

        const itemListOptions = ref([] as IDependentItemOptions[])
        item.dependItemList?.forEach(
          async (dependItem: IDependentItem, itemIndex: number) => {
            itemListOptions.value[itemIndex] = {}

            if (!dependItem.dependentType) {
              if (dependItem.depTaskCode == 0)
                dependItem.dependentType = 'DEPENDENT_ON_WORKFLOW'
              else
                dependItem.dependentType = 'DEPENDENT_ON_TASK'
            }
            if (dependItem.projectCode) {
              itemListOptions.value[itemIndex].definitionCodeOptions =
                await getProcessList(dependItem.projectCode)
            }
            if (dependItem.projectCode && dependItem.definitionCode) {
              itemListOptions.value[itemIndex].depTaskCodeOptions =
                await getTaskList(
                  dependItem.projectCode,
                  dependItem.definitionCode
                )
            }
            if (dependItem.cycle) {
              itemListOptions.value[itemIndex].dateOptions =
                DATE_LIST[dependItem.cycle]
            }
          }
        )
        selectOptions.value[taskIndex] = {} as IDependTaskOptions
        selectOptions.value[taskIndex].dependItemList = itemListOptions.value
      })
    }
  )

  return [
    ...useDependentTimeout(model),
    ...useRelationCustomParams({
      model,
      children: (i = 0) => ({
        type: 'custom-parameters',
        field: 'dependItemList',
        span: 18,
        children: [
          (j = 0) => ({
            type: 'select',
            field: 'dependentType',
            name: t('project.node.dependent_type'),
            span: 24,
            props: {
              onUpdateValue: (dependentType: string) => {
                const item = model.dependTaskList[i].dependItemList[j]
                if (item.definitionCode)
                  item.depTaskCode = dependentType === 'DEPENDENT_ON_WORKFLOW' ? 0 : -1
              }
            },
            options: DependentTypeOptions,
            value: 'DEPENDENT_ON_WORKFLOW'
          }),
          (j = 0) => ({
            type: 'select',
            field: 'projectCode',
            name: t('project.node.project_name'),
            span: 24,
            props: {
              filterable: true,
              filter: (query: string, option: IRenderOption) => {
                return option.filterLabel
                  .toLowerCase()
                  .includes(query.toLowerCase())
              },
              onUpdateValue: async (projectCode: number) => {
                const item = model.dependTaskList[i].dependItemList[j]
                const options = selectOptions?.value[i] || {}
                const itemListOptions = options?.dependItemList || []
                const itemOptions = {} as IDependentItemOptions
                itemOptions.definitionCodeOptions = await getProcessList(
                  projectCode
                )
                itemListOptions[j] = itemOptions
                options.dependItemList = itemListOptions
                selectOptions.value[i] = options
                item.depTaskCode = null
                item.definitionCode = null
                item.parameterPassing = false
              }
            },
            options: projectList,
            path: `dependTaskList.${i}.dependItemList.${j}.projectCode`,
            rule: {
              required: true,
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.project_name_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'select',
            field: 'definitionCode',
            span: 24,
            name: t('project.node.process_name'),
            props: {
              filterable: true,
              filter: (query: string, option: IRenderOption) => {
                return option.filterLabel
                  .toLowerCase()
                  .includes(query.toLowerCase())
              },
              onUpdateValue: async (processCode: number) => {
                const item = model.dependTaskList[i].dependItemList[j]
                selectOptions.value[i].dependItemList[j].depTaskCodeOptions =
                  await getTaskList(item.projectCode, processCode)
                item.depTaskCode = item.dependentType === 'DEPENDENT_ON_WORKFLOW' ? 0 : -1
              }
            },
            options:
              selectOptions.value[i]?.dependItemList[j]
                ?.definitionCodeOptions || [],
            path: `dependTaskList.${i}.dependItemList.${j}.definitionCode`,
            rule: {
              required: true,
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.process_name_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'select',
            field: 'depTaskCode',
            span: computed(() => {
              const item = model.dependTaskList[i].dependItemList[j]
              return item.dependentType === 'DEPENDENT_ON_WORKFLOW' ? 0 : 24
            }),
            name: t('project.node.task_name'),
            props: {
              filterable: true,
              filter: (query: string, option: IRenderOption) => {
                return option.filterLabel
                  .toLowerCase()
                  .includes(query.toLowerCase())
              }
            },
            options:
              selectOptions.value[i]?.dependItemList[j]?.depTaskCodeOptions ||
              [],
            path: `dependTaskList.${i}.dependItemList.${j}.depTaskCode`,
            rule: {
              required: true,
              trigger: ['input', 'blur'],
              validator(validate: any, value: number) {
                if (!value && value !== 0) {
                  return Error(t('project.node.task_name_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'select',
            field: 'cycle',
            span: 10,
            name: t('project.node.cycle_time'),
            props: {
              onUpdateValue: (value: IDateType) => {
                selectOptions.value[i].dependItemList[j].dateOptions =
                  DATE_LIST[value]
                model.dependTaskList[i].dependItemList[j].dateValue = null
              }
            },
            options: CYCLE_LIST,
            path: `dependTaskList.${i}.dependItemList.${j}.cycle`,
            rule: {
              required: true,
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.cycle_time_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'select',
            field: 'dateValue',
            span: 10,
            name: ' ',
            options:
              selectOptions.value[i]?.dependItemList[j]?.dateOptions || [],
            path: `dependTaskList.${i}.dependItemList.${j}.dateValue`,
            rule: {
              trigger: ['input', 'blur'],
              validator(validate: any, value: string) {
                if (!value) {
                  return Error(t('project.node.date_tips'))
                }
              }
            }
          }),
          (j = 0) => ({
            type: 'switch',
            field: 'parameterPassing',
            span: 20,
            name: t('project.node.dependent_task_parameter_passing'),
            path: `dependTaskList.${i}.dependItemList.${j}.parameterPassing`
          }),
          (j = 0) => ({
            type: 'custom',
            field: 'state',
            span: 2,
            name: ' ',
            widget: renderState(model.dependTaskList[i]?.dependItemList[j])
          })
        ]
      }),
      childrenField: 'dependItemList',
      name: 'add_dependency'
    }),
    {
      type: 'input-number',
      field: 'checkInterval',
      name: t('project.node.check_interval'),
      span: 12,
      props: {
        max: Math.pow(9, 10) - 1
      },
      slots: {
        suffix: () => t('project.node.second')
      },
      validate: {
        trigger: ['input'],
        validator(validate: any, value: number) {
          if (!value && !/^[1-9]\d*$/.test(String(value))) {
            return new Error(t('project.node.check_interval_tips'))
          }
        }
      }
    },
    {
      type: 'radio',
      field: 'failurePolicy',
      name: t('project.node.dependent_failure_policy'),
      options: dependentFailurePolicyOptions,
      span: 24
    },
    {
      type: 'input-number',
      field: 'failureWaitingTime',
      name: t('project.node.dependent_failure_waiting_time'),
      span: failureWaitingTimeSpan,
      props: {
        max: Math.pow(9, 10) - 1
      },
      slots: {
        suffix: () => t('project.node.minute')
      },
      validate: {
        trigger: ['input'],
        required: true,
        validator(validate: any, value: number) {
          if (model.timeoutFlag && !/^[1-9]\d*$/.test(String(value))) {
            return new Error(
              t('project.node.dependent_failure_waiting_time_tips')
            )
          }
        }
      }
    }
  ]
}
