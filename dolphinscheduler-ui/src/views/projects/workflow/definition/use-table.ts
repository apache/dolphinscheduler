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

import _ from 'lodash'
import { h, ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useAsyncState } from '@vueuse/core'
import { useTextCopy } from '../components/dag/use-text-copy'
import {
  batchCopyByCodes,
  batchDeleteByCodes,
  batchExportByCodes,
  deleteByCode,
  queryListPaging,
  release
} from '@/service/modules/process-definition'
import TableAction from './components/table-action'
import styles from './index.module.scss'
import { NTag, NSpace, NIcon, NButton, NEllipsis } from 'naive-ui'
import { CopyOutlined } from '@vicons/antd'
import ButtonLink from '@/components/button-link'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { IDefinitionParam } from './types'
import type { Router } from 'vue-router'
import type { TableColumns, RowKey } from 'naive-ui/es/data-table/src/interface'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()
  const { copy } = useTextCopy()
  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    checkedRowKeys: [] as Array<RowKey>,
    row: {},
    tableData: [],
    projectCode: ref(Number(router.currentRoute.value.params.projectCode)),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    totalPage: ref(1),
    showRef: ref(false),
    startShowRef: ref(false),
    timingShowRef: ref(false),
    versionShowRef: ref(false),
    copyShowRef: ref(false),
    loadingRef: ref(false)
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        type: 'selection',
        className: 'btn-selected',
        ...COLUMN_WIDTH_CONFIG['selection']
      },
      {
        title: '#',
        key: 'id',
        ...COLUMN_WIDTH_CONFIG['index'],
        render: (row, index) => index + 1
      },
      {
        title: t('project.workflow.workflow_name'),
        key: 'name',
        className: 'workflow-name',
        ...COLUMN_WIDTH_CONFIG['name'],
        titleColSpan: 2,
        render: (row) =>
          h(
            NSpace,
            {
              justify: 'space-between',
              wrap: false,
              class: styles['workflow-name']
            },
            {
              default: () =>
                h(
                  ButtonLink,
                  {
                    onClick: () =>
                      void router.push({
                        name: 'workflow-definition-detail',
                        params: { code: row.code }
                      })
                  },
                  {
                    default: () =>
                      h(
                        NEllipsis,
                        COLUMN_WIDTH_CONFIG['linkEllipsis'],
                        () => row.name
                      )
                  }
                )
            }
          )
      },
      {
        title: 'Copy',
        key: 'copy',
        ...COLUMN_WIDTH_CONFIG['copy'],
        render: (row) =>
          h(
            NButton,
            {
              quaternary: true,
              circle: true,
              type: 'info',
              size: 'tiny',
              onClick: () => void copy(row.name)
            },
            { icon: () => h(NIcon, { size: 16 }, () => h(CopyOutlined)) }
          )
      },
      {
        title: t('project.workflow.status'),
        key: 'releaseState',
        ...COLUMN_WIDTH_CONFIG['state'],
        render: (row) =>
          row.releaseState === 'ONLINE'
            ? t('project.workflow.up_line')
            : t('project.workflow.down_line')
      },
      {
        title: t('project.workflow.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.workflow.update_time'),
        key: 'updateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('project.workflow.description'),
        key: 'description',
        ...COLUMN_WIDTH_CONFIG['note']
      },
      {
        title: t('project.workflow.create_user'),
        key: 'userName',
        ...COLUMN_WIDTH_CONFIG['userName']
      },
      {
        title: t('project.workflow.modify_user'),
        key: 'modifyBy',
        ...COLUMN_WIDTH_CONFIG['userName']
      },
      {
        title: t('project.workflow.schedule_publish_status'),
        key: 'scheduleReleaseState',
        ...COLUMN_WIDTH_CONFIG['state'],
        render: (row) => {
          if (row.scheduleReleaseState === 'ONLINE') {
            return h(
              NTag,
              { type: 'success', size: 'small' },
              {
                default: () => t('project.workflow.up_line')
              }
            )
          } else if (row.scheduleReleaseState === 'OFFLINE') {
            return h(
              NTag,
              { type: 'warning', size: 'small' },
              {
                default: () => t('project.workflow.down_line')
              }
            )
          } else {
            return '-'
          }
        }
      },
      {
        title: t('project.workflow.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](10),
        render: (row) =>
          h(TableAction, {
            row,
            onEditWorkflow: () => editWorkflow(row),
            onStartWorkflow: () => startWorkflow(row),
            onTimingWorkflow: () => timingWorkflow(row),
            onVersionWorkflow: () => versionWorkflow(row),
            onDeleteWorkflow: () => deleteWorkflow(row),
            onReleaseWorkflow: () => releaseWorkflow(row),
            onCopyWorkflow: () => copyWorkflow(row),
            onExportWorkflow: () => exportWorkflow(row),
            onGotoTimingManage: () => gotoTimingManage(row),
            onGotoWorkflowTree: () => gotoWorkflowTree(row)
          })
      }
    ] as TableColumns<any>
    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const editWorkflow = (row: any) => {
    variables.row = row
    router.push({
      name: 'workflow-definition-detail',
      params: { code: row.code }
    })
  }

  const startWorkflow = (row: any) => {
    variables.startShowRef = true
    variables.row = row
  }

  const timingWorkflow = (row: any) => {
    variables.timingShowRef = true
    variables.row = row
  }

  const versionWorkflow = (row: any) => {
    variables.versionShowRef = true
    variables.row = row
  }

  const deleteWorkflow = (row: any) => {
    deleteByCode(variables.projectCode, row.code).then(() => {
      window.$message.success(t('project.workflow.success'))
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const batchDeleteWorkflow = () => {
    const data = {
      codes: _.join(variables.checkedRowKeys, ',')
    }

    batchDeleteByCodes(data, variables.projectCode).then(() => {
      window.$message.success(t('project.workflow.success'))

      if (
        variables.tableData.length === variables.checkedRowKeys.length &&
        variables.page > 1
      ) {
        variables.page -= 1
      }

      variables.checkedRowKeys = []
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const batchExportWorkflow = () => {
    const fileName = 'workflow_' + new Date().getTime()
    const data = {
      codes: _.join(variables.checkedRowKeys, ',')
    }

    batchExportByCodes(data, variables.projectCode).then((res: any) => {
      downloadBlob(res, fileName)
      window.$message.success(t('project.workflow.success'))
      variables.checkedRowKeys = []
    })
  }

  const batchCopyWorkflow = () => {}

  const releaseWorkflow = (row: any) => {
    const data = {
      name: row.name,
      releaseState: (row.releaseState === 'ONLINE' ? 'OFFLINE' : 'ONLINE') as
        | 'OFFLINE'
        | 'ONLINE'
    }
    release(data, variables.projectCode, row.code).then(() => {
      window.$message.success(t('project.workflow.success'))
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const copyWorkflow = (row: any) => {
    const data = {
      codes: String(row.code),
      targetProjectCode: variables.projectCode
    }
    batchCopyByCodes(data, variables.projectCode).then(() => {
      window.$message.success(t('project.workflow.success'))
      getTableData({
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    })
  }

  const downloadBlob = (data: any, fileNameS = 'json') => {
    if (!data) {
      return
    }
    const blob = new Blob([data])
    const fileName = `${fileNameS}.json`
    if ('download' in document.createElement('a')) {
      // Not IE
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = url
      link.setAttribute('download', fileName)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link) // remove element after downloading is complete.
      window.URL.revokeObjectURL(url) // release blob object
    } else {
      // IE 10+
      if (window.navigator.msSaveBlob) {
        window.navigator.msSaveBlob(blob, fileName)
      }
    }
  }

  const exportWorkflow = (row: any) => {
    const fileName = 'workflow_' + new Date().getTime()

    const data = {
      codes: String(row.code)
    }
    batchExportByCodes(data, variables.projectCode).then((res: any) => {
      downloadBlob(res, fileName)
    })
  }

  const gotoTimingManage = (row: any) => {
    router.push({
      name: 'workflow-definition-timing',
      params: { projectCode: variables.projectCode, definitionCode: row.code }
    })
  }

  const gotoWorkflowTree = (row: any) => {
    router.push({
      name: 'workflow-definition-tree',
      params: { projectCode: variables.projectCode, definitionCode: row.code }
    })
  }

  const getTableData = (params: IDefinitionParam) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const { state } = useAsyncState(
      queryListPaging({ ...params }, variables.projectCode).then((res: any) => {
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

  return {
    variables,
    createColumns,
    getTableData,
    batchDeleteWorkflow,
    batchExportWorkflow,
    batchCopyWorkflow
  }
}
