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
import { queryExecuteResultListPaging } from '@/service/modules/data-quality'
import { format } from 'date-fns'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type {
  ResultItem,
  ResultListRes
} from '@/service/modules/data-quality/types'
import { parseTime } from '@/common/common'
import ButtonLink from '@/components/button-link'
import { NEllipsis, NTag } from 'naive-ui'
import { useRouter } from 'vue-router'

export function useTable() {
  const { t } = useI18n()
  const router = useRouter()

  const variables = reactive({
    columns: [],
    tableWidth: DefaultTableWidth,
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    ruleType: ref(null),
    state: ref(null),
    searchVal: ref(null),
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
        title: t('data_quality.task_result.task_name'),
        key: 'taskName',
        ...COLUMN_WIDTH_CONFIG['userName']
      },
      {
        title: t('data_quality.task_result.workflow_instance'),
        key: 'processInstanceName',
        ...COLUMN_WIDTH_CONFIG['name'],
        render: (row: ResultItem) =>
          h(
            ButtonLink,
            {
              onClick: () =>
                void router.push({
                  name: 'workflow-instance-detail',
                  params: {
                    projectCode: row.projectCode,
                    id: row.processInstanceId
                  },
                  query: { code: row.processDefinitionCode }
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
        title: t('data_quality.task_result.rule_type'),
        key: 'ruleType',
        render: (row: ResultItem) => {
          if (row.ruleType === 0) {
            return t('data_quality.task_result.single_table')
          } else if (row.ruleType === 1) {
            return t('data_quality.task_result.single_table_custom_sql')
          } else if (row.ruleType === 2) {
            return t('data_quality.task_result.multi_table_accuracy')
          } else if (row.ruleType === 3) {
            return t('data_quality.task_result.multi_table_comparison')
          }
        },
        ...COLUMN_WIDTH_CONFIG['ruleType']
      },
      {
        title: t('data_quality.task_result.rule_name'),
        key: 'ruleName',
        ...COLUMN_WIDTH_CONFIG['name']
      },
      {
        title: t('data_quality.task_result.state'),
        key: 'state',
        render: (row: ResultItem) => {
          if (row.state === 0) {
            return h(
              NTag,
              { type: 'info', size: 'small' },
              {
                default: () => t('data_quality.task_result.undone')
              }
            )
          } else if (row.state === 1) {
            return h(
              NTag,
              { type: 'success', size: 'small' },
              {
                default: () => t('data_quality.task_result.success')
              }
            )
          } else if (row.state === 2) {
            return h(
              NTag,
              { type: 'error', size: 'small' },
              {
                default: () => t('data_quality.task_result.failure')
              }
            )
          } else {
            return '-'
          }
        },
        ...COLUMN_WIDTH_CONFIG['state']
      },
      {
        title: t('data_quality.task_result.actual_value'),
        key: 'statisticsValue',
        width: 140
      },
      {
        title: t('data_quality.task_result.excepted_value'),
        key: 'comparisonValue',
        width: 140
      },
      {
        title: t('data_quality.task_result.check_type'),
        key: 'checkType',
        render: (row: ResultItem) => {
          if (row.checkType === 0) {
            return t('data_quality.task_result.expected_and_actual')
          } else if (row.checkType === 1) {
            return t('data_quality.task_result.actual_and_expected')
          } else if (row.checkType === 2) {
            return t('data_quality.task_result.actual_or_expected')
          } else if (row.checkType === 3) {
            return t('data_quality.task_result.expected_and_actual_or_expected')
          }
        },
        ...COLUMN_WIDTH_CONFIG['type']
      },
      {
        title: t('data_quality.task_result.operator'),
        key: 'operator',
        render: (row: ResultItem) => {
          if (row.operator === 0) {
            return '='
          } else if (row.operator === 1) {
            return '<'
          } else if (row.operator === 2) {
            return '<='
          } else if (row.operator === 3) {
            return '>'
          } else if (row.operator === 4) {
            return '>='
          } else if (row.operator === 5) {
            return '!='
          }
        },
        ...COLUMN_WIDTH_CONFIG['userName']
      },
      {
        title: t('data_quality.task_result.threshold'),
        key: 'threshold',
        width: 120
      },
      {
        title: t('data_quality.task_result.failure_strategy'),
        key: 'failureStrategy',
        width: 150,
        render: (row: ResultItem) => {
          if (row.failureStrategy === 0) {
            return 'Alert'
          }
          if (row.failureStrategy === 1) {
            return 'Block'
          }
          return ''
        }
      },
      {
        title: t('data_quality.task_result.excepted_value_type'),
        key: 'comparisonTypeName',
        width: 200
      },
      {
        title: t('data_quality.task_result.error_output_path'),
        key: 'errorOutputPath',
        render: (row: ResultItem) => {
          return row.errorOutputPath ? row.errorOutputPath : '-'
        },
        width: 200
      },
      {
        title: t('data_quality.task_result.username'),
        key: 'userName',
        ...COLUMN_WIDTH_CONFIG['userName']
      },
      {
        title: t('data_quality.task_result.create_time'),
        key: 'createTime',
        ...COLUMN_WIDTH_CONFIG['time']
      },
      {
        title: t('data_quality.task_result.update_time'),
        key: 'updateTime',
        ...COLUMN_WIDTH_CONFIG['time']
      }
    ]
    if (variables.tableWidth) {
      variables.tableWidth = calculateTableWidth(variables.columns)
    }
  }

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const data = {
      pageSize: params.pageSize,
      pageNo: params.pageNo,
      ruleType: params.ruleType,
      state: params.state,
      searchVal: params.searchVal,
      startDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[0]), 'yyyy-MM-dd HH:mm:ss')
        : '',
      endDate: params.datePickerRange
        ? format(parseTime(params.datePickerRange[1]), 'yyyy-MM-dd HH:mm:ss')
        : ''
    }

    const { state } = useAsyncState(
      queryExecuteResultListPaging(data).then((res: ResultListRes) => {
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
    createColumns
  }
}
