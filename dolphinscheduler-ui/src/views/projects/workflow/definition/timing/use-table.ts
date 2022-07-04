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

import { h, ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { NSpace, NTooltip, NButton, NPopconfirm } from 'naive-ui'
import {
  deleteScheduleById,
  offline,
  online,
  queryScheduleListPaging
} from '@/service/modules/schedules'
import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  DeleteOutlined,
  EditOutlined
} from '@vicons/antd'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import { format } from 'date-fns-tz'
import { ISearchParam } from './types'
import styles from '../index.module.scss'
import type { Router } from 'vue-router'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    row: {},
    tableData: [],
    projectCode: ref(Number(router.currentRoute.value.params.projectCode)),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    totalPage: ref(1),
    showRef: ref(false),
    loadingRef: ref(false)
  })

  const renderTime = (time: string, timeZone: string) => {
    if (!timeZone) {
      return time
    }

    const utc = format(new Date(time), 'zzz', {
      timeZone
    }).replace('GMT', 'UTC')
    return h('span', [
      h('span', null, time),
      h('span', { style: 'color: #1890ff; margin-left: 5px' }, `(${utc})`)
    ])
  }

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'id',
        ...COLUMN_WIDTH_CONFIG['index'],
        render: (row: any, index: number) => index + 1
      },
      {
        title: t('project.workflow.workflow_name'),
        key: 'processDefinitionName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.workflow.start_time'),
        key: 'startTime',
        ...COLUMN_WIDTH_CONFIG['timeZone'],
        render: (row: any) => renderTime(row.startTime, row.timezoneId)
      },
      {
        title: t('project.workflow.end_time'),
        key: 'endTime',
        ...COLUMN_WIDTH_CONFIG['timeZone'],
        render: (row: any) => renderTime(row.endTime, row.timezoneId)
      },
      {
        title: t('project.workflow.crontab'),
        key: 'crontab',
        width: 140
      },
      {
        title: t('project.workflow.failure_strategy'),
        key: 'failureStrategy',
        render: (row: any) => {
          if (row.failureStrategy === 'CONTINUE') {
            return t('project.workflow.continue')
          } else if (row.failureStrategy === 'END') {
            return t('project.workflow.end')
          }
        },
        width: 140
      },
      {
        title: t('project.workflow.status'),
        key: 'releaseState',
        ...COLUMN_WIDTH_CONFIG['state'],
        render: (row: any) =>
          row.releaseState === 'ONLINE'
            ? t('project.workflow.up_line')
            : t('project.workflow.down_line')
      },
      {
        title: t('project.workflow.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.workflow.update_time'),
        key: 'updateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.workflow.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](3),
        className: styles.operation,
        render: (row: any) => {
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
                        disabled: row.releaseState === 'ONLINE',
                        onClick: () => {
                          handleEdit(row)
                        }
                      },
                      {
                        icon: () => h(EditOutlined)
                      }
                    ),
                  default: () => t('project.workflow.edit')
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
                        type:
                          row.releaseState === 'ONLINE' ? 'error' : 'warning',
                        size: 'small',
                        onClick: () => {
                          handleReleaseState(row)
                        }
                      },
                      {
                        icon: () =>
                          h(
                            row.releaseState === 'ONLINE'
                              ? ArrowDownOutlined
                              : ArrowUpOutlined
                          )
                      }
                    ),
                  default: () =>
                    row.releaseState === 'ONLINE'
                      ? t('project.workflow.down_line')
                      : t('project.workflow.up_line')
                }
              ),
              h(
                NPopconfirm,
                {
                  onPositiveClick: () => {
                    handleDelete(row.id)
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
                              size: 'small'
                            },
                            {
                              icon: () => h(DeleteOutlined)
                            }
                          ),
                        default: () => t('project.workflow.delete')
                      }
                    ),
                  default: () => t('project.workflow.delete_confirm')
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

  const handleEdit = (row: any) => {
    variables.showRef = true
    variables.row = row
  }

  const getTableData = (params: ISearchParam) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const definitionCode = Number(
      router.currentRoute.value.params.definitionCode
    )
    queryScheduleListPaging(
      { ...params, processDefinitionCode: definitionCode },
      variables.projectCode
    ).then((res: any) => {
      variables.totalPage = res.totalPage
      variables.tableData = res.totalList.map((item: any) => {
        return { ...item }
      })
      variables.loadingRef = false
    })
  }

  const handleReleaseState = (row: any) => {
    let handle = online
    if (row.releaseState === 'ONLINE') {
      handle = offline
    }

    handle(variables.projectCode, row.id).then(() => {
      window.$message.success(t('project.workflow.success'))
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const handleDelete = (id: number) => {
    /* after deleting data from the current page, you need to jump forward when the page is empty. */
    if (variables.tableData.length === 1 && variables.page > 1) {
      variables.page -= 1
    }
    deleteScheduleById(id, variables.projectCode).then(() => {
      window.$message.success(t('project.workflow.success'))
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  return {
    variables,
    createColumns,
    getTableData
  }
}
