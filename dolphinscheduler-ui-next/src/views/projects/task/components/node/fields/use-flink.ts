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
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { queryResourceByProgramType } from '@/service/modules/resources'
import { removeUselessChildren } from './use-shell'
import { useCustomParams, useDeployMode } from '.'
import type { IJsonItem, ProgramType } from '../types'

export function useFlink(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const mainClassSpan = computed(() =>
    model.programType === 'PYTHON' ? 0 : 24
  )

  const taskManagerNumberSpan = computed(() =>
    model.flinkVersion === '<1.10' && model.deployMode === 'cluster' ? 12 : 0
  )

  const deployModeSpan = computed(() =>
    model.deployMode === 'cluster' ? 12 : 0
  )

  const mainJarOptions = ref([])
  const resources: { [field: string]: any } = {}

  const getResourceList = async (programType: ProgramType) => {
    if (resources[programType] !== void 0) {
      mainJarOptions.value = resources[programType]
      return
    }
    const res = await queryResourceByProgramType({
      type: 'FILE',
      programType
    })
    removeUselessChildren(res)
    mainJarOptions.value = res || []
    resources[programType] = res
  }

  onMounted(() => {
    getResourceList(model.programType)
  })

  return [
    {
      type: 'select',
      field: 'programType',
      span: 12,
      name: t('project.node.program_type'),
      options: PROGRAM_TYPES,
      props: {
        'on-update:value': (value: ProgramType) => {
          model.mainJar = null
          model.mainClass = ''
          getResourceList(value)
        }
      },
      value: model.programType
    },
    {
      type: 'input',
      field: 'mainClass',
      span: mainClassSpan,
      name: t('project.node.main_class'),
      props: {
        placeholder: t('project.node.main_class_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: model.programType !== 'PYTHON',
        validator(validate: any, value: string) {
          if (model.programType !== 'PYTHON' && !value) {
            return new Error(t('project.node.main_class_tips'))
          }
        }
      }
    },
    {
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
        required: model.programType !== 'PYTHON',
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.main_package_tips'))
          }
        }
      },
      options: mainJarOptions
    },
    useDeployMode(),
    {
      type: 'select',
      field: 'flinkVersion',
      span: 12,
      name: t('project.node.flink_version'),
      options: FLINK_VERSIONS,
      value: model.flinkVersion
    },
    {
      type: 'input',
      field: 'appName',
      name: t('project.node.app_name'),
      props: {
        placeholder: t('project.node.app_name_tips')
      }
    },
    {
      type: 'input',
      field: 'jobManagerMemory',
      name: t('project.node.job_manager_memory'),
      span: deployModeSpan,
      props: {
        placeholder: t('project.node.job_manager_memory_tips'),
        min: 1
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: string) {
          if (!value) {
            return
          }
          if (!Number.isInteger(parseInt(value))) {
            return new Error(
              t('project.node.job_manager_memory_tips') +
                t('project.node.positive_integer_tips')
            )
          }
        }
      }
    },
    {
      type: 'input',
      field: 'taskManagerMemory',
      name: t('project.node.task_manager_memory'),
      span: deployModeSpan,
      props: {
        placeholder: t('project.node.task_manager_memory_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        validator(validate: any, value: string) {
          if (!value) {
            return
          }
          if (!Number.isInteger(parseInt(value))) {
            return new Error(
              t('project.node.task_manager_memory') +
                t('project.node.positive_integer_tips')
            )
          }
        }
      },
      value: model.taskManagerMemory
    },
    {
      type: 'input-number',
      field: 'slot',
      name: t('project.node.slot_number'),
      span: deployModeSpan,
      props: {
        placeholder: t('project.node.slot_number_tips'),
        min: 1
      },
      value: model.slot
    },
    {
      type: 'input-number',
      field: 'taskManager',
      name: t('project.node.task_manager_number'),
      span: taskManagerNumberSpan,
      props: {
        placeholder: t('project.node.task_manager_number_tips')
      },
      value: model.taskManager
    },
    {
      type: 'input-number',
      field: 'parallelism',
      name: t('project.node.parallelism'),
      span: 12,
      props: {
        placeholder: t('project.node.parallelism_tips'),
        min: 1
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.parallelism_tips'))
          }
        }
      },
      value: model.parallelism
    },
    {
      type: 'input',
      field: 'mainArgs',
      name: t('project.node.main_arguments'),
      props: {
        type: 'textarea',
        placeholder: t('project.node.main_arguments_tips')
      }
    },
    {
      type: 'input',
      field: 'others',
      name: t('project.node.option_parameters'),
      props: {
        type: 'textarea',
        placeholder: t('project.node.option_parameters_tips')
      }
    },
    {
      type: 'tree-select',
      field: 'resourceList',
      name: t('project.node.resources'),
      options: mainJarOptions,
      props: {
        multiple: true,
        checkable: true,
        cascade: true,
        showPath: true,
        checkStrategy: 'child',
        placeholder: t('project.node.resources_tips'),
        keyField: 'id',
        labelField: 'name'
      }
    },
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: true
    })
  ]
}

const PROGRAM_TYPES = [
  {
    label: 'JAVA',
    value: 'JAVA'
  },
  {
    label: 'SCALA',
    value: 'SCALA'
  },
  {
    label: 'PYTHON',
    value: 'PYTHON'
  }
]

const FLINK_VERSIONS = [
  {
    label: '<1.10',
    value: '<1.10'
  },
  {
    label: '>=1.10',
    value: '>=1.10'
  }
]
