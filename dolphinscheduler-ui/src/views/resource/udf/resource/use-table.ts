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
import { bytesToSize } from '@/common/common'
import { useFileStore } from '@/store/file/file'
import type { Router } from 'vue-router'
import { NEllipsis } from 'naive-ui'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { NSpace, NTooltip, NButton, NPopconfirm } from 'naive-ui'
import { EditOutlined, DeleteOutlined, DownloadOutlined } from '@vicons/antd'
import { useAsyncState } from '@vueuse/core'
import {
  queryResourceListPaging,
  downloadResource,
  deleteResource,
  queryCurrentResourceByFileName,
  queryCurrentResourceByFullName
} from '@/service/modules/resources'
import ButtonLink from '@/components/button-link'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { IUdfResourceParam } from './types'
import { ResourceFile } from '@/service/modules/resources/types'

const goSubFolder = (router: Router, item: any) => {
  const fileStore = useFileStore()
  fileStore.setFileInfo(`${item.alias}|${item.size}`)
  if (item.directory) {
    fileStore.setCurrentDir(`${item.fullName}`)
    router.push({ name: 'resource-sub-manage',
        query: {prefix: item.fullName, tenantCode: item.userName} })
  }
}

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()
  const fileStore = useFileStore()

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    row: {},
    tableData: [],
    breadList: [] as String[],
    fullName: ref(String(router.currentRoute.value.query.prefix || "")),
    tenantCode: ref(String(router.currentRoute.value.query.tenantCode || "")),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    totalPage: ref(1),
    folderShowRef: ref(false),
    uploadShowRef: ref(false),
    loadingRef: ref(false)
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'id',
        ...COLUMN_WIDTH_CONFIG['index'],
        render: (_row, index) => index + 1
      },
      {
        title: t('resource.udf.udf_source_name'),
        key: 'alias',
        ...COLUMN_WIDTH_CONFIG['linkName'],
        render: (row) => {
          return !row.directory
            ? row.alias
            : h(
                ButtonLink,
                {
                  onClick: () => void goSubFolder(router, row)
                },
                {
                  default: () =>
                    h(
                      NEllipsis,
                      COLUMN_WIDTH_CONFIG['linkEllipsis'],
                      () => row.alias
                    )
                }
              )
        }
      },
      {
        title: t('resource.udf.tenant_name'),
        ...COLUMN_WIDTH_CONFIG['userName'],
        key: 'userName'
      },
      {
        title: t('resource.udf.whether_directory'),
        key: 'whether_directory',
        ...COLUMN_WIDTH_CONFIG['yesOrNo'],
        render: (row) =>
          row.directory ? t('resource.file.yes') : t('resource.file.no')
      },
      {
        title: t('resource.udf.file_name'),
        ...COLUMN_WIDTH_CONFIG['name'],
        key: 'fullName'
      },
      {
        title: t('resource.udf.file_size'),
        key: 'size',
        ...COLUMN_WIDTH_CONFIG['size'],
        render: (row) => bytesToSize(row.size)
      },
      {
        title: t('resource.udf.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('resource.udf.update_time'),
        key: 'updateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('resource.udf.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](3),
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
                        class: 'btn-edit',
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
                        class: 'btn-download',
                        disabled: row?.directory ? true : false,
                        onClick: () => downloadResource({fullName: row.fullName})
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
                    handleDelete({fullName: row.fullName, tenantCode: row.userName})
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
                              size: 'tiny',
                              class: 'btn-delete'
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
    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const getTableData = (params: IUdfResourceParam) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const { state } = useAsyncState(
      queryResourceListPaging({ ...params, type: 'UDF' }).then((res: any) => {
        // use strict checking here
        if (variables.fullName !== ""){
            queryCurrentResourceByFullName(
              {
                type: 'UDF',
                fullName: variables.fullName,
                tenantCode: variables.tenantCode,
              }
            ).then((res: ResourceFile) => {
                if (res.fileName) {
                  const breadList = res.fileName.split('/')
                  // pop the alias from the fullname path
                  breadList.pop()
                  variables.breadList = breadList
                }
            })
        } else {
            variables.breadList = []
        }
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item: any) => {
          return { ...item }
        })
        variables.loadingRef = false
      }),
      { total: 0, table: [] }
    )
    return state
  }

  const handleEdit = (row: any) => {
    variables.folderShowRef = true
    variables.row = row
  }

  const handleDelete = (fullNameObj: {fullName: string, tenantCode: string}) => {
    /* after deleting data from the current page, you need to jump forward when the page is empty. */
    if (variables.tableData.length === 1 && variables.page > 1) {
      variables.page -= 1
    }

    deleteResource(fullNameObj).then(() =>
      getTableData({
        id: -1,
        fullName: variables.fullName,
        tenantCode: variables.tenantCode,
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    )
  }

  const goUdfManage = () => {
    router.push({ name: 'resource-manage' })
  }

  const goBread = (fileName: string) => {
    queryCurrentResourceByFileName(
      {
        type: 'UDF',
        fileName: fileName + "/",
        tenantCode: variables.tenantCode
      }
    ).then((res: any) => {
      fileStore.setCurrentDir(res.fullName)
      router.push({ name: 'resource-sub-manage', query: {prefix: res.fullName, tenantCode: res.userName} })
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
