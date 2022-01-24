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
import { NSpace, NTooltip, NButton, NPopconfirm } from 'naive-ui'
import { EditOutlined, DeleteOutlined } from '@vicons/antd'
import { useAsyncState } from '@vueuse/core'
import {
  queryUdfFuncListPaging,
  deleteUdfFunc
} from '@/service/modules/resources'
import { queryListPaging } from '@/service/modules/process-definition'
import TableAction from './components/table-action'

import { IDefinitionParam } from './types'

export function useTable() {
  const { t } = useI18n()
  const router: Router = useRouter()

  const columns: TableColumns<any> = [
    {
      title: t('project.workflow.id'),
      key: 'id',
      width: 50,
      render: (_row, index) => index + 1
    },
    {
      title: t('project.workflow.workflow_name'),
      key: 'name'
    },
    {
      title: t('project.workflow.status'),
      key: 'releaseState'
    },
    {
      title: t('project.workflow.create_time'),
      key: 'createTime'
    },
    {
      title: t('project.workflow.update_time'),
      key: 'updateTime'
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
      key: 'scheduleReleaseState'
    },
    {
      title: t('project.workflow.operation'),
      key: 'operation',
      render: (row) =>
        h(TableAction, {
          row
        })
    }
  ]

  const variables = reactive({
    columns,
    row: {},
    tableData: [],
    projectCode: ref(Number(router.currentRoute.value.params.projectCode)),
    page: ref(1),
    pageSize: ref(10),
    searchVal: ref(),
    totalPage: ref(1),
    showRef: ref(false)
  })

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

  const handleEdit = (row: any) => {
    variables.showRef = true
    variables.row = row
  }

  const handleDelete = (id: number) => {
    /* after deleting data from the current page, you need to jump forward when the page is empty. */
    if (variables.tableData.length === 1 && variables.page > 1) {
      variables.page -= 1
    }

    deleteUdfFunc(id).then(() =>
      getTableData({
        id: variables.id,
        pageSize: variables.pageSize,
        pageNo: variables.page,
        searchVal: variables.searchVal
      })
    )
  }

  return {
    variables,
    getTableData
  }
}
