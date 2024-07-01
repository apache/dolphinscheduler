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
import { useAsyncState } from '@vueuse/core'
import { queryList } from '@/service/modules/queues'
import {
  verifyTenantCode,
  createTenant,
  updateTenant
} from '@/service/modules/tenants'
import { useI18n } from 'vue-i18n'

export function useModalData(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()
  const variables = reactive({
    tenantFormRef: ref(),
    model: {
      id: ref<number>(-1),
      tenantCode: ref(''),
      description: ref(''),
      queueId: ref<number | null>(null),
      generalOptions: []
    },
    saving: false,
    rules: {
      tenantCode: {
        required: true,
        validator() {
          if (!variables.model.tenantCode) {
            return new Error(t('security.tenant.tenant_code_tips'))
          }
          if (variables.model.tenantCode.search('[^-._a-zA-Z0-9]') !== -1) {
            return new Error(t('security.tenant.tenant_code_invalid_tips'))
          }
        }
      },
      queueId: {
        required: true
      }
    }
  })

  const getListData = (status: number) => {
    const { state } = useAsyncState(
      queryList().then((res: any) => {
        variables.model.generalOptions = res.map((item: any) => {
          return {
            label: item.queueName,
            value: item.id
          }
        })
        if (status === 0) {
          variables.model.queueId = res[0].id
        }
      }),
      {}
    )

    return state
  }

  const handleValidate = async (statusRef: number) => {
    await variables.tenantFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0 ? await submitTenantModal() : await updateTenantModal()
      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const submitTenantModal = () => {
    verifyTenantCode({ tenantCode: variables.model.tenantCode }).then(
      (unused: any) => {
        const data = {
          tenantCode: variables.model.tenantCode,
          queueId: variables.model.queueId as number,
          description: variables.model.description
        }
        createTenant(data).then(
          (unused: any) => {
            variables.model.tenantCode = ''
            variables.model.description = ''
            variables.model.queueId = null
            ctx.emit('confirmModal', props.showModalRef)
          },
          (unused: any) => {
            return
          }
        )
      }
    )
  }

  const updateTenantModal = () => {
    const data = {
      tenantCode: variables.model.tenantCode,
      queueId: variables.model.queueId,
      description: variables.model.description,
      id: variables.model.id
    }
    updateTenant(data, { id: variables.model.id }).then((unused: any) => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    getListData,
    handleValidate
  }
}
