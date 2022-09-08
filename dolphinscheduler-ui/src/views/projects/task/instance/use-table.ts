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
import { h, reactive, ref } from 'vue'
import { useAsyncState } from '@vueuse/core'
import {
  queryTaskListPaging,
  forceSuccess,
  downloadLog
} from '@/service/modules/task-instances'
import {
  NButton,
  NIcon,
  NSpace,
  NTooltip,
  NSpin,
  NEllipsis
} from 'naive-ui'
import ButtonLink from '@/components/button-link'
import {
  AlignLeftOutlined,
  CheckCircleOutlined,
  DownloadOutlined
} from '@vicons/antd'
import { format } from 'date-fns'
import { useRoute, useRouter } from 'vue-router'
import {
  parseTime,
  renderTableTime,
  tasksState
} from '@/common/common'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { Router, TaskInstancesRes, IRecord, ITaskState } from './types'
import {renderEnvironmentalDistinctionCell} from "@/utils/environmental-distinction";


export function useTable() {
  const { t } = useI18n()
  const route = useRoute()
  const router: Router = useRouter()
  const projectCode = Number(route.params.projectCode)
  const processInstanceId = Number(route.params.processInstanceId)
  const taskName = route.params.taskName

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    tableData: [] as IRecord[],
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(taskName || null),
    processInstanceId: ref(processInstanceId ? processInstanceId : null),
    host: ref(null),
    stateType: ref(null),
    datePickerRange: ref(null),
    executorName: ref(null),
    processInstanceName: ref(null),
    totalPage: ref(1),
    showModalRef: ref(false),
    row: {},
    loadingRef: ref(false),
    logRef: '',
    logLoadingRef: ref(true),
    skipLineNum: ref(0),
    limit: ref(1000)
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
        title: t('project.task.workflow_instance'),
        key: 'processInstanceName',
        ...COLUMN_WIDTH_CONFIG['linkName'],
        render: (row: {
          processInstanceId: number
          processInstanceName: string
        }) =>
          h(
            ButtonLink,
            {
              onClick: () =>
                void router.push({
                  name: 'workflow-instance-detail',
                  params: { id: row.processInstanceId },
                  query: { code: projectCode }
                })
            },
            {
              default: () =>
                h(
                  NEllipsis,
                  COLUMN_WIDTH_CONFIG['linkEllipsis'],
                  () => row.processInstanceName
                )
            }
          )
      },
      {
        title: t('project.task.executor'),
        key: 'executorName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('project.task.operating_environment'),
        key: 'testFlag',
        width: 160,
        render: (_row: IRecord) =>
          renderEnvironmentalDistinctionCell(_row.testFlag, t)
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
        render: (row: IRecord) => renderStateCell(row.state, t)
      },
      {
        title: t('project.task.submit_time'),
        ...COLUMN_WIDTH_CONFIG['time'],
        key: 'submitTime',
        render: (row: IRecord) => renderTableTime(row.submitTime)
      },
      {
        title: t('project.task.start_time'),
        ...COLUMN_WIDTH_CONFIG['time'],
        key: 'startTime',
        render: (row: IRecord) => renderTableTime(row.startTime)
      },
      {
        title: t('project.task.end_time'),
        ...COLUMN_WIDTH_CONFIG['time'],
        key: 'endTime',
        render: (row: IRecord) => renderTableTime(row.endTime)
      },
      {
        title: t('project.task.duration'),
        key: 'duration',
        ...COLUMN_WIDTH_CONFIG['duration'],
        render: (row: any) => h('span', null, row.duration ? row.duration : '-')
      },
      {
        title: t('project.task.retry_count'),
        key: 'retryTimes',
        ...COLUMN_WIDTH_CONFIG['times']
      },
      {
        title: t('project.task.dry_run_flag'),
        key: 'dryRun',
        ...COLUMN_WIDTH_CONFIG['dryRun'],
        render: (row: IRecord) => (row.dryRun === 1 ? 'YES' : 'NO')
      },
      {
        title: t('project.task.host'),
        key: 'host',
        ...COLUMN_WIDTH_CONFIG['name'],
        render: (row: IRecord) => row.host || '-'
      },
      {
        title: t('project.task.operation'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['operation'](3),
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
                        disabled: !(
                          row.state === 'FAILURE' ||
                          row.state === 'NEED_FAULT_TOLERANCE' ||
                          row.state === 'KILL'
                        ),
                        onClick: () => {
                          handleForcedSuccess(row)
                        }
                      },
                      {
                        icon: () =>
                          h(NIcon, null, {
                            default: () => h(CheckCircleOutlined)
                          })
                      }
                    ),
                  default: () => t('project.task.forced_success')
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

  const handleForcedSuccess = (row: any) => {
    forceSuccess({ id: row.id }, { projectCode }).then(() => {
      getTableData({
        pageSize: variables.pageSize,
        pageNo:
          variables.tableData.length === 1 && variables.page > 1
            ? variables.page - 1
            : variables.page,
        searchVal: variables.searchVal,
        processInstanceId: variables.processInstanceId,
        host: variables.host,
        stateType: variables.stateType,
        datePickerRange: variables.datePickerRange,
        executorName: variables.executorName,
        processInstanceName: variables.processInstanceName
      })
    })
  }

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const data = {
      pageSize: params.pageSize,
      pageNo: params.pageNo,
      searchVal: params.searchVal,
      processInstanceId: params.processInstanceId,
      host: params.host,
      stateType: params.stateType,
      startDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[0]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      endDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[1]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      executorName: params.executorName,
      processInstanceName: params.processInstanceName
    }

    const { state } = useAsyncState(
      queryTaskListPaging(data, { projectCode }).then(
        (res: TaskInstancesRes) => {
          variables.tableData = res.totalList as IRecord[]
          variables.totalPage = res.totalPage
          variables.loadingRef = false
        }
      ),
      {}
    )

    return state
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
