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

import { useAsyncState } from '@vueuse/core'
import { reactive, h, ref } from 'vue'
import { NButton, NIcon, NPopconfirm, NSpace, NTag, NTooltip } from 'naive-ui'
import ButtonLink from '@/components/button-link'
import { useI18n } from 'vue-i18n'
import {
  DeleteOutlined,
  EditOutlined,
  DragOutlined,
  ExclamationCircleOutlined
} from '@vicons/antd'
import {
  queryTaskDefinitionListPaging,
  deleteTaskDefinition
} from '@/service/modules/task-definition'
import { useRoute } from 'vue-router'
import type {
  TaskDefinitionItem,
  TaskDefinitionRes
} from '@/service/modules/task-definition/types'
import type { IRecord } from './types'

export function useTable(onEdit: Function) {
  const { t } = useI18n()
  const route = useRoute()
  const projectCode = Number(route.params.projectCode)

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'index',
        render: (row: any, index: number) => index + 1
      },
      {
        title: t('project.task.task_name'),
        key: 'taskName',
        render: (row: IRecord) =>
          h(
            ButtonLink,
            {
              onClick: () => void onEdit(row, true)
            },
            { default: () => row.taskName }
          )
      },
      {
        title: t('project.task.workflow_name'),
        key: 'processDefinitionName'
      },
      {
        title: t('project.task.workflow_state'),
        key: 'processReleaseState',
        render: (row: any) => {
          if (row.processReleaseState === 'OFFLINE') {
            return h(
              NTag,
              { type: 'error', size: 'small' },
              t('project.task.offline')
            )
          } else if (row.processReleaseState === 'ONLINE') {
            return h(
              NTag,
              { type: 'info', size: 'small' },
              t('project.task.online')
            )
          }
        }
      },
      {
        title: t('project.task.task_type'),
        key: 'taskType'
      },
      {
        title: t('project.task.version'),
        key: 'taskVersion',
        render: (row: TaskDefinitionItem) =>
          h('span', null, 'v' + row.taskVersion)
      },
      {
        title: t('project.task.upstream_tasks'),
        key: 'upstreamTaskMap',
        render: (row: TaskDefinitionItem) =>
          h(
            'span',
            null,
            row.upstreamTaskMap.length < 1
              ? '-'
              : h(NSpace, null, {
                  default: () =>
                    row.upstreamTaskMap.map((item: string) => {
                      return h(
                        NTag,
                        { type: 'info', size: 'small' },
                        { default: () => item }
                      )
                    })
                })
          )
      },
      {
        title: t('project.task.create_time'),
        key: 'taskCreateTime'
      },
      {
        title: t('project.task.update_time'),
        key: 'taskUpdateTime'
      },
      {
        title: t('project.task.operation'),
        key: 'operation',
        render(row: any) {
          return h(NSpace, null, {
            default: () => [
              h(
                NTooltip,
                {},
                {
                  trigger: () =>
                    h(
                      NButton,
                      {
                        circle: true,
                        type: 'info',
                        size: 'small',
                        disabled:
                          ['CONDITIONS', 'SWITCH'].includes(row.taskType) ||
                          (!!row.processDefinitionCode &&
                            row.processReleaseState === 'ONLINE'),
                        onClick: () => {
                          onEdit(row, false)
                        }
                      },
                      {
                        icon: () =>
                          h(NIcon, null, { default: () => h(EditOutlined) })
                      }
                    ),
                  default: () => t('project.task.edit')
                }
              ),
              h(
                NTooltip,
                {},
                {
                  trigger: () =>
                    h(
                      NButton,
                      {
                        circle: true,
                        type: 'info',
                        size: 'small',
                        disabled:
                          !!row.processDefinitionCode &&
                          row.processReleaseState === 'ONLINE',
                        onClick: () => {
                          variables.showMoveModalRef = true
                          variables.row = row
                        }
                      },
                      {
                        icon: () =>
                          h(NIcon, null, { default: () => h(DragOutlined) })
                      }
                    ),
                  default: () => t('project.task.move')
                }
              ),
              h(
                NTooltip,
                {},
                {
                  trigger: () =>
                    h(
                      NButton,
                      {
                        circle: true,
                        type: 'info',
                        size: 'small',
                        onClick: () => {
                          variables.showVersionModalRef = true
                          variables.row = row
                        }
                      },
                      {
                        icon: () =>
                          h(NIcon, null, {
                            default: () => h(ExclamationCircleOutlined)
                          })
                      }
                    ),
                  default: () => t('project.task.version')
                }
              ),
              h(
                NPopconfirm,
                {
                  onPositiveClick: () => {
                    handleDelete(row)
                  }
                },
                {
                  trigger: () =>
                    h(
                      NTooltip,
                      {},
                      {
                        trigger: () =>
                          h(
                            NButton,
                            {
                              circle: true,
                              type: 'error',
                              size: 'small',
                              disabled:
                                !!row.processDefinitionCode &&
                                row.processReleaseState === 'ONLINE'
                            },
                            {
                              icon: () =>
                                h(NIcon, null, {
                                  default: () => h(DeleteOutlined)
                                })
                            }
                          ),
                        default: () => t('project.task.delete')
                      }
                    ),
                  default: () => t('project.task.delete_confirm')
                }
              )
            ]
          })
        }
      }
    ]
  }

  const variables = reactive({
    columns: [],
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    searchTaskName: ref(null),
    searchWorkflowName: ref(null),
    totalPage: ref(1),
    taskType: ref(null),
    showVersionModalRef: ref(false),
    showMoveModalRef: ref(false),
    row: {}
  })

  const handleDelete = (row: any) => {
    deleteTaskDefinition({ code: row.taskCode }, { projectCode }).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo:
          variables.tableData.length === 1 && variables.page > 1
            ? variables.page - 1
            : variables.page,
        searchTaskName: variables.searchTaskName,
        searchWorkflowName: variables.searchWorkflowName,
        taskType: variables.taskType
      })
    })
  }

  const getTableData = (params: any) => {
    const { state } = useAsyncState(
      queryTaskDefinitionListPaging({ ...params }, { projectCode }).then(
        (res: TaskDefinitionRes) => {
          variables.tableData = res.totalList.map((item, unused) => {
            if (Object.keys(item.upstreamTaskMap).length > 0) {
              item.upstreamTaskMap = Object.keys(item.upstreamTaskMap).map(
                (code) => item.upstreamTaskMap[code]
              )
            } else {
              item.upstreamTaskMap = []
            }

            return {
              ...item
            }
          }) as any
          variables.totalPage = res.totalPage
        }
      ),
      {}
    )

    return state
  }

  return {
    variables,
    getTableData,
    createColumns
  }
}
