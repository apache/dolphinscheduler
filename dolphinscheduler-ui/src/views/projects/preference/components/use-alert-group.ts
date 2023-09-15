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
import type { IJsonItem } from '../../task/components/node/types'
import { listAlertGroupById } from '@/service/modules/alert-group'

export function useAlertGroup(): IJsonItem {
  const { t } = useI18n()

  const options = ref([] as { label: string; value: string }[])

  const getAlertGroups = async () => {
    const res = await listAlertGroupById()
    options.value = res.map((item: any) => ({
      label: item.groupName,
      value: item.id
    }))
  }

  onMounted(() => {
    getAlertGroups()
  })

  return {
    type: 'select',
    field: 'alertGroups',
    span: 12,
    name: t('project.workflow.alarm_group'),
    options: options
  }
}
