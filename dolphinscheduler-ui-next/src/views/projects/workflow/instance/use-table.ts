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
import { format } from 'date-fns'
import { reactive, h, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import type { Router } from 'vue-router'
import { RowKey, TableColumns } from 'naive-ui/lib/data-table/src/interface'
import {
  queryProcessInstanceListPaging,
  deleteProcessInstanceById,
  batchDeleteProcessInstanceByIds
} from '@/service/modules/process-instances'
import { execute } from '@/service/modules/executors'
import TableAction from './components/table-action'
import { runningType } from '@/utils/common'
import { IWorkflowInstance } from '@/service/modules/process-instances/types'
import { ICountDownParam } from './types'
import { ExecuteReq } from '@/service/modules/executors/types'
import styles from './index.module.scss'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const columns: TableColumns<IWorkflowInstance> = [
    {
      type: 'selection'
    },
    {
      title: t('project.workflow.id'),
      key: 'id',
      width: 50
    },
    {
      title: t('project.workflow.workflow_name'),
      key: 'name',
      width: 200,
      render: (_row) =>
        h(
          'a',
          {
            href: 'javascript:',
            class: styles.links,
            onClick: () =>
              router.push({
                name: 'workflow-instance-detail',
                params: { id: _row.id },
                query: { code: _row.processDefinitionCode }
              })
          },
          {
            default: () => {
              return _row.name
            }
          }
        )
    },
    {
      title: t('project.workflow.status'),
      key: 'state'
    },
    {
      title: t('project.workflow.run_type'),
      key: 'commandType',
      render: (_row) =>
        (_.filter(runningType(t), (v) => v.code === _row.commandType)[0] || {})
          .desc
    },
    {
      title: t('project.workflow.scheduling_time'),
      key: 'scheduleTime',
      render: (_row) =>
        _row.scheduleTime
          ? format(new Date(_row.scheduleTime), 'yyyy-MM-dd HH:mm:ss')
          : '-'
    },
    {
      title: t('project.workflow.start_time'),
      key: 'startTime',
      render: (_row) =>
        _row.startTime
          ? format(new Date(_row.startTime), 'yyyy-MM-dd HH:mm:ss')
          : '-'
    },
    {
      title: t('project.workflow.end_time'),
      key: 'endTime',
      render: (_row) =>
        _row.endTime
          ? format(new Date(_row.endTime), 'yyyy-MM-dd HH:mm:ss')
          : '-'
    },
    {
      title: t('project.workflow.duration'),
      key: 'duration',
      render: (_row) => _row.duration || '-'
    },
    {
      title: t('project.workflow.run_times'),
      key: 'runTimes'
    },
    {
      title: t('project.workflow.fault_tolerant_sign'),
      key: 'recovery'
    },
    {
      title: t('project.workflow.dry_run_flag'),
      key: 'dryRun',
      render: (_row) => (_row.dryRun === 1 ? 'YES' : 'NO')
    },
    {
      title: t('project.workflow.executor'),
      key: 'executorName'
    },
    {
      title: t('project.workflow.host'),
      key: 'host'
    },
    {
      title: t('project.workflow.operation'),
      key: 'operation',
      width: 220,
      fixed: 'right',
      className: styles.operation,
      render: (_row, index) =>
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

  const variables = reactive({
    columns,
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
    projectCode: ref(Number(router.currentRoute.value.params.projectCode))
  })

  const getTableData = () => {
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
      }
    )
  }

  const deleteInstance = (id: number) => {
    deleteProcessInstanceById(id, variables.projectCode)
      .then(() => {
        window.$message.success(t('project.workflow.success'))
        getTableData()
      })
      .catch((error: any) => {
        window.$message.error(error.message || '')
        getTableData()
      })
  }

  const batchDeleteInstance = () => {
    const data = {
      processInstanceIds: _.join(variables.checkedRowKeys, ',')
    }

    batchDeleteProcessInstanceByIds(data, variables.projectCode)
      .then(() => {
        window.$message.success(t('project.workflow.success'))
        variables.checkedRowKeys = []
        getTableData()
      })
      .catch((error: any) => {
        window.$message.error(error.message || '')
        getTableData()
      })
  }

  /**
   * operating
   */
  const _upExecutorsState = (param: ExecuteReq) => {
    execute(param, variables.projectCode)
      .then(() => {
        window.$message.success(t('project.workflow.success'))

        getTableData()
      })
      .catch((error: any) => {
        window.$message.error(error.message || '')
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
          // this.$forceUpdate()
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
    execute(param, variables.projectCode)
      .then(() => {
        variables.tableData[index].disabled = true
        // forceUpdate
        window.$message.success(t('project.workflow.success'))
        _countDown(() => {
          getTableData()
        }, index)
      })
      .catch((error: any) => {
        window.$message.error(error.message)
        getTableData()
      })
  }

  return {
    variables,
    getTableData,
    batchDeleteInstance
  }
}
