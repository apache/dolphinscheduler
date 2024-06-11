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
import { useCustomParams, useResources, useJavaTaskMainJar } from '.'
import type { IJsonItem } from '../types'

export function useJava(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const rawScriptSpan = computed(() => (model.runType === 'JAR' ? 0 : 24))
  return [
    {
      type: 'select',
      field: 'runType',
      span: 12,
      name: t('project.node.run_type'),
      options: RUN_TYPES,
      value: model.runType
    },
    {
      type: 'switch',
      field: 'isModulePath',
      span: 24,
      name: t('project.node.is_module_path'),
      value: model.isModulePath
    },
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
      field: 'jvmArgs',
      name: t('project.node.jvm_args'),
      props: {
        type: 'textarea',
        placeholder: t('project.node.jvm_args_tips')
      }
    },
    useJavaTaskMainJar(model),
    {
      type: 'editor',
      field: 'rawScript',
      span: rawScriptSpan,
      name: t('project.node.script'),
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.script_tips')
      }
    },
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}

export const RUN_TYPES = [
  {
    label: 'JAVA',
    value: 'JAVA'
  },
  {
    label: 'JAR',
    value: 'JAR'
  }
]
