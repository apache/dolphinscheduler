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
  verifyUserName,
  grantProject,
  grantResource,
  grantDataSource,
  grantUDFFunc
} from '@/service/modules/users'
import {
  queryAuthorizedProject,
  queryUnauthorizedProject
} from '@/service/modules/projects'
import {
  authorizedFile,
  authorizeResourceTree,
  authUDFFunc,
  unAuthUDFFunc
} from '@/service/modules/resources'
import {
  authedDatasource,
  unAuthDatasource
} from '@/service/modules/data-source'
import regexUtils from '@/utils/regex'
export type Mode =
  | 'add'
  | 'edit'
  | 'delete'
  | 'auth_project'
  | 'auth_resource'
  | 'auth_datasource'
  | 'auth_udf'

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
  const authorizedProjects = ref<string[]>([])
  const projects = ref<any[]>([])
  const authorizedFiles = ref<string[]>([])
  const originResourceTree = ref<any[]>([])
  const resourceType = ref<'file' | 'udf'>()
  const authorizedUDF = ref<string[]>([])
  const UDFs = ref<any[]>([])
  const authorizedDatasource = ref<string[]>([])
  const datasource = ref<any[]>([])
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

  const resourceTree = computed(() => {
    const loopTree = (arr: any[]): any[] =>
      arr
        .map((d) => {
          if (
            (resourceType.value &&
              `${d.type}`.toLowerCase() === resourceType.value) ||
            !resourceType.value
          ) {
            const obj = { key: `${d.pid}-${d.id}`, label: d.name }
            const children = d.children
            if (children instanceof Array && children.length > 0) {
              return {
                ...obj,
                children: loopTree(children)
              }
            }
            return obj
          }
          return null
        })
        .filter((f) => f)
    const data = loopTree(originResourceTree.value)
    return data
  })

  const titleMap = computed(() => {
    const titles: Record<Mode, string> = {
      add: t('security.user.create_user'),
      edit: t('security.user.update_user'),
      delete: t('security.user.delete_user'),
      auth_project: t('security.user.authorize_project'),
      auth_resource: t('security.user.authorize_resource'),
      auth_datasource: t('security.user.authorize_datasource'),
      auth_udf: t('security.user.authorize_udf')
    }
    return titles
  })

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

  const fetchProjects = async () => {
    optionsLoading.value = true
    Promise.all([
      queryAuthorizedProject({ userId: user.value.id }),
      queryUnauthorizedProject({ userId: user.value.id })
    ])
      .then((res: any[]) => {
        const ids: string[] = []
        res[0]?.forEach((d: any) => {
          if (!ids.includes(d.id)) {
            ids.push(d.id)
          }
        })
        authorizedProjects.value = ids
        projects.value =
          res?.flat().map((d: any) => ({ label: d.name, value: d.id })) || []
      })
      .finally(() => {
        optionsLoading.value = false
      })
  }

  const fetchResources = async () => {
    optionsLoading.value = true
    Promise.all([
      authorizedFile({ userId: user.value.id }),
      authorizeResourceTree({ userId: user.value.id })
    ])
      .then((res: any[]) => {
        const ids: string[] = []
        const getIds = (arr: any[]) => {
          arr.forEach((d) => {
            const children = d.children
            if (children instanceof Array && children.length > 0) {
              getIds(children)
            } else {
              ids.push(`${d.pid}-${d.id}`)
            }
          })
        }
        getIds(res[0] || [])
        authorizedFiles.value = ids
        originResourceTree.value = res[1] || []
      })
      .finally(() => {
        optionsLoading.value = false
      })
  }

  const fetchDatasource = async () => {
    optionsLoading.value = true
    Promise.all([
      authedDatasource({ userId: user.value.id }),
      unAuthDatasource({ userId: user.value.id })
    ])
      .then((res: any[]) => {
        const ids: string[] = []
        res[0]?.forEach((d: any) => {
          if (!ids.includes(d.id)) {
            ids.push(d.id)
          }
        })
        authorizedDatasource.value = ids
        datasource.value =
          res?.flat().map((d: any) => ({ label: d.name, value: d.id })) || []
      })
      .finally(() => {
        optionsLoading.value = false
      })
  }

  const fetchUDFs = async () => {
    optionsLoading.value = true
    Promise.all([
      authUDFFunc({ userId: user.value.id }),
      unAuthUDFFunc({ userId: user.value.id })
    ])
      .then((res: any[]) => {
        const ids: string[] = []
        res[0]?.forEach((d: any) => {
          if (!ids.includes(d.id)) {
            ids.push(d.id)
          }
        })
        authorizedUDF.value = ids
        UDFs.value =
          res?.flat().map((d: any) => ({ label: d.name, value: d.id })) || []
      })
      .finally(() => {
        optionsLoading.value = false
      })
  }

  const onModalCancel = () => {
    show.value = false
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

  const onGrant = (grantReq: () => Promise<any>) => {
    confirmLoading.value = true
    grantReq()
      .then(
        () => {
          onSuccess?.(mode.value)
          onModalCancel()
          message.success(t('security.user.auth_success_msg'))
        },
        () => {
          message.error(t('security.user.auth_error_msg'))
        }
      )
      .finally(() => {
        confirmLoading.value = false
      })
  }

  const onConfirm = () => {
    if (mode.value === 'add' || mode.value === 'edit') {
      formRef.value.validate((errors: any) => {
        if (!errors) {
          user.value ? onUpdateUser() : onCreateUser()
        }
      })
    } else {
      mode.value === 'delete' && onDelete()
      mode.value === 'auth_project' &&
        onGrant(() =>
          grantProject({
            userId: user.value.id,
            projectIds: authorizedProjects.value.join(',')
          })
        )
      mode.value === 'auth_resource' &&
        onGrant(() =>
          grantResource({
            userId: user.value.id,
            resourceIds: authorizedFiles.value.join(',')
          })
        )
      mode.value === 'auth_datasource' &&
        onGrant(() =>
          grantDataSource({
            userId: user.value.id,
            datasourceIds: authorizedDatasource.value.join(',')
          })
        )
      mode.value === 'auth_udf' &&
        onGrant(() =>
          grantUDFFunc({
            userId: user.value.id,
            udfIds: authorizedUDF.value.join(',')
          })
        )
    }
  }

  watch([show, mode], () => {
    show.value && ['add', 'edit'].includes(mode.value) && prepareOptions()
    show.value && mode.value === 'auth_project' && fetchProjects()
    show.value && mode.value === 'auth_resource' && fetchResources()
    show.value && mode.value === 'auth_datasource' && fetchDatasource()
    show.value && mode.value === 'auth_udf' && fetchUDFs()
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
    authorizedProjects,
    projects,
    authorizedDatasource,
    datasource,
    authorizedUDF,
    UDFs,
    authorizedFiles,
    resourceTree,
    resourceType,
    optionsLoading,
    onConfirm,
    confirmLoading
  }
}
