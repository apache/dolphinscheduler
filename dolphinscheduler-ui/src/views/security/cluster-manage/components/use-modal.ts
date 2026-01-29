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
  verifyCluster,
  createCluster,
  updateCluster
} from '@/service/modules/cluster'

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()

  const variables = reactive({
    clusterFormRef: ref(),
    model: {
      code: ref<number>(-1),
      name: ref(''),
      k8s_config: ref(''),
      yarn_config: ref(''),
      description: ref('')
    },
    saving: false,
    rules: {
      name: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.name === '') {
            return new Error(t('security.cluster.cluster_name_tips'))
          }
        }
      },
      description: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.description === '') {
            return new Error(t('security.cluster.cluster_description_tips'))
          }
        }
      }
    }
  })

  const handleValidate = async (statusRef: number) => {
    await variables.clusterFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0 ? await submitClusterModal() : await updateClusterModal()
    } finally {
      variables.saving = false
    }
  }

  const submitClusterModal = () => {
    verifyCluster({ clusterName: variables.model.name }).then(() => {
      const data = {
        name: variables.model.name,
        config: JSON.stringify({
          k8s: variables.model.k8s_config,
          yarn: variables.model.yarn_config
        }),
        description: variables.model.description
      }

      createCluster(data).then(() => {
        variables.model.name = ''
        variables.model.k8s_config = ''
        variables.model.yarn_config = ''
        variables.model.description = ''
        ctx.emit('confirmModal', props.showModalRef)
      })
    })
  }

  const updateClusterModal = () => {
    const data = {
      code: variables.model.code,
      name: variables.model.name,
      config: JSON.stringify({
        k8s: variables.model.k8s_config,
        yarn: variables.model.yarn_config
      }),
      description: variables.model.description
    }

    updateCluster(data).then(() => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    handleValidate
  }
}
