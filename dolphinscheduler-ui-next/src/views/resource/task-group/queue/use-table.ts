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

import { useAsyncState, useAsyncQueue } from '@vueuse/core'
import { h, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { format } from 'date-fns'
import { useRouter } from 'vue-router'
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { queryTaskGroupListPaging, queryTaskListInTaskGroupQueueById } from '@/service/modules/task-group'
import { queryAllProjectList } from '@/service/modules/projects'
import TableAction from './components/table-action'
import _ from 'lodash'

export function useTable(
  updateItem = (
    id: number,
    name: string,
    projectCode: number,
    groupSize: number,
    description: string,
    status: number
  ): void => {},
  resetTableData = () => {}
) {
  const { t } = useI18n()
  const router: Router = useRouter()

  const columns: TableColumns<any> = [
    { title: t('resource.task_group_queue.id'), key: 'index' },
    { title: t('resource.task_group_queue.project_name'), key: 'projectName' },
    { title: t('resource.task_group_queue.task_name'), key: 'taskName' },
    { title: t('resource.task_group_queue.task_name'), key: 'processInstanceName' },
    { title: t('resource.task_group_queue.task_name'), key: 'taskGroupName' },
    { title: t('resource.task_group_queue.task_name'), key: 'priority' },
    { title: t('resource.task_group_queue.task_name'), key: 'forceStart' },
    { title: t('resource.task_group_queue.task_name'), key: 'inQueue' },
    { title: t('resource.task_group_queue.task_name'), key: 'status' },
    { title: t('resource.task_group_queue.create_time'), key: 'createTime' },
    { title: t('resource.task_group_queue.update_time'), key: 'updateTime' },
    {
      title: t('resource.task_group_queue.actions'),
      key: 'actions',
      width: 150,
      render: (row: any) =>
        h(TableAction, {
          row,
          onResetTableData: () => {
            if (variables.page > 1 && variables.tableData.length === 1) {
              variables.page -= 1
            }
            resetTableData()
          },
          onUpdateItem: (
            id: number,
            name: string,
            projectCode: number,
            groupSize: number,
            description: string,
            status: number
          ) => {
            updateItem(id, name, projectCode, groupSize, description, status)
          }
        })
    }
  ]

  const variables = reactive({
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    name: ref(null),
    totalPage: ref(1)
  })

  const getTableData = (params: any) => {
    Promise.all([queryTaskGroupListPaging(params), queryAllProjectList()]).then(
      (values: any[]) => {
        variables.totalPage = values[0].totalPage
        variables.tableData = values[0].totalList.map(
          (item: any, index: number) => {
            item.projectName = _.find(values[1], {
              code: item.projectCode
            }).name
            item.createTime = format(
              new Date(item.createTime),
              'yyyy-MM-dd HH:mm:ss'
            )
            item.updateTime = format(
              new Date(item.updateTime),
              'yyyy-MM-dd HH:mm:ss'
            )
            return {
              index: index + 1,
              ...item
            }
          }
        )
      }
    )
  }

  return { getTableData, variables, columns }
}
