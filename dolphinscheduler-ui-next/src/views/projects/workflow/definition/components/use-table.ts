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

import { h, ref, reactive, SetupContext } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { NSpace, NTooltip, NButton, NPopconfirm, NTag } from 'naive-ui'
import {
  deleteVersion,
  queryVersions,
  switchVersion
} from '@/service/modules/process-definition'
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { DeleteOutlined, ExclamationCircleOutlined } from '@vicons/antd'
import styles from '../index.module.scss'

export function useTable(
  ctx: SetupContext<('update:show' | 'update:row' | 'updateList')[]>
) {
  const { t } = useI18n()
  const router: Router = useRouter()

  const columns: TableColumns<any> = [
    {
      title: '#',
      key: 'id',
      width: 50,
      render: (_row, index) => index + 1
    },
    {
      title: t('project.workflow.version'),
      key: 'version',
      render: (_row) => {
        if (_row.version === variables.row.version) {
          return h(
            NTag,
            { type: 'success', size: 'small' },
            {
              default: () =>
                `V${_row.version} ${t('project.workflow.current_version')}`
            }
          )
        } else {
          return `V${_row.version}`
        }
      }
    },
    {
      title: t('project.workflow.description'),
      key: 'description'
    },
    {
      title: t('project.workflow.create_time'),
      key: 'createTime'
    },
    {
      title: t('project.workflow.operation'),
      key: 'operation',
      className: styles.operation,
      render: (_row) => {
        return h(NSpace, null, {
          default: () => [
            h(
              NPopconfirm,
              {
                onPositiveClick: () => {
                  handleSwitchVersion(_row.version)
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
                            type: 'info',
                            size: 'tiny',
                            disabled: _row.version === variables.row.version
                          },
                          {
                            icon: () => h(ExclamationCircleOutlined)
                          }
                        ),
                      default: () => t('project.workflow.switch_version')
                    }
                  ),
                default: () => t('project.workflow.confirm_switch_version')
              }
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => {
                  handleDeleteVersion(_row.version)
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
                            size: 'tiny',
                            disabled: _row.version === variables.row.version
                          },
                          {
                            icon: () => h(DeleteOutlined)
                          }
                        ),
                      default: () => t('project.workflow.delete')
                    }
                  ),
                default: () => t('project.workflow.delete_confirm')
              }
            )
          ]
        })
      }
    }
  ]

  const variables = reactive({
    columns,
    row: {} as any,
    tableData: [],
    projectCode: ref(Number(router.currentRoute.value.params.projectCode))
  })

  const getTableData = (row: any) => {
    variables.row = row
    const params = {
      pageSize: 10,
      pageNo: 1
    }
    queryVersions(
      { ...params },
      variables.projectCode,
      variables.row.code
    ).then((res: any) => {
      variables.tableData = res.totalList.map((item: any) => ({ ...item }))
    })
  }

  const handleSwitchVersion = (version: number) => {
    switchVersion(variables.projectCode, variables.row.code, version)
      .then(() => {
        window.$message.success(t('project.workflow.success'))
        ctx.emit('updateList')
        getTableData(variables.row)
      })
  }

  const handleDeleteVersion = (version: number) => {
    deleteVersion(variables.projectCode, variables.row.code, version)
      .then(() => {
        window.$message.success(t('project.workflow.success'))
        ctx.emit('updateList')
        getTableData(variables.row)
      })
  }

  return {
    variables,
    getTableData
  }
}
