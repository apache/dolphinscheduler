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
import type { ITaskRemoteHostOption, IJsonItem } from '../types'
import { queryAllTaskRemoteHostList } from '@/service/modules/task-remote-host'

export function useTaskRemoteHost(
  model: { [field: string]: any },
  isCreate: boolean
): IJsonItem {
  const { t } = useI18n()

  let taskRemoteHostList = [] as ITaskRemoteHostOption[]
  const options = ref([] as ITaskRemoteHostOption[])
  const loading = ref(false)

  const getTaskRemoteHostList = async () => {
    if (loading.value) return
    loading.value = true
    const res = await queryAllTaskRemoteHostList()
    taskRemoteHostList = res.map((item: { code: string; name: string }) => ({
      label: item.name,
      value: item.code
    }))
    options.value = taskRemoteHostList
    loading.value = false
    if (options.value.length === 0) {
      model.remoteHostCode = null
    } else {
      isCreate && (model.remoteHostCode = options.value[0].value)
    }
  }

  onMounted(async () => {
    await getTaskRemoteHostList()
  })

  return {
    type: 'select',
    field: 'remoteHostCode',
    span: 12,
    name: t('project.node.task_remote_host_name'),
    props: {
      loading: loading,
      clearable: true
    },
    options: options
  }
}
