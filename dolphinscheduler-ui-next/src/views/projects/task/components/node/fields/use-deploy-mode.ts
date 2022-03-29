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

export function useDeployMode(span = 24, showClient = true): IJsonItem {
  const { t } = useI18n()

  return {
    type: 'radio',
    field: 'deployMode',
    name: t('project.node.deploy_mode'),
    options: DEPLOY_MODES.filter((option) =>
      option.value === 'client' ? showClient : true
    ),
    span
  }
}

export const DEPLOY_MODES = [
  {
    label: 'cluster',
    value: 'cluster'
  },
  {
    label: 'client',
    value: 'client'
  },
  {
    label: 'local',
    value: 'local'
  }
]
