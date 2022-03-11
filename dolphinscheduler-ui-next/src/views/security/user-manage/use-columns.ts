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

import { h, ref, watch, onMounted, Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NSpace,
  NTooltip,
  NButton,
  NIcon,
  NTag,
  NDropdown,
  NPopconfirm
} from 'naive-ui'
import { EditOutlined, DeleteOutlined, UserOutlined } from '@vicons/antd'
import { TableColumns, InternalRowData } from './types'

export function useColumns(onCallback: Function) {
  const { t } = useI18n()

  const columnsRef = ref([]) as Ref<TableColumns>

  const createColumns = () => {
    columnsRef.value = [
      {
        title: '#',
        key: 'index',
        render: (rowData: InternalRowData, rowIndex: number) => rowIndex + 1
      },
      {
        title: t('security.user.username'),
        key: 'userName'
      },
      {
        title: t('security.user.user_type'),
        key: 'userType',
        render: (rowData: InternalRowData) =>
          rowData.userType === 'GENERAL_USER'
            ? t('security.user.ordinary_user')
            : t('security.user.administrator')
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
        render: (rowData: any, unused: number) =>
          h(
            NTag,
            { type: rowData.state === 1 ? 'success' : 'error' },
            {
              default: () =>
                t(
                  `security.user.state_${
                    rowData.state === 1 ? 'enabled' : 'disabled'
                  }`
                )
            }
          )
      },
      {
        title: t('security.user.create_time'),
        key: 'createTime'
      },
      {
        title: t('security.user.update_time'),
        key: 'updateTime'
      },
      {
        title: t('security.user.operation'),
        key: 'operation',
        render: (rowData: any, unused: number) => {
          return h(NSpace, null, {
            default: () => [
              h(
                NDropdown,
                {
                  trigger: 'click',
                  options: [
                    {
                      label: t('security.user.project'),
                      key: 'authorize_project'
                    },
                    {
                      label: t('security.user.resource'),
                      key: 'authorize_resource'
                    },
                    {
                      label: t('security.user.datasource'),
                      key: 'authorize_datasource'
                    },
                    { label: t('security.user.udf'), key: 'authorize_udf' }
                  ],
                  onSelect: (key) =>
                    void onCallback({ rowData, key }, 'authorize')
                },
                () =>
                  h(
                    NTooltip,
                    {
                      trigger: 'hover'
                    },
                    {
                      trigger: () =>
                        h(
                          NButton,
                          {
                            circle: true,
                            type: 'warning',
                            size: 'small',
                            class: 'authorize'
                          },
                          {
                            icon: () => h(NIcon, null, () => h(UserOutlined))
                          }
                        ),
                      default: () => t('security.user.authorize')
                    }
                  )
              ),
              h(
                NTooltip,
                { trigger: 'hover' },
                {
                  trigger: () =>
                    h(
                      NButton,
                      {
                        circle: true,
                        type: 'info',
                        size: 'small',
                        onClick: () => void onCallback({ rowData }, 'edit')
                      },
                      () => h(NIcon, null, () => h(EditOutlined))
                    ),
                  default: () => t('security.user.edit')
                }
              ),
              h(
                NPopconfirm,
                {
                  onPositiveClick: () => void onCallback({ rowData }, 'delete')
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
                        default: () => t('security.user.delete')
                      }
                    ),
                  default: () => t('security.user.delete_confirm')
                }
              )
            ]
          })
        }
      }
    ]
  }

  onMounted(() => {
    createColumns()
  })

  watch(useI18n().locale, () => {
    createColumns()
  })

  return {
    columnsRef,
    createColumns
  }
}
