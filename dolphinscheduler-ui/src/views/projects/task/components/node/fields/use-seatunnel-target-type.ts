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
import { useDeployMode, useResources, useCustomParams } from '.'
import type { IJsonItem } from '../types'
import { indexOf, find } from 'lodash'
import { queryDataSourceList, getDatasourceTablesById, queryDataSource } from '@/service/modules/data-source'
import type { TypeReq } from '@/service/modules/data-source/types'
import styles from '../index.module.scss'


export function useSeaTunnelTargetType(
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

  const targetDbTypeSPan = computed(() => (useCustomSpan.value ? 12 : 0))
  const targetDatabaseSpan = computed(() => (useCustomSpan.value && model.targetDbType !== 'HDFS' ? 12 : 0))
  const targetFilePathSpan = computed(() => (useCustomSpan.value && model.targetDbType === 'HDFS' ? 24 : 0))
  const customFileFormatSpan = computed(() => (useCustomSpan.value && model.targetDbType === 'HDFS' ? 6 : 0))
  const targetTableSpan = computed(() => (useCustomSpan.value && model.targetDbType !== 'HDFS' ? 12 : 0))
  const defaultFsSpan = computed(() => (useCustomSpan.value && model.targetDbType === 'HDFS' ? 24 : 0))
  
  const options = ref([] as { label: string; value: string }[])
  const targetDatasourceOptions = ref([] as { label: string; value: number }[])
  const targetTableOptions = ref([] as { label: string; value: string }[])

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

  const refreshTargetDbOptions = async () => {
    const parameters = { type: model[params.typeField || 'targetDbType'] }
    const targetField = 'targetDatabase'

    if (!parameters.type || parameters.type === 'HDFS') {
        model[targetField] = null
        return ;
    }
    const res = await queryDataSourceList(parameters)
    targetDatasourceOptions.value = res.map((item: any) => ({
      label: item.name,
      value: item.id
    }))
    if (!res.length && model[targetField]) model[targetField] = null
    if (res.length && model[targetField]) {
      const item = find(res, { id: model[targetField] })
      if (!item) {
        model[targetField] = null
      }
    }

    if (!model[targetField]) {
      refreshTargetTableOptions()
    }
  }

  const refreshTargetTableOptions = async () => {
    const parameters = model[params.typeField || 'targetDatabase'] 
    const targetField = 'targetTable'

    if (!parameters) {
        model[targetField] = null
        return ;
    }

    let database = ""
    const dataSourceRes = await queryDataSource(parameters)
    if (dataSourceRes) {
        database = dataSourceRes.database
    }
    const tableRes = await getDatasourceTablesById(parameters, database)
    targetTableOptions.value = tableRes.map((item: any) => ({
      label: item.label,
      value: item.value
    }))

    if (!tableRes.length && model[targetField]) model[targetField] = null
    if (tableRes.length && model[targetField]) {
      const item = find(tableRes, { label: model[targetField] })
      if (!item) {
        model[targetField] = null
      }
    }
  }

  const onTargetChange = () => {
    refreshTargetDbOptions()
  }

  const onTargetTableChange = () => {
    refreshTargetTableOptions()
  }

  onMounted(async () => {
    getDatasourceTypes()
    await nextTick()
    refreshTargetDbOptions()
    refreshTargetTableOptions()
  })

  return [
    {
      type: 'custom',
      field: 'custom-title-target',
      span: useCustomSpan,
      widget: h(
        'div',
        { class: styles['field-title'] },
        t('project.node.sea_tunnel_custom_title_target')
      )
    },
    {
      type: 'select',
      field: 'targetDbType',
      span: targetDbTypeSPan,
      name: t('project.node.sea_tunnel_target_datasource_type'),
      props: {
        'on-update:value': onTargetChange
      },
      options: options,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.sea_tunnel_target_datasource_type'))
          }
        }
      }
    },
    {
      type: 'select',
      field: 'targetDatabase',
      span: targetDatabaseSpan,
      name: t('project.node.sea_tunnel_target_datasource_instances'),
      options: targetDatasourceOptions,
      props: {
        'on-update:value': onTargetTableChange
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.sea_tunnel_target_datasource_instances'))
          }
        }
      }
    },
    {
      type: 'select',
      field: 'targetTable',
      span: targetTableSpan,
      name: t('project.node.sea_tunnel_target_table_name'),
      options: targetTableOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value) {
          if (!value && value !== 0) {
            return Error(t('project.node.sea_tunnel_target_table_name'))
          }
        }
      }
    },
    {
      type: 'select',
      field: 'targetFileFormat',
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
      field: 'targetDefaultFs',
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
      field: 'targetFilePath',
      span: targetFilePathSpan,
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
        field: 'targetCustomParams',
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
    disabled: false,
  },
  {
    id: 2,
    code: 'DORIS',
    disabled: false
  },
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
  },
]
