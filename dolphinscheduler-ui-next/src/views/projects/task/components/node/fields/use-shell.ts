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
import { queryResourceList } from '@/service/modules/resources'
import { useCustomParams } from './use-custom-params'
import { removeUselessChildren } from '@/utils/tree-format'
import type { IJsonItem } from '../types'

export function useShell(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const options = ref([])

  const loading = ref(false)

  const getResourceList = async () => {
    if (loading.value) return
    loading.value = true
    const res = await queryResourceList({ type: 'FILE' })
    removeUselessChildren(res)
    options.value = res || []
    loading.value = false
  }

  onMounted(() => {
    getResourceList()
  })

  return [
    {
      type: 'editor',
      field: 'rawScript',
      name: t('project.node.script'),
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.script_tips')
      }
    },
    {
      type: 'tree-select',
      field: 'resourceList',
      name: t('project.node.resources'),
      options,
      props: {
        multiple: true,
        checkable: true,
        cascade: true,
        showPath: true,
        checkStrategy: 'child',
        placeholder: t('project.node.resources_tips'),
        keyField: 'id',
        labelField: 'name',
        loading
      }
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
  ]
}
