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
import { queryList } from '@/service/modules/queues'
import { verifyTenantCode, createTenant } from '@/service/modules/tenants'

export function useModalData(props: any, ctx: SetupContext<("cancelModal" | "confirmModal")[]>) {
  const { t } = useI18n()

  const variables = reactive({
    tenantFormRef: ref(),
    model: {
      tenantCode: ref(''),
      description: ref(''),
      queueId: ref<number>(-1),
      generalOptions: []
    },
    rules: {
      tenantCode: {
        required: true
      },
      queueId: {
        required: true
      }
    }
  })

  const getListData = () => {
    const { state } = useAsyncState(
      queryList().then((res: any) => {
        variables.model.generalOptions = res.map((item: any) => {
          return {
            label: item.queueName,
            value: item.id
          }
        })
        variables.model.queueId = res[0].id
      }),
      {}
    )

    return state
  }

  const handleValidate = () => {
    variables.tenantFormRef.validate((errors: any) => {
      if (!errors) {
        console.log('验证成功')
        submitTenantModal()
      } else {
        console.log(errors, '验证失败')
        return
      }
    })
  }

  const submitTenantModal = () => {
    verifyTenantCode({tenantCode: variables.model.tenantCode}).then((res: any) => {
      const data = {
        tenantCode: variables.model.tenantCode,
        queueId: variables.model.queueId,
        description: variables.model.description
      }
      createTenant(data).then((res: any) => {
        ctx.emit('confirmModal', props.showModalRef)
      }, (err: any) => {
        console.log('err', err)
      })
    })
  }

  return {
    variables,
    getListData,
    handleValidate
  }
}
