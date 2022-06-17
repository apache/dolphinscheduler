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
import { reactive, h, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import ButtonLink from '@/components/button-link'
import { RowKey } from 'naive-ui/lib/data-table/src/interface'
import { NEllipsis } from 'naive-ui'
import {
  queryProcessInstanceListPaging,
  deleteProcessInstanceById,
  batchDeleteProcessInstanceByIds
} from '@/service/modules/process-instances'
import { execute } from '@/service/modules/executors'
import TableAction from './components/table-action'
import { renderTableTime, runningType } from '@/common/common'
import styles from './index.module.scss'
import { renderStateCell } from '../../task/instance/use-table'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { Router } from 'vue-router'
import type { IWorkflowInstance } from '@/service/modules/process-instances/types'
import type { ICountDownParam } from './types'
import type { ExecuteReq } from '@/service/modules/executors/types'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    checkedRowKeys: [] as Array<RowKey>,
    tableData: [] as Array<IWorkflowInstance>,
    page: ref(1),
    pageSize: ref(10),
    totalPage: ref(1),
    searchVal: ref(),
    executorName: ref(),
    host: ref(),
    stateType: ref(),
    startDate: ref(),
    endDate: ref(),
    projectCode: ref(Number(router.currentRoute.value.params.projectCode)),
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
        render: (rowData: any, rowIndex: number) => rowIndex + 1
      },
      {
        title: t('project.workflow.workflow_name'),
        key: 'name',
        ...COLUMN_WIDTH_CONFIG['linkName'],
        className: 'workflow-name',
        render: (row: IWorkflowInstance) =>
          h(
            ButtonLink,
            {
              onClick: () =>
                void router.push({
                  name: 'workflow-instance-detail',
                  params: { id: row.id },
                  query: { code: row.processDefinitionCode }
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
      },
      {
        title: t('project.workflow.status'),
        key: 'state',
        ...COLUMN_WIDTH_CONFIG['state'],
        className: 'workflow-status',
        render: (_row: IWorkflowInstance) => renderStateCell(_row.state, t)
      },
      {
        title: t('project.workflow.run_type'),
        key: 'commandType',
        width: 160,
        className: 'workflow-run-type',
        render: (_row: IWorkflowInstance) =>
          (
            _.filter(runningType(t), (v) => v.code === _row.commandType)[0] ||
            {}
          ).desc
      },
      {
        title: t('project.workflow.scheduling_time'),
        key: 'scheduleTime',
        ...COLUMN_WIDTH_CONFIG['time'],
        render: (_row: IWorkflowInstance) => renderTableTime(_row.scheduleTime)
      },
      {
        title: t('project.workflow.start_time'),
        key: 'startTime',
        ...COLUMN_WIDTH_CONFIG['time'],
        render: (_row: IWorkflowInstance) => renderTableTime(_row.startTime)
      },
      {
        title: t('project.workflow.end_time'),
        key: 'endTime',
        ...COLUMN_WIDTH_CONFIG['time'],
        render: (_row: IWorkflowInstance) => renderTableTime(_row.endTime)
      },
      {
        title: t('project.workflow.duration'),
        key: 'duration',
        ...COLUMN_WIDTH_CONFIG['duration'],
        render: (_row: IWorkflowInstance) => _row.duration || '-'
      },
      {
        title: t('project.workflow.run_times'),
        key: 'runTimes',
        ...COLUMN_WIDTH_CONFIG['times'],
        className: 'workflow-run-times'
      },
      {
        title: t('project.workflow.fault_tolerant_sign'),
        key: 'recovery',
        width: 100
      },
      {
        title: t('project.workflow.dry_run_flag'),
        key: 'dryRun',
        ...COLUMN_WIDTH_CONFIG['dryRun'],
        render: (_row: IWorkflowInstance) => (_row.dryRun === 1 ? 'YES' : 'NO')
      },
      {
        title: t('project.workflow.executor'),
        key: 'executorName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.workflow.host'),
        key: 'host',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.workflow.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](6),
        className: styles.operation,
        render: (_row: IWorkflowInstance, index: number) =>
          h(TableAction, {
            row: _row,
            onReRun: () =>
              _countDownFn({
                index,
                processInstanceId: _row.id,
                executeType: 'REPEAT_RUNNING',
                buttonType: 'run'
              }),
            onReStore: () =>
              _countDownFn({
                index,
                processInstanceId: _row.id,
                executeType: 'START_FAILURE_TASK_PROCESS',
                buttonType: 'store'
              }),
            onStop: () => {
              if (_row.state === 'STOP') {
                _countDownFn({
                  index,
                  processInstanceId: _row.id,
                  executeType: 'RECOVER_SUSPENDED_PROCESS',
                  buttonType: 'suspend'
                })
              } else {
                _upExecutorsState({
                  processInstanceId: _row.id,
                  executeType: 'STOP'
                })
              }
            },
            onSuspend: () => {
              if (_row.state === 'PAUSE') {
                _countDownFn({
                  index,
                  processInstanceId: _row.id,
                  executeType: 'RECOVER_SUSPENDED_PROCESS',
                  buttonType: 'suspend'
                })
              } else {
                _upExecutorsState({
                  processInstanceId: _row.id,
                  executeType: 'PAUSE'
                })
              }
            },
            onDeleteInstance: () => deleteInstance(_row.id)
          })
      }
    ]
    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const getTableData = () => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const params = {
      pageNo: variables.page,
      pageSize: variables.pageSize,
      searchVal: variables.searchVal,
      executorName: variables.executorName,
      host: variables.host,
      stateType: variables.stateType,
      startDate: variables.startDate,
      endDate: variables.endDate
    }
    queryProcessInstanceListPaging({ ...params }, variables.projectCode).then(
      (res: any) => {
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item: any) => {
          return { ...item }
        })
        variables.loadingRef = false
      }
    )
  }

  const deleteInstance = (id: number) => {
    deleteProcessInstanceById(id, variables.projectCode).then(() => {
      window.$message.success(t('project.workflow.success'))
      if (variables.tableData.length === 1 && variables.page > 1) {
        variables.page -= 1
      }

      getTableData()
    })
  }

  const batchDeleteInstance = () => {
    const data = {
      processInstanceIds: _.join(variables.checkedRowKeys, ',')
    }

    batchDeleteProcessInstanceByIds(data, variables.projectCode).then(() => {
      window.$message.success(t('project.workflow.success'))

      if (
        variables.tableData.length === variables.checkedRowKeys.length &&
        variables.page > 1
      ) {
        variables.page -= 1
      }

      variables.checkedRowKeys = []
      getTableData()
    })
  }

  /**
   * operating
   */
  const _upExecutorsState = (param: ExecuteReq) => {
    execute(param, variables.projectCode).then(() => {
      window.$message.success(t('project.workflow.success'))

      getTableData()
    })
  }

  /**
   * Countdown
   */
  const _countDown = (fn: any, index: number) => {
    const TIME_COUNT = 10
    let timer: number | undefined
    let $count: number
    if (!timer) {
      $count = TIME_COUNT
      timer = setInterval(() => {
        if ($count > 0 && $count <= TIME_COUNT) {
          $count--
          variables.tableData[index].count = $count
        } else {
          fn()
          clearInterval(timer)
          timer = undefined
        }
      }, 1000)
    }
  }

  /**
   * Countdown method refresh
   */
  const _countDownFn = (param: ICountDownParam) => {
    const { index } = param
    variables.tableData[index].buttonType = param.buttonType
    execute(param, variables.projectCode).then(() => {
      variables.tableData[index].disabled = true
      window.$message.success(t('project.workflow.success'))
      _countDown(() => {
        getTableData()
      }, index)
    })
  }

  return {
    variables,
    createColumns,
    getTableData,
    batchDeleteInstance
  }
}
