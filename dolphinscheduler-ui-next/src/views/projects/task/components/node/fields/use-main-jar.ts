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
import { queryResourceByProgramType } from '@/service/modules/resources'
import { useTaskNodeStore } from '@/store/project/task-node'
import { removeUselessChildren } from '@/utils/tree-format'
import type { IJsonItem, ProgramType, IMainJar } from '../types'

export function useMainJar(model: { [field: string]: any }): IJsonItem {
  const { t } = useI18n()
  const mainJarOptions = ref([] as IMainJar[])
  const taskStore = useTaskNodeStore()

  const getMainJars = async (programType: ProgramType) => {
    const storeMainJar = taskStore.getMainJar(programType)
    if (storeMainJar) {
      mainJarOptions.value = storeMainJar
      return
    }
    const res = await queryResourceByProgramType({
      type: 'FILE',
      programType
    })
    removeUselessChildren(res)
    mainJarOptions.value = res || []
    taskStore.updateMainJar(programType, res)
  }

  onMounted(() => {
    getMainJars(model.programType)
  })

  watch(
    () => model.programType,
    (value) => {
      getMainJars(value)
    }
  )

  return {
    type: 'tree-select',
    field: 'mainJar',
    name: t('project.node.main_package'),
    props: {
      cascade: true,
      showPath: true,
      checkStrategy: 'child',
      placeholder: t('project.node.main_package_tips'),
      keyField: 'id',
      labelField: 'fullName'
    },
    validate: {
      trigger: ['input', 'blur'],
      required: true,
      validator(validate: any, value: string) {
        if (!value) {
          return new Error(t('project.node.main_package_tips'))
        }
      }
    },
    options: mainJarOptions
  }
}
