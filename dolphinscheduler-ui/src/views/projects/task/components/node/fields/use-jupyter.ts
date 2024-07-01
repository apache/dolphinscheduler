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
import { useCustomParams, useResources } from '.'
import type { IJsonItem } from '../types'

export function useJupyter(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'input',
      field: 'condaEnvName',
      name: t('project.node.jupyter_conda_env_name'),
      props: {
        placeholder: t('project.node.jupyter_conda_env_name_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.jupyter_conda_env_name_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'inputNotePath',
      name: t('project.node.jupyter_input_note_path'),
      props: {
        placeholder: t('project.node.jupyter_input_note_path_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.jupyter_input_note_path_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'outputNotePath',
      name: t('project.node.jupyter_output_note_path'),
      props: {
        placeholder: t('project.node.jupyter_output_note_path_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.jupyter_output_note_path_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'parameters',
      name: t('project.node.jupyter_parameters'),
      props: {
        placeholder: t('project.node.jupyter_parameters_tips')
      }
    },
    {
      type: 'input',
      field: 'kernel',
      name: t('project.node.jupyter_kernel'),
      props: {
        placeholder: t('project.node.jupyter_kernel_tips')
      }
    },
    {
      type: 'input',
      field: 'engine',
      name: t('project.node.jupyter_engine'),
      props: {
        placeholder: t('project.node.jupyter_engine_tips')
      }
    },
    {
      type: 'input',
      field: 'executionTimeout',
      name: t('project.node.jupyter_execution_timeout'),
      props: {
        placeholder: t('project.node.jupyter_execution_timeout_tips')
      }
    },
    {
      type: 'input',
      field: 'startTimeout',
      name: t('project.node.jupyter_start_timeout'),
      props: {
        placeholder: t('project.node.jupyter_start_timeout_tips')
      }
    },
    {
      type: 'input',
      field: 'others',
      name: t('project.node.jupyter_others'),
      props: {
        placeholder: t('project.node.jupyter_others_tips')
      }
    },
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}
