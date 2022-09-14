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
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomParams, useMainJar, useResources } from '.'
import type { IJsonItem } from '../types'

export function useFlinkStream(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const mainArgsSpan = computed(() => (model.programType === 'SQL' ? 0 : 24))

  const scriptSpan = computed(() => (model.programType === 'SQL' ? 24 : 0))

  const deployModeSpan = computed(() => (model.deployMode !== 'local' ? 12 : 0))

  const appNameSpan = computed(() => (model.deployMode !== 'local' ? 24 : 0))

  const deployModeOptions = computed(() => {
    return [
      {
        label: 'yarn per-job',
        value: 'YARN_PER_JOB'
      },
      {
        label: 'yarn application',
        value: 'YARN_APPLICATION'
      }
    ]
  })

  return [
    {
      type: 'select',
      field: 'programType',
      span: 24,
      name: t('project.node.program_type'),
      options: PROGRAM_TYPES,
      props: {
        'on-update:value': () => {
          model.mainJar = null
        }
      }
    },
    useMainJar(model),
    {
      type: 'radio',
      field: 'deployMode',
      name: t('project.node.deploy_mode'),
      options: deployModeOptions,
      span: 24
    },
    {
      type: 'editor',
      field: 'initScript',
      span: scriptSpan,
      name: t('project.node.init_script'),
      validate: {
        trigger: ['input', 'trigger'],
        required: false,
        message: t('project.node.init_script_tips')
      }
    },
    {
      type: 'editor',
      field: 'rawScript',
      span: scriptSpan,
      name: t('project.node.script'),
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.script_tips')
      }
    },
    {
      type: 'input',
      field: 'appName',
      name: t('project.node.app_name'),
      props: {
        placeholder: t('project.node.app_name_tips')
      },
      span: appNameSpan
    },
    {
      type: 'input',
      field: 'jobManagerMemory',
      name: t('project.node.job_manager_memory'),
      span: deployModeSpan,
      props: {
        placeholder: t('project.node.job_manager_memory_tips'),
        min: 1
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: string) {
          if (!value) {
            return
          }
          if (!Number.isInteger(parseInt(value))) {
            return new Error(
              t('project.node.job_manager_memory_tips') +
                t('project.node.positive_integer_tips')
            )
          }
        }
      }
    },
    {
      type: 'input',
      field: 'taskManagerMemory',
      name: t('project.node.task_manager_memory'),
      span: deployModeSpan,
      props: {
        placeholder: t('project.node.task_manager_memory_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: string) {
          if (!value) {
            return
          }
          if (!Number.isInteger(parseInt(value))) {
            return new Error(
              t('project.node.task_manager_memory') +
                t('project.node.positive_integer_tips')
            )
          }
        }
      },
      value: model.taskManagerMemory
    },
    {
      type: 'input-number',
      field: 'slot',
      name: t('project.node.slot_number'),
      span: deployModeSpan,
      props: {
        placeholder: t('project.node.slot_number_tips'),
        min: 1
      },
      value: model.slot
    },
    {
      type: 'input-number',
      field: 'taskManager',
      name: t('project.node.task_manager_number'),
      span: 12,
      props: {
        placeholder: t('project.node.task_manager_number_tips'),
        min: 1
      },
      value: model.taskManager
    },
    {
      type: 'input-number',
      field: 'parallelism',
      name: t('project.node.parallelism'),
      span: 12,
      props: {
        placeholder: t('project.node.parallelism_tips'),
        min: 1
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.parallelism_tips'))
          }
        }
      },
      value: model.parallelism
    },
    {
      type: 'input',
      field: 'mainArgs',
      span: mainArgsSpan,
      name: t('project.node.main_arguments'),
      props: {
        type: 'textarea',
        placeholder: t('project.node.main_arguments_tips')
      }
    },
    {
      type: 'input',
      field: 'others',
      name: t('project.node.option_parameters'),
      props: {
        type: 'textarea',
        placeholder: t('project.node.option_parameters_tips')
      }
    },
    useResources(),
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: true
    })
  ]
}

const PROGRAM_TYPES = [
  {
    label: 'JAVA',
    value: 'JAVA'
  },
  {
    label: 'SCALA',
    value: 'SCALA'
  }
]
