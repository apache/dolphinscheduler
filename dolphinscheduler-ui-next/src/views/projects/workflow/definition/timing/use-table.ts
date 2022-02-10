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
import { NSpace, NTooltip, NButton, NPopconfirm, NEllipsis } from 'naive-ui'
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
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { ISearchParam } from './types'
import styles from '../index.module.scss'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const columns: TableColumns<any> = [
    {
      title: t('project.workflow.id'),
      key: 'id',
      width: 50,
      render: (_row, index) => index + 1
    },
    {
      title: t('project.workflow.workflow_name'),
      key: 'processDefinitionName',
      width: 200,
      render: (_row) =>
        h(
          NEllipsis,
          { style: 'max-width: 200px' },
          {
            default: () => _row.processDefinitionName
          }
        )
    },
    {
      title: t('project.workflow.start_time'),
      key: 'startTime'
    },
    {
      title: t('project.workflow.end_time'),
      key: 'endTime'
    },
    {
      title: t('project.workflow.crontab'),
      key: 'crontab'
    },
    {
      title: t('project.workflow.failure_strategy'),
      key: 'failureStrategy'
    },
    {
      title: t('project.workflow.status'),
      key: 'releaseState',
      render: (_row) =>
        _row.releaseState === 'ONLINE'
          ? t('project.workflow.up_line')
          : t('project.workflow.down_line')
    },
    {
      title: t('project.workflow.create_time'),
      key: 'createTime'
    },
    {
      title: t('project.workflow.update_time'),
      key: 'updateTime'
    },
    {
      title: t('project.workflow.operation'),
      key: 'operation',
      fixed: 'right',
      className: styles.operation,
      render: (row) => {
        return h(NSpace, null, {
          default: () => [
            h(
              NButton,
              {
                circle: true,
                type: 'info',
                size: 'tiny',
                disabled: row.releaseState === 'ONLINE',
                onClick: () => {
                  handleEdit(row)
                }
              },
              {
                icon: () => h(EditOutlined)
              }
            ),
            h(
              NButton,
              {
                circle: true,
                type: row.releaseState === 'ONLINE' ? 'error' : 'warning',
                size: 'tiny',
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
                            size: 'tiny'
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

  const handleEdit = (row: any) => {
    variables.showRef = true
    variables.row = row
  }

  const variables = reactive({
    columns,
    row: {},
    tableData: [],
    projectCode: ref(Number(router.currentRoute.value.params.projectCode)),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    totalPage: ref(1),
    showRef: ref(false)
  })

  const getTableData = (params: ISearchParam) => {
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
    deleteScheduleById(id, variables.projectCode)
      .then(() => {
        window.$message.success(t('project.workflow.success'))
        getTableData({
          pageSize: variables.pageSize,
          pageNo: variables.page,
          searchVal: variables.searchVal
        })
      })
      .catch((error: any) => {
        window.$message.error(error.message)
      })
  }

  return {
    variables,
    getTableData
  }
}
