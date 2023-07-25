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
import type { IJsonItem } from '../types'
import { useI18n } from 'vue-i18n'
import { onMounted, ref, VNodeChild } from 'vue'
import { getAllNamespaces } from '@/service/modules/k8s-namespace'
import { SelectOption } from 'naive-ui'

export function useNamespace(): IJsonItem {
  const { t } = useI18n()

  const options = ref([])
  const loading = ref(false)

  const getNamespaceList = async () => {
    if (loading.value) return
    loading.value = true
    const totalList = await getAllNamespaces()
    options.value = (totalList || []).map(
      (item: { id: string; namespace: string; clusterName: string }) => ({
        label: `${item.namespace}(${item.clusterName})`,
        value: JSON.stringify({
          name: item.namespace,
          cluster: item.clusterName
        })
      })
    )
    loading.value = false
  }

  onMounted(() => {
    getNamespaceList()
  })

  const renderLabel = (option: SelectOption): VNodeChild => {
    if (option.type === 'group') return option.label as string
    return [option.label as string]
  }

  return {
    type: 'select',
    field: 'namespace',
    name: t('project.node.namespace_cluster'),
    props: {
      loading,
      'render-label': renderLabel,
      clearable: true
    },
    options: [
      {
        type: 'group',
        label: t('project.node.namespace_cluster'),
        key: t('project.node.namespace_cluster'),
        children: options as any
      }
    ]
  }
}
