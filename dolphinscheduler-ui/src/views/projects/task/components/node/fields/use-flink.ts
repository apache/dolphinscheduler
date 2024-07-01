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
import { computed, watch, watchEffect } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomParams, useMainJar, useResources, useYarnQueue } from '.'
import type { IJsonItem } from '../types'

export function useFlink(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const mainClassSpan = computed(() =>
    model.programType === 'PYTHON' || model.programType === 'SQL' ? 0 : 24
  )

  const mainArgsSpan = computed(() => (model.programType === 'SQL' ? 0 : 24))

  const scriptSpan = computed(() => (model.programType === 'SQL' ? 24 : 0))

  const flinkVersionOptions = computed(() =>
    model.programType === 'SQL'
      ? [{ label: '>=1.13', value: '>=1.13' }]
      : FLINK_VERSIONS
  )

  const taskManagerNumberSpan = computed(() =>
    model.flinkVersion === '<1.10' && model.deployMode !== 'local' ? 12 : 0
  )

  const deployModeSpan = computed(() => (model.deployMode !== 'local' ? 12 : 0))

  const appNameSpan = computed(() => (model.deployMode !== 'local' ? 24 : 0))

  const deployModeOptions = computed(() => {
    if (model.programType === 'SQL') {
      return [
        {
          label: 'per-job/cluster',
          value: 'cluster'
        },
        {
          label: 'local',
          value: 'local'
        },
        {
          label: 'standalone',
          value: 'standalone'
        }
      ]
    }
    if (model.flinkVersion === '<1.10') {
      return [
        {
          label: 'cluster',
          value: 'cluster'
        },
        {
          label: 'local',
          value: 'local'
        }
      ]
    } else {
      return [
        {
          label: 'per-job/cluster',
          value: 'cluster'
        },
        {
          label: 'application',
          value: 'application'
        },
        {
          label: 'local',
          value: 'local'
        }
      ]
    }
  })

  watch(
    () => model.flinkVersion,
    () => {
      if (
        model.flinkVersion === '<1.10' &&
        model.deployMode === 'application'
      ) {
        model.deployMode = 'cluster'
      }
    }
  )

  watchEffect(() => {
    model.flinkVersion =
      model.programType === 'SQL' ? '>=1.13' : model.flinkVersion
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
          model.mainClass = ''
        }
      }
    },
    {
      type: 'input',
      field: 'mainClass',
      span: mainClassSpan,
      name: t('project.node.main_class'),
      props: {
        placeholder: t('project.node.main_class_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: model.programType !== 'PYTHON' && model.programType !== 'SQL',
        validator(validate: any, value: string) {
          if (
            model.programType !== 'PYTHON' &&
            !value &&
            model.programType !== 'SQL'
          ) {
            return new Error(t('project.node.main_class_tips'))
          }
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
      props: {
        language: 'sql'
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.script_tips')
      }
    },
    {
      type: 'select',
      field: 'flinkVersion',
      name: t('project.node.flink_version'),
      options: flinkVersionOptions,
      value: model.flinkVersion,
      span: deployModeSpan
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
      span: taskManagerNumberSpan,
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
    useYarnQueue(),
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
  },
  {
    label: 'PYTHON',
    value: 'PYTHON'
  },
  {
    label: 'SQL',
    value: 'SQL'
  }
]

const FLINK_VERSIONS = [
  {
    label: '<1.10',
    value: '<1.10'
  },
  {
    label: '1.11',
    value: '1.11'
  },
  {
    label: '>=1.12',
    value: '>=1.12'
  }
]
