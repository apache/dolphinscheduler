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

import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'

export function useName(from?: number): IJsonItem {
  const { t } = useI18n()
  return {
    type: 'input',
    field: 'name',
    class: 'input-node-name',
    name: from === 1 ? t('project.node.task_name') : t('project.node.name'),
    props: {
      placeholder: t('project.node.name_tips'),
      maxLength: 100
    },
    validate: {
      trigger: ['input', 'blur'],
      required: true,
      message: t('project.node.name_tips')
    }
  }
}
