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
import { useUserStore } from '@/store/user/user'
import { useAsyncState } from '@vueuse/core'
import { format } from 'date-fns'
import { listAll } from '@/service/modules/users'
import {
  generateToken,
  createToken,
  updateToken
} from '@/service/modules/token'
import type { UserListRes } from '@/service/modules/users/types'
import type { UserInfoRes } from '@/service/modules/users/types'

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()
  const userStore = useUserStore()
  const variables = reactive({
    alertGroupFormRef: ref(),
    model: {
      id: ref<number>(-1),
      userId: ref(
        (userStore.getUserInfo as UserInfoRes).userType === 'GENERAL_USER'
          ? (userStore.getUserInfo as UserInfoRes).id
          : null
      ),
      expireTime: ref(Date.now()),
      token: ref(''),
      generalOptions: []
    },
    saving: false,
    rules: {
      userId: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (!variables.model.userId) {
            return new Error(t('security.token.user_tips'))
          }
        }
      },
      expireTime: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (!variables.model.expireTime) {
            return new Error(t('security.token.expiration_time_tips'))
          }
        }
      },
      token: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.token === '') {
            return new Error(t('security.token.token_tips'))
          }
        }
      }
    }
  })

  const getListData = () => {
    const { state } = useAsyncState(
      listAll().then((res: Array<UserListRes>) => {
        variables.model.generalOptions = res.map(
          (item): { label: string; value: number } => {
            return {
              label: item.userName,
              value: item.id
            }
          }
        ) as any
      }),
      {}
    )

    return state
  }

  const getToken = () => {
    const data = {
      userId: (userStore.getUserInfo as UserInfoRes).id,
      expireTime: format(variables.model.expireTime, 'yyyy-MM-dd HH:mm:ss')
    }

    useAsyncState(
      generateToken(data).then((res: string) => {
        variables.model.token = res
      }),
      {}
    )
  }

  const handleValidate = async (statusRef: number) => {
    await variables.alertGroupFormRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0 ? await submitTokenModal() : await updateTokenModal()
      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const submitTokenModal = () => {
    const data = {
      userId: Number(variables.model.userId),
      expireTime: format(variables.model.expireTime, 'yyyy-MM-dd HH:mm:ss'),
      token: variables.model.token
    }

    createToken(data).then(() => {
      variables.model.userId =
        (userStore.getUserInfo as UserInfoRes).userType === 'GENERAL_USER'
          ? (userStore.getUserInfo as UserInfoRes).id
          : null
      variables.model.expireTime = Date.now()
      variables.model.token = ''
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  const updateTokenModal = () => {
    const data = {
      id: variables.model.id,
      userId: Number(variables.model.userId),
      expireTime: format(variables.model.expireTime, 'yyyy-MM-dd HH:mm:ss'),
      token: variables.model.token
    }

    updateToken(data).then(() => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    handleValidate,
    getListData,
    getToken
  }
}
