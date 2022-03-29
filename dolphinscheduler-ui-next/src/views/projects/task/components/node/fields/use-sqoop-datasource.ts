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

import { onMounted, ref, Ref } from 'vue'
import { queryDataSourceList } from '@/service/modules/data-source'
import { useI18n } from 'vue-i18n'
import type { IJsonItem, IDataBase } from '../types'

export function useDatasource(
  model: { [field: string]: any },
  span: Ref,
  fieldType: string,
  fieldDatasource: string
): IJsonItem[] {
  const { t } = useI18n()
  const dataSourceList = ref([])
  const loading = ref(false)

  const getDataSource = async (type: IDataBase) => {
    if (loading.value) return
    loading.value = true
    const result = await queryDataSourceList({ type })
    dataSourceList.value = result.map((item: { name: string; id: number }) => ({
      label: item.name,
      value: item.id
    }))
    loading.value = false
  }
  onMounted(() => {
    getDataSource('MYSQL')
  })

  return [
    {
      type: 'select',
      field: fieldType,
      name: t('project.node.datasource'),
      span: span,
      options: [{ label: 'MYSQL', value: 'MYSQL' }],
      validate: {
        required: true
      }
    },
    {
      type: 'select',
      field: fieldDatasource,
      name: ' ',
      span: span,
      props: {
        placeholder: t('project.node.datasource_tips'),
        filterable: true,
        loading
      },
      options: dataSourceList,
      validate: {
        trigger: ['blur', 'input'],
        validator(validate, value) {
          if (!value) {
            return new Error(t('project.node.datasource_tips'))
          }
        }
      }
    }
  ]
}
