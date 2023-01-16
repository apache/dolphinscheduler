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
import { useAsyncState } from '@vueuse/core'
import {
  queryWorkerAddressList,
  saveWorkerGroup
} from '@/service/modules/worker-groups'
import { queryTenantList } from '@/service/modules/tenants'

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()

  const variables = reactive({
    workerGroupFormRef: ref(),
    model: {
      id: ref<number>(-1),
      name: ref(''),
      addrList: ref<Array<number>>([]),
      generalOptions: [],
      tenantId: null,
      tenantOptions:[]
    },
    tenants: [] as { label: string; value: number }[],
    saving: false,
    rules: {
      name: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.name === '') {
            return new Error(t('security.worker_group.group_name_tips'))
          }
        }
      },
      addrList: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.addrList.length < 1) {
            return new Error(t('security.worker_group.worker_addresses_tips'))
          }
        }
      }
    }
  })

  const getListData = () => {
    const { state } = useAsyncState(
      queryWorkerAddressList().then((res: Array<string>) => {
        variables.model.generalOptions = res.map(
          (item): { label: string; value: string } => {
            return {
              label: item,
              value: item
            }
          }
        ) as any
      }),
      {}
    )

    return state
  }

  const getTenants = async () => {
    const result = await queryTenantList()
    variables.model.tenantOptions = result.map(
      (tenant: { tenantCode: string; id: number }) => ({
        label: tenant.tenantCode,
        value: tenant.id
      })
    )
    if (variables.model.tenantOptions.length) {
      variables.model.tenantId = variables.model.tenantOptions[0].value;
    }
  }

  const handleValidate = async (statusRef: number) => {
    await variables.workerGroupFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0
        ? await submitWorkerGroupModal()
        : await updateWorkerGroupModal()

      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const submitWorkerGroupModal = () => {
    const data = {
      name: variables.model.name,
      addrList: variables.model.addrList.toString(),
      tenantId: variables.model.tenantId
    }

    saveWorkerGroup(data).then(() => {
      variables.model.name = ''
      variables.model.addrList = [],
      variables.model.tenantId= null
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  const updateWorkerGroupModal = () => {
    const data = {
      id: variables.model.id,
      name: variables.model.name,
      addrList: variables.model.addrList.toString(),
      tenantId: variables.model.tenantId
    }

    saveWorkerGroup(data).then(() => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    handleValidate,
    getListData,
    getTenants
  }
}
