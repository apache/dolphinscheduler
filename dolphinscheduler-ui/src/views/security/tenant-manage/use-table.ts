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
import {
  queryTenantListPaging,
  deleteTenantById
} from '@/service/modules/tenants'
import { reactive, h, ref } from 'vue'
import { NButton, NIcon, NPopconfirm, NSpace, NTooltip } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { DeleteOutlined, EditOutlined } from '@vicons/antd'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'

export function useTable() {
  const { t } = useI18n()

  const handleEdit = (row: any) => {
    variables.showModalRef = true
    variables.statusRef = 1
    variables.row = row
  }

  const handleDelete = (row: any) => {
    deleteTenantById(row.id).then(() => {
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

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'index',
        render: (row: any, index: number) => index + 1,
        ...COLUMN_WIDTH_CONFIG['index']
      },
      {
        title: t('security.tenant.tenant_code'),
        key: 'tenantCode',
        className: 'tenant-code',
        ...COLUMN_WIDTH_CONFIG['userName']
      },
      {
        title: t('security.tenant.description'),
        key: 'description',
        ...COLUMN_WIDTH_CONFIG['note']
      },
      {
        title: t('security.tenant.queue_name'),
        key: 'queueName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('security.tenant.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('security.tenant.update_time'),
        key: 'updateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('security.tenant.actions'),
        key: 'actions',
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
                        class: 'edit',
                        onClick: () => {
                          handleEdit(row)
                        }
                      },
                      {
                        icon: () =>
                          h(NIcon, null, { default: () => h(EditOutlined) })
                      }
                    ),
                  default: () => t('security.tenant.edit')
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
                              class: 'delete'
                            },
                            {
                              icon: () =>
                                h(NIcon, null, {
                                  default: () => h(DeleteOutlined)
                                })
                            }
                          ),
                        default: () => t('security.tenant.delete')
                      }
                    ),
                  default: () => t('security.tenant.delete_confirm')
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
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(null),
    totalPage: ref(1),
    showModalRef: ref(false),
    statusRef: ref(0),
    row: {},
    loadingRef: ref(false)
  })

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const { state } = useAsyncState(
      queryTenantListPaging({ ...params }).then((res: any) => {
        variables.tableData = res.totalList.map((item: any, unused: number) => {
          return {
            ...item
          }
        })
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
