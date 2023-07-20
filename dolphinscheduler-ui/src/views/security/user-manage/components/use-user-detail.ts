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
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { pick } from 'lodash'
import { queryTenantList } from '@/service/modules/tenants'
import { queryList } from '@/service/modules/queues'
import { verifyUserName, createUser, updateUser } from '@/service/modules/users'
import { useUserStore } from '@/store/user/user'
import type { IRecord, UserReq, UserInfoRes } from '../types'

export function useUserDetail() {
  const { t } = useI18n()
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo as UserInfoRes
  const IS_ADMIN = userInfo.userType === 'ADMIN_USER'

  const initialValues = {
    userName: '',
    userPassword: '',
    tenantId: null,
    email: '',
    queue: '',
    phone: '',
    state: 1
  } as UserReq

  let PREV_NAME: string

  const state = reactive({
    formRef: ref(),
    formData: { ...initialValues },
    saving: false,
    loading: false,
    queues: [] as { label: string; value: string }[],
    tenants: [] as { label: string; value: number }[]
  })

  const formRules = {
    userName: {
      trigger: ['input', 'blur'],
      required: true,
      validator(validator: any, value: string) {
        if (!value.trim()) {
          return new Error(t('security.user.username_tips'))
        }
      }
    },
    userPassword: {
      trigger: ['input', 'blur'],
      required: true,
      validator(validator: any, value: string) {
        if (
          !value ||
          !/^(?![0-9]+$)(?![a-z]+$)(?![A-Z]+$)(?![`~!@#$%^&*()_\-+=<>?:"{}|,./;'\\[\]·~！@#￥%……&*（）——\-+={}|《》？：“”【】、；‘’，。、]+$)[`~!@#$%^&*()_\-+=<>?:"{}|,./;'\\[\]·~！@#￥%……&*（）——\-+={}|《》？：“”【】、；‘’，。、0-9A-Za-z]{6,22}$/.test(
            value
          )
        ) {
          return new Error(t('security.user.user_password_tips'))
        }
      }
    },
    tenantId: {
      trigger: ['input', 'blur'],
      required: true,
      validator(validator: any, value: string) {
        if (IS_ADMIN && !value) {
          return new Error(t('security.user.tenant_id_tips'))
        }
      }
    },
    email: {
      trigger: ['input', 'blur'],
      required: true,
      validator(validator: any, value: string) {
        if (!value) {
          return new Error(t('security.user.email_empty_tips'))
        }
        if (
          !/^([a-zA-Z0-9]+[_|\-|\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\-|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,}$/.test(
            value
          )
        ) {
          return new Error(t('security.user.emial_correct_tips'))
        }
      }
    },
    phone: {
      trigger: ['input', 'blur'],
      required: false,
      validator(validator: any, value: string) {
        const regExp = new RegExp('^[1][3,4,5,6,7,8,9][0-9]{9}$')
        if (value && !regExp.test(value)) {
          return new Error(t('security.user.phone_correct_tips'))
        }
      }
    }
  }

  const getQueues = async () => {
    const result = await queryList()
    state.queues = result.map((queue: { queueName: string; id: string }) => ({
      label: queue.queueName,
      value: queue.queueName
    }))
    if (state.queues.length) {
      initialValues.queue = state.queues[0].value
      state.formData.queue = state.queues[0].value
    }
  }
  const getTenants = async () => {
    const result = await queryTenantList()
    state.tenants = result.map(
      (tenant: { tenantCode: string; id: number }) => ({
        label: tenant.tenantCode,
        value: tenant.id
      })
    )
    if (state.tenants.length) {
      initialValues.tenantId = state.tenants[0].value
      state.formData.tenantId = state.tenants[0].value
    }
  }
  const onReset = () => {
    state.formData = { ...initialValues }
  }
  const onSave = async (id?: number): Promise<boolean> => {
    try {
      await state.formRef.validate()
      if (state.saving) return false
      state.saving = true
      if (PREV_NAME !== state.formData.userName) {
        await verifyUserName({ userName: state.formData.userName })
      }

      id
        ? await updateUser({ id, ...state.formData })
        : await createUser(state.formData)

      state.saving = false
      return true
    } catch (err) {
      state.saving = false
      return false
    }
  }
  const onSetValues = (record: IRecord) => {
    state.formData = {
      ...pick(record, [
        'userName',
        'tenantId',
        'email',
        'queue',
        'phone',
        'state'
      ]),
      userPassword: ''
    } as UserReq
    PREV_NAME = state.formData.userName
  }

  onMounted(async () => {
    if (IS_ADMIN) {
      getQueues()
      getTenants()
    }
  })

  return { state, formRules, IS_ADMIN, onReset, onSave, onSetValues }
}
