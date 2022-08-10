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

import { useI18n } from 'vue-i18n'
import { h, reactive } from 'vue'
import {
  downloadLog,
  queryTaskListPaging,
  savePoint,
  streamTaskStop
} from '@/service/modules/task-instances'
import { NButton, NIcon, NSpace, NTooltip, NSpin } from 'naive-ui'
import {
  AlignLeftOutlined,
  DownloadOutlined,
  RetweetOutlined,
  SaveOutlined,
  StopOutlined
} from '@vicons/antd'
import { format } from 'date-fns'
import { useRoute } from 'vue-router'
import { parseTime, renderTableTime, tasksState } from '@/common/common'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { TaskInstancesRes, ITaskState } from './types'

export function useTable() {
  const { t } = useI18n()
  const route = useRoute()
  const projectCode = Number(route.params.projectCode)
  const processInstanceId = Number(route.params.processInstanceId)

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    tableData: [] as any[],
    page: 1,
    pageSize: 10,
    searchVal: null,
    processInstanceId: processInstanceId ? processInstanceId : null,
    host: null,
    stateType: null,
    datePickerRange: null,
    executorName: null,
    processDefinitionName: null,
    totalPage: 1,
    showModalRef: false,
    row: {},
    loadingRef: false,
    logRef: '',
    logLoadingRef: true,
    skipLineNum: 0,
    limit: 1000
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'index',
        render: (row: any, index: number) => index + 1,
        ...COLUMN_WIDTH_CONFIG['index']
      },
      {
        title: t('project.task.task_name'),
        key: 'name',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.task.workflow_name'),
        key: 'processDefinitionName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.task.node_type'),
        key: 'taskType',
        ...COLUMN_WIDTH_CONFIG['type']
      },
      {
        title: t('project.task.state'),
        key: 'state',
        ...COLUMN_WIDTH_CONFIG['state'],
        render: (row: any) => renderStateCell(row.state, t)
      },
      {
        title: t('project.task.executor'),
        key: 'executorName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.task.host'),
        key: 'host',
        ...COLUMN_WIDTH_CONFIG['name'],
        render: (row: any) => row.host || '-'
      },
      {
        title: t('project.task.app_id'),
        key: 'applicationID',
        ...COLUMN_WIDTH_CONFIG['name'],
        render: (row: any) => row.applicationID || '-'
      },
      {
        title: t('project.task.dry_run_flag'),
        key: 'dryRun',
        ...COLUMN_WIDTH_CONFIG['dryRun'],
        render: (row: any) => (row.dryRun === 1 ? 'YES' : 'NO')
      },
      {
        title: t('project.task.start_time'),
        ...COLUMN_WIDTH_CONFIG['time'],
        key: 'startTime',
        render: (row: any) => renderTableTime(row.startTime)
      },
      {
        title: t('project.task.end_time'),
        ...COLUMN_WIDTH_CONFIG['time'],
        key: 'endTime',
        render: (row: any) => renderTableTime(row.endTime)
      },
      {
        title: t('project.task.duration'),
        key: 'duration',
        ...COLUMN_WIDTH_CONFIG['duration'],
        render: (row: any) => h('span', null, row.duration ? row.duration : '-')
      },
      {
        title: t('project.task.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](5),
        render(row: any) {
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
                        size: 'small',
                        onClick: () => onSavePoint(row.id)
                      },
                      {
                        icon: () =>
                          h(NIcon, null, {
                            default: () => h(SaveOutlined)
                          })
                      }
                    ),
                  default: () => t('project.task.savepoint')
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
                        size: 'small',
                        onClick: () => onExecute(row.id)
                      },
                      {
                        icon: () =>
                          h(NIcon, null, {
                            default: () => h(StopOutlined)
                          })
                      }
                    ),
                  default: () => t('project.task.stop')
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
                        size: 'small',
                        disabled: !row.host,
                        onClick: () => handleLog(row)
                      },
                      {
                        icon: () =>
                          h(NIcon, null, {
                            default: () => h(AlignLeftOutlined)
                          })
                      }
                    ),
                  default: () => t('project.task.view_log')
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
                        size: 'small',
                        onClick: () => downloadLog(row.id)
                      },
                      {
                        icon: () =>
                          h(NIcon, null, { default: () => h(DownloadOutlined) })
                      }
                    ),
                  default: () => t('project.task.download_log')
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
                        type: 'warning',
                        size: 'small',
                        disabled: true,
                        tag: 'div'
                      },
                      {
                        icon: () =>
                          h(NIcon, null, {
                            default: () => h(RetweetOutlined)
                          })
                      }
                    ),
                  default: () => t('project.task.jump_tip')
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

  const handleLog = (row: any) => {
    variables.showModalRef = true
    variables.row = row
  }

  const getTableData = () => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const data = {
      pageSize: variables.pageSize,
      pageNo: variables.page,
      searchVal: variables.searchVal,
      processInstanceId: variables.processInstanceId,
      host: variables.host,
      stateType: variables.stateType,
      startDate: variables.datePickerRange
        ? format(parseTime(variables.datePickerRange[0]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      endDate: variables.datePickerRange
        ? format(parseTime(variables.datePickerRange[1]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      executorName: variables.executorName,
      processDefinitionName: variables.processDefinitionName,
      taskExecuteType: 'STREAM' as 'BATCH' | 'STREAM'
    } as any

    queryTaskListPaging(data, { projectCode })
      .then((res: TaskInstancesRes) => {
        variables.tableData = [...res.totalList]
        variables.totalPage = res.totalPage
      })
      .finally(() => (variables.loadingRef = false))
  }

  const onExecute = (taskId: number) => {
    streamTaskStop(projectCode, taskId).then(() => {
      window.$message.success(t('project.task.success'))
      getTableData()
    })
  }

  const onSavePoint = (taskId: number) => {
    savePoint(projectCode, taskId).then(() => {
      window.$message.success(t('project.task.success'))
      getTableData()
    })
  }

  return {
    t,
    variables,
    getTableData,
    createColumns
  }
}

export function renderStateCell(state: ITaskState, t: Function) {
  if (!state) return ''

  const stateOption = tasksState(t)[state]

  const Icon = h(
    NIcon,
    {
      color: stateOption.color,
      class: stateOption.classNames,
      style: {
        display: 'flex'
      },
      size: 20
    },
    () => h(stateOption.icon)
  )
  return h(NTooltip, null, {
    trigger: () => {
      if (!stateOption.isSpin) return Icon
      return h(NSpin, { size: 20 }, { icon: () => Icon })
    },
    default: () => stateOption.desc
  })
}
