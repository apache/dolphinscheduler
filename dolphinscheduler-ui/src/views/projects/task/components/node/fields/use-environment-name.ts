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
import { queryAllEnvironmentList } from '@/service/modules/environment'
import type { IEnvironmentNameOption, IJsonItem } from '../types'

export function useEnvironmentName(
  model: { [field: string]: any },
  isCreate: boolean
): IJsonItem {
  const { t } = useI18n()

  let environmentList = [] as IEnvironmentNameOption[]
  const options = ref([] as IEnvironmentNameOption[])
  const loading = ref(false)
  let mounted = false

  const getEnvironmentList = async () => {
    if (loading.value) return
    loading.value = true
    const res = await queryAllEnvironmentList()
    environmentList = res.map(
      (item: { code: string; name: string; workerGroups: string[] }) => ({
        label: item.name,
        value: item.code,
        workerGroups: item.workerGroups
      })
    )
    options.value = environmentList.filter((option: IEnvironmentNameOption) =>
      filterByWorkerGroup(option)
    )
    loading.value = false
    if (options.value.length === 0) {
      model.environmentCode = null
    } else {
      isCreate &&
        !model.environmentCode &&
        (model.environmentCode = options.value[0].value)
    }
  }

  const filterByWorkerGroup = (option: IEnvironmentNameOption) => {
    if (!model.workerGroup) return false
    if (!option?.workerGroups?.length) return false
    return option.workerGroups.indexOf(model.workerGroup) !== -1
  }

  watch(
    () => model.workerGroup,
    () => {
      if (!model.workerGroup || !mounted) return
      options.value = environmentList.filter((option: IEnvironmentNameOption) =>
        filterByWorkerGroup(option)
      )
      if (model?.environmentCode) {
        if (options.value) {
          let elementExists =
            options.value.find(
              (item) => item.value === model.environmentCode
            ) !== undefined
          if (!elementExists) {
            model.environmentCode = null
          }
        }
      } else {
        model.environmentCode =
          options.value.length === 0 ? null : options.value[0].value
      }
    }
  )

  onMounted(async () => {
    await getEnvironmentList()
    mounted = true
  })

  return {
    type: 'select',
    field: 'environmentCode',
    span: 12,
    name: t('project.node.environment_name'),
    props: {
      loading: loading,
      clearable: true
    },
    options: options
  }
}
