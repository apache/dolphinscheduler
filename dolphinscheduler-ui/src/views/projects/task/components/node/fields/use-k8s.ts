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
import { useCustomParams, useCustomLabels, useNodeSelectors } from '.'
import type { IJsonItem } from '../types'
import { useI18n } from 'vue-i18n'
import { onMounted, ref, watch } from 'vue'

export function useK8s(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  // default width for
  const yamlEditorSpan = ref(0)
  const nodeSelectorSpan = ref(24)
  const customLabelsSpan = ref(24)
  const inputCommandSpan = ref(24)
  const inputArgsSpan = ref(24)
  const inputPullSecretSpan = ref(24)
  const inputImageSpan = ref(17)
  const selectImagePullPolicySpan = ref(7)
  const inputNumberMinCpuCoresSpan = ref(12)
  const inputNumberMinMemorySpace = ref(12)
  const localParamsSpan = ref(24)

  const initConstants = () => {
    if (model.customConfig) {
      // when user selects 'Custom Template' option, display yamlEditor and hide low-code fields
      yamlEditorSpan.value = 24
      nodeSelectorSpan.value = 0
      customLabelsSpan.value = 0
      inputCommandSpan.value = 0
      inputArgsSpan.value = 0
      inputPullSecretSpan.value = 0
      inputImageSpan.value = 0
      selectImagePullPolicySpan.value = 0
      inputNumberMinCpuCoresSpan.value = 0
      inputNumberMinMemorySpace.value = 0
      localParamsSpan.value = 0
    } else {
      yamlEditorSpan.value = 0
      nodeSelectorSpan.value = 24
      customLabelsSpan.value = 24
      inputCommandSpan.value = 24
      inputArgsSpan.value = 24
      inputPullSecretSpan.value = 24
      inputImageSpan.value = 17
      selectImagePullPolicySpan.value = 7
      inputNumberMinCpuCoresSpan.value = 12
      inputNumberMinMemorySpace.value = 12
      localParamsSpan.value = 24
    }
  }

  onMounted(() => {
    initConstants()
  })
  watch(
    () => model.customConfig,
    () => {
      initConstants()
    }
  )

  return [
    {
      type: 'switch',
      field: 'customConfig',
      name: t('project.node.k8s_custom_template')
    },
    {
      type: 'editor',
      field: 'yamlContent',
      name: t('project.node.k8s_yaml_template'),
      span: yamlEditorSpan,
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.k8s_yaml_empty_tips')
      }
    },
    {
      type: 'input-number',
      field: 'minCpuCores',
      span: inputNumberMinCpuCoresSpan,
      props: {
        min: 0
      },
      name: t('project.node.min_cpu'),
      slots: {
        suffix: () => t('project.node.cores')
      }
    },
    {
      type: 'input-number',
      field: 'minMemorySpace',
      span: inputNumberMinMemorySpace,
      props: {
        min: 0
      },
      name: t('project.node.min_memory'),
      slots: {
        suffix: () => t('project.node.mb')
      }
    },
    {
      type: 'input',
      field: 'image',
      name: t('project.node.image'),
      span: inputImageSpan,
      props: {
        placeholder: t('project.node.image_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        message: t('project.node.image_tips')
      }
    },
    {
      type: 'select',
      field: 'imagePullPolicy',
      name: t('project.node.image_pull_policy'),
      span: selectImagePullPolicySpan,
      options: IMAGE_PULL_POLICY_LIST,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        message: t('project.node.image_pull_policy_tips')
      },
      value: 'IfNotPresent'
    },
    {
      type: 'input',
      field: 'pullSecret',
      name: t('project.node.pull_secret'),
      span: inputPullSecretSpan,
      props: {
        placeholder: t('project.node.pull_secret_tips')
      }
    },
    {
      type: 'input',
      field: 'command',
      name: t('project.node.command'),
      span: inputCommandSpan,
      props: {
        placeholder: t('project.node.command_tips')
      }
    },
    {
      type: 'input',
      field: 'args',
      name: t('project.node.args'),
      span: inputArgsSpan,
      props: {
        placeholder: t('project.node.args_tips')
      }
    },
    ...useCustomLabels({
      model,
      field: 'customizedLabels',
      name: 'custom_labels',
      span: customLabelsSpan
    }),
    ...useNodeSelectors({
      model,
      field: 'nodeSelectors',
      name: 'node_selectors',
      span: nodeSelectorSpan
    }),
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: false,
      span: localParamsSpan
    })
  ]
}

export const IMAGE_PULL_POLICY_LIST = [
  {
    value: 'IfNotPresent',
    label: 'IfNotPresent'
  },
  {
    value: 'Always',
    label: 'Always'
  },
  {
    value: 'Never',
    label: 'Never'
  }
]
