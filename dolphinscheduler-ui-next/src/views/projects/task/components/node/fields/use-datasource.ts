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

import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryDataSourceList } from '@/service/modules/data-source'
import type { IJsonItem } from '../types'
import { TypeReq } from '@/service/modules/data-source/types'
import { find } from 'lodash'

export function useDatasource(
  model: { [field: string]: any },
  field?: string
): IJsonItem {
  const { t } = useI18n()

  const options = ref([] as { label: string; value: string }[])
  const loading = ref(false)
  const defaultValue = ref(null)

  const getDatasources = async () => {
    if (loading.value) return
    loading.value = true
    await refreshOptions()
    loading.value = false
  }

  const refreshOptions = async () => {
    const params = { type: model.type } as TypeReq
    const res = await queryDataSourceList(params)
    defaultValue.value = null
    options.value = []

    res.map((item: any) => {
      options.value.push({ label: item.name, value: String(item.id) })
    })
    if (options.value && model.datasource) {
      const item = find(options.value, { value: String(model.datasource) })
      if (!item) {
        model.datasource = null
      }
    }
  }

  watch(
    () => model.type,
    () => {
      if (model.type) {
        refreshOptions()
      }
    }
  )

  onMounted(() => {
    getDatasources()
  })
  return {
    type: 'select',
    field: field ? field : 'datasource',
    span: 12,
    name: t('project.node.datasource_instances'),
    props: {
      loading: loading
    },
    options: options,
    validate: {
      trigger: ['input', 'blur'],
      required: true
    }
  }
}
