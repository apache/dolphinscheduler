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
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { pick } from 'lodash'
import { useUserStore } from '@/store/user/user'
import { updateUser } from '@/service/modules/users'
import type { IRecord, UserInfoRes } from '../types'
import { FormItemRule } from 'naive-ui'
import { UserReq } from '../types'
import { IdReq } from '@/service/modules/users/types'

export function usePassword() {
  const { t } = useI18n()
  const userStore = useUserStore()
  const userInfo = userStore.getUserInfo as UserInfoRes
  const IS_ADMIN = userInfo.userType === 'ADMIN_USER'

  const initialValues = {
    userName: '',
    userPassword: '',
    confirmPassword: ''
  }

  const state = reactive({
    formRef: ref(),
    formData: { ...initialValues },
    saving: false,
    loading: false
  })

  function validatePasswordStartWith(
    rule: FormItemRule,
    value: string
  ): boolean {
    return (
      !!state.formRef.model.userPassword &&
      state.formRef.model.userPassword.startsWith(value) &&
      state.formRef.model.userPassword.length >= value.length
    )
  }

  function validatePasswordSame(rule: FormItemRule, value: string): boolean {
    return value === state.formRef.model.userPassword
  }

  const formRules = {
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
    confirmPassword: [
      {
        trigger: ['input', 'blur'],
        required: true
      },
      {
        validator: validatePasswordStartWith,
        message: t('security.user.confirm_password_tips'),
        trigger: ['input']
      },
      {
        validator: validatePasswordSame,
        message: t('security.user.confirm_password_tips'),
        trigger: ['blur', 'password-input']
      }
    ]
  }

  const onReset = () => {
    state.formData = { ...initialValues }
  }
  const onSave = async (record: IRecord): Promise<boolean> => {
    try {
      await state.formRef.validate()
      if (state.saving) return false
      state.saving = true

      const resetPasswordReq = {
        ...pick(record, [
          'id',
          'userName',
          'tenantId',
          'email',
          'queue',
          'phone',
          'state'
        ]),
        userPassword: state.formData.userPassword
      } as IdReq & UserReq

      await updateUser(resetPasswordReq)

      state.saving = false
      return true
    } catch (err) {
      state.saving = false
      return false
    }
  }
  const onSetValues = (record: IRecord) => {
    state.formData = {
      ...pick(record, ['userName']),
      userPassword: '',
      confirmPassword: ''
    }
  }

  return { state, formRules, IS_ADMIN, onReset, onSave, onSetValues }
}
