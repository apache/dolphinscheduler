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

import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'
import { indexOf } from 'lodash'

export function useDatasourceType(
  model: { [field: string]: any },
  supportedDatasourceType?: string[],
  field?: string
): IJsonItem {
  const { t } = useI18n()

  const options = ref([] as { label: string; value: string }[])
  const loading = ref(false)

  const datasourceTypes = [
    {
      id: 0,
      code: 'MYSQL',
      disabled: false
    },
    {
      id: 1,
      code: 'POSTGRESQL',
      disabled: false
    },
    {
      id: 2,
      code: 'HIVE',
      disabled: false
    },
    {
      id: 3,
      code: 'SPARK',
      disabled: false
    },
    {
      id: 4,
      code: 'CLICKHOUSE',
      disabled: false
    },
    {
      id: 5,
      code: 'ORACLE',
      disabled: false
    },
    {
      id: 6,
      code: 'SQLSERVER',
      disabled: false
    },
    {
      id: 7,
      code: 'DB2',
      disabled: false
    },
    {
      id: 8,
      code: 'PRESTO',
      disabled: false
    },
    {
      id: 9,
      code: 'REDSHIFT',
      disabled: false
    }
  ]

  const getDatasourceTypes = async () => {
    if (loading.value) return
    loading.value = true
    options.value = datasourceTypes
      .filter((item) => {
        if (item.disabled) {
          return false
        }
        if (supportedDatasourceType) {
          return indexOf(supportedDatasourceType, item.code) !== -1
        }
        return true
      })
      .map((item) => ({ label: item.code, value: item.code }))
    loading.value = false
  }

  const onChange = (type: string) => {
    if (field) {
      model[field] = type
    } else {
      model.type = type
    }
  }

  onMounted(() => {
    getDatasourceTypes()
  })
  return {
    type: 'select',
    field: field ? field : 'datasourceType',
    span: 12,
    name: t('project.node.datasource_type'),
    props: {
      loading: loading,
      'on-update:value': onChange
    },
    options: options,
    validate: {
      trigger: ['input', 'blur'],
      required: true
    },
    value: model.type
  }
}
