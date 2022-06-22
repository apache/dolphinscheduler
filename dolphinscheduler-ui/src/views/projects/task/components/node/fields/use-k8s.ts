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
import { useCustomParams, useNamespace } from '.'
import type { IJsonItem } from '../types'
import { useI18n } from 'vue-i18n'

export function useK8s(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    useNamespace(),
    {
      type: 'input-number',
      field: 'minCpuCores',
      span: 12,
      props: {
        min: 0
      },
      name: t('project.node.min_cpu'),
      slots: {
        suffix: () => t('project.node.cores')
      }
    },
    {
      type: 'input-number',
      field: 'minMemorySpace',
      span: 12,
      props: {
        min: 0
      },
      name: t('project.node.min_memory'),
      slots: {
        suffix: () => t('project.node.mb')
      }
    },
    {
      type: 'input',
      field: 'image',
      name: t('project.node.image'),
      props: {
        placeholder: t('project.node.image_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        message: t('project.node.min_memory_tips')
      }
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
  ]
}
