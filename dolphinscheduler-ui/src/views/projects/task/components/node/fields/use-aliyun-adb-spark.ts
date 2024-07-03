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
import {useCustomParams, useResources} from '.'
import type { IJsonItem } from '../types'
import {ref} from "vue";

export function useADBSPARK(model: { [field: string]: any}): IJsonItem[] {

    const { t } = useI18n()

    return [
        // mandatory field
        {
            type: 'input',
            field: 'dbClusterId',
            name: t('project.node.adb_cluster_id'),
            props: {
                placeholder: t('project.node.adb_cluster_id_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.adb_cluster_id_tips'))
                    }
                }
            }
        },

        {
            type: 'input',
            field: 'resourceGroupName',
            name: t('project.node.adb_resource_group_name'),
            props: {
                placeholder: t('project.node.adb_resource_group_name_tips')
            },
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                validator(validate: any, value: string) {
                    if (!value) {
                        return new Error(t('project.node.adb_resource_group_name_tips'))
                    }
                }
            }
        },

        {
            type: 'select',
            field: 'appType',
            name: t('project.node.adb_spark_app_type'),
            options: APP_TYPE,
            validate: {
                trigger: ['input', 'blur'],
                required: true,
                message: t('project.node.adb_spark_app_type_tips')
            },
            value: 'Batch'
        },

        {
            type: 'editor',
            field: 'data',
            name: t('project.node.adb_spark_data'),
            props: {
                placeholder: t('project.node.adb_spark_data_tips')
            }
        },
        ...useCustomParams({ model, field: 'localParams', isSimple: false })
    ]
}

export const APP_TYPE = [
    {
        value: 'Batch',
        label: 'Batch'
    },
    {
        value: 'SQL',
        label: 'SQL'
    }
]