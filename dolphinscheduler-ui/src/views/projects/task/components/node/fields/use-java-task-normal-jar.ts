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
import { computed, ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryResourceByProgramType } from '@/service/modules/resources'
import { useTaskNodeStore } from '@/store/project/task-node'
import utils from '@/utils'
import type { IJsonItem, ProgramType, IMainJar } from '../types'

export function useJavaTaskNormalJar(model: {
  [field: string]: any
}): IJsonItem[] {
  const { t } = useI18n()
  const mainJarOptions = ref([] as IMainJar[])
  const taskStore = useTaskNodeStore()

  const mainJarSpan = computed(() => (model.runType === 'NORMAL_JAR' ? 24 : 0))
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
    utils.removeUselessChildren(res)
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

  return [
    {
      type: 'input',
      field: 'mainClass',
      name: t('project.node.main_class'),
      span: mainJarSpan,
      props: {
        type: 'textarea',
        placeholder: t('project.node.main_class_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(_: any, value: string) {
          if (
            value &&
            !/^([A-Za-z_$][A-Za-z\d_$]*\.)*[A-Za-z_$][A-Za-z\d_$]*$/.test(
              value.trim()
            )
          ) {
            return new Error(t('project.node.main_class_invalid'))
          }
          return true
        }
      }
    },
    {
      type: 'tree-select',
      field: 'mainJar',
      name: t('project.node.main_package'),
      span: mainJarSpan,
      props: {
        checkable: true,
        cascade: true,
        showPath: true,
        checkStrategy: 'child',
        placeholder: t('project.node.main_package_tips'),
        keyField: 'fullName',
        labelField: 'name'
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(_: any, value: string) {
          if (!value) {
            return new Error(t('project.node.main_package_tips'))
          }
          return true
        }
      },
      options: mainJarOptions
    }
  ]
}
