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
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  useCustomParams,
  useDeployMode,
  useDriverCores,
  useDriverMemory,
  useExecutorNumber,
  useExecutorMemory,
  useExecutorCores,
  useMainJar,
  useNamespace,
  useResources,
  useYarnQueue
} from '.'
import type { IJsonItem } from '../types'

export function useSpark(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const mainClassSpan = computed(() =>
    model.programType === 'PYTHON' || model.programType === 'SQL' ? 0 : 24
  )

  const mainArgsSpan = computed(() => (model.programType === 'SQL' ? 0 : 24))

  const rawScriptSpan = computed(() => (model.programType === 'SQL' && model.sqlExecutionType === 'SCRIPT' ? 24 : 0))

  const showCluster = computed(() => model.programType !== 'SQL')

  const resourcesRequired = ref(
    model.programType === 'SQL' && model.sqlExecutionType === 'FILE'
  )

  const resourcesLimit = computed(() =>
    model.programType === 'SQL' && model.sqlExecutionType === 'FILE' ? 1 : -1
  )

  const sqlExecutionTypeSpan = computed(() => (model.programType === 'SQL' ? 12 : 0))

  const SQL_EXECUTION_TYPES = [
    {
      label: t('project.node.sql_execution_type_from_script'),
      value: 'SCRIPT'
    },
    {
      label: t('project.node.sql_execution_type_from_file'),
      value: 'FILE'
    }
  ]

  watch(
    () => [model.sqlExecutionType, model.programType],
    () => {
      resourcesRequired.value =
        model.programType === 'SQL' && model.sqlExecutionType === 'FILE'
    }
  )

  return [
    {
      type: 'select',
      field: 'programType',
      span: 12,
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
      type: 'select',
      field: 'sqlExecutionType',
      span: sqlExecutionTypeSpan,
      name: t('project.node.sql_execution_type'),
      options: SQL_EXECUTION_TYPES,
      validate: {
        trigger: ['input', 'blur'],
        required: true
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
      type: 'editor',
      field: 'rawScript',
      span: rawScriptSpan,
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
    useDeployMode(24, ref(true), showCluster),
    useNamespace(),
    {
      type: 'input',
      field: 'appName',
      name: t('project.node.app_name'),
      props: {
        placeholder: t('project.node.app_name_tips')
      }
    },
    useDriverCores(),
    useDriverMemory(),
    useExecutorNumber(),
    useExecutorMemory(),
    useExecutorCores(),
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
    useResources(24, resourcesRequired, resourcesLimit),
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}

export const PROGRAM_TYPES = [
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
