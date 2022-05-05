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

import { h } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  NPopover,
  NButton,
  NIcon,
  NPopconfirm,
  NSpace,
  NTooltip
} from 'naive-ui'
import { EditOutlined, DeleteOutlined } from '@vicons/antd'
import JsonHighlight from './json-highlight'
import ButtonLink from '@/components/button-link'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { TableColumns } from './types'

export function useColumns(onCallback: Function) {
  const { t } = useI18n()

  const getColumns = (): { columns: TableColumns; tableWidth: number } => {
    const columns = [
      {
        title: '#',
        key: 'index',
        render: (unused, rowIndex) => rowIndex + 1,
        ...COLUMN_WIDTH_CONFIG['index']
      },
      {
        title: t('datasource.datasource_name'),
        key: 'name',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('datasource.datasource_user_name'),
        key: 'userName',
        ...COLUMN_WIDTH_CONFIG['userName']
      },
      {
        title: t('datasource.datasource_type'),
        key: 'type',
        width: 180
      },
      {
        title: t('datasource.datasource_parameter'),
        key: 'parameter',
        width: 180,
        render: (rowData) => {
          return h(
            NPopover,
            { trigger: 'click' },
            {
              trigger: () =>
                h(ButtonLink, null, {
                  default: () => t('datasource.click_to_view')
                }),
              default: () => h(JsonHighlight, { rowData })
            }
          )
        }
      },
      {
        title: t('datasource.description'),
        key: 'note',
        ...COLUMN_WIDTH_CONFIG['note'],
        render: (rowData) => rowData.description || '-'
      },
      {
        title: t('datasource.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('datasource.update_time'),
        key: 'updateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('datasource.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](2),
        render: (rowData) => {
          return h(NSpace, null, {
            default: () => [
              h(NTooltip, null, {
                trigger: () =>
                  h(
                    NButton,
                    {
                      circle: true,
                      type: 'info',
                      size: 'small',
                      onClick: () => void onCallback(rowData.id, 'edit')
                    },
                    {
                      default: () =>
                        h(NIcon, null, { default: () => h(EditOutlined) })
                    }
                  ),
                default: () => t('datasource.edit')
              }),
              h(NTooltip, null, {
                trigger: () =>
                  h(
                    NPopconfirm,
                    {
                      onPositiveClick: () =>
                        void onCallback(rowData.id, 'delete'),
                      negativeText: t('datasource.cancel'),
                      positiveText: t('datasource.confirm')
                    },
                    {
                      trigger: () =>
                        h(
                          NButton,
                          {
                            circle: true,
                            type: 'error',
                            size: 'small',
                            class: 'btn-delete'
                          },
                          {
                            default: () =>
                              h(NIcon, null, {
                                default: () => h(DeleteOutlined)
                              })
                          }
                        ),
                      default: () => t('datasource.delete_confirm')
                    }
                  ),
                default: () => t('datasource.delete')
              })
            ]
          })
        }
      }
    ] as TableColumns

    return {
      columns,
      tableWidth: calculateTableWidth(columns) || DefaultTableWidth
    }
  }

  return {
    getColumns
  }
}
