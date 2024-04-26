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

export function useAliyunServerlessSpark(model: { [field: string]: any }): IJsonItem[] {
    const { t } = useI18n()

    return [
        // mandatory field
        {
            type: 'input',
            field: 'regionId',
            name: t('project.node.region_id'),
            props: {
                placeholder: t('project.node.region_id_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.region_id_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'accessKeyId',
            name: t('project.node.access_key_id'),
            props: {
                placeholder: t('project.node.access_key_id_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.access_key_id_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'accessKeySecret',
            name: t('project.node.access_key_secret'),
            props: {
                placeholder: t('project.node.access_key_secret_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.access_key_secret_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'workspaceId',
            name: t('project.node.workspace_id'),
            props: {
                placeholder: t('project.node.workspace_id_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.workspace_id_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'resourceQueueId',
            name: t('project.node.resource_queue_id'),
            props: {
                placeholder: t('project.node.resource_queue_id_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.resource_queue_id_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'codeType',
            name: t('project.node.code_type'),
            props: {
                placeholder: t('project.node.code_type_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.code_type_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'jobName',
            name: t('project.node.job_name'),
            props: {
                placeholder: t('project.node.job_name_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.job_name_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'entryPoint',
            name: t('project.node.entry_point'),
            props: {
                placeholder: t('project.node.entry_point_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.entry_point_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'entryPointArguments',
            name: t('project.node.entry_point_arguments'),
            props: {
                placeholder: t('project.node.entry_point_arguments_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.entry_point_arguments_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'sparkSubmitParameters',
            name: t('project.node.spark_submit_parameters'),
            props: {
                placeholder: t('project.node.spark_submit_parameters_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.spark_submit_parameters_tips'))
                    }
                }
            }
        },

        // optional field
        {
            type: 'input',
            field: 'engineReleaseVersion',
            name: t('project.node.engine_release_version'),
            props: {
                placeholder: t('project.node.engine_release_version_tips')
            }
        },

        {
            type: 'switch',
            field: 'isProduction',
            name: t('project.node.is_production'),
            span: 12
        },

        ...useCustomParams({ model, field: 'localParams', isSimple: false })
    ]
}
