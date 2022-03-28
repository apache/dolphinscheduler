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
import {
  useCustomParams,
  useDeployMode,
  useDriverCores,
  useDriverMemory,
  useExecutorNumber,
  useExecutorMemory,
  useExecutorCores,
  useMainJar,
  useResources
} from '.'
import type { IJsonItem } from '../types'

export function useSpark(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const mainClassSpan = computed(() =>
    model.programType === 'PYTHON' ? 0 : 24
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
      field: 'sparkVersion',
      span: 12,
      name: t('project.node.spark_version'),
      options: SPARK_VERSIONS
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
        required: model.programType !== 'PYTHON',
        validator(validate: any, value: string) {
          if (model.programType !== 'PYTHON' && !value) {
            return new Error(t('project.node.main_class_tips'))
          }
        }
      }
    },
    useMainJar(model),
    useDeployMode(),
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
    {
      type: 'input',
      field: 'mainArgs',
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
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
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
  }
]

export const SPARK_VERSIONS = [
  {
    label: 'SPARK2',
    value: 'SPARK2'
  },
  {
    label: 'SPARK1',
    value: 'SPARK1'
  }
]
