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
import { useAsyncState } from '@vueuse/core'
import { queryProjectListPaging } from '@/service/modules/projects'
import { format } from 'date-fns'
import { useRouter } from 'vue-router'
import TableAction from './components/table-action'
import styles from './index.module.scss'
import type { Router } from 'vue-router'
import type { TableColumns } from 'naive-ui/es/data-table/src/interface'
import type { ProjectRes } from '@/service/modules/projects/types'
import { useMenuStore } from '@/store/menu/menu'

export function useTable(
  updateProjectItem = (
    code: number,
    name: string,
    description: string
  ): void => {},
  resetTableData = () => {}
) {
  const { t } = useI18n()
  const router: Router = useRouter()
  const menuStore = useMenuStore()

  const columns: TableColumns<any> = [
    { title: '#', key: 'index' },
    {
      title: t('project.list.project_name'),
      key: 'name',
      render: (row) =>
        h(
          'a',
          {
            class: styles.links,
            onClick: () => {
              menuStore.setProjectCode(row.code)
              router.push({ path: `/projects/${row.code}` })
            }
          },
          {
            default: () => {
              return row.name
            }
          }
        )
    },
    { title: t('project.list.owned_users'), key: 'userName' },
    { title: t('project.list.workflow_define_count'), key: 'defCount' },
    {
      title: t('project.list.process_instance_running_count'),
      key: 'instRunningCount'
    },
    { title: t('project.list.description'), key: 'description' },
    { title: t('project.list.create_time'), key: 'createTime' },
    { title: t('project.list.update_time'), key: 'updateTime' },
    {
      title: t('project.list.operation'),
      key: 'actions',
      render: (row: any) =>
        h(TableAction, {
          row,
          onResetTableData: () => {
            if (variables.page > 1 && variables.tableData.length === 1) {
              variables.page -= 1
            }
            resetTableData()
          },
          onUpdateProjectItem: (code, name, description) =>
            updateProjectItem(code, name, description)
        })
    }
  ]

  const variables = reactive({
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(null),
    totalPage: ref(1)
  })

  const getTableData = (params: any) => {
    const { state } = useAsyncState(
      queryProjectListPaging(params).then((res: ProjectRes) => {
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item, index) => {
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
        }) as any
      }),
      {}
    )
    return state
  }

  return { getTableData, variables, columns }
}
