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
        render: (row: any, index: number) => index + 1
      },
      {
        title: t('security.k8s_namespace.k8s_namespace'),
        key: 'namespace'
      },
      {
        title: t('security.k8s_namespace.k8s_cluster'),
        key: 'k8s'
      },
      {
        title: t('security.k8s_namespace.owner'),
        key: 'owner'
      },
      {
        title: t('security.k8s_namespace.tag'),
        key: 'tag'
      },
      {
        title: t('security.k8s_namespace.limit_cpu'),
        key: 'limitsCpu'
      },
      {
        title: t('security.k8s_namespace.limit_memory'),
        key: 'limitsMemory'
      },
      {
        title: t('security.k8s_namespace.create_time'),
        key: 'createTime'
      },
      {
        title: t('security.k8s_namespace.update_time'),
        key: 'updateTime'
      },
      {
        title: t('security.k8s_namespace.operation'),
        key: 'operation',
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
                        icon: () => h(EditOutlined)
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
                              icon: () => h(DeleteOutlined)
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

  const getTableData = (params: any) => {
    const { state } = useAsyncState(
      queryNamespaceListPaging({ ...params }).then((res: NamespaceListRes) => {
        variables.tableData = res.totalList.map((item, index) => {
          item.createTime = format(
            new Date(item.createTime),
            'yyyy-MM-dd HH:mm:ss'
          )
          item.updateTime = format(
            new Date(item.updateTime),
            'yyyy-MM-dd HH:mm:ss'
          )
          return {
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
