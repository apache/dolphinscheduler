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

import { ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  querySimpleList,
  queryProcessDefinitionByCode
} from '@/service/modules/process-definition'
import type { IJsonItem } from '../types'
import { queryProjectListPaging } from '@/service/modules/projects'
import type { ProjectRes, ProjectList } from '@/service/modules/projects/types'

export function useChildNode({
  model,
  projectCode,
  from,
  processName,
  code
}: {
  model: { [field: string]: any }
  projectCode: number
  from?: number
  processName?: number
  code?: number
}): IJsonItem[] {
  const { t } = useI18n()

  const options = ref([] as { label: string; value: string }[])
  const accessibleProjectList = ref([] as ProjectList[])
  const loading = ref(false)

  const diffCode = ref(0)

  const getProcessList = async (project_code: number) => {
    loading.value = true
    const res = await querySimpleList(project_code)
    options.value = res
      .filter((option: { name: string; code: number }) => option.code !== code)
      .map((option: { name: string; code: number }) => ({
        label: option.name,
        value: option.code
      }))
    loading.value = false
  }
  const getProcessListByCode = async (processCode: number) => {
    if (!processCode) return
    const res = await queryProcessDefinitionByCode(processCode, projectCode)
    model.definition = res
  }

  const getAccessibleProjectList = async () => {
    queryProjectListPaging({
      pageNo: 1,
      pageSize: 999
    })
      .then((res: ProjectRes) => {
        accessibleProjectList.value = res.totalList
      })
      .catch(() => {
        accessibleProjectList.value = []
      })
  }

  watch(
    () => model?.childNodeProjectCode,
    (val) => {
      diffCode.value = val || projectCode
    }
  )

  onMounted(() => {
    if (from === 1 && processName) {
      getProcessListByCode(processName)
    }
    getAccessibleProjectList()

    getProcessList(projectCode).then(() => {
      if (diffCode.value && diffCode.value != projectCode) {
        getProcessList(diffCode.value)
      }
    })
  })

  return [
    {
      type: 'select',
      field: 'childNodeProjectCode',
      span: 24,
      name: t('project.node.child_node_project'),
      props: {
        'label-field': 'name',
        'value-field': 'code',
        'onUpdate:value': (value: any) => {
          model.processDefinitionCode = null
          getProcessList(value || projectCode)
        },
        filterable: true,
        clearable: true,
        placeholder: t('project.node.child_node_project_tips')
      },
      options: accessibleProjectList,
      class: 'select-project-name'
    },
    {
      type: 'select',
      field: 'processDefinitionCode',
      span: 24,
      name: t('project.node.child_node'),
      props: {
        loading: loading,
        filterable: true
      },
      options: options,
      class: 'select-child-node',
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(unuse: any, value: number) {
          if (!value) {
            return Error(t('project.node.child_node_tips'))
          }
        }
      }
    }
  ]
}
