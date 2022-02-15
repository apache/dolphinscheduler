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
import { querySimpleList } from '@/service/modules/process-definition'
import type { IJsonItem } from '../types'

export function useProcessName(
  projectCode: number,
  isCreate: boolean
): IJsonItem {
  const { t } = useI18n()

  const options = ref([] as { label: string; value: string }[])
  const loading = ref(false)

  const getProcessList = async () => {
    if (loading.value) return
    loading.value = true
    try {
      const res = await querySimpleList(projectCode)
      options.value = res.map((option: { name: string; code: number }) => ({
        label: option.name,
        value: option.code
      }))
      loading.value = false
    } catch (err) {
      loading.value = false
    }
  }

  onMounted(() => {
    getProcessList()
  })

  return {
    type: 'select',
    field: 'processCode',
    span: 24,
    name: t('project.node.process_name'),
    props: {
      loading: loading,
      disabled: !isCreate
    },
    options: options
  }
}
