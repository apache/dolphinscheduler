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

import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  COLUMN_WIDTH_CONFIG,
  calculateTableWidth,
  DefaultTableWidth
} from '@/common/column-width-config'
import type { TableColumns } from '../types'

// const { t } = useI18n()
const PERM_LIST = [
  {
    label: 'project.list.no_permission',
    value: 0
  },
  {
    label: 'project.list.read_permission',
    value: 2
  },
  {
    label: 'project.list.all_permission',
    value: 7
  }
]

export function useColumns() {
  const { t } = useI18n()

  const columnsRef = ref({
    columns: [] as TableColumns,
    tableWidth: DefaultTableWidth
  })

  const createColumns = () => {
    const columns: any = [
      {
        type: 'selection',
        key: 'selection',
        ...COLUMN_WIDTH_CONFIG['checkbox']
      },
      {
        title: t('project.list.project_name'),
        key: 'name',
        ...COLUMN_WIDTH_CONFIG['size']
      },
      {
        title: t('project.list.authorize_level'),
        key: 'perm',
        render: (record: any): any => {
          return PERM_LIST.filter((item) => item.value == record.perm).map(
            (item) => t(item.label)
          )
        },
        ...COLUMN_WIDTH_CONFIG['index']
      }
    ]
    columnsRef.value = {
      columns,
      tableWidth: calculateTableWidth(columns)
    }
  }

  onMounted(() => {
    createColumns()
  })

  watch(useI18n().locale, () => {
    createColumns()
  })

  return {
    columnsRef,
    createColumns
  }
}
