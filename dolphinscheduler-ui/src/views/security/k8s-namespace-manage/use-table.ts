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
import { NButton, NIcon, NPopconfirm, NSpace, NTooltip } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { format } from 'date-fns'
import { DeleteOutlined, EditOutlined } from '@vicons/antd'
import {
  queryNamespaceListPaging,
  delNamespaceById
} from '@/service/modules/k8s-namespace'
import type {
  NamespaceListRes,
  NamespaceItem
} from '@/service/modules/k8s-namespace/types'
import { parseTime } from '@/common/common'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'

export function useTable() {
  const { t } = useI18n()

  const handleEdit = (row: NamespaceItem) => {
    variables.showModalRef = true
    variables.statusRef = 1
    variables.row = row
  }

  const handleDelete = (row: NamespaceItem) => {
    delNamespaceById(row.id).then(() => {
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
        title: t('security.k8s_namespace.k8s_namespace'),
        key: 'namespace',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('security.k8s_namespace.k8s_cluster'),
        key: 'clusterName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('security.k8s_namespace.limit_cpu'),
        key: 'limitsCpu',
        width: 140
      },
      {
        title: t('security.k8s_namespace.limit_memory'),
        key: 'limitsMemory',
        width: 140
      },
      {
        title: t('security.k8s_namespace.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('security.k8s_namespace.update_time'),
        key: 'updateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('security.k8s_namespace.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](2),
        render(row: NamespaceItem) {
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
                        icon: () =>
                          h(NIcon, null, { default: () => h(EditOutlined) })
                      }
                    ),
                  default: () => t('security.k8s_namespace.edit')
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
                              icon: () =>
                                h(NIcon, null, {
                                  default: () => h(DeleteOutlined)
                                })
                            }
                          ),
                        default: () => t('security.k8s_namespace.delete')
                      }
                    ),
                  default: () => t('security.k8s_namespace.delete_confirm')
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
    tableData: [],
    tableWidth: DefaultTableWidth,
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
      queryNamespaceListPaging({ ...params }).then((res: NamespaceListRes) => {
        variables.tableData = res.totalList.map((item, unused) => {
          item.createTime = format(
            parseTime(item.createTime),
            'yyyy-MM-dd HH:mm:ss'
          )
          item.updateTime = format(
            parseTime(item.updateTime),
            'yyyy-MM-dd HH:mm:ss'
          )
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
