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

import { ref, watch, onBeforeMount, computed } from 'vue'
import { NSpace, NTooltip, NButton, NIcon, NTag } from 'naive-ui'
import { EditOutlined, DeleteOutlined } from '@vicons/antd'
import { queryUserList } from '@/service/modules/users'
import { useI18n } from 'vue-i18n'

type UseTableProps = {
  onEdit: (user: any) => void
  onDelete: (user: any) => void
}

function useColumns({ onEdit, onDelete }: UseTableProps) {
  const { t } = useI18n()
  const columns = computed(() =>
    [
      {
        title: t('security.user.index'),
        key: 'index',
        width: 80,
        render: (rowData: any, rowIndex: number) => rowIndex + 1
      },
      {
        title: t('security.user.username'),
        key: 'userName'
      },
      {
        title: t('security.user.tenant_code'),
        key: 'tenantCode'
      },
      {
        title: t('security.user.queue'),
        key: 'queue'
      },
      {
        title: t('security.user.email'),
        key: 'email'
      },
      {
        title: t('security.user.phone'),
        key: 'phone'
      },
      {
        title: t('security.user.state'),
        key: 'state',
        render: (rowData: any, rowIndex: number) => {
          return rowData.state === 1 ? (
            <NTag type='success'>{t('security.user.state_enabled')}</NTag>
          ) : (
            <NTag type='error'>{t('security.user.state_disabled')}</NTag>
          )
        }
      },
      {
        title: t('security.user.create_time'),
        key: 'createTime',
        width: 200
      },
      {
        title: t('security.user.update_time'),
        key: 'updateTime',
        width: 200
      },
      {
        title: t('security.user.operation'),
        key: 'operation',
        fixed: 'right',
        width: 120,
        render: (rowData: any, rowIndex: number) => {
          return (
            <NSpace>
              <NTooltip trigger='hover'>
                {{
                  trigger: () => (
                    <NButton
                      circle
                      type='info'
                      size='small'
                      onClick={() => {
                        onEdit(rowData)
                      }}
                    >
                      {{
                        icon: () => (
                          <NIcon>
                            <EditOutlined />
                          </NIcon>
                        )
                      }}
                    </NButton>
                  ),
                  default: () => t('security.user.edit')
                }}
              </NTooltip>
              <NTooltip trigger='hover'>
                {{
                  trigger: () => (
                    <NButton
                      circle
                      type='error'
                      size='small'
                      onClick={() => {
                        onDelete(rowData)
                      }}
                    >
                      {{
                        icon: () => (
                          <NIcon>
                            <DeleteOutlined />
                          </NIcon>
                        )
                      }}
                    </NButton>
                  ),
                  default: () => t('security.user.delete')
                }}
              </NTooltip>
            </NSpace>
          )
        }
      }
    ].map((d: any) => ({ ...d, width: d.width || 160 }))
  )

  const scrollX = columns.value.reduce((p, c) => p + c.width, 0)

  return {
    columns,
    scrollX
  }
}

export function useTable(props: UseTableProps) {
  const page = ref(1)
  const pageCount = ref(0)
  const pageSize = ref(10)
  const searchInputVal = ref()
  const searchVal = ref('')
  const pageSizes = [10, 30, 50]
  const userListLoading = ref(false)
  const userList = ref([])
  const { columns, scrollX } = useColumns(props)

  const getUserList = () => {
    userListLoading.value = true
    queryUserList({
      pageNo: page.value,
      pageSize: pageSize.value,
      searchVal: searchVal.value
    })
      .then((res: any) => {
        userList.value = res?.totalList || []
        pageCount.value = res?.totalPage || 0
      })
      .finally(() => {
        userListLoading.value = false
      })
  }

  const resetPage = () => {
    page.value = 1
  }

  const onSearchValOk = () => {
    resetPage()
    searchVal.value = searchInputVal.value
  }

  const onSearchValClear = () => {
    resetPage()
    searchVal.value = ''
  }

  onBeforeMount(() => {
    getUserList()
  })

  watch([page, pageSize, searchVal], () => {
    getUserList()
  })

  return {
    userList,
    userListLoading,
    getUserList,
    page,
    pageCount,
    pageSize,
    searchVal,
    searchInputVal,
    pageSizes,
    columns,
    scrollX,
    onSearchValOk,
    onSearchValClear,
    resetPage
  }
}
