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
import { NButton, NPopconfirm, NSpace, NTooltip } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { DeleteOutlined, EditOutlined } from '@vicons/antd'
import {
  queryAllWorkerGroupsPaging,
  deleteById
} from '@/service/modules/worker-groups'
import type { WorkerGroupRes } from '@/service/modules/worker-groups/types'

export function useTable() {
  const { t } = useI18n()

  const handleEdit = (row: any) => {
    variables.showModalRef = true
    variables.statusRef = 1
    variables.row = row
  }

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'index'
      },
      {
        title: t('security.worker_group.group_name'),
        key: 'name'
      },
      {
        title: t('security.worker_group.worker_addresses'),
        key: 'addrList'
      },
      {
        title: t('security.worker_group.create_time'),
        key: 'createTime'
      },
      {
        title: t('security.worker_group.update_time'),
        key: 'updateTime'
      },
      {
        title: t('security.worker_group.operation'),
        key: 'operation',
        render(row: any) {
          if (row.systemDefault) {
            return false
          }

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
                        onClick: () => {
                          handleEdit(row)
                        }
                      },
                      {
                        icon: () => h(EditOutlined)
                      }
                    ),
                  default: () => t('security.worker_group.edit')
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
                              size: 'small'
                            },
                            {
                              icon: () => h(DeleteOutlined)
                            }
                          ),
                        default: () => t('security.worker_group.delete')
                      }
                    ),
                  default: () => t('security.worker_group.delete_confirm')
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
    searchVal: ref(null),
    totalPage: ref(1),
    showModalRef: ref(false),
    statusRef: ref(0),
    row: {}
  })

  const handleDelete = (row: any) => {
    deleteById({ id: row.id }).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo:
          variables.tableData.length === 1 && variables.page > 1
            ? variables.page - 1
            : variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const getTableData = (params: any) => {
    const { state } = useAsyncState(
      queryAllWorkerGroupsPaging({ ...params }).then((res: WorkerGroupRes) => {
        variables.tableData = res.totalList.map((item, index) => {
          return {
            index: index + 1,
            ...item
          }
        }) as any
        variables.totalPage = res.totalPage
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
