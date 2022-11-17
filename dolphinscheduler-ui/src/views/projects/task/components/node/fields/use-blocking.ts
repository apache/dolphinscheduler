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

import {useI18n} from "vue-i18n"
import type {IJsonItem} from "../types"
import {useTimeoutAlarm} from "@/views/projects/task/components/node/fields/use-timeout-alarm";
import {useRelationCustomParams} from "@/views/projects/task/components/node/fields/use-relation-custom-params";
import {useTaskNodeStore} from "@/store/project/task-node";

export function useBlocking(model: { [field: string]: any }) : IJsonItem[] {
    const {t} = useI18n()
    const blockingOpportunityOptions = [
        {
            label: t('project.node.blocking_on_condition_failed'),
            value: 'BlockingOnFailed'
        }, {
            label: t('project.node.blocking_on_condition_success'),
            value: 'BlockingOnSuccess'
        }
    ]
    const blockingAlertOptions = [
        {
            label: t('project.node.alert'),
            value: 'true'
        }, {
            label: t('project.node.do_not_alert'),
            value: 'false'
        }
    ]
    const taskStore = useTaskNodeStore()
    const preTaskOptions = taskStore.preTaskOptions.filter((option) =>
        taskStore.preTasks.includes(Number(option.value))
    )
    return [
        {
            type: 'radio',
            field: 'blockingOpportunity',
            name: t('project.node.blocking_opportunity'),
            options: blockingOpportunityOptions
        },
        {
            type: 'radio',
            field: 'isAlertWhenBlocking',
            options: blockingAlertOptions,
            name: t('project.node.alert_on_blocking')
        },
        ...useTimeoutAlarm(model),
        ...useRelationCustomParams({
            model,
            children: {
                type: 'custom-parameters',
                field: 'dependItemList',
                span: 18,
                children: [
                    {
                        type: 'select',
                        field: 'depTaskCode',
                        span: 10,
                        options: preTaskOptions
                    },
                    {
                        type: 'select',
                        field: 'status',
                        span: 10,
                        options: [
                            {
                                value: 'SUCCESS',
                                label: t('project.node.success')
                            },
                            {
                                value: 'FAILURE',
                                label: t('project.node.failed')
                            }
                        ]
                    }
                ]
            },
            childrenField: 'dependItemList',
            name: 'add_pre_task_check_condition'
        })
    ]
}