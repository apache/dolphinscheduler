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
import { NPopover, NButton, NIcon, NPopconfirm, NSpace } from 'naive-ui'
import { EditOutlined, DeleteOutlined } from '@vicons/antd'
import JsonHighlight from './json-highlight'
import styles from './index.module.scss'
import { TableColumns } from './types'

export function useColumns(onCallback: Function) {
  const { t } = useI18n()

  const columnsRef: TableColumns = [
    {
      title: t('datasource.serial_number'),
      key: 'index',
      render: (rowData, rowIndex) => rowIndex + 1
    },
    {
      title: t('datasource.datasource_name'),
      key: 'name'
    },
    {
      title: t('datasource.datasource_user_name'),
      key: 'userName'
    },
    {
      title: t('datasource.datasource_type'),
      key: 'type'
    },
    {
      title: t('datasource.datasource_parameter'),
      key: 'parameter',
      render: (rowData) => {
        return h(
          NPopover,
          { trigger: 'click' },
          {
            trigger: () =>
              h(
                NButton,
                {
                  quaternary: true,
                  type: 'primary'
                },
                {
                  default: () => t('datasource.click_to_view')
                }
              ),
            default: () => h(JsonHighlight, { rowData })
          }
        )
      }
    },
    {
      title: t('datasource.description'),
      key: 'note'
    },
    {
      title: t('datasource.create_time'),
      key: 'createTime'
    },
    {
      title: t('datasource.update_time'),
      key: 'updateTime'
    },
    {
      title: t('datasource.operation'),
      key: 'operation',
      width: 150,
      render: (rowData, rowIndex) => {
        return h(NSpace, null, {
          default: () => [
            h(
              NButton,
              {
                circle: true,
                type: 'info',
                onClick: () => void onCallback(rowData.id, 'edit')
              },
              {
                default: () =>
                  h(NIcon, null, { default: () => h(EditOutlined) })
              }
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => void onCallback(rowData.id, 'delete'),
                negativeText: t('datasource.cancel'),
                positiveText: t('datasource.confirm')
              },
              {
                trigger: () =>
                  h(
                    NButton,
                    {
                      circle: true,
                      type: 'error'
                    },
                    {
                      default: () =>
                        h(NIcon, null, { default: () => h(DeleteOutlined) })
                    }
                  ),
                default: () => t('datasource.delete')
              }
            )
          ]
        })
      }
    }
  ]

  return {
    columnsRef
  }
}
