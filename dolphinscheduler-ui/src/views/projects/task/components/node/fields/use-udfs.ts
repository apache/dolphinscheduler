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
import { ref, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryUdfFuncList } from '@/service/modules/resources'
import type { IJsonItem } from '../types'

export function useUdfs(model: { [field: string]: any }): IJsonItem {
  const { t } = useI18n()
  const options = ref([])
  const loading = ref(false)
  const span = computed(() => (['HIVE', 'SPARK'].includes(model.type) ? 24 : 0))

  const getUdfs = async () => {
    if (loading.value) return
    loading.value = true
    const res = await queryUdfFuncList({ type: model.type })
    options.value = res.map((udf: { id: number; funcName: string }) => ({
      value: String(udf.id),
      label: udf.funcName
    }))
    loading.value = false
  }

  watch(
    () => model.type,
    (value) => {
      if (['HIVE', 'SPARK'].includes(value)) {
        getUdfs()
      }
    }
  )

  return {
    type: 'select',
    field: 'udfs',
    options: options,
    name: t('project.node.udf_function'),
    props: {
      multiple: true,
      loading
    },
    span
  }
}
