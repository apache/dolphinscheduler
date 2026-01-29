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
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'
import { computed } from 'vue'

export function useEmr(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const jobFlowDefineJsonSpan = computed(() =>
    model.programType === 'RUN_JOB_FLOW' ? 24 : 0
  )

  const stepsDefineJsonSpan = computed(() =>
    model.programType === 'ADD_JOB_FLOW_STEPS' ? 24 : 0
  )

  return [
    {
      type: 'select',
      field: 'programType',
      span: 24,
      name: t('project.node.program_type'),
      options: PROGRAM_TYPES,
      validate: {
        required: true
      }
    },
    {
      type: 'editor',
      field: 'jobFlowDefineJson',
      span: jobFlowDefineJsonSpan,
      name: t('project.node.emr_flow_define_json'),
      props: {
        language: 'json'
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.emr_flow_define_json_tips')
      }
    },
    {
      type: 'editor',
      field: 'stepsDefineJson',
      span: stepsDefineJsonSpan,
      name: t('project.node.emr_steps_define_json'),
      props: {
        language: 'json'
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.emr_steps_define_json_tips')
      }
    },
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: true
    })
  ]
}

export const PROGRAM_TYPES = [
  {
    label: 'RUN_JOB_FLOW',
    value: 'RUN_JOB_FLOW'
  },
  {
    label: 'ADD_JOB_FLOW_STEPS',
    value: 'ADD_JOB_FLOW_STEPS'
  }
]
