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
<<<<<<< HEAD
=======

>>>>>>> 2ad2801cd0 ('[refactor]flinksql')
import { computed, ref, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryResourceByProgramType } from '@/service/modules/resources'
import { useTaskNodeStore } from '@/store/project/task-node'
import utils from '@/utils'
import type { IJsonItem, ProgramType, IMainJar } from '../types'

export function useMainJar(model: { [field: string]: any }): IJsonItem {
  const { t } = useI18n()
  const mainJarOptions = ref([] as IMainJar[])
  const taskStore = useTaskNodeStore()

  const mainJarsSpan = computed(() =>
      model.programType === 'SQL' ? 0 : 24
  )

  const mainJarSpan = computed(() => (model.programType === 'SQL' ? 0 : 24))
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

  return {
    type: 'tree-select',
    field: 'mainJar',
    name: t('project.node.main_package'),
<<<<<<< HEAD
<<<<<<< HEAD
    span: mainJarSpan,
=======
    span: span,
>>>>>>> 4d65bc7c46 ('[refactor]flinksql')
=======
    span: mainJarsSpan,
>>>>>>> 2ad2801cd0 ('[refactor]flinksql')
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
<<<<<<< HEAD
<<<<<<< HEAD
      required: model.programType !== 'SQL',
=======
      // required: true,
      required: require,
>>>>>>> 7c048165d6 (flink_sql)
=======
      required: model.programType !== 'SQL',
>>>>>>> 2ad2801cd0 ('[refactor]flinksql')
      validator(validate: any, value: string) {
        if (!value && model.programType !== 'SQL') {
          return new Error(t('project.node.main_package_tips'))
        }
      }
    },
    options: mainJarOptions
  }
}
