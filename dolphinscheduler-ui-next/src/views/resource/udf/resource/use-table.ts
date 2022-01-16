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
import { bytesToSize } from '@/utils/common'
import { useFileStore } from '@/store/file/file'
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { NSpace, NTooltip, NButton, NPopconfirm } from 'naive-ui'
import { EditOutlined, DeleteOutlined, DownloadOutlined } from '@vicons/antd'
import { useAsyncState } from '@vueuse/core'
import {
  queryResourceListPaging,
  downloadResource,
  deleteResource
} from '@/service/modules/resources'
import { IUdfResourceParam, IUdfRes } from './types'
import styles from './index.module.scss'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const columns: TableColumns<any> = [
    {
      title: t('resource.udf.id'),
      key: 'id',
      width: 50,
      render: (_row, index) => index + 1
    },
    {
      title: t('resource.udf.udf_source_name'),
      key: 'alias'
    },
    {
      title: t('resource.udf.whether_directory'),
      key: 'whether_directory',
      render: (row) =>
        row.directory ? t('resource.file.yes') : t('resource.file.no')
    },
    {
      title: t('resource.udf.file_name'),
      key: 'fileName'
    },
    {
      title: t('resource.udf.file_size'),
      key: 'size'
    },
    {
      title: t('resource.udf.description'),
      key: 'description'
    },
    {
      title: t('resource.udf.create_time'),
      key: 'createTime'
    },
    {
      title: t('resource.udf.update_time'),
      key: 'updateTime'
    },
    {
      title: t('resource.udf.operation'),
      key: 'operation',
      render: (row) => {
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
                      size: 'tiny',
                      onClick: () => {
                        handleEdit(row)
                      }
                    },
                    {
                      icon: () => h(EditOutlined)
                    }
                  ),
                default: () => t('resource.udf.edit')
              }
            ),
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
                      onClick: () => downloadResource(row.id)
                    },
                    {
                      icon: () => h(DownloadOutlined)
                    }
                  ),
                default: () => t('resource.udf.download')
              }
            ),
            h(
              NPopconfirm,
              {
                onPositiveClick: () => {
                  handleDelete(row.id)
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
                            size: 'tiny'
                          },
                          {
                            icon: () => h(DeleteOutlined)
                          }
                        ),
                      default: () => t('resource.udf.delete')
                    }
                  ),
                default: () => t('resource.udf.delete_confirm')
              }
            )
          ]
        })
      }
    }
  ]

  const variables = reactive({
    columns,
    row: {},
    tableData: [],
    id: ref(-1),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    totalPage: ref(1),
    folderShowRef: ref(false),
    uploadShowRef: ref(false)
  })

  const getTableData = (params: IUdfResourceParam) => {
    const { state } = useAsyncState(
      queryResourceListPaging({ ...params, type: 'UDF' }).then((res: any) => {
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item: any) => {
          return { ...item }
        })
      }),
      { total: 0, table: [] }
    )
    return state
  }

  const handleEdit = (row: any) => {
    variables.folderShowRef = true
    variables.row = row
  }

  const handleDelete = (id: number) => {
    deleteResource(id).then(() =>
      getTableData({
        id: variables.id,
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    )
  }

  return {
    variables,
    getTableData
  }
}
