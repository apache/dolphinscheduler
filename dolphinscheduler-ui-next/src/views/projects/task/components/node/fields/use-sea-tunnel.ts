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
import { ref, onMounted, watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryResourceList } from '@/service/modules/resources'
import { useDeployMode } from '.'
import type { IJsonItem } from '../types'

export function useSeaTunnel(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const options = ref([])

  const masterTypeOptions = [
    {
      label: 'yarn',
      value: 'yarn'
    },
    {
      label: 'local',
      value: 'local'
    },
    {
      label: 'spark://',
      value: 'spark://'
    },
    {
      label: 'mesos://',
      value: 'mesos://'
    }
  ]

  const queueOptions = [
    {
      label: 'default',
      value: 'default'
    }
  ]

  const loading = ref(false)

  const getResourceList = async () => {
    if (loading.value) return
    loading.value = true
    model.resourceFiles = []
    const res = await queryResourceList({ type: 'FILE' })
    removeUselessChildren(res)
    options.value = res || []
    loading.value = false
  }

  function removeUselessChildren(
    list: { children?: []; fullName: string; id: number }[]
  ) {
    if (!list.length) return
    list.forEach((item) => {
      if (!item.children) {
        return
      }
      if (item.children.length === 0) {
        model.resourceFiles.push({ id: item.id, fullName: item.fullName })
        delete item.children
        return
      }
      removeUselessChildren(item.children)
    })
  }

  onMounted(() => {
    getResourceList()
  })

  const masterSpan = computed(() => (model.deployMode === 'local' ? 0 : 12))
  const queueSpan = computed(() =>
    model.deployMode === 'local' || model.master != 'yarn' ? 0 : 12
  )
  const masterUrlSpan = computed(() =>
    model.deployMode === 'local' ||
    (model.master != 'spark://' && model.master != 'mesos://')
      ? 0
      : 12
  )

  const baseScript = 'sh ${WATERDROP_HOME}/bin/start-waterdrop.sh'

  const parseRawScript = () => {
    if (model.rawScript) {
      model.rawScript.split('\n').forEach((script: string) => {
        const params = script.replace(baseScript, '').split('--')
        params?.forEach((param: string) => {
          const pair = param.split(' ')
          if (pair && pair.length >= 2) {
            if (pair[0] === 'master') {
              const prefix = pair[1].substring(0, 8)
              if (pair[1] && (prefix === 'mesos://' || prefix === 'spark://')) {
                model.master = prefix
                model.masterUrl = pair[1].substring(8, pair[1].length)
              } else {
                model.master = pair[1]
              }
            } else if (pair[0] === 'deploy-mode') {
              model.deployMode = pair[1]
            } else if (pair[0] === 'queue') {
              model.queue = pair[1]
            }
          }
        })
      })
    }
  }

  watch(
    () => model.rawScript,
    () => {
      parseRawScript()
    }
  )

  return [
    useDeployMode(),
    {
      type: 'select',
      field: 'master',
      name: t('project.node.sea_tunnel_master'),
      options: masterTypeOptions,
      value: model.master,
      span: masterSpan
    },
    {
      type: 'input',
      field: 'masterUrl',
      name: t('project.node.sea_tunnel_master_url'),
      value: model.masterUrl,
      span: masterUrlSpan,
      props: {
        placeholder: t('project.node.sea_tunnel_master_url_tips')
      }
    },
    {
      type: 'select',
      field: 'queue',
      name: t('project.node.sea_tunnel_queue'),
      options: queueOptions,
      value: model.queue,
      span: queueSpan
    },

    {
      type: 'tree-select',
      field: 'resourceList',
      name: t('project.node.resources'),
      options,
      props: {
        multiple: true,
        checkable: true,
        cascade: true,
        showPath: true,
        checkStrategy: 'child',
        placeholder: t('project.node.resources_tips'),
        keyField: 'id',
        labelField: 'name',
        loading,
        validate: {
          trigger: ['input', 'blur'],
          required: true
        }
      }
    },
    {
      type: 'custom-parameters',
      field: 'localParams',
      name: t('project.node.custom_parameters'),
      children: [
        {
          type: 'input',
          field: 'prop',
          span: 6,
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
          type: 'select',
          field: 'direct',
          span: 4,
          options: DIRECT_LIST,
          value: 'IN'
        },
        {
          type: 'select',
          field: 'type',
          span: 6,
          options: TYPE_LIST,
          value: 'VARCHAR'
        },
        {
          type: 'input',
          field: 'value',
          span: 6,
          props: {
            placeholder: t('project.node.value_tips'),
            maxLength: 256
          }
        }
      ]
    }
  ]
}

export const TYPE_LIST = [
  {
    value: 'VARCHAR',
    label: 'VARCHAR'
  },
  {
    value: 'INTEGER',
    label: 'INTEGER'
  },
  {
    value: 'LONG',
    label: 'LONG'
  },
  {
    value: 'FLOAT',
    label: 'FLOAT'
  },
  {
    value: 'DOUBLE',
    label: 'DOUBLE'
  },
  {
    value: 'DATE',
    label: 'DATE'
  },
  {
    value: 'TIME',
    label: 'TIME'
  },
  {
    value: 'TIMESTAMP',
    label: 'TIMESTAMP'
  },
  {
    value: 'BOOLEAN',
    label: 'BOOLEAN'
  }
]

export const DIRECT_LIST = [
  {
    value: 'IN',
    label: 'IN'
  },
  {
    value: 'OUT',
    label: 'OUT'
  }
]
