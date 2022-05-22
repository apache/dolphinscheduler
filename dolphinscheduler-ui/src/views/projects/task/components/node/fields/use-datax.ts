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
import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { IJsonItem } from '../types'
import { find } from 'lodash'
import { TypeReq } from '@/service/modules/data-source/types'
import { queryDataSourceList } from '@/service/modules/data-source'

export function useDataX(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const datasourceTypes = [
    {
      id: 0,
      code: 'MYSQL',
      disabled: false
    },
    {
      id: 1,
      code: 'POSTGRESQL',
      disabled: false
    },
    {
      id: 2,
      code: 'HIVE',
      disabled: true
    },
    {
      id: 3,
      code: 'SPARK',
      disabled: true
    },
    {
      id: 4,
      code: 'CLICKHOUSE',
      disabled: false
    },
    {
      id: 5,
      code: 'ORACLE',
      disabled: false
    },
    {
      id: 6,
      code: 'SQLSERVER',
      disabled: false
    },
    {
      id: 7,
      code: 'DB2',
      disabled: true
    },
    {
      id: 8,
      code: 'PRESTO',
      disabled: true
    }
  ]
  const datasourceTypeOptions = ref([] as any)
  const datasourceOptions = ref([] as any)
  const destinationDatasourceOptions = ref([] as any)
  const jobSpeedByteOptions: any[] = [
    {
      label: `0(${t('project.node.unlimited')})`,
      value: 0
    },
    {
      label: '1KB',
      value: 1024
    },
    {
      label: '10KB',
      value: 10240
    },
    {
      label: '50KB',
      value: 51200
    },
    {
      label: '100KB',
      value: 102400
    },
    {
      label: '512KB',
      value: 524288
    }
  ]
  const jobSpeedRecordOptions: any[] = [
    {
      label: `0(${t('project.node.unlimited')})`,
      value: 0
    },
    {
      label: '500',
      value: 500
    },
    {
      label: '1000',
      value: 1000
    },
    {
      label: '1500',
      value: 1500
    },
    {
      label: '2000',
      value: 2000
    },
    {
      label: '2500',
      value: 2500
    },
    {
      label: '3000',
      value: 3000
    }
  ]
  const memoryLimitOptions = [
    {
      label: '1G',
      value: 1
    },
    {
      label: '2G',
      value: 2
    },
    {
      label: '3G',
      value: 3
    },
    {
      label: '4G',
      value: 4
    }
  ]
  const loading = ref(false)

  const getDatasourceTypes = async () => {
    if (loading.value) return
    loading.value = true
    datasourceTypeOptions.value = datasourceTypes
      .filter((item) => !item.disabled)
      .map((item) => ({ label: item.code, value: item.code }))
    loading.value = false
  }

  const getDatasourceInstances = async () => {
    const params = { type: model.dsType } as TypeReq
    const res = await queryDataSourceList(params)
    datasourceOptions.value = []
    res.map((item: any) => {
      datasourceOptions.value.push({ label: item.name, value: String(item.id) })
    })
    if (datasourceOptions.value && model.dataSource) {
      const item = find(datasourceOptions.value, {
        value: String(model.dataSource)
      })
      if (!item) {
        model.dataSource = null
      }
    }
  }

  const getDestinationDatasourceInstances = async () => {
    const params = { type: model.dtType } as TypeReq
    const res = await queryDataSourceList(params)
    destinationDatasourceOptions.value = []
    res.map((item: any) => {
      destinationDatasourceOptions.value.push({
        label: item.name,
        value: String(item.id)
      })
    })
    if (destinationDatasourceOptions.value && model.dataTarget) {
      const item = find(destinationDatasourceOptions.value, {
        value: String(model.dataTarget)
      })
      if (!item) {
        model.dataTarget = null
      }
    }
  }

  const sqlEditorSpan = ref(24)
  const jsonEditorSpan = ref(0)
  const datasourceSpan = ref(12)
  const destinationDatasourceSpan = ref(8)
  const otherStatementSpan = ref(22)
  const jobSpeedSpan = ref(12)
  const customParameterSpan = ref(0)

  const initConstants = () => {
    if (model.customConfig) {
      sqlEditorSpan.value = 0
      jsonEditorSpan.value = 24
      datasourceSpan.value = 0
      destinationDatasourceSpan.value = 0
      otherStatementSpan.value = 0
      jobSpeedSpan.value = 0
      customParameterSpan.value = 24
    } else {
      sqlEditorSpan.value = 24
      jsonEditorSpan.value = 0
      datasourceSpan.value = 12
      destinationDatasourceSpan.value = 8
      otherStatementSpan.value = 22
      jobSpeedSpan.value = 12
      customParameterSpan.value = 0
    }
  }

  onMounted(() => {
    getDatasourceTypes()
    getDatasourceInstances()
    getDestinationDatasourceInstances()
    initConstants()
  })

  const onSourceTypeChange = (type: string) => {
    model.dsType = type
    getDatasourceInstances()
  }

  const onDestinationTypeChange = (type: string) => {
    model.dtType = type
    getDestinationDatasourceInstances()
  }

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
      name: t('project.node.datax_custom_template')
    },
    {
      type: 'select',
      field: 'dsType',
      span: datasourceSpan,
      name: t('project.node.datasource_type'),
      props: {
        loading: loading,
        'on-update:value': onSourceTypeChange
      },
      options: datasourceTypeOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'select',
      field: 'dataSource',
      span: datasourceSpan,
      name: t('project.node.datasource_instances'),
      props: {
        loading: loading
      },
      options: datasourceOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'editor',
      field: 'sql',
      name: t('project.node.sql_statement'),
      span: sqlEditorSpan,
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.sql_empty_tips')
      }
    },
    {
      type: 'editor',
      field: 'json',
      name: t('project.node.datax_json_template'),
      span: jsonEditorSpan,
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.sql_empty_tips')
      }
    },
    {
      type: 'select',
      field: 'dtType',
      name: t('project.node.datax_target_datasource_type'),
      span: destinationDatasourceSpan,
      props: {
        loading: loading,
        'on-update:value': onDestinationTypeChange
      },
      options: datasourceTypeOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'select',
      field: 'dataTarget',
      name: t('project.node.datax_target_database'),
      span: destinationDatasourceSpan,
      props: {
        loading: loading
      },
      options: destinationDatasourceOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'input',
      field: 'targetTable',
      name: t('project.node.datax_target_table'),
      span: destinationDatasourceSpan,
      props: {
        placeholder: t('project.node.datax_target_table_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true
      }
    },
    {
      type: 'multi-input',
      field: 'preStatements',
      name: t('project.node.datax_target_database_pre_sql'),
      span: otherStatementSpan,
      props: {
        placeholder: t('project.node.datax_non_query_sql_tips'),
        type: 'textarea',
        autosize: { minRows: 1 }
      }
    },
    {
      type: 'multi-input',
      field: 'postStatements',
      name: t('project.node.datax_target_database_post_sql'),
      span: otherStatementSpan,
      props: {
        placeholder: t('project.node.datax_non_query_sql_tips'),
        type: 'textarea',
        autosize: { minRows: 1 }
      }
    },
    {
      type: 'select',
      field: 'jobSpeedByte',
      name: t('project.node.datax_job_speed_byte'),
      span: jobSpeedSpan,
      options: jobSpeedByteOptions,
      value: 0
    },
    {
      type: 'select',
      field: 'jobSpeedRecord',
      name: t('project.node.datax_job_speed_record'),
      span: jobSpeedSpan,
      options: jobSpeedRecordOptions,
      value: 1000
    },
    {
      type: 'custom-parameters',
      field: 'localParams',
      name: t('project.node.custom_parameters'),
      span: customParameterSpan,
      children: [
        {
          type: 'input',
          field: 'prop',
          span: 10,
          props: {
            placeholder: t('project.node.prop_tips'),
            maxLength: 256
          },
          validate: {
            trigger: ['input', 'blur'],
            required: true,
            validator(validate: any, value: string) {
              if (!value) {
                return new Error(t('project.node.prop_tips'))
              }

              const sameItems = model.localParams.filter(
                (item: { prop: string }) => item.prop === value
              )

              if (sameItems.length > 1) {
                return new Error(t('project.node.prop_repeat'))
              }
            }
          }
        },
        {
          type: 'input',
          field: 'value',
          span: 10,
          props: {
            placeholder: t('project.node.value_tips'),
            maxLength: 256
          }
        }
      ]
    },
    {
      type: 'select',
      field: 'xms',
      name: t('project.node.datax_job_runtime_memory_xms'),
      span: 12,
      options: memoryLimitOptions,
      value: 1
    },
    {
      type: 'select',
      field: 'xmx',
      name: t('project.node.datax_job_runtime_memory_xmx'),
      span: 12,
      options: memoryLimitOptions,
      value: 1
    }
  ]
}
