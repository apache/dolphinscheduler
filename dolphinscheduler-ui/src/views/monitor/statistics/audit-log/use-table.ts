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
import {
  queryAuditLogListPaging,
  queryAuditLogObjectType,
  queryAuditLogOperationType
} from '@/service/modules/audit'
import { format } from 'date-fns'
import { parseTime } from '@/common/common'
import type {
  AuditListRes,
  AuditObjectTypeItem,
  AuditOperationTypeItem
} from '@/service/modules/audit/types'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import { sortBy } from 'lodash'

export function useTable() {
  const { t } = useI18n()

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    objectType: ref(null),
    operationType: ref(null),
    ObjectTypeData: [],
    OperationTypeData: [],
    userName: ref(null),
    objectName: ref(null),
    datePickerRange: ref(null),
    totalPage: ref(1),
    loadingRef: ref(false)
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
        title: t('monitor.audit_log.user_name'),
        key: 'userName',
        ...COLUMN_WIDTH_CONFIG['userName']
      },
      {
        title: t('monitor.audit_log.object_type'),
        key: 'objectType',
        ...COLUMN_WIDTH_CONFIG['type']
      },
      {
        title: t('monitor.audit_log.object_name'),
        key: 'objectName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('monitor.audit_log.operation_type'),
        key: 'operation',
        ...COLUMN_WIDTH_CONFIG['type']
      },

      {
        title: t('monitor.audit_log.description'),
        key: 'description',
        ...COLUMN_WIDTH_CONFIG['note']
      },
      {
        title: t('monitor.audit_log.latency') + ' (s)',
        key: 'latency',
        ...COLUMN_WIDTH_CONFIG['times']
      },
      {
        title: t('monitor.audit_log.create_time'),
        key: 'time',
        ...COLUMN_WIDTH_CONFIG['time']
      }
    ]

    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const getObjectTypeData = async () => {
    try {
      variables.ObjectTypeData = await queryAuditLogObjectType().then(
        (res: AuditObjectTypeItem[]) => res || []
      )
    } catch {
      variables.ObjectTypeData = []
    }
  }

  const getOperationTypeData = async () => {
    try {
      variables.OperationTypeData = await queryAuditLogOperationType().then(
        (res: AuditOperationTypeItem[]) => sortBy(res, 'name')
      )
    } catch {
      variables.OperationTypeData = []
    }
  }

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const data = {
      pageSize: params.pageSize,
      pageNo: params.pageNo,
      objectTypeCodes: params.objectType,
      operationTypeCodes: params.operationType,
      userName: params.userName,
      objectName: params.objectName,
      startDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[0]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      endDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[1]), 'yyyy-MM-dd HH:mm:ss')
        : ''
    }

    const { state } = useAsyncState(
      queryAuditLogListPaging(data).then((res: AuditListRes) => {
        variables.totalPage = res.totalPage
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
    createColumns,
    getObjectTypeData,
    getOperationTypeData
  }
}
