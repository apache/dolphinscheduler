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
import { computed, ref, Ref, onMounted, nextTick, h } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'
import { indexOf, find } from 'lodash'
import {
  queryDataSourceList,
  getDatasourceTablesById,
  queryDataSource
} from '@/service/modules/data-source'
import styles from '../index.module.scss'

export function useSeaTunnelSourceType(
  model: { [field: string]: any },
  useCustomSpan: Ref<number>,
  params: {
    supportedDatasourceType?: string[]
    typeField?: string
    sourceField?: string
    span?: Ref | number
  } = {}
): IJsonItem[] {
  const { t } = useI18n()

  const sourceTypeSpan = computed(() => (useCustomSpan.value ? 12 : 0))
  const sourceDatabaseSpan = computed(() =>
    useCustomSpan.value && model.sourceType !== 'HDFS' ? 12 : 0
  )
  const filePathSpan = computed(() =>
    useCustomSpan.value && model.sourceType === 'HDFS' ? 24 : 0
  )
  const defaultFsSpan = computed(() =>
    useCustomSpan.value && model.sourceType === 'HDFS' ? 24 : 0
  )

  const customFileFormatSpan = computed(() =>
    useCustomSpan.value && model.sourceType === 'HDFS' ? 6 : 0
  )
  const sourceTableSpan = computed(() =>
    useCustomSpan.value && model.sourceType !== 'HDFS' ? 12 : 0
  )

  const options = ref([] as { label: string; value: string }[])
  const sourceDatasourceOptions = ref([] as { label: string; value: number }[])
  const sourceTableOptions = ref([] as { label: string; value: string }[])

  const customParamsSpan = computed(() => (useCustomSpan.value ? 24 : 0))

  const getDatasourceTypes = async () => {
    options.value = datasourceTypes
      .filter((item) => {
        if (item.disabled) {
          return false
        }
        if (params.supportedDatasourceType) {
          return indexOf(params.supportedDatasourceType, item.code) !== -1
        }
        return true
      })
      .map((item) => ({ label: item.code, value: item.code }))
  }

  const refreshSourceDbOptions = async () => {
    const parameters = { type: model[params.typeField ?? 'sourceType'] }
    const sourceField = 'sourceDatabase'

    if (!parameters.type || parameters.type === 'HDFS') {
      model[sourceField] = null
      return
    }
    const res = await queryDataSourceList(parameters)
    sourceDatasourceOptions.value = res.map((item: any) => ({
      label: item.name,
      value: item.id
    }))
    if (!res.length && model[sourceField]) model[sourceField] = null
    if (res.length && model[sourceField]) {
      const item = find(res, { id: model[sourceField] })
      if (!item) {
        model[sourceField] = null
      }
    }

    if (!model[sourceField]) {
      refreshSourceTableOptions()
    }
  }

  const refreshSourceTableOptions = async () => {
    const parameters = model[params.typeField ?? 'sourceDatabase']
    const sourceField = 'sourceTable'

    if (!parameters) {
      model[sourceField] = null
      return
    }

    let database = ''
    const dataSourceRes = await queryDataSource(parameters)
    if (dataSourceRes) {
      database = dataSourceRes.database
    }
    const tableRes = await getDatasourceTablesById(parameters, database)

    sourceTableOptions.value = tableRes.map((item: any) => ({
      label: item.label,
      value: item.value
    }))
    if (!tableRes.length && model[sourceField]) model[sourceField] = null
    if (tableRes.length && model[sourceField]) {
      const item = find(tableRes, { label: model[sourceField] })
      if (!item) {
        model[sourceField] = null
      }
    }
  }

  const onSourceChange = () => {
    refreshSourceDbOptions()
  }

  const onSourceTableChange = () => {
    refreshSourceTableOptions()
  }

  onMounted(async () => {
    getDatasourceTypes()
    await nextTick()
    refreshSourceDbOptions()
    refreshSourceTableOptions()
  })

  return [
    {
      type: 'custom',
      field: 'custom-title-source',
      span: useCustomSpan,
      widget: h(
        'div',
        { class: styles['field-title'] },
        t('project.node.sea_tunnel_custom_title_source')
      )
    },
    {
      type: 'select',
      field: 'sourceType',
      span: sourceTypeSpan,
      name: t('project.node.sea_tunnel_source_datasource_type'),
      options: options,
      props: {
        'on-update:value': onSourceChange
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.sea_tunnel_source_datasource_type'))
          }
        }
      }
    },
    {
      type: 'select',
      field: 'sourceDatabase',
      span: sourceDatabaseSpan,
      name: t('project.node.sea_tunnel_source_datasource_instances'),
      options: sourceDatasourceOptions,
      props: {
        'on-update:value': onSourceTableChange
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(
              t('project.node.sea_tunnel_source_datasource_instances')
            )
          }
        }
      }
    },
    {
      type: 'select',
      field: 'sourceTable',
      span: sourceTableSpan,
      name: t('project.node.sea_tunnel_source_table_name'),
      options: sourceTableOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.sea_tunnel_source_table_name'))
          }
        }
      }
    },
    {
      type: 'select',
      field: 'sourceFileFormat',
      span: customFileFormatSpan,
      name: t('project.node.sea_tunnel_file_format'),
      options: fileFormatOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.sea_tunnel_file_format'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'sourceDefaultFs',
      span: defaultFsSpan,
      name: t('project.node.sea_tunnel_default_fs'),
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.sea_tunnel_default_fs'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'sourceFilePath',
      span: filePathSpan,
      name: t('project.node.sea_tunnel_file_path'),
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.sea_tunnel_file_path'))
          }
        }
      }
    },
    ...useCustomParams({
      model,
      field: 'sourceCustomParams',
      name: 'sea_tunnel_add_custom_params',
      isSimple: true,
      span: customParamsSpan
    })
  ]
}

export const datasourceTypes = [
  {
    id: 0,
    code: 'MYSQL',
    disabled: false
  },
  {
    id: 1,
    code: 'HDFS',
    disabled: false
  },
  {
    id: 2,
    code: 'DORIS',
    disabled: false
  }
]

export const fileFormatOptions = [
  {
    label: 'text',
    value: 'text'
  },
  {
    label: 'parquet',
    value: 'parquet'
  },
  {
    label: 'orc',
    value: 'orc'
  },
  {
    label: 'csv',
    value: 'csv'
  },
  {
    label: 'json',
    value: 'json'
  },
  {
    label: 'excel',
    value: 'excel'
  },
  {
    label: 'xml',
    value: 'xml'
  },
  {
    label: 'binary',
    value: 'binary'
  }
]
