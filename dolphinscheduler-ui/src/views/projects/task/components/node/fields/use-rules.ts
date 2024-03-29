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
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  queryRuleList,
  getRuleFormCreateJson,
  getDatasourceOptionsById
} from '@/service/modules/data-quality'
import {
  getDatasourceDatabasesById,
  getDatasourceTablesById,
  getDatasourceTableColumnsById
} from '@/service/modules/data-source'
import type { IJsonItem, IResponseJsonItem, IJsonItemParams } from '../types'

export function useRules(
  model: { [field: string]: any },
  updateRules: (items: IJsonItem[], len: number) => void
): IJsonItem[] {
  const { t } = useI18n()
  const rules = ref([])
  const ruleLoading = ref(false)
  const srcDatasourceOptions = ref([] as { label: string; value: number }[])
  const srcDatabaseOptions = ref([] as { label: string; value: number }[])
  const srcTableOptions = ref([] as { label: string; value: number }[])
  const srcTableColumnOptions = ref([] as { label: string; value: number }[])
  const targetDatasourceOptions = ref([] as { label: string; value: number }[])
  const targetDatabaseOptions = ref([] as { label: string; value: number }[])
  const targetTableOptions = ref([] as { label: string; value: string }[])
  const targetTableColumnOptions = ref([] as { label: string; value: number }[])
  const writerDatasourceOptions = ref([] as { label: string; value: number }[])

  const fixValueSpan = computed(() => (model.comparison_type === 1 ? 24 : 0))

  let preItemLen = 0

  const getRuleList = async () => {
    if (ruleLoading.value) return
    ruleLoading.value = true
    const result = await queryRuleList()
    rules.value = result.map((item: { id: number; name: string }) => {
      let name = ''
      if (item.name) {
        name = item.name.replace('$t(', '').replace(')', '')
      }
      return {
        value: item.id,
        label: name ? t(`project.node.${name}`) : ''
      }
    })
    ruleLoading.value = false
  }

  const getRuleById = async (ruleId: number) => {
    if (ruleLoading.value) return
    ruleLoading.value = true
    const result = await getRuleFormCreateJson(ruleId)
    const items = JSON.parse(result).map((item: IResponseJsonItem) =>
      formatResponseJson(item)
    )
    updateRules(items, preItemLen)
    preItemLen = items.length
    ruleLoading.value = false
  }

  const formatResponseJson = (
    responseItem: IResponseJsonItem
  ): IJsonItemParams => {
    const item: IJsonItemParams = {
      field: responseItem.field,
      options: responseItem.options,
      validate: responseItem.validate,
      props: responseItem.props,
      value: responseItem.value
    }
    item.props.filterable = true
    const name = responseItem.name?.replace('$t(', '').replace(')', '')
    item.name = name ? t(`project.node.${name}`) : ''

    if (responseItem.type !== 'group') {
      item.type = responseItem.type
    } else {
      item.type = 'custom-parameters'
      item.children = item.props.rules.map((child: IJsonItemParams) => {
        child.span = Math.floor(22 / item.props.rules.length)
        return child
      })
      model[item.field] = model[item.field] || []
      delete item.props.rules
    }
    if (responseItem.emit) {
      responseItem.emit.forEach((emit) => {
        if (emit === 'change') {
          item.props.onUpdateValue = (value: string | number) => {
            onFieldChange(value, item.field, true)
          }
        }
      })
    }
    if (responseItem.props.placeholder) {
      item.props.placeholder = t(
        'project.node.' +
          responseItem.props.placeholder
            .split(' ')
            .join('_')
            .split(',')
            .join('')
            .toLowerCase()
      )
    }
    if (item.field === 'src_datasource_id') {
      item.options = srcDatasourceOptions
    }
    if (item.field === 'src_database') {
      item.options = srcDatabaseOptions
    }
    if (item.field === 'target_datasource_id') {
      item.options = targetDatasourceOptions
    }
    if (item.field === 'target_database') {
      item.options = targetDatabaseOptions
    }
    if (item.field === 'writer_datasource_id') {
      item.options = writerDatasourceOptions
    }
    if (item.field === 'src_table') {
      item.options = srcTableOptions
      item.props.filterable = true
    }
    if (item.field === 'target_table') {
      item.options = targetTableOptions
      item.props.filterable = true
    }
    if (item.field === 'src_field') {
      item.options = srcTableColumnOptions
    }
    if (item.field === 'target_field') {
      item.options = targetTableColumnOptions
    }

    if (model[item.field] !== void 0) {
      onFieldChange(model[item.field], item.field, false)
      item.value = model[item.field]
    }

    return item
  }
  const onFieldChange = async (
    value: string | number,
    field: string,
    reset: boolean
  ) => {
    if (field === 'src_connector_type' && typeof value === 'number') {
      const result = await getDatasourceOptionsById(value)
      srcDatasourceOptions.value = result || []
      if (reset) {
        srcDatabaseOptions.value = []
        srcTableOptions.value = []
        srcTableColumnOptions.value = []
        model.src_datasource_id = null
        model.src_database = null
        model.src_table = null
        model.src_field = null
      }
      return
    }
    if (field === 'target_connector_type' && typeof value === 'number') {
      const result = await getDatasourceOptionsById(value)
      targetDatasourceOptions.value = result || []
      if (reset) {
        targetDatabaseOptions.value = []
        targetTableOptions.value = []
        targetTableColumnOptions.value = []
        model.target_datasource_id = null
        model.target_database = null
        model.target_table = null
        model.target_field = null
      }
      return
    }
    if (field === 'writer_connector_type' && typeof value === 'number') {
      const result = await getDatasourceOptionsById(value)
      writerDatasourceOptions.value = result || []
      if (reset) {
        model.writer_datasource_id = null
      }
      return
    }
    if (field === 'src_datasource_id' && typeof value === 'number') {
      const result = await getDatasourceDatabasesById(value)
      srcDatabaseOptions.value = result || []
      if (reset) {
        srcTableOptions.value = []
        srcTableColumnOptions.value = []
        model.src_database = null
        model.src_table = null
        model.src_field = null
      }
    }
    if (field === 'target_datasource_id' && typeof value === 'number') {
      const result = await getDatasourceDatabasesById(value)
      targetDatabaseOptions.value = result || []
      if (reset) {
        targetTableOptions.value = []
        targetTableColumnOptions.value = []
        model.target_database = null
        model.target_table = null
        model.target_field = null
      }
    }

    if (field === 'src_database' && typeof value === 'string') {
      const result = await getDatasourceTablesById(
        model.src_datasource_id,
        value
      )
      srcTableOptions.value = result || []
      if (reset) {
        srcTableColumnOptions.value = []
        model.src_table = null
        model.src_field = null
      }
    }

    if (field === 'target_database' && typeof value === 'string') {
      const result = await getDatasourceTablesById(
        model.target_datasource_id,
        value
      )
      targetTableOptions.value = result || []
      if (reset) {
        targetTableColumnOptions.value = []
        model.target_table = null
        model.target_field = null
      }
    }

    if (field === 'src_table' && typeof value === 'string') {
      const result = await getDatasourceTableColumnsById(
        model.src_datasource_id,
        model.src_database,
        value
      )
      srcTableColumnOptions.value = result || []
      if (reset) {
        model.src_field = null
      }
    }
    if (field === 'target_table' && typeof value === 'string') {
      const result = await getDatasourceTableColumnsById(
        model.target_datasource_id,
        model.target_database,
        value
      )
      targetTableColumnOptions.value = result || []
      if (reset) {
        model.target_field = null
      }
    }
  }

  onMounted(async () => {
    await getRuleList()
    await getRuleById(model.ruleId)
  })

  return [
    {
      type: 'select',
      field: 'ruleId',
      name: t('project.node.rule_name'),
      props: {
        loading: ruleLoading,
        filterable: true,
        onUpdateValue: getRuleById
      },
      options: rules
    },
    {
      type: 'input',
      field: 'comparison_name',
      name: t('project.node.fix_value'),
      props: {
        placeholder: t('project.node.fix_value')
      },
      span: fixValueSpan
    }
  ]
}
