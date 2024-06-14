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
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'

export function useFlinkMaterializedTable(model: { [field: string]: any }): IJsonItem[] {
    const { t } = useI18n()

    return [
        // mandatory field
        {
            type: 'input',
            field: 'identifier',
            name: t('project.node.identifier'),
            props: {
                placeholder: t('project.node.identifier_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.identifier_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'gatewayEndpoint',
            name: t('project.node.gateway_endpoint'),
            props: {
                placeholder: t('project.node.gateway_endpoint_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.gateway_endpoint_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'dynamicOptions',
            name: t('project.node.dynamic_options'),
            props: {
                placeholder: t('project.node.dynamic_options_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.dynamic_options_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'staticPartitions',
            name: t('project.node.static_partitions'),
            props: {
                placeholder: t('project.node.static_partitions_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.static_partitions_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'executionConfig',
            name: t('project.node.execution_config'),
            props: {
                placeholder: t('project.node.execution_config_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.execution_config_tips'))
                    }
                }
            }
        },

        {
            type: 'switch',
            field: 'isPeriodic',
            name: t('project.node.is_periodic'),
            span: 12
        },

        ...useCustomParams({ model, field: 'localParams', isSimple: false })
    ]
}