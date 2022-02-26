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
import {find, indexOf} from "lodash";
import { TypeReq } from "@/service/modules/data-source/types";
import { queryDataSourceList } from "@/service/modules/data-source";

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
  const jobSpeedByteOptions : any[] = [
    {
      label: '不限制', value: 0
    },
    {
      label: '1KB', value: 1024
    },
    {
      label: '10KB', value: 10240
    },
    {
      label: '50KB', value: 51200
    },
    {
      label: '100KB', value: 102400
    },
    {
      label: '512KB', value: 524288
    }
  ]
  const jobSpeedRecordOptions : any[] = [
    {
      label: '不限制', value: 0
    },
    {
      label: '500', value: 500
    },
    {
      label: '1000', value: 1000
    },
    {
      label: '1500', value: 1500
    },
    {
      label: '2000', value: 2000
    },
    {
      label: '2500', value: 2500
    },
    {
      label: '3000', value: 3000
    }
  ]
  const memoryLimitOptions = [
    {
      label: '1G', value: 1
    },
    {
      label: '2G', value: 2
    },
    {
      label: '3G', value: 3
    },
    {
      label: '4G', value: 4
    }
  ]
  const loading = ref(false)

  const getDatasourceTypes = async () => {
    if (loading.value) return
    loading.value = true
    try {
      datasourceTypeOptions.value = datasourceTypes
      .filter((item) => !item.disabled)
      .map((item) => ({ label: item.code, value: item.code }))
      loading.value = false
    } catch (err) {
      loading.value = false
    }
  }

  const getDatasourceInstances = async () => {
    const params = { type: model.dsType } as TypeReq
    const res = await queryDataSourceList(params)
    datasourceOptions.value = []
    res.map((item: any) => {
      datasourceOptions.value.push({ label: item.name, value: String(item.id) })
    })
    if (datasourceOptions.value && model.dataSource) {
      let item = find(datasourceOptions.value, { value: String(model.dataSource) })
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
      destinationDatasourceOptions.value.push({ label: item.name, value: String(item.id) })
    })
    if (destinationDatasourceOptions.value && model.dataTarget) {
      let item = find(destinationDatasourceOptions.value, { value: String(model.dataTarget) })
      if (!item) {
        model.dataTarget = null
      }
    }
  }

  onMounted(() => {
    getDatasourceTypes()
    getDatasourceInstances()
    getDestinationDatasourceInstances()
  })

  const onSourceTypeChange = (type: string) => {
      model.dsType = type
      getDatasourceInstances()
  }

  const onDestinationTypeChange = (type: string) => {
      model.dtType = type
      getDestinationDatasourceInstances()
  }

  const editorField = ref('sql')
  const editorName = ref(t('project.node.sql_statement'))


  watch(
      () => model.customConfigSwitch,
      () => {
        console.log(model.customConfigSwitch)
        if (model.customConfigSwitch) {
          editorField.value = 'json'
          model.customConfig = 1
          editorName.value = t('project.node.datax_json_template')
        } else {
          editorField.value = 'sql'
          model.customConfig = 0
          editorName.value = t('project.node.sql_statement')
        }
      }
  )

  return [
    {
      type: 'switch',
      field: 'customConfigSwitch',
      name: t('project.node.datax_custom_template')
    },
    {
      type: 'select',
      field: 'dsType',
      span: 12,
      name: t('project.node.datasource_type'),
      props: {
        loading: loading,
        'on-update:value': onSourceTypeChange
      },
      options: datasourceTypeOptions,
      validate: {
        trigger: ['input', 'blur'],
        required: true
      },
    },
    {
      type: 'select',
      field: 'dataSource',
      span: 12,
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
      span: 0,
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.sql_empty_tips')
      }
    },
    {
      type: 'editor',
      field: 'json',
      name: t('project.node.sql_statement'),
      span: 0,
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.datax_json_template')
      }
    },
    {
      type: 'select',
      field: 'dtType',
      name: t('project.node.datax_target_datasource_type'),
      span: 8,
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
      span: 8,
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
      span: 8,
      props: {
        placeholder: t('project.node.datax_target_table_tips'),
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
      span: 22,
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
      span: 22,
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
      span: 12,
      options: jobSpeedByteOptions,
      value: 0
    },
    {
      type: 'select',
      field: 'jobSpeedRecord',
      name: t('project.node.datax_job_speed_record'),
      span: 12,
      options: jobSpeedRecordOptions,
      value: 1000
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