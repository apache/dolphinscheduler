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

import { reactive, ref, SetupContext } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  verifyNamespaceK8s,
  createK8sNamespace,
  updateK8sNamespace
} from '@/service/modules/k8s-namespace'
import { queryAllClusterList } from '@/service/modules/cluster'
import { useAsyncState } from '@vueuse/core'

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()

  const variables = reactive({
    k8sNamespaceFormRef: ref(),
    model: {
      id: ref<number>(-1),
      namespace: ref(''),
      clusterCode: ref(''),
      userId: ref(''),
      limitsCpu: ref(''),
      limitsMemory: ref(''),
      clusterOptions: []
    },
    saving: false,
    rules: {
      namespace: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.namespace === '') {
            return new Error(t('security.k8s_namespace.k8s_namespace_tips'))
          }
        }
      },
      clusterCode: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.clusterCode === '') {
            return new Error(t('security.k8s_namespace.k8s_cluster_tips'))
          }
        }
      }
    }
  })

  const handleValidate = async (statusRef: number) => {
    await variables.k8sNamespaceFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0
        ? await submitK8SNamespaceModal()
        : await updateK8SNamespaceModal()
      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const getListData = () => {
    const { state } = useAsyncState(
      queryAllClusterList().then((res: any) => {
        variables.model.clusterOptions = res
          .filter((item: any) => {
            if (item.config) {
              const k8s = JSON.parse(item.config).k8s
              return !!k8s
            }
            return false
          })
          .map((item: any) => {
            return {
              label: item.name,
              value: item.code
            }
          })
      }),
      {}
    )

    return state
  }

  const submitK8SNamespaceModal = () => {
    verifyNamespaceK8s(variables.model).then(() => {
      createK8sNamespace(variables.model).then(() => {
        variables.model.namespace = ''
        variables.model.clusterCode = ''
        variables.model.limitsCpu = ''
        variables.model.limitsMemory = ''
        variables.model.userId = ''
        ctx.emit('confirmModal', props.showModalRef)
      })
    })
  }

  const updateK8SNamespaceModal = () => {
    updateK8sNamespace(variables.model, variables.model.id).then(
      (ignored: any) => {
        ctx.emit('confirmModal', props.showModalRef)
      }
    )
  }

  return {
    variables,
    handleValidate,
    getListData
  }
}
