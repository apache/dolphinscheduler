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

import { ref, onMounted, Ref, isRef } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryResourceList } from '@/service/modules/resources'
import { useTaskNodeStore } from '@/store/project/task-node'
import utils from '@/utils'
import type { IJsonItem, IResource } from '../types'
import { ResourceFileV2 } from '@/service/modules/resources/types'

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
    const res = await queryResourceList({ type: 'FILE', fullName: '' })
    utils.removeUselessChildren(res)
    resourcesOptions.value = res || []
    resourcesLoading.value = false
    taskStore.updateResource(res)
  }

  const validateResourceExist = (
    fullName: string,
    parentDir: string,
    resources: ResourceFileV2[]
  ): boolean => {
    if (resources.length >= 0) {
      for (const res of resources) {
        if (res.dirctory) {
          const isPrefix = new RegExp(`^${res.fullName}`).test(fullName)
          if (!isPrefix) {
            continue
          }
          if (!res.children) {
            res.children = []
          }
          return validateResourceExist(fullName, res.name, res.children)
        }
        if (res.fullName === fullName) {
          return true
        }
      }
    }
    addResourceNode(fullName, parentDir, resources)
    return false
  }

  const addResourceNode = (
    fullName: string,
    parentDir: string,
    resources: ResourceFileV2[]
  ) => {
    const resourceNode = {
      fullName: fullName,
      name: getResourceDirAfter(fullName, parentDir),
      disable: true
    }
    resources.push(resourceNode)
  }

  const getResourceDirAfter = (fullName: string, parentDir: string) => {
    parentDir = '/resources/' + parentDir
    const delimiterIndex = fullName.indexOf(parentDir)
    if (delimiterIndex !== -1) {
      return fullName.substring(delimiterIndex + parentDir.length)
    } else {
      return fullName
    }
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
      filterable: true,
      clearFilterAfterSelect: false,
      checkStrategy: 'child',
      placeholder: t('project.node.resources_tips'),
      keyField: 'fullName',
      labelField: 'name',
      disabledField: 'disable',
      loading: resourcesLoading
    },
    validate: {
      trigger: ['blur'],
      required: required,
      validator(validate: any, value: string[]) {
        if (value) {
          const errorNames: string[] = []
          value.forEach((item) => {
            if (
              !validateResourceExist(
                item,
                '',
                resourcesOptions.value as ResourceFileV2[]
              )
            ) {
              errorNames.push(item)
            }
          })
          if (errorNames.length > 0) {
            let errorName = ': '
            errorNames.forEach((name) => {
              value.splice(value.indexOf(name), 1)
              errorName += getResourceDirAfter(name, '') + ';'
            })
            return new Error(
              t('project.node.useless_resources_tips') + errorName
            )
          }
        }

        if (isRef(required) ? required.value : required) {
          if (!value || value.length == 0) {
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
