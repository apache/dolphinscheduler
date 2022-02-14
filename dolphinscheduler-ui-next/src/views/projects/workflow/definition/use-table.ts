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
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { useAsyncState } from '@vueuse/core'
import {
  batchCopyByCodes,
  batchExportByCodes,
  deleteByCode,
  queryListPaging,
  release
} from '@/service/modules/process-definition'
import TableAction from './components/table-action'

import { IDefinitionParam } from './types'
import styles from './index.module.scss'
import { NEllipsis, NTag } from 'naive-ui'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const variables = reactive({
    columns: [],
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
    versionShowRef: ref(false)
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: t('project.workflow.id'),
        key: 'id',
        width: 50,
        render: (row, index) => index + 1
      },
      {
        title: t('project.workflow.workflow_name'),
        key: 'name',
        width: 200,
        render: (row) =>
          h(
            NEllipsis,
            { style: 'max-width: 200px; color: #2080f0' },
            {
              default: () =>
                h(
                  'a',
                  {
                    href: 'javascript:',
                    class: styles.links,
                    onClick: () =>
                      router.push({
                        name: 'workflow-definition-detail',
                        params: { code: row.code }
                      })
                  },
                  row.name
                ),
              tooltip: () => row.name
            }
          )
      },
      {
        title: t('project.workflow.status'),
        key: 'releaseState',
        render: (row) =>
          row.releaseState === 'ONLINE'
            ? t('project.workflow.up_line')
            : t('project.workflow.down_line')
      },
      {
        title: t('project.workflow.create_time'),
        key: 'createTime',
        width: 150
      },
      {
        title: t('project.workflow.update_time'),
        key: 'updateTime',
        width: 150
      },
      {
        title: t('project.workflow.description'),
        key: 'description'
      },
      {
        title: t('project.workflow.create_user'),
        key: 'userName'
      },
      {
        title: t('project.workflow.modify_user'),
        key: 'modifyBy'
      },
      {
        title: t('project.workflow.schedule_publish_status'),
        key: 'scheduleReleaseState',
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
        width: 300,
        fixed: 'right',
        className: styles.operation,
        render: (row) =>
          h(TableAction, {
            row,
            onStartWorkflow: () => startWorkflow(row),
            onTimingWorkflow: () => timingWorkflow(row),
            onVersionWorkflow: () => versionWorkflow(row),
            onDeleteWorkflow: () => deleteWorkflow(row),
            onReleaseWorkflow: () => releaseWorkflow(row),
            onCopyWorkflow: () => copyWorkflow(row),
            onExportWorkflow: () => exportWorkflow(row),
            onGotoTimingManage: () => gotoTimingManage(row)
          })
      }
    ] as TableColumns<any>
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
    deleteByCode(variables.projectCode, row.code)
      .then(() => {
        window.$message.success(t('project.workflow.success'))
        getTableData({
          pageSize: variables.pageSize,
          pageNo: variables.page,
          searchVal: variables.searchVal
        })
      })
      .catch((error: any) => {
        window.$message.error(error.message)
      })
  }

  const releaseWorkflow = (row: any) => {
    const data = {
      name: row.name,
      releaseState: (row.releaseState === 'ONLINE' ? 'OFFLINE' : 'ONLINE') as
        | 'OFFLINE'
        | 'ONLINE'
    }
    release(data, variables.projectCode, row.code)
      .then(() => {
        window.$message.success(t('project.workflow.success'))
        getTableData({
          pageSize: variables.pageSize,
          pageNo: variables.page,
          searchVal: variables.searchVal
        })
      })
      .catch((error: any) => {
        window.$message.error(error.message)
      })
  }

  const copyWorkflow = (row: any) => {
    const data = {
      codes: String(row.code),
      targetProjectCode: variables.projectCode
    }
    batchCopyByCodes(data, variables.projectCode)
      .then(() => {
        window.$message.success(t('project.workflow.success'))
        getTableData({
          pageSize: variables.pageSize,
          pageNo: variables.page,
          searchVal: variables.searchVal
        })
      })
      .catch((error: any) => {
        window.$message.error(error.message)
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
    batchExportByCodes(data, variables.projectCode)
      .then((res: any) => {
        downloadBlob(res, fileName)
      })
      .catch((error: any) => {
        window.$message.error(error.message)
      })
  }

  const gotoTimingManage = (row: any) => {
    router.push({
      name: 'workflow-definition-timing',
      params: { projectCode: variables.projectCode, definitionCode: row.code }
    })
  }

  const getTableData = (params: IDefinitionParam) => {
    const { state } = useAsyncState(
      queryListPaging({ ...params }, variables.projectCode).then((res: any) => {
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item: any) => {
          return { ...item }
        })
      }),
      { total: 0, table: [] }
    )
    return state
  }

  return {
    variables,
    createColumns,
    getTableData
  }
}
