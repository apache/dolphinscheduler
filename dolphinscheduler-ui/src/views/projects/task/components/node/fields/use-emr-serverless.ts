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

export function useEmrServerless(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'input',
      field: 'applicationId',
      span: 24,
      name: t('project.node.emr_serverless_application_id'),
      props: {
        placeholder: t('project.node.emr_serverless_application_id_tips')
      },
      validate: {
        required: true,
        trigger: ['input', 'blur'],
        message: t('project.node.emr_serverless_application_id_tips')
      }
    },
    {
      type: 'input',
      field: 'executionRoleArn',
      span: 24,
      name: t('project.node.emr_serverless_execution_role_arn'),
      props: {
        placeholder: t('project.node.emr_serverless_execution_role_arn_tips')
      },
      validate: {
        required: true,
        trigger: ['input', 'blur'],
        message: t('project.node.emr_serverless_execution_role_arn_tips')
      }
    },
    {
      type: 'input',
      field: 'jobName',
      span: 24,
      name: t('project.node.emr_serverless_job_name'),
      props: {
        placeholder: t('project.node.emr_serverless_job_name_tips')
      }
    },
    {
      type: 'editor',
      field: 'startJobRunRequestJson',
      span: 24,
      name: t('project.node.emr_serverless_start_job_run_json'),
      props: {
        language: 'json'
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.emr_serverless_start_job_run_json_tips')
      }
    },
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: true
    })
  ]
}
