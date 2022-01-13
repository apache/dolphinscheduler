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

  const createColumns = () => {
    return [
      {
        title: '编号',
        key: 'num',
      },
      {
        title: '操作系统租户',
        key: 'tenantCode',
      },
      {
        title: '描述',
        key: 'description',
      },
      {
        title: '队列',
        key: 'queueName',
      },
      {
        title: '创建时间',
        key: 'createTime',
      },
      {
        title: '更新时间',
        key: 'updateTime',
      },
      {
        title: '操作',
        key: 'actions',
        render(row: any) {
          return h('div', null, [
            h(
              NButton,
              {
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
                      size: 'small',
                      style: {'margin-left': '5px'},
                    },
                    {
                      icon: () => h(DeleteOutlined),
                    }
                  ),
                  default: () => {return '确定删除吗?'}
                }
              )
            ]
          )
        }
      }
    ]
  }

  const variables = reactive({
    columns: createColumns(),
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(null),
    totalPage: ref(1),
    showModalRef: ref(false),
    statusRef: ref(0),
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
  }
}
