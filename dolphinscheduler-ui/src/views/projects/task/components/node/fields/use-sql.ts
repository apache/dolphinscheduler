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
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'

export function useSql(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const hiveSpan = computed(() => (model.type === 'HIVE' ? 24 : 0))
  const showScriptEditor = computed(
    () => model.sqlSource === 'SCRIPT' || !model.sqlSource
  )

  return [
    {
      type: 'input',
      field: 'connParams',
      name: t('project.node.sql_parameter'),
      props: {
        placeholder:
          t('project.node.format_tips') + ' key1=value1;key2=value2...'
      },
      span: hiveSpan
    },
    {
      type: 'radio',
      field: 'sqlSource',
      name: t('project.node.sql_source'),
      options: [
        {
          label: t('project.node.sql_source_script'),
          value: 'SCRIPT'
        },
        {
          label: t('project.node.sql_source_file'),
          value: 'FILE'
        }
      ],
      span: 24
    },
    {
      type: 'editor',
      field: 'sql',
      name: t('project.node.sql_statement'),
      if: showScriptEditor,
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.sql_empty_tips')
      },
      props: {
        language: 'sql'
      }
    },
    {
      type: 'tree-select',
      field: 'sqlResource',
      name: t('project.node.sql_resource_file'),
      span: 24,
      if: () => model.sqlSource === 'FILE',
      props: {
        placeholder: t('project.node.resources_tips'),
        keyField: 'fullName',
        labelField: 'name',
        disabledField: 'disable'
      },
      slots: {
        // 这里占位，真正的资源数据从全局资源 store 中获取，保持与 use-resources 一致行为
      }
    },
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: model.readonly
    }),
    {
      type: 'multi-input',
      field: 'preStatements',
      name: t('project.node.pre_sql_statement'),
      span: 22,
      props: {
        placeholder: t('project.node.sql_input_placeholder'),
        type: 'textarea',
        autosize: { minRows: 1 }
      }
    },
    {
      type: 'multi-input',
      field: 'postStatements',
      name: t('project.node.post_sql_statement'),
      span: 22,
      props: {
        placeholder: t('project.node.sql_input_placeholder'),
        type: 'textarea',
        autosize: { minRows: 1 }
      }
    }
  ]
}
