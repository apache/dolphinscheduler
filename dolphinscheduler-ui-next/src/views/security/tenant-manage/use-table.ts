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
import { queryTenantListPaging, deleteTenantById } from '@/service/modules/tenants'
import { reactive, h, ref } from 'vue'
import { NButton, NPopconfirm } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { DeleteOutlined, EditOutlined } from '@vicons/antd'

export function useTable() {
  const { t } = useI18n()

  const handleEdit= (row: any) => {
    variables.showModalRef = true
    variables.statusRef = 1
    variables.row = row
  }

  const handleDelete = (row: any) => {
    deleteTenantById(row.id).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: t('security.tenant.num'),
        key: 'num',
      },
      {
        title: t('security.tenant.tenantCode'),
        key: 'tenantCode',
      },
      {
        title: t('security.tenant.description'),
        key: 'description',
      },
      {
        title: t('security.tenant.queueName'),
        key: 'queueName',
      },
      {
        title: t('security.tenant.createTime'),
        key: 'createTime',
      },
      {
        title: t('security.tenant.updateTime'),
        key: 'updateTime',
      },
      {
        title: t('security.tenant.actions'),
        key: 'actions',
        render(row: any) {
          return h('div', null, [
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
            h(
              NPopconfirm,
              {
                onPositiveClick: () => { handleDelete(row) }
              },
              {
                trigger: () => h(
                  NButton,
                  {
                    circle: true,
                    type: 'error',
                    size: 'small',
                    style: {'margin-left': '5px'},
                  },
                  {
                    icon: () => h(DeleteOutlined),
                  }
                ),
                default: () => {return t('security.tenant.delete_confirm')}
              }
            )
          ])
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
      queryTenantListPaging({ ...params }).then((res: any) => {
        variables.tableData = res.totalList.map((item: any, index: number) => {
          return {
            num: index + 1,
            ...item,
          }
        })
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
