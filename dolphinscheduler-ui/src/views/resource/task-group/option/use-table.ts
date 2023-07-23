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
import { queryTaskGroupListPaging } from '@/service/modules/task-group'
import { queryProjectCreatedAndAuthorizedByUser } from '@/service/modules/projects'
import TableAction from './components/table-action'
import _ from 'lodash'
import { parseTime } from '@/common/common'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'

export function useTable(
  updateItem = (
    unusedId: number,
    unusedName: string,
    unusedProjectCode: number,
    unusedGroupSize: number,
    unusedDescription: string,
    unusedStatus: number
  ): void => {},
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
      title: t('resource.task_group_option.name'),
      key: 'name',
      ...COLUMN_WIDTH_CONFIG['name']
    },
    {
      title: t('resource.task_group_option.project_name'),
      key: 'projectName',
      ...COLUMN_WIDTH_CONFIG['name']
    },
    {
      title: t('resource.task_group_option.resource_pool_size'),
      key: 'groupSize',
      width: 160
    },
    {
      title: t('resource.task_group_option.resource_used_pool_size'),
      key: 'useSize',
      width: 140
    },
    {
      title: t('resource.task_group_option.desc'),
      key: 'description',
      ...COLUMN_WIDTH_CONFIG['note']
    },
    {
      title: t('resource.task_group_option.create_time'),
      key: 'createTime',
      ...COLUMN_WIDTH_CONFIG['time']
    },
    {
      title: t('resource.task_group_option.update_time'),
      key: 'updateTime',
      ...COLUMN_WIDTH_CONFIG['time']
    },
    {
      title: t('resource.task_group_option.actions'),
      key: 'actions',
      ...COLUMN_WIDTH_CONFIG['operation'](3),
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
    tableWidth: calculateTableWidth(columns) || DefaultTableWidth,
    page: ref(1),
    pageSize: ref(10),
    name: ref(null),
    totalPage: ref(1),
    loadingRef: ref(false)
  })

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    Promise.all([queryTaskGroupListPaging(params), queryProjectCreatedAndAuthorizedByUser()]).then(
      (values: any[]) => {
        variables.totalPage = values[0].totalPage
        variables.tableData = values[0].totalList.map(
          (item: any, unused: number) => {
            let projectName = ''
            if (values[1]) {
              const project = _.find(values[1], { code: item.projectCode })
              if (project) {
                projectName = project.name
              }
            }

            item.projectName = projectName
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
      }
    )
  }

  return { getTableData, variables, columns }
}
