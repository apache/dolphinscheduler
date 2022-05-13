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
import {useI18n} from 'vue-i18n'
import {useCustomParams} from '.'
import type {IJsonItem} from '../types'
import {computed} from "vue";

export function useMlflow(model: { [field: string]: any }): IJsonItem[] {
    const {t} = useI18n()
    const registerModelSpan = computed(() => (model.registerModel ? 12 : 0))
    const isSearchParamsSpan = computed(() => (model.isSearchParams ? 24 : 0))

    return [
        {
            type: 'input',
            field: 'mlflowTrackingUri',
            name: t('project.node.mlflow_mlflowTrackingUri'),
            span: 12,
            props: {
                placeholder: t('project.node.mlflow_mlflowTrackingUri_tips'),
            },
            validate: {
                trigger: ['input', 'blur'],
                required: false,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.mlflow_mlflowTrackingUri_error_tips'))
                    }
                }
            }
        },
        {
            type: 'select',
            field: 'algorithm',
            name: t('project.node.mlflow_algorithm'),
            span: 12,
            options: ALGORITHM,
        },
        {
            type: 'input',
            field: 'dataPath',
            name: t('project.node.mlflow_dataPath'),
            props: {
                placeholder: t('project.node.mlflow_dataPath_tips')
            },
        },
        {
            type: 'input',
            field: 'experimentName',
            name: t('project.node.mlflow_experimentName'),
            span: 24,
            props: {
                placeholder: t('project.node.mlflow_experimentName_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: false,
                // validator(validate: any, value: string) {
                //     if (!value) {
                //         return new Error(t('project.node.mlflow_experimentName_tips'))
                //     }
                // }
            }
        },
        {
            type: 'switch',
            field: 'registerModel',
            name: t('project.node.mlflow_registerModel'),
            span: 4,
        },
        {
            type: 'input',
            field: 'modelName',
            name: t('project.node.mlflow_modelName'),
            span: registerModelSpan,
            props: {
                placeholder: t('project.node.mlflow_modelName_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: false,
                // validator(validate: any, value: string) {
                //     if (!value) {
                //         return new Error(t('project.node.mlflow_modelName_tips'))
                //     }
                // }
            }
        },

        {
            type: 'input',
            field: 'params',
            name: t('project.node.mlflow_params'),
            props: {
                placeholder: t('project.node.mlflow_params_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: false,
                // validator(validate: any, value: string) {
                //     if (!value) {
                //         return new Error(t('project.node.mlflow_params_tips'))
                //     }
                // }
            }
        },
        {
            type: 'switch',
            field: 'isSearchParams',
            name: t('project.node.mlflow_isSearchParams'),
            span: 6,
        },
        {
            type: 'input',
            field: 'searchParams',
            name: t('project.node.mlflow_searchParams'),
            props: {
                placeholder: t('project.node.mlflow_searchParams_tips')
            },
            span: isSearchParamsSpan,
            validate: {
                trigger: ['input', 'blur'],
                required: false,
            }
        },
        ...useCustomParams({model, field: 'localParams', isSimple: false})
    ]
}

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
