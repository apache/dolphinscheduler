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
import { NButton, NIcon, NPopconfirm, NSpace, NTooltip } from 'naive-ui'
import { EditOutlined, DeleteOutlined } from '@vicons/antd'
import type { TableColumns } from './types'

export function useColumns(onCallback: Function) {
  const { t } = useI18n()

  const getColumns = (): TableColumns => {
    return [
      {
        title: '#',
        key: 'index',
        render: (rowData, rowIndex) => rowIndex + 1
      },
      {
        title: t('security.alarm_instance.alarm_instance_name'),
        key: 'instanceName'
      },
      {
        title: t('security.alarm_instance.alarm_instance_type'),
        key: 'instanceType'
      },
      {
        title: t('security.alarm_instance.alarm_plugin_name'),
        key: 'alertPluginName'
      },
      {
        title: t('security.alarm_instance.create_time'),
        key: 'createTime'
      },
      {
        title: t('security.alarm_instance.update_time'),
        key: 'updateTime'
      },
      {
        title: t('security.alarm_instance.operation'),
        key: 'operation',
        width: 150,
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
                      onClick: () => void onCallback(rowData, 'edit')
                    },
                    {
                      default: () =>
                        h(NIcon, null, { default: () => h(EditOutlined) })
                    }
                  ),
                default: () => t('security.alarm_instance.edit')
              }),
              h(NTooltip, null, {
                trigger: () =>
                  h(
                    NPopconfirm,
                    {
                      onPositiveClick: () => void onCallback(rowData, 'delete'),
                      negativeText: t('security.alarm_instance.cancel'),
                      positiveText: t('security.alarm_instance.confirm')
                    },
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
                            default: () =>
                              h(NIcon, null, {
                                default: () => h(DeleteOutlined)
                              })
                          }
                        ),
                      default: () => t('security.alarm_instance.delete_confirm')
                    }
                  ),
                default: () => t('security.alarm_instance.delete')
              })
            ]
          })
        }
      }
    ]
  }

  return {
    getColumns
  }
}
