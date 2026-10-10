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
import { useFlinkSqlGatewayFields } from './use-flink-sql-gateway'
import type { IJsonItem } from '../types'

export function useFlink(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const gateway = useFlinkSqlGatewayFields(model)
  const isSqlGateway = computed(
    () => model.programType === 'SQL' && model.sqlSubmitType === 'SQL_GATEWAY'
  )
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

  const deployModeSpan = computed(() =>
    isSqlGateway.value || model.deployMode === 'local' ? 0 : 12
  )

  const appNameSpan = computed(() =>
    isSqlGateway.value || model.deployMode === 'local' ? 0 : 24
  )
  const deployModeRadioSpan = computed(() => (isSqlGateway.value ? 0 : 24))
  const parallelismSpan = computed(() => (isSqlGateway.value ? 0 : 12))
  const othersSpan = computed(() => (isSqlGateway.value ? 0 : 24))
  const resourceSpan = computed(() => {
    if (!isSqlGateway.value) {
      return 24
    }
    return model.rawScriptType === 'FILE' ? 24 : 0
  })
  const resourceRequired = computed(
    () => isSqlGateway.value && model.rawScriptType === 'FILE'
  )
  const resourceLimit = computed(() =>
    isSqlGateway.value && model.rawScriptType === 'FILE' ? 1 : -1
  )

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
    gateway.submitType,
    useMainJar(model),
    {
      type: 'radio',
      field: 'deployMode',
      name: t('project.node.deploy_mode'),
      options: deployModeOptions,
      span: deployModeRadioSpan
    },
    ...gateway.beforeInit,
    {
      type: 'editor',
      field: 'initScript',
      span: scriptSpan,
      name: t('project.node.init_script'),
      props: {
        language: 'sql',
        readOnly: gateway.initScriptReadOnly
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: false,
        message: t('project.node.init_script_tips')
      }
    },
    ...gateway.beforeScript,
    {
      type: 'editor',
      field: 'rawScript',
      span: scriptSpan,
      name: t('project.node.script'),
      props: {
        language: 'sql',
        readOnly: gateway.scriptReadOnly
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
      span: parallelismSpan,
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
    { ...useYarnQueue(), span: computed(() => (isSqlGateway.value ? 0 : 12)) },
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
      span: othersSpan,
      props: {
        type: 'textarea',
        placeholder: t('project.node.option_parameters_tips')
      }
    },
    useResources(resourceSpan, resourceRequired, resourceLimit),
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
