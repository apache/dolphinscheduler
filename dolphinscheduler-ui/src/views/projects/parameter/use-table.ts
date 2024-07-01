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
import { NSpace, NTooltip, NButton, NPopconfirm } from 'naive-ui'
import {
  deleteProjectParameterByCode,
  queryProjectParameterListPaging
} from '@/service/modules/projects-parameter'
import { ProjectParameterCodeReq } from '@/service/modules/projects-parameter/types'
import { DeleteOutlined, EditOutlined } from '@vicons/antd'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { Router } from 'vue-router'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    row: {},
    tableData: [],
    projectCode: ref(Number(router.currentRoute.value.params.projectCode)),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    projectParameterDataType: ref(),
    totalPage: ref(1),
    showRef: ref(false),
    statusRef: ref(0),
    loadingRef: ref(false)
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'id',
        ...COLUMN_WIDTH_CONFIG['index'],
        render: (row: any, index: number) => index + 1
      },
      {
        title: t('project.parameter.name'),
        key: 'paramName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.parameter.value'),
        key: 'paramValue',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.parameter.data_type'),
        key: 'paramDataType',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.parameter.create_user'),
        key: 'createUser',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.parameter.modify_user'),
        key: 'modifyUser',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.parameter.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.parameter.update_time'),
        key: 'updateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.parameter.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](3),
        render: (row: any) => {
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
                        onClick: () => {
                          handleEdit(row)
                        }
                      },
                      {
                        icon: () => h(EditOutlined)
                      }
                    ),
                  default: () => t('project.parameter.edit')
                }
              ),
              h(
                NPopconfirm,
                {
                  onPositiveClick: () => {
                    handleDelete(row.code)
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
                              size: 'small'
                            },
                            {
                              icon: () => h(DeleteOutlined)
                            }
                          ),
                        default: () => t('project.parameter.delete')
                      }
                    ),
                  default: () => t('project.parameter.delete_confirm')
                }
              )
            ]
          })
        }
      }
    ]
    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const handleEdit = (row: any) => {
    variables.showRef = true
    variables.statusRef = 1
    variables.row = row
  }

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true

    queryProjectParameterListPaging({ ...params }, variables.projectCode).then(
      (res: any) => {
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item: any) => {
          return { ...item }
        })
        variables.loadingRef = false
      }
    )
  }

  const handleDelete = (code: number) => {
    if (variables.tableData.length === 1 && variables.page > 1) {
      variables.page -= 1
    }
    const data: ProjectParameterCodeReq = {
      code: code
    }
    deleteProjectParameterByCode(data, variables.projectCode).then(() => {
      window.$message.success(t('project.parameter.success'))
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal,
        projectParameterDataType: variables.projectParameterDataType
      })
    })
  }

  return {
    variables,
    createColumns,
    getTableData
  }
}
