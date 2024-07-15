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

import { ref, onMounted, nextTick, Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryDataSourceList } from '@/service/modules/data-source'
import { indexOf, find } from 'lodash'
import type { IJsonItem } from '../types'
import type { TypeReq } from '@/service/modules/data-source/types'

export function useDatasource(
  model: { [field: string]: any },
  params: {
    supportedDatasourceType?: string[]
    typeField?: string
    sourceField?: string
    span?: Ref | number
    testFlag?: Ref | number
  } = {}
): IJsonItem[] {
  const { t } = useI18n()

  const options = ref([] as { label: string; value: string }[])
  const datasourceOptions = ref([] as { label: string; value: number }[])

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
      id: 10,
      code: 'REDSHIFT',
      disabled: false
    },
    {
      id: 11,
      code: 'ATHENA',
      disabled: false
    },
    {
      id: 12,
      code: 'TRINO',
      disabled: false
    },
    {
      id: 13,
      code: 'STARROCKS',
      disabled: false
    },
    {
      id: 14,
      code: 'AZURESQL',
      disabled: false
    },
    {
      id: 15,
      code: 'DAMENG',
      disabled: false
    },
    {
      id: 16,
      code: 'OCEANBASE',
      disabled: false
    },
    {
      id: 17,
      code: 'SSH',
      disabled: true
    },
    {
      id: 18,
      code: 'KYUUBI',
      disabled: false
    },
    {
      id: 19,
      code: 'DATABEND',
      disabled: false
    },
    {
      id: 21,
      code: 'VERTICA',
      disabled: false
    },
    {
      id: 22,
      code: 'HANA',
      disabled: false
    },
    {
      id: 23,
      code: 'DORIS',
      disabled: false
    },
    {
      id: 24,
      code: 'ZEPPELIN',
      disabled: false
    },
    {
      id: 25,
      code: 'SAGEMAKER',
      disabled: false
    },
    {
      id: 26,
      code: 'DOLPHINDB',
      disabled: false
    }
  ]

  const getDatasourceTypes = async () => {
    options.value = datasourceTypes
      .filter((item) => {
        if (item.disabled) {
          return false
        }
        if (params.supportedDatasourceType) {
          return indexOf(params.supportedDatasourceType, item.code) !== -1
        }
        return true
      })
      .map((item) => ({ label: item.code, value: item.code }))
  }

  const refreshOptions = async () => {
    const parameters = {
      type: model[params.typeField || 'type'],
      testFlag: 0
    } as TypeReq
    const res = await queryDataSourceList(parameters)
    datasourceOptions.value = res.map((item: any) => ({
      label: item.name,
      value: item.id
    }))
    const sourceField = params.sourceField || 'datasource'
    if (!res.length && model[sourceField]) model[sourceField] = null
    if (res.length && model[sourceField]) {
      const item = find(res, { id: model[sourceField] })
      if (!item) {
        model[sourceField] = null
      }
    }
  }

  const onChange = () => {
    refreshOptions()
  }

  onMounted(async () => {
    getDatasourceTypes()
    await nextTick()
    refreshOptions()
  })
  return [
    {
      type: 'select',
      field: params.typeField || 'type',
      span: params.span || 12,
      name: t('project.node.datasource_type'),
      props: {
        'on-update:value': onChange
      },
      options: options,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'select',
      field: params.sourceField || 'datasource',
      span: params.span || 12,
      name: t('project.node.datasource_instances'),
      options: datasourceOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.datasource_instances'))
          }
        }
      }
    }
  ]
}
