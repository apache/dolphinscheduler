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
import { queryRuleListPaging } from '@/service/modules/data-quality'
import type { RuleRes } from '@/service/modules/data-quality/types'
import TableAction from './components/table-action'
import _ from 'lodash'
import { format } from 'date-fns'
import { TableColumns } from 'naive-ui/es/data-table/src/interface'
import { parseTime } from '@/common/common'

export function useTable(viewRuleEntry = (unusedRuleJson: string): void => {}) {
  const { t } = useI18n()

  const variables = reactive({
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    state: ref(null),
    searchVal: ref(null),
    totalPage: ref(1),
    loadingRef: ref(false)
  })

  const columns: TableColumns<any> = [
    {
      title: t('data_quality.rule.name'),
      key: 'ruleName'
    },
    {
      title: t('data_quality.rule.type'),
      key: 'ruleTypeName'
    },
    {
      title: t('data_quality.rule.username'),
      key: 'userName'
    },
    {
      title: t('data_quality.rule.create_time'),
      key: 'createTime'
    },
    {
      title: t('data_quality.rule.update_time'),
      key: 'updateTime'
    },
    {
      title: t('data_quality.rule.actions'),
      key: 'actions',
      width: 150,
      render: (row: any) =>
        h(TableAction, {
          row,
          onViewRuleEntry: (ruleJson: string) => {
            viewRuleEntry(ruleJson)
          }
        })
    }
  ]

  const ruleTypeMapping = [
    {
      code: -1,
      label: t('data_quality.rule.all')
    },
    {
      code: 0,
      label: t('data_quality.rule.single_table')
    },
    {
      code: 1,
      label: t('data_quality.rule.custom_sql')
    },
    {
      code: 2,
      label: t('data_quality.rule.multi_table_accuracy')
    },
    {
      code: 3,
      label: t('data_quality.rule.multi_table_value_comparison')
    }
  ]

  const getTableData = (params: any) => {
    if (variables.loadingRef) return
    variables.loadingRef = true
    const data = {
      pageSize: params.pageSize,
      pageNo: params.pageNo,
      searchVal: params.searchVal,
      startDate: params.startDate,
      endDate: params.endDate
    }

    const { state } = useAsyncState(
      queryRuleListPaging(data).then((res: RuleRes) => {
        variables.totalPage = res.totalPage
        variables.tableData = res.totalList.map((item, unused) => {
          const ruleName =
            'data_quality.rule.' + item.name.substring(3, item.name.length - 1)
          const ruleNameLocale = t(ruleName)

          const ruleType = _.find(ruleTypeMapping, { code: item.type })

          let ruleTypeName = ''

          if (ruleType) {
            ruleTypeName = ruleType.label
          }

          item.createTime = format(
            parseTime(item.createTime),
            'yyyy-MM-dd HH:mm:ss'
          )
          item.updateTime = format(
            parseTime(item.updateTime),
            'yyyy-MM-dd HH:mm:ss'
          )

          return {
            ...item,
            ruleName: ruleNameLocale,
            ruleTypeName: ruleTypeName
          }
        }) as any

        variables.loadingRef = false
      }),
      {}
    )

    return state
  }

  return { t, variables, getTableData, columns }
}
