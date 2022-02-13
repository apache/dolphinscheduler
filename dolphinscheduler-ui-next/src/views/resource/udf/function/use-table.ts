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
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { NSpace, NTooltip, NButton, NPopconfirm } from 'naive-ui'
import { EditOutlined, DeleteOutlined } from '@vicons/antd'
import { useAsyncState } from '@vueuse/core'
import {
  queryUdfFuncListPaging,
  deleteUdfFunc
} from '@/service/modules/resources'
import { IUdfFunctionParam } from './types'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const variables = reactive({
    columns: [],
    row: {},
    tableData: [],
    id: ref(Number(router.currentRoute.value.params.id) || -1),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    totalPage: ref(1),
    showRef: ref(false)
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: t('resource.function.id'),
        key: 'id',
        width: 50,
        render: (_row, index) => index + 1
      },
      {
        title: t('resource.function.udf_function_name'),
        key: 'funcName'
      },
      {
        title: t('resource.function.class_name'),
        key: 'className'
      },
      {
        title: t('resource.function.type'),
        key: 'type'
      },
      {
        title: t('resource.function.description'),
        key: 'description'
      },
      {
        title: t('resource.function.jar_package'),
        key: 'resourceName'
      },
      {
        title: t('resource.function.update_time'),
        key: 'updateTime'
      },
      {
        title: t('resource.function.operation'),
        key: 'operation',
        render: (row) => {
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
                        size: 'tiny',
                        onClick: () => {
                          handleEdit(row)
                        }
                      },
                      {
                        icon: () => h(EditOutlined)
                      }
                    ),
                  default: () => t('resource.function.edit')
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
                        default: () => t('resource.function.delete')
                      }
                    ),
                  default: () => t('resource.function.delete_confirm')
                }
              )
            ]
          })
        }
      }
    ] as TableColumns<any>
  }

  const getTableData = (params: IUdfFunctionParam) => {
    const { state } = useAsyncState(
      queryUdfFuncListPaging({ ...params }).then((res: any) => {
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item: any) => {
          return { ...item }
        })
      }),
      { total: 0, table: [] }
    )
    return state
  }

  const handleEdit = (row: any) => {
    variables.showRef = true
    variables.row = row
  }

  const handleDelete = (id: number) => {
    /* after deleting data from the current page, you need to jump forward when the page is empty. */
    if (variables.tableData.length === 1 && variables.page > 1) {
      variables.page -= 1
    }

    deleteUdfFunc(id).then(() =>
      getTableData({
        id: variables.id,
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    )
  }

  return {
    variables,
    createColumns,
    getTableData
  }
}
