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
  deleteResource,
  queryResourceById
} from '@/service/modules/resources'
import { IUdfResourceParam } from './types'
import styles from './index.module.scss'

const goSubFolder = (router: Router, item: any) => {
  const fileStore = useFileStore()
  fileStore.setFileInfo(`${item.alias}|${item.size}`)

  if (item.directory) {
    fileStore.setCurrentDir(`${item.fullName}`)
    router.push({ name: 'resource-sub-manage', params: { id: item.id } })
  }
}

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()
  const fileStore = useFileStore()

  const variables = reactive({
    columns: [],
    row: {},
    tableData: [],
    breadList: [],
    id: ref(Number(router.currentRoute.value.params.id) || -1),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    totalPage: ref(1),
    folderShowRef: ref(false),
    uploadShowRef: ref(false)
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: t('resource.udf.id'),
        key: 'id',
        width: 50,
        render: (_row, index) => index + 1
      },
      {
        title: t('resource.udf.udf_source_name'),
        key: 'alias',
        render: (row) => {
          if (!row.directory) {
            return row.alias
          } else {
            return h(
              'a',
              {
                href: 'javascript:',
                class: styles.links,
                onClick: () => goSubFolder(router, row)
              },
              {
                default: () => {
                  return row.alias
                }
              }
            )
          }
        }
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
        key: 'size',
        render: (row) => bytesToSize(row.size)
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
                        tag: 'div',
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
                        tag: 'div',
                        circle: true,
                        type: 'info',
                        size: 'tiny',
                        disabled: row?.directory ? true : false,
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
                              tag: 'div',
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
    ] as TableColumns<any>
  }

  const getTableData = (params: IUdfResourceParam) => {
    const { state } = useAsyncState(
      queryResourceListPaging({ ...params, type: 'UDF' }).then((res: any) => {
        const breadList =
          variables.id === -1
            ? []
            : (fileStore.getCurrentDir.split('/') as Array<never>)
        breadList.shift()

        variables.breadList = breadList
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
    /* after deleting data from the current page, you need to jump forward when the page is empty. */
    if (variables.tableData.length === 1 && variables.page > 1) {
      variables.page -= 1
    }

    deleteResource(id).then(() =>
      getTableData({
        id: variables.id,
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    )
  }

  const goUdfManage = () => {
    router.push({ name: 'resource-manage' })
  }

  const goBread = (fullName: string) => {
    const { id } = variables
    queryResourceById(
      {
        id,
        type: 'UDF',
        fullName
      },
      id
    )
      .then((res: any) => {
        fileStore.setCurrentDir(res.fullName)
        router.push({ name: 'resource-sub-manage', params: { id: res.id } })
      })
      .catch((error: any) => {
        window.$message.error(error.message)
      })
  }

  return {
    variables,
    createColumns,
    getTableData,
    goUdfManage,
    goBread
  }
}
