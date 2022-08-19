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

import { useI18n } from 'vue-i18n'
import { h, reactive, ref } from 'vue'
import { NButton, NPopconfirm, NSpace, NTag, NTooltip } from 'naive-ui'
import { DeleteOutlined, CheckOutlined } from '@vicons/antd'
import { useAsyncState } from '@vueuse/core'
import {
  queryTaskVersions,
  switchVersion,
  deleteVersion
} from '@/service/modules/task-definition'
import { useRoute } from 'vue-router'
import type {
  TaskDefinitionVersionRes,
  TaskDefinitionVersionItem
} from '@/service/modules/task-definition/types'

export function useVersion() {
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
        title: t('project.task.version'),
        key: 'version',
        render: (row: TaskDefinitionVersionItem) =>
          h(
            'span',
            null,
            row.version !== variables.taskVersion
              ? 'v' + row.version
              : h(
                  NTag,
                  { type: 'success', size: 'small' },
                  {
                    default: () =>
                      `v${row.version} ${t('project.task.current_version')}`
                  }
                )
          )
      },
      {
        title: t('project.task.description'),
        key: 'description',
        render: (row: TaskDefinitionVersionItem) =>
          h('span', null, row.description ? row.description : '-')
      },
      {
        title: t('project.task.create_time'),
        key: 'createTime'
      },
      {
        title: t('project.task.operation'),
        key: 'operation',
        render(row: TaskDefinitionVersionItem) {
          return h(NSpace, null, {
            default: () => [
              h(
                NPopconfirm,
                {
                  onPositiveClick: () => {
                    handleSwitchVersion(row)
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
                              type: 'info',
                              size: 'small',
                              disabled: row.version === variables.taskVersion
                            },
                            {
                              icon: () => h(CheckOutlined)
                            }
                          ),
                        default: () => t('project.task.switch_version')
                      }
                    ),
                  default: () => t('project.task.confirm_switch_version')
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
                              disabled: row.version === variables.taskVersion
                            },
                            {
                              icon: () => h(DeleteOutlined)
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
    totalPage: ref(1),
    taskVersion: ref(null),
    taskCode: ref(null),
    refreshTaskDefinition: ref(false),
    row: {},
    loadingRef: ref(false)
  })

  const handleSwitchVersion = (row: TaskDefinitionVersionItem) => {
    switchVersion(
      { version: row.version },
      { code: variables.taskCode },
      { projectCode }
    ).then(() => {
      variables.refreshTaskDefinition = true
    })
  }

  const handleDelete = (row: TaskDefinitionVersionItem) => {
    deleteVersion(
      { version: row.version },
      { code: variables.taskCode },
      { projectCode }
    ).then(() => {
      variables.refreshTaskDefinition = true
    })
  }

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const { state } = useAsyncState(
      queryTaskVersions(
        { ...params },
        { code: variables.taskCode },
        { projectCode }
      ).then((res: TaskDefinitionVersionRes) => {
        variables.tableData = res.totalList.map((item, unused) => {
          return {
            ...item
          }
        }) as any
        variables.totalPage = res.totalPage
        variables.loadingRef = false
      }),
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
