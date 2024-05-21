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
import type { Router } from 'vue-router'
import {
  createProjectParameter,
  updateProjectParameter
} from '@/service/modules/projects-parameter'
import {
  ProjectParameterReq,
  UpdateProjectParameterReq
} from '@/service/modules/projects-parameter/types'
import { useRouter } from 'vue-router'
import { DEFAULT_DATA_TYPE } from "@/views/projects/parameter/data_type";

export function useModal(
  props: any,
  ctx: SetupContext<('cancelModal' | 'confirmModal')[]>
) {
  const { t } = useI18n()
  const router: Router = useRouter()

  const variables = reactive({
    formRef: ref(),
    projectCode: ref(Number(router.currentRoute.value.params.projectCode)),
    model: {
      code: ref<number>(-1),
      projectParameterName: ref(''),
      projectParameterValue: ref(''),
      projectParameterDataType: ref(DEFAULT_DATA_TYPE)
    },
    saving: false,
    rules: {
      name: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.projectParameterName === '') {
            return new Error(t('project.parameter.name_tips'))
          }
        }
      },
      value: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.projectParameterValue === '') {
            return new Error(t('project.parameter.value_tips'))
          }
        }
      },
      data_type: {
        required: true,
        trigger: ['input', 'blur'],
        validator() {
          if (variables.model.projectParameterDataType === '') {
            return new Error(t('project.parameter.data_type_tips'))
          }
        }
      }
    }
  })

  const handleValidate = async (statusRef: number) => {
    // await variables.formRef.validate()

    if (variables.saving) return
    variables.saving = true

    try {
      statusRef === 0 ? await submitModal() : await updateModal()
      variables.saving = false
    } catch (err) {
      variables.saving = false
    }
  }

  const submitModal = () => {
    const data: ProjectParameterReq = {
      projectParameterName: variables.model.projectParameterName,
      projectParameterValue: variables.model.projectParameterValue,
      projectParameterDataType: variables.model.projectParameterDataType
    }

    createProjectParameter(data, variables.projectCode).then(() => {
      variables.model.projectParameterName = ''
      variables.model.projectParameterValue = ''
      variables.model.projectParameterDataType = DEFAULT_DATA_TYPE
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  const updateModal = () => {
    const data: UpdateProjectParameterReq = {
      code: variables.model.code,
      projectParameterName: variables.model.projectParameterName,
      projectParameterValue: variables.model.projectParameterValue,
      projectParameterDataType: variables.model.projectParameterDataType
    }

    updateProjectParameter(data, variables.projectCode).then(() => {
      ctx.emit('confirmModal', props.showModalRef)
    })
  }

  return {
    variables,
    handleValidate
  }
}
