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

import { ref, onMounted, Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryResourceList } from '@/service/modules/resources'
import { useTaskNodeStore } from '@/store/project/task-node'
import utils from '@/utils'
import type { IJsonItem, IResource } from '../types'

export function useResources(
  span: number | Ref<number> = 24,
  required: boolean | Ref<boolean> = false,
  limit: number | Ref<number> = -1
): IJsonItem {
  const { t } = useI18n()

  const resourcesOptions = ref([] as IResource[])
  const resourcesLoading = ref(false)

  const taskStore = useTaskNodeStore()

  const getResources = async () => {
    if (taskStore.resources.length) {
      resourcesOptions.value = taskStore.resources
      return
    }
    if (resourcesLoading.value) return
    resourcesLoading.value = true
    const res = await queryResourceList({ type: 'FILE' })
    utils.removeUselessChildren(res)
    resourcesOptions.value = res || []
    resourcesLoading.value = false
    taskStore.updateResource(res)
  }

  onMounted(() => {
    getResources()
  })

  return {
    type: 'tree-select',
    field: 'resourceList',
    name: t('project.node.resources'),
    span: span,
    options: resourcesOptions,
    props: {
      multiple: true,
      checkable: true,
      cascade: true,
      showPath: true,
      checkStrategy: 'child',
      placeholder: t('project.node.resources_tips'),
      keyField: 'id',
      labelField: 'name',
      loading: resourcesLoading
    },
    validate: {
      trigger: ['input', 'blur'],
      required: required,
      validator(validate: any, value: IResource[]) {
        if (required) {
          if (!value) {
            return new Error(t('project.node.resources_tips'))
          }

          if (limit > 0 && value.length > limit) {
            return new Error(t('project.node.resources_limit_tips') + limit)
          }
        }
      }
    }
  }
}
