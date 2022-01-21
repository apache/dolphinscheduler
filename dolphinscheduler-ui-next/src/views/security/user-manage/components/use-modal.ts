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

import { ref, watch, computed, InjectionKey } from 'vue'
import { useI18n } from 'vue-i18n'
import { useMessage } from 'naive-ui'
import { queryTenantList } from '@/service/modules/tenants'
import { queryList } from '@/service/modules/queues'
import {
  createUser,
  updateUser,
  delUserById,
  verifyUserName
} from '@/service/modules/users'
import regexUtils from '@/utils/regex'
export type Mode = 'add' | 'edit' | 'delete'

export type UserModalSharedStateType = ReturnType<
  typeof useSharedUserModalState
> & {
  onSuccess?: (mode: Mode) => void
}

export const UserModalSharedStateKey: InjectionKey<UserModalSharedStateType> =
  Symbol()

export function useSharedUserModalState() {
  return {
    show: ref(false),
    mode: ref<Mode>('add'),
    user: ref()
  }
}

export function useModal({
  onSuccess,
  show,
  mode,
  user
}: UserModalSharedStateType) {
  const message = useMessage()
  const { t } = useI18n()
  const formRef = ref()
  const formValues = ref({
    userName: '',
    userPassword: '',
    tenantId: 0,
    email: '',
    queue: '',
    phone: '',
    state: 1
  })
  const tenants = ref<any[]>([])
  const queues = ref<any[]>([])
  const optionsLoading = ref(false)
  const confirmLoading = ref(false)

  const formRules = computed(() => {
    return {
      userName: {
        required: true,
        message: t('security.user.username_rule_msg'),
        trigger: 'blur'
      },
      userPassword: {
        required: mode.value === 'add',
        validator(rule: any, value?: string) {
          if (mode.value !== 'add' && !value) {
            return true
          }
          const msg = t('security.user.user_password_rule_msg')
          if (!value || !regexUtils.password.test(value)) {
            return new Error(msg)
          }
          return true
        },
        trigger: ['blur', 'input']
      },
      tenantId: {
        required: true,
        validator(rule: any, value?: number) {
          const msg = t('security.user.tenant_id_rule_msg')
          if (typeof value === 'number') {
            return true
          }
          return new Error(msg)
        },
        trigger: 'blur'
      },
      email: {
        required: true,
        validator(rule: any, value?: string) {
          const msg = t('security.user.email_rule_msg')
          if (!value || !regexUtils.email.test(value)) {
            return new Error(msg)
          }
          return true
        },
        trigger: ['blur', 'input']
      },
      phone: {
        validator(rule: any, value?: string) {
          const msg = t('security.user.phone_rule_msg')
          if (value && !regexUtils.phone.test(value)) {
            return new Error(msg)
          }
          return true
        },
        trigger: ['blur', 'input']
      }
    }
  })

  const titleMap: Record<Mode, string> = {
    add: t('security.user.create_user'),
    edit: t('security.user.update_user'),
    delete: t('security.user.delete_user')
  }

  const setFormValues = () => {
    const defaultValues = {
      userName: '',
      userPassword: '',
      tenantId: tenants.value[0]?.value,
      email: '',
      queue: queues.value[0]?.value,
      phone: '',
      state: 1
    }
    if (!user.value) {
      formValues.value = defaultValues
    } else {
      const v: any = {}
      Object.keys(defaultValues).map((k) => {
        v[k] = user.value[k]
      })
      v.userPassword = ''
      formValues.value = v
    }
  }

  const prepareOptions = async () => {
    optionsLoading.value = true
    Promise.all([queryTenantList(), queryList()])
      .then((res) => {
        tenants.value =
          res[0]?.map((d: any) => ({
            label: d.tenantCode,
            value: d.id
          })) || []
        queues.value =
          res[1]?.map((d: any) => ({
            label: d.queueName,
            value: d.queue
          })) || []
      })
      .finally(() => {
        optionsLoading.value = false
      })
  }

  const onDelete = () => {
    confirmLoading.value = true
    delUserById({ id: user.value.id })
      .then(
        () => {
          onSuccess?.(mode.value)
          onModalCancel()
        },
        () => {
          message.error(t('security.user.delete_error_msg'))
        }
      )
      .finally(() => {
        confirmLoading.value = false
      })
  }

  const onCreateUser = () => {
    confirmLoading.value = true
    verifyUserName({ userName: formValues.value.userName })
      .then(
        () => createUser(formValues.value),
        (error) => {
          if (`${error.message}`.includes('exists')) {
            message.error(t('security.user.username_exists'))
          }
          return false
        }
      )
      .then(
        (res) => {
          if (res) {
            onSuccess?.(mode.value)
            onModalCancel()
          }
        },
        () => {
          message.error(t('security.user.save_error_msg'))
        }
      )
      .finally(() => {
        confirmLoading.value = false
      })
  }

  const onUpdateUser = () => {
    confirmLoading.value = true
    updateUser({ id: user.value.id, ...formValues.value })
      .then(
        () => {
          onSuccess?.(mode.value)
          onModalCancel()
        },
        () => {
          message.error(t('security.user.save_error_msg'))
        }
      )
      .finally(() => {
        confirmLoading.value = false
      })
  }

  const onConfirm = () => {
    if (mode.value === 'delete') {
      onDelete()
    } else {
      formRef.value.validate((errors: any) => {
        if (!errors) {
          user.value ? onUpdateUser() : onCreateUser()
        }
      })
    }
  }

  const onModalCancel = () => {
    show.value = false
  }

  watch([show, mode], () => {
    show.value && mode.value !== 'delete' && prepareOptions()
  })

  watch([queues, tenants, user], () => {
    setFormValues()
  })

  return {
    show,
    mode,
    user,
    titleMap,
    onModalCancel,
    formRef,
    formValues,
    formRules,
    tenants,
    queues,
    optionsLoading,
    onConfirm,
    confirmLoading
  }
}
