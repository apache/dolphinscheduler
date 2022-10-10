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
import { useDeployMode, useResources, useCustomParams } from '.'
import type { IJsonItem } from '../types'

export function useSeaTunnel(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const configEditorSpan = computed(() => (model.useCustom ? 24 : 0))
  const resourceEditorSpan = computed(() => (model.useCustom ? 0 : 24))
  const flinkSpan = computed(() => (model.engine === 'FLINK' ? 24 : 0))
  const deployModeSpan = computed(() => (model.engine === 'SPARK' ? 24 : 0))
  const masterSpan = computed(() =>
    model.engine === 'SPARK' && model.deployMode !== 'local' ? 12 : 0
  )
  const masterUrlSpan = computed(() =>
    model.engine === 'SPARK' &&
    model.deployMode !== 'local' &&
    (model.master === 'SPARK' || model.master === 'MESOS')
      ? 12
      : 0
  )

  return [
    {
      type: 'select',
      field: 'engine',
      span: 12,
      name: t('project.node.engine'),
      options: ENGINE,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        message: t('project.node.engine_tips')
      }
    },

    // SeaTunnel flink parameter
    {
      type: 'select',
      field: 'runMode',
      name: t('project.node.run_mode'),
      options: FLINK_RUN_MODE,
      value: model.runMode,
      span: flinkSpan
    },
    {
      type: 'input',
      field: 'others',
      name: t('project.node.option_parameters'),
      span: flinkSpan,
      props: {
        type: 'textarea',
        placeholder: t('project.node.option_parameters_tips')
      }
    },

    // SeaTunnel spark parameter
    useDeployMode(deployModeSpan),
    {
      type: 'select',
      field: 'master',
      name: t('project.node.sea_tunnel_master'),
      options: masterTypeOptions,
      value: model.master,
      span: masterSpan
    },
    {
      type: 'input',
      field: 'masterUrl',
      name: t('project.node.sea_tunnel_master_url'),
      value: model.masterUrl,
      span: masterUrlSpan,
      props: {
        placeholder: t('project.node.sea_tunnel_master_url_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: masterUrlSpan.value !== 0,
        validator(validate: any, value: string) {
          if (masterUrlSpan.value !== 0 && !value) {
            return new Error(t('project.node.sea_tunnel_master_url_tips'))
          }
        }
      }
    },

    // SeaTunnel config parameter
    {
      type: 'switch',
      field: 'useCustom',
      name: t('project.node.custom_config')
    },
    {
      type: 'editor',
      field: 'rawScript',
      name: t('project.node.script'),
      span: configEditorSpan,
      validate: {
        trigger: ['input', 'trigger'],
        required: model.useCustom,
        validator(validate: any, value: string) {
          if (model.useCustom && !value) {
            return new Error(t('project.node.script_tips'))
          }
        }
      }
    },
    useResources(resourceEditorSpan, true, 1),
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
  ]
}

export const ENGINE = [
  {
    label: 'SPARK',
    value: 'SPARK'
  },
  {
    label: 'FLINK',
    value: 'FLINK'
  }
]

export const FLINK_RUN_MODE = [
  {
    label: 'run',
    value: 'RUN'
  },
  {
    label: 'run-application',
    value: 'RUN_APPLICATION'
  }
]

export const masterTypeOptions = [
  {
    label: 'yarn',
    value: 'YARN'
  },
  {
    label: 'local',
    value: 'LOCAL'
  },
  {
    label: 'spark://',
    value: 'SPARK'
  },
  {
    label: 'mesos://',
    value: 'MESOS'
  }
]
