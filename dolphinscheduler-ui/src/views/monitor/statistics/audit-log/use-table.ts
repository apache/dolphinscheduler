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
import { reactive, ref } from 'vue'
import { useAsyncState } from '@vueuse/core'
import { queryAuditLogListPaging } from '@/service/modules/audit'
import { format } from 'date-fns'
import { parseTime } from '@/common/common'
import type { AuditListRes } from '@/service/modules/audit/types'

export function useTable() {
  const { t } = useI18n()

  const variables = reactive({
    columns: [],
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    resourceType: ref(null),
    operationType: ref(null),
    userName: ref(null),
    datePickerRange: ref(null),
    totalPage: ref(1),
    loadingRef: ref(false)
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: '#',
        key: 'index',
        render: (row: any, index: number) => index + 1
      },
      {
        title: t('monitor.audit_log.user_name'),
        key: 'userName'
      },
      {
        title: t('monitor.audit_log.resource_type'),
        key: 'resource'
      },
      {
        title: t('monitor.audit_log.project_name'),
        key: 'resourceName'
      },
      {
        title: t('monitor.audit_log.operation_type'),
        key: 'operation'
      },
      {
        title: t('monitor.audit_log.create_time'),
        key: 'time'
      }
    ]
  }

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const data = {
      pageSize: params.pageSize,
      pageNo: params.pageNo,
      resourceType: params.resourceType,
      operationType: params.operationType,
      userName: params.userName,
      startDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[0]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      endDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[1]), 'yyyy-MM-dd HH:mm:ss')
        : ''
    }

    const { state } = useAsyncState(
      queryAuditLogListPaging(data).then((res: AuditListRes) => {
        variables.tableData = res.totalList.map((item, unused) => {
          return {
            ...item
          }
        }) as any
        variables.loadingRef = false
      }),
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
