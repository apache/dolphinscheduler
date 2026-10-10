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
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useResources } from '.'
import { viewResource } from '@/service/modules/resources'
import { useUserStore } from '@/store/user/user'
import type { IJsonItem } from '../types'

const SCRIPT = 'SCRIPT'
const FILE = 'FILE'

export function useFlinkSqlGatewayFields(model: { [field: string]: any }): {
  submitType: IJsonItem
  beforeInit: IJsonItem[]
  beforeScript: IJsonItem[]
  scriptReadOnly: ReturnType<typeof computed>
  initScriptReadOnly: ReturnType<typeof computed>
} {
  const { t } = useI18n()
  const userStore = useUserStore()
  const resourceContentLoading = ref(false)
  const initResourceContentLoading = ref(false)

  const gatewaySpan = computed(() =>
    model.programType === 'SQL' && model.sqlSubmitType === 'SQL_GATEWAY' ? 24 : 0
  )
  const initResourcesSpan = computed(() =>
    model.sqlSubmitType === 'SQL_GATEWAY' && model.initScriptType === FILE ? 24 : 0
  )
  const scriptReadOnly = computed(
    () =>
      model.sqlSubmitType === 'SQL_GATEWAY' && model.rawScriptType === FILE
  )
  const initScriptReadOnly = computed(
    () =>
      model.sqlSubmitType === 'SQL_GATEWAY' && model.initScriptType === FILE
  )

  const loadResourceContent = (
    fullName: string,
    target: 'rawScript' | 'initScript',
    loading: { value: boolean }
  ) => {
    const tenantCode = (userStore.getUserInfo as any)?.tenantCode
    if (!tenantCode) {
      model[target] = ''
      return
    }
    loading.value = true
    viewResource({
      fullName,
      tenantCode,
      skipLineNum: 0,
      limit: -1
    })
      .then((res: { content: string }) => {
        model[target] = res?.content ?? ''
      })
      .catch(() => {
        model[target] = ''
      })
      .finally(() => {
        loading.value = false
      })
  }

  watch(
    () => model.sqlSubmitType,
    (value) => {
      if (value !== 'SQL_GATEWAY') {
        return
      }
      if (!model.flinkJdbcUrl) {
        model.flinkJdbcUrl = 'jdbc:flink://host:port'
      }
      if (!model.statementSeparator) {
        model.statementSeparator = ';'
      }
      if (model.maxPrintRows == null) {
        model.maxPrintRows = 0
      }
      if (!model.rawScriptType) {
        model.rawScriptType = model.rawScript ? SCRIPT : FILE
      }
      if (!model.initScriptType) {
        model.initScriptType = model.initScript ? SCRIPT : FILE
      }
      if (!model.initScriptResourceList) {
        model.initScriptResourceList = []
      }
    }
  )

  watch(
    () => model.resourceList,
    (list) => {
      if (
        model.sqlSubmitType !== 'SQL_GATEWAY' ||
        model.rawScriptType !== FILE
      ) {
        return
      }
      if (list?.length === 1) {
        loadResourceContent(list[0], 'rawScript', resourceContentLoading)
      }
    },
    { deep: true }
  )

  watch(
    () => model.initScriptResourceList,
    (list) => {
      if (
        model.sqlSubmitType !== 'SQL_GATEWAY' ||
        model.initScriptType !== FILE
      ) {
        return
      }
      if (list?.length === 1) {
        loadResourceContent(list[0], 'initScript', initResourceContentLoading)
      }
    },
    { deep: true }
  )

  const sourceOptions = [
    {
      label: t('project.node.sql_execution_type_from_script'),
      value: SCRIPT
    },
    {
      label: t('project.node.sql_execution_type_from_file'),
      value: FILE
    }
  ]

  return {
    submitType: {
      type: 'select',
      field: 'sqlSubmitType',
      span: computed(() => (model.programType === 'SQL' ? 24 : 0)),
      name: t('project.node.sql_submit_type'),
      options: [
        { label: t('project.node.sql_submit_client'), value: 'CLIENT' },
        { label: t('project.node.sql_submit_gateway'), value: 'SQL_GATEWAY' }
      ],
      value: model.sqlSubmitType || 'CLIENT',
      props: {
        'on-update:value': (value: string) => {
          model.sqlSubmitType = value
        }
      }
    },
    beforeInit: [
      {
        type: 'input',
        field: 'flinkJdbcUrl',
        span: gatewaySpan,
        name: 'JDBC URL',
        props: { placeholder: 'jdbc:flink://host:port' },
        value: model.flinkJdbcUrl,
        validate: {
          trigger: ['blur', 'input'],
          required: model.sqlSubmitType === 'SQL_GATEWAY'
        }
      },
      {
        type: 'input',
        field: 'statementSeparator',
        span: gatewaySpan,
        name: t('project.node.statement_separator'),
        props: { placeholder: ';' },
        value: model.statementSeparator
      },
      {
        type: 'input-number',
        field: 'maxPrintRows',
        span: gatewaySpan,
        name: t('project.node.max_print_rows'),
        props: { min: 0 },
        value: model.maxPrintRows
      },
      {
        type: 'select',
        field: 'initScriptType',
        span: computed(() => (gatewaySpan.value ? 12 : 0)),
        name: t('project.node.init_script_source'),
        options: sourceOptions
      },
      useResources(
        initResourcesSpan,
        computed(() => model.initScriptType === FILE && gatewaySpan.value > 0),
        computed(() => (model.initScriptType === FILE ? 1 : -1)),
        'initScriptResourceList'
      )
    ],
    beforeScript: [
      {
        type: 'select',
        field: 'rawScriptType',
        span: computed(() => (gatewaySpan.value ? 12 : 0)),
        name: t('project.node.sql_execution_type'),
        options: sourceOptions
      }
    ],
    scriptReadOnly,
    initScriptReadOnly
  }
}
