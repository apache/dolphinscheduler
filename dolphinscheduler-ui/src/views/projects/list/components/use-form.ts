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

import { useI18n } from 'vue-i18n'
import { reactive, ref, SetupContext } from 'vue'
import { useUserStore } from '@/store/user/user'
import type { FormRules } from 'naive-ui'
import type { UserInfoRes } from '@/service/modules/users/types'
import { createProject, updateProject } from '@/service/modules/projects'

export function useForm(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()
  const userStore = useUserStore()

  const resetForm = () => {
    variables.model = {
      projectName: '',
      description: '',
      userName: (userStore.getUserInfo as UserInfoRes).userName
    }
  }

  const variables = reactive({
    projectFormRef: ref(),
    model: {
      projectName: '',
      description: '',
      userName: (userStore.getUserInfo as UserInfoRes).userName
    },
    saving: false,
    rules: {
      projectName: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.projectName === '') {
            return new Error(t('project.list.project_tips'))
          }
        }
      },
      userName: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.userName === '') {
            return new Error(t('project.list.username_tips'))
          }
        }
      }
    } as FormRules
  })

  const handleValidate = (statusRef: number) => {
    variables.projectFormRef.validate((errors: any) => {
      if (!errors) {
        statusRef === 0 ? submitProjectModal() : updateProjectModal()
      } else {
        return
      }
    })
  }

  const submitProjectModal = async () => {
    if (variables.saving) return
    variables.saving = true
    try {
      await createProject(variables.model)
      variables.saving = false
      resetForm()
      ctx.emit('confirmModal', props.showModalRef)
    } catch (err) {
      variables.saving = false
    }
  }

  const updateProjectModal = async () => {
    if (variables.saving) return
    variables.saving = true
    try {
      await updateProject(variables.model, props.row.code)
      variables.saving = false
      resetForm()
      ctx.emit('confirmModal', props.showModalRef)
    } catch (err) {
      variables.saving = false
    }
  }

  return { variables, t, handleValidate }
}
