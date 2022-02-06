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
import { queryRuleListPaging } from '@/service/modules/data-quality'
import { format } from 'date-fns'
import type { Rule, RuleRes } from '@/service/modules/data-quality/types'

export function useTable() {
  const { t } = useI18n()

  const variables = reactive({
    columns: [],
    tableData: [],
    page: ref(1),
    pageSize: ref(10),
    state: ref(null),
    searchVal: ref(null),
    totalPage: ref(1)
  })

  const createColumns = (variables: any) => {
    variables.columns = [
      {
        title: t('data_quality.rule.name'),
        key: 'name'
      },
      {
        title: t('data_quality.rule.type'),
        key: 'type'
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
      }
    ]
  }

  const getTableData = (params: any) => {
    const data = {
      pageSize: params.pageSize,
      pageNo: params.pageNo,
      searchVal: params.searchVal
    }

    const { state } = useAsyncState(
        queryRuleListPaging(data).then((res: RuleRes) => {
        variables.tableData = res.totalList.map((item, index) => {
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

  return {
    t,
    variables,
    getTableData,
    createColumns
  }
}
