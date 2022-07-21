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
import { DeleteOutlined, EditOutlined } from '@vicons/antd'
import { queryAccessTokenList, deleteToken } from '@/service/modules/token'
import { renderTableTime } from '@/common/common'
import type { TokenRes } from '@/service/modules/token/types'

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
        key: 'index',
        render: (row: any, index: number) => index + 1
      },
      {
        title: t('security.token.user'),
        key: 'userName',
        className: 'username'
      },
      {
        title: t('security.token.token'),
        key: 'token',
        className: 'token'
      },
      {
        title: t('security.token.expiration_time'),
        key: 'expireTime',
        render: (row: any) => renderTableTime(row.expireTime)
      },
      {
        title: t('security.token.create_time'),
        key: 'createTime',
        render: (row: any) => renderTableTime(row.createTime)
      },
      {
        title: t('security.token.update_time'),
        key: 'updateTime',
        render: (row: any) => renderTableTime(row.updateTime)
      },
      {
        title: t('security.token.operation'),
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
                  default: () => t('security.token.edit')
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
                        default: () => t('security.token.delete')
                      }
                    ),
                  default: () => t('security.token.delete_confirm')
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
    row: {},
    loadingRef: ref(false)
  })

  const handleDelete = (row: any) => {
    if (variables.tableData.length === 1 && variables.page > 1) {
      --variables.page
    }
    deleteToken(row.id).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const { state } = useAsyncState(
      queryAccessTokenList({ ...params }).then((res: TokenRes) => {
        variables.tableData = res.totalList as any
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
