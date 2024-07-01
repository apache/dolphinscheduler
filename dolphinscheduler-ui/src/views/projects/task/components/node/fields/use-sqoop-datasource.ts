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

import { onMounted, ref, Ref, watch } from 'vue'
import { queryDataSourceList } from '@/service/modules/data-source'
import { useI18n } from 'vue-i18n'
import type { IJsonItem, IDataBase } from '../types'
import type { TypeReq } from '@/service/modules/data-source/types'

export function useDatasource(
  model: { [field: string]: any },
  span: Ref,
  fieldType: string,
  fieldDatasource: string
): IJsonItem[] {
  const { t } = useI18n()
  const dataSourceList = ref([])
  const loading = ref(false)
  const hadoopSourceTypes = ref(['HIVE', 'HDFS'])
  const getDataSource = async (type: IDataBase) => {
    if (hadoopSourceTypes.value.some((source) => source === type)) {
      loading.value = false
      return
    }
    loading.value = true
    if (model.modelType === 'import') {
      model.sourceMysqlDatasource = model.sourceMysqlDatasource
        ? model.sourceMysqlDatasource
        : ''
      model.sourceMysqlType = type
    } else {
      model.sourceMysqlDatasource = model.targetMysqlDatasource
        ? model.targetMysqlDatasource
        : ''
      model.targetMysqlType = type
    }
    const params = { type, testFlag: 0 } as TypeReq
    const result = await queryDataSourceList(params)
    dataSourceList.value = result.map((item: { name: string; id: number }) => ({
      label: item.name,
      value: item.id
    }))
    loading.value = false
  }
  onMounted(() => {
    getDataSource(model.sourceType)
  })

  watch(
    () => [model.sourceType],
    () => {
      getDataSource(model.sourceType)
    }
  )

  watch(
    () => [model.targetType],
    () => {
      getDataSource(model.targetType)
    }
  )
  return [
    {
      type: 'input',
      field: fieldType,
      name: t('project.node.datasource'),
      span: 0,
      validate: {
        required: true
      }
    },
    {
      type: 'select',
      field: fieldDatasource,
      name: t('project.node.datasource'),
      span: span,
      props: {
        placeholder: t('project.node.datasource_tips'),
        filterable: true,
        loading
      },
      options: dataSourceList,
      validate: {
        trigger: ['blur', 'input'],
        validator(validate, value) {
          if (!value) {
            return new Error(t('project.node.datasource_tips'))
          }
        }
      }
    }
  ]
}
