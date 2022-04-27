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

import { h, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { format } from 'date-fns'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import {
  queryTaskGroupListPaging,
  queryTaskListInTaskGroupQueueById
} from '@/service/modules/task-group'
import TableAction from './components/table-action'
import _ from 'lodash'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import { parseTime } from '@/common/common'

export function useTable(
  updatePriority = (unusedQueueId: number, unusedPriority: number): void => {},
  resetTableData = () => {}
) {
  const { t } = useI18n()

  const columns: TableColumns<any> = [
    {
      title: '#',
      key: 'index',
      render: (row, index) => index + 1,
      ...COLUMN_WIDTH_CONFIG['index']
    },
    {
      title: t('resource.task_group_queue.project_name'),
      key: 'projectName',
      ...COLUMN_WIDTH_CONFIG['name']
    },
    {
      title: t('resource.task_group_queue.task_name'),
      key: 'taskName',
      ...COLUMN_WIDTH_CONFIG['name']
    },
    {
      title: t('resource.task_group_queue.workflow_instance_name'),
      key: 'processInstanceName',
      ...COLUMN_WIDTH_CONFIG['name']
    },
    {
      title: t('resource.task_group_queue.task_group_name'),
      key: 'taskGroupName',
      ...COLUMN_WIDTH_CONFIG['name']
    },
    {
      title: t('resource.task_group_queue.priority'),
      key: 'priority',
      width: 120
    },
    {
      title: t('resource.task_group_queue.force_starting_status'),
      key: 'forceStart',
      ...COLUMN_WIDTH_CONFIG['state']
    },
    {
      title: t('resource.task_group_queue.in_queue'),
      key: 'inQueue',
      width: 120
    },
    {
      title: t('resource.task_group_queue.task_status'),
      key: 'status',
      ...COLUMN_WIDTH_CONFIG['state']
    },
    {
      title: t('resource.task_group_queue.create_time'),
      key: 'createTime',
      ...COLUMN_WIDTH_CONFIG['time']
    },
    {
      title: t('resource.task_group_queue.update_time'),
      key: 'updateTime',
      ...COLUMN_WIDTH_CONFIG['time']
    },
    {
      title: t('resource.task_group_queue.actions'),
      key: 'actions',
      ...COLUMN_WIDTH_CONFIG['operation'](2),
      render: (row: any) =>
        h(TableAction, {
          row,
          onResetTableData: () => {
            if (variables.page > 1 && variables.tableData.length === 1) {
              variables.page -= 1
            }
            resetTableData()
          },
          onUpdatePriority: (queueId: number, priority: number) => {
            updatePriority(queueId, priority)
          }
        })
    }
  ]

  const variables = reactive({
    tableData: [],
    tableWidth: calculateTableWidth(columns) || DefaultTableWidth,
    page: ref(1),
    pageSize: ref(10),
    groupId: ref(3),
    totalPage: ref(1),
    loadingRef: ref(false)
  })

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const taskGroupSearchParams = {
      pageNo: 1,
      pageSize: 2147483647
    }
    Promise.all([
      queryTaskListInTaskGroupQueueById(params),
      queryTaskGroupListPaging(taskGroupSearchParams)
    ]).then((values: any[]) => {
      const taskGroupList = values[1].totalList
      variables.totalPage = values[0].totalPage
      variables.tableData = values[0].totalList.map(
        (item: any, unused: number) => {
          let taskGroupName = ''
          if (taskGroupList) {
            const taskGroup = _.find(taskGroupList, { id: item.groupId })
            if (taskGroup) {
              taskGroupName = taskGroup.name
            }
          }

          item.taskGroupName = taskGroupName
          item.createTime = format(
            parseTime(item.createTime),
            'yyyy-MM-dd HH:mm:ss'
          )
          item.updateTime = format(
            parseTime(item.updateTime),
            'yyyy-MM-dd HH:mm:ss'
          )
          return {
            ...item
          }
        }
      )
      variables.loadingRef = false
    })
  }

  return { getTableData, variables, columns }
}
