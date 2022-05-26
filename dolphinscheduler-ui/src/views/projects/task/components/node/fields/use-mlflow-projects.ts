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
import type { IJsonItem } from '../types'
import { watch, ref } from 'vue'

export function useMlflowProjects(model: {
  [field: string]: any
}): IJsonItem[] {
  const { t } = useI18n()

  const experimentNameSpan = ref(0)
  const registerModelSpan = ref(0)
  const modelNameSpan = ref(0)
  const mlflowJobTypeSpan = ref(0)
  const dataPathSpan = ref(0)
  const paramsSpan = ref(0)

  const setFlag = () => {
    model.isProjects = model.mlflowTaskType === 'MLflow Projects' ? true : false
  }

  const resetSpan = () => {
    experimentNameSpan.value = model.isProjects ? 12 : 0
    mlflowJobTypeSpan.value = model.isProjects ? 12 : 0
    paramsSpan.value = model.isProjects ? 24 : 0
    registerModelSpan.value =
      model.isProjects && model.mlflowJobType != 'CustomProject' ? 6 : 0
    dataPathSpan.value =
      model.isProjects && model.mlflowJobType != 'CustomProject' ? 24 : 0
  }

  watch(
    () => [model.mlflowTaskType, model.mlflowJobType],
    () => {
      setFlag()
      resetSpan()
    }
  )

  watch(
    () => [model.registerModel],
    () => {
      modelNameSpan.value = model.isProjects && model.registerModel ? 6 : 0
    }
  )

  setFlag()
  resetSpan()

  return [
    {
      type: 'select',
      field: 'mlflowJobType',
      name: t('project.node.mlflow_jobType'),
      span: mlflowJobTypeSpan,
      options: MLFLOW_JOB_TYPE
    },
    {
      type: 'input',
      field: 'experimentName',
      name: t('project.node.mlflow_experimentName'),
      span: experimentNameSpan,
      props: {
        placeholder: t('project.node.mlflow_experimentName_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: false
      }
    },
    {
      type: 'switch',
      field: 'registerModel',
      name: t('project.node.mlflow_registerModel'),
      span: registerModelSpan
    },
    {
      type: 'input',
      field: 'modelName',
      name: t('project.node.mlflow_modelName'),
      span: modelNameSpan,
      props: {
        placeholder: t('project.node.mlflow_modelName_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: false
      }
    },
    {
      type: 'input',
      field: 'dataPath',
      name: t('project.node.mlflow_dataPath'),
      span: dataPathSpan,
      props: {
        placeholder: t('project.node.mlflow_dataPath_tips')
      }
    },
    {
      type: 'input',
      field: 'params',
      name: t('project.node.mlflow_params'),
      span: paramsSpan,
      props: {
        placeholder: t('project.node.mlflow_params_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: false
      }
    },
    ...useBasicAlgorithm(model),
    ...useAutoML(model),
    ...useCustomProject(model)
  ]
}

export function useBasicAlgorithm(model: {
  [field: string]: any
}): IJsonItem[] {
  const { t } = useI18n()

  const algorithmSpan = ref(0)
  const searchParamsSpan = ref(0)

  const setFlag = () => {
    model.isBasicAlgorithm =
      model.mlflowJobType === 'BasicAlgorithm' &&
      model.mlflowTaskType === 'MLflow Projects'
        ? true
        : false
  }

  const resetSpan = () => {
    algorithmSpan.value = model.isBasicAlgorithm ? 24 : 0
    searchParamsSpan.value = model.isBasicAlgorithm ? 24 : 0
  }

  watch(
    () => [model.mlflowTaskType, model.mlflowJobType],
    () => {
      setFlag()
      resetSpan()
    }
  )
  setFlag()
  resetSpan()

  return [
    {
      type: 'select',
      field: 'algorithm',
      name: t('project.node.mlflow_algorithm'),
      span: algorithmSpan,
      options: ALGORITHM
    },
    {
      type: 'input',
      field: 'searchParams',
      name: t('project.node.mlflow_searchParams'),
      props: {
        placeholder: t('project.node.mlflow_searchParams_tips')
      },
      span: searchParamsSpan,
      validate: {
        trigger: ['input', 'blur'],
        required: false
      }
    }
  ]
}

export function useAutoML(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const automlToolSpan = ref(0)

  const setFlag = () => {
    model.isAutoML =
      model.mlflowJobType === 'AutoML' &&
      model.mlflowTaskType === 'MLflow Projects'
        ? true
        : false
  }

  const resetSpan = () => {
    automlToolSpan.value = model.isAutoML ? 12 : 0
  }

  watch(
    () => [model.mlflowTaskType, model.mlflowJobType],
    () => {
      setFlag()
      resetSpan()
    }
  )

  setFlag()
  resetSpan()

  return [
    {
      type: 'select',
      field: 'automlTool',
      name: t('project.node.mlflow_automlTool'),
      span: automlToolSpan,
      options: AutoMLTOOL
    }
  ]
}

export function useCustomProject(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const mlflowProjectRepositorySpan = ref(0)
  const mlflowProjectVersionSpan = ref(0)
  const customParamsSpan = ref(0)

  const setFlag = () => {
    model.isCustomProject =
      model.mlflowJobType === 'CustomProject' &&
      model.mlflowTaskType === 'MLflow Projects'
        ? true
        : false
  }

  const resetSpan = () => {
    mlflowProjectRepositorySpan.value = model.isCustomProject ? 24 : 0
    mlflowProjectVersionSpan.value = model.isCustomProject ? 12 : 0
    customParamsSpan.value = model.isCustomProject ? 24 : 0
  }

  watch(
    () => [model.mlflowTaskType, model.mlflowJobType],
    () => {
      setFlag()
      resetSpan()
    }
  )

  setFlag()
  resetSpan()

  return [
    {
      type: 'input',
      field: 'mlflowProjectRepository',
      name: t('project.node.mlflowProjectRepository'),
      span: mlflowProjectRepositorySpan,
      props: {
        placeholder: t('project.node.mlflowProjectRepository_tips')
      }
    },
    {
      type: 'input',
      field: 'mlflowProjectVersion',
      name: t('project.node.mlflowProjectVersion'),
      span: mlflowProjectVersionSpan,
      props: {
        placeholder: t('project.node.mlflowProjectVersion_tips')
      }
    }
  ]
}

export const MLFLOW_JOB_TYPE = [
  {
    label: 'BasicAlgorithm',
    value: 'BasicAlgorithm'
  },
  {
    label: 'AutoML',
    value: 'AutoML'
  },
  {
    label: 'Custom Project',
    value: 'CustomProject'
  }
]
export const ALGORITHM = [
  {
    label: 'svm',
    value: 'svm'
  },
  {
    label: 'lr',
    value: 'lr'
  },
  {
    label: 'lightgbm',
    value: 'lightgbm'
  },
  {
    label: 'xgboost',
    value: 'xgboost'
  }
]
export const AutoMLTOOL = [
  {
    label: 'autosklearn',
    value: 'autosklearn'
  },
  {
    label: 'flaml',
    value: 'flaml'
  }
]
