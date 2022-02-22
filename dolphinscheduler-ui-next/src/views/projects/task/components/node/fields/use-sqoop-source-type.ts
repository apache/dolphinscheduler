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

import { ref, h, watch, computed, unref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDatasource } from './use-sqoop-datasource'
import { useCustomParams } from '.'
import styles from '../index.module.scss'
import type { IJsonItem, IOption, ModelType } from '../types'

export function useSourceType(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const unCustomSpan = computed(() => (model.isCustomTask ? 0 : 24))
  const tableSpan = computed(() =>
    model.sourceType === 'MYSQL' && model.srcQueryType === '0' ? 24 : 0
  )
  const editorSpan = computed(() =>
    model.sourceType === 'MYSQL' && model.srcQueryType === '1' ? 24 : 0
  )
  const columnSpan = computed(() =>
    model.sourceType === 'MYSQL' && model.srcColumnType === '1' ? 24 : 0
  )
  const mysqlSpan = computed(() => (model.sourceType === 'MYSQL' ? 24 : 0))
  const hiveSpan = computed(() => (model.sourceType === 'HIVE' ? 24 : 0))
  const hdfsSpan = computed(() => (model.sourceType === 'HDFS' ? 24 : 0))
  const datasourceSpan = computed(() => (model.sourceType === 'MYSQL' ? 12 : 0))

  const sourceTypes = ref([
    {
      label: 'MYSQL',
      value: 'MYSQL'
    }
  ] as IOption[])

  const getSourceTypesByModelType = (modelType: ModelType): IOption[] => {
    switch (modelType) {
      case 'import':
        return [
          {
            label: 'MYSQL',
            value: 'MYSQL'
          }
        ]
      case 'export':
        return [
          {
            label: 'HDFS',
            value: 'HDFS'
          },
          {
            label: 'HIVE',
            value: 'HIVE'
          }
        ]
      default:
        return [
          {
            label: 'MYSQL',
            value: 'MYSQL'
          },
          {
            label: 'HDFS',
            value: 'HDFS'
          },
          {
            label: 'HIVE',
            value: 'HIVE'
          }
        ]
    }
  }

  watch(
    () => model.modelType,
    (modelType: ModelType) => {
      getSourceTypesByModelType(modelType)
    }
  )

  return [
    {
      type: 'custom',
      field: 'custom-title',
      span: unCustomSpan,
      widget: h(
        'div',
        { class: styles['field-title'] },
        t('project.node.data_source')
      )
    },
    {
      type: 'select',
      field: 'sourceType',
      name: t('project.node.type'),
      span: unCustomSpan,
      options: sourceTypes
    },
    ...useDatasource(
      model,
      datasourceSpan,
      'sourceMysqlType',
      'sourceMysqlDatasource'
    ),
    {
      type: 'radio',
      field: 'srcQueryType',
      name: t('project.node.model_type'),
      span: mysqlSpan,
      options: [
        {
          label: t('project.node.form'),
          value: '0'
        },
        {
          label: 'SQL',
          value: '1'
        }
      ]
    },
    {
      type: 'input',
      field: 'srcTable',
      name: t('project.node.table'),
      span: tableSpan,
      props: {
        placeholder: t('project.node.table_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: !!unref(tableSpan),
        validator(validate, value) {
          if (!!unref(tableSpan) && !value) {
            return new Error(t('project.node.table_tips'))
          }
        }
      }
    },
    {
      type: 'radio',
      field: 'srcColumnType',
      name: t('project.node.column_type'),
      span: tableSpan,
      options: [
        { label: t('project.node.all_columns'), value: '0' },
        { label: t('project.node.some_columns'), value: '1' }
      ]
    },
    {
      type: 'input',
      field: 'srcColumns',
      name: t('project.node.column'),
      span: columnSpan,
      props: {
        placeholder: t('project.node.column_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: !!unref(columnSpan),
        validator(validate, value) {
          if (!!unref(columnSpan) && !value) {
            return new Error(t('project.node.column_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'sourceHiveDatabase',
      name: t('project.node.database'),
      span: hiveSpan,
      props: {
        placeholder: t('project.node.database_tips')
      },
      validate: {
        trigger: ['blur', 'input'],
        required: !!unref(hiveSpan),
        validator(validate, value) {
          if (!!unref(hiveSpan) && !value) {
            return new Error(t('project.node.database_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'sourceHiveTable',
      name: t('project.node.table'),
      span: hiveSpan,
      props: {
        placeholder: t('project.node.hive_table_tips')
      },
      validate: {
        trigger: ['blur', 'input'],
        required: !!unref(hiveSpan),
        validator(validate, value) {
          if (!!unref(hiveSpan) && !value) {
            return new Error(t('project.node.hive_table_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'sourceHivePartitionKey',
      name: t('project.node.hive_partition_keys'),
      span: hiveSpan,
      props: {
        placeholder: t('project.node.hive_partition_keys_tips')
      }
    },
    {
      type: 'input',
      field: 'sourceHivePartitionValue',
      name: t('project.node.hive_partition_values'),
      span: hiveSpan,
      props: {
        placeholder: t('project.node.hive_partition_values_tips')
      }
    },
    {
      type: 'input',
      field: 'sourceHdfsExportDir',
      name: t('project.node.export_dir'),
      span: hdfsSpan,
      props: {
        placeholder: t('project.node.export_dir_tips')
      },
      validate: {
        trigger: ['blur', 'input'],
        required: !!unref(hdfsSpan),
        validator(validate, value) {
          if (!!unref(hdfsSpan) && !value) {
            return new Error(t('project.node.export_dir_tips'))
          }
        }
      }
    },
    {
      type: 'editor',
      field: 'sourceMysqlSrcQuerySql',
      name: t('project.node.sql_statement'),
      span: editorSpan,
      validate: {
        trigger: ['blur', 'input'],
        required: !!unref(editorSpan),
        validator(validate, value) {
          if (!!unref(editorSpan) && !value) {
            return new Error(t('project.node.sql_statement_tips'))
          }
        }
      }
    },
    ...useCustomParams({
      model,
      field: 'mapColumnHive',
      name: 'map_column_hive',
      isSimple: true,
      span: editorSpan
    }),
    ...useCustomParams({
      model,
      field: 'mapColumnJava',
      name: 'map_column_java',
      isSimple: true,
      span: editorSpan
    })
  ]
}
