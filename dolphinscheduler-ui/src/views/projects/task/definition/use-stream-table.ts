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

import { reactive, h } from 'vue'
import { NButton, NIcon, NSpace, NTooltip, NEllipsis } from 'naive-ui'
import ButtonLink from '@/components/button-link'
import { useI18n } from 'vue-i18n'
import { EditOutlined, PlayCircleOutlined } from '@vicons/antd'
import { queryTaskDefinitionListPaging } from '@/service/modules/task-definition'
import { useRoute } from 'vue-router'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
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
        render: (row: any, index: number) => index + 1,
        ...COLUMN_WIDTH_CONFIG['index']
      },
      {
        title: t('project.task.task_name'),
        key: 'taskName',
        ...COLUMN_WIDTH_CONFIG['linkName'],
        render: (row: IRecord) =>
          h(
            ButtonLink,
            {
              onClick: () => void onEdit(row, true)
            },
            {
              default: () =>
                h(
                  NEllipsis,
                  COLUMN_WIDTH_CONFIG['linkEllipsis'],
                  () => row.taskName
                )
            }
          )
      },
      {
        title: t('project.task.version'),
        key: 'taskVersion',
        render: (row: TaskDefinitionItem) =>
          h('span', null, 'v' + row.taskVersion),
        ...COLUMN_WIDTH_CONFIG['version']
      },
      {
        title: t('project.task.workflow_name'),
        key: 'processDefinitionName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.task.task_type'),
        key: 'taskType',
        ...COLUMN_WIDTH_CONFIG['type']
      },
      {
        title: t('project.task.create_time'),
        key: 'taskCreateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.task.update_time'),
        key: 'taskUpdateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.task.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](2),
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
                        onClick: () => onStart(row)
                      },
                      {
                        icon: () =>
                          h(NIcon, null, {
                            default: () => h(PlayCircleOutlined)
                          })
                      }
                    ),
                  default: () => t('project.task.execute')
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
                        onClick: () => onEdit(row, false)
                      },
                      {
                        icon: () =>
                          h(NIcon, null, { default: () => h(EditOutlined) })
                      }
                    ),
                  default: () => t('project.task.edit')
                }
              )
            ]
          })
        }
      }
    ]
    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    tableData: [],
    page: 1,
    pageSize: 10,
    searchTaskName: null,
    searchWorkflowName: null,
    totalPage: 1,
    row: {},
    loading: false,
    startShow: false
  })

  const getTableData = () => {
    if (variables.loading) return
    variables.loading = true
    const params = {
      pageSize: variables.pageSize,
      pageNo: variables.page,
      searchTaskName: variables.searchTaskName,
      searchWorkflowName: variables.searchWorkflowName,
      taskExecuteType: 'STREAM' as 'BATCH' | 'STREAM'
    } as any

    queryTaskDefinitionListPaging(params, { projectCode })
      .then((res: TaskDefinitionRes) => {
        variables.tableData = [...res.totalList] as any
        variables.totalPage = res.totalPage
      })
      .finally(() => {
        variables.loading = false
      })
  }

  const onStart = (row: any) => {
    variables.row = row
    variables.startShow = true
  }

  return {
    variables,
    getTableData,
    createColumns
  }
}
