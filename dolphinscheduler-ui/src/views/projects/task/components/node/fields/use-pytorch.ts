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
import { watch, ref } from 'vue'
import { useCustomParams, useResources } from '.'

export function usePytorch(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const isCreateEnvironmentSpan = ref(0)
  const pythonPathSpan = ref(0)
  const pythonEnvToolSpan = ref(0)
  const pythonCommandSpan = ref(0)
  const requirementsSpan = ref(0)
  const condaPythonVersionSpan = ref(0)

  const setFlag = () => {
    model.showCreateEnvironment = model.isCreateEnvironment && model.otherParams
    model.showCreateConda =
      model.showCreateEnvironment && model.pythonEnvTool === 'conda'
        ? true
        : false
    model.showCreateEnv =
      model.showCreateEnvironment && model.pythonEnvTool === 'virtualenv'
        ? true
        : false
  }

  const resetSpan = () => {
    isCreateEnvironmentSpan.value = model.otherParams ? 12 : 0
    pythonPathSpan.value = model.otherParams ? 24 : 0
    pythonEnvToolSpan.value = model.showCreateEnvironment ? 12 : 0
    pythonCommandSpan.value =
      ~model.showCreateEnvironment & model.otherParams ? 12 : 0
    requirementsSpan.value = model.showCreateEnvironment ? 24 : 0
    condaPythonVersionSpan.value = model.showCreateConda ? 24 : 0
  }

  watch(
    () => [model.isCreateEnvironment, model.pythonEnvTool, model.otherParams],
    () => {
      setFlag()
      resetSpan()
    }
  )

  return [
    {
      type: 'input',
      field: 'script',
      name: t('project.node.pytorch_script'),
      span: 24
    },
    {
      type: 'input',
      field: 'scriptParams',
      name: t('project.node.pytorch_script_params'),
      span: 24
    },
    {
      type: 'switch',
      field: 'otherParams',
      name: t('project.node.pytorch_other_params'),
      span: 24
    },
    {
      type: 'input',
      field: 'pythonPath',
      name: t('project.node.pytorch_python_path'),
      span: pythonPathSpan
    },
    {
      type: 'switch',
      field: 'isCreateEnvironment',
      name: t('project.node.pytorch_is_create_environment'),
      span: isCreateEnvironmentSpan
    },
    {
      type: 'input',
      field: 'pythonCommand',
      name: t('project.node.pytorch_python_command'),
      span: pythonCommandSpan,
      props: {
        placeholder: t('project.node.pytorch_python_command_tips')
      }
    },
    {
      type: 'select',
      field: 'pythonEnvTool',
      name: t('project.node.pytorch_python_env_tool'),
      span: pythonEnvToolSpan,
      options: PYTHON_ENV_TOOL
    },
    {
      type: 'input',
      field: 'requirements',
      name: t('project.node.pytorch_requirements'),
      span: requirementsSpan
    },
    {
      type: 'input',
      field: 'condaPythonVersion',
      name: t('project.node.pytorch_conda_python_version'),
      span: condaPythonVersionSpan,
      props: {
        placeholder: t('project.node.pytorch_conda_python_version_tips')
      }
    },
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}

export const PYTHON_ENV_TOOL = [
  {
    label: 'conda',
    value: 'conda'
  },
  {
    label: 'virtualenv',
    value: 'virtualenv'
  }
]
