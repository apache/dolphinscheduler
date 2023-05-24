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

export function useZeppelin(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  return [
    {
      type: 'input',
      field: 'noteId',
      name: t('project.node.zeppelin_note_id'),
      props: {
        placeholder: t('project.node.zeppelin_note_id_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.zeppelin_note_id_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'paragraphId',
      name: t('project.node.zeppelin_paragraph_id'),
      props: {
        placeholder: t('project.node.zeppelin_paragraph_id_tips')
      }
    },
    {
      type: 'input',
      field: 'restEndpoint',
      name: t('project.node.zeppelin_rest_endpoint'),
      props: {
        placeholder: t('project.node.zeppelin_rest_endpoint_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.zeppelin_rest_endpoint_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'productionNoteDirectory',
      name: t('project.node.zeppelin_production_note_directory'),
      props: {
        placeholder: t('project.node.zeppelin_production_note_directory_tips')
      }
    },
    {
      type: 'input',
      field: 'username',
      name: t('project.node.zeppelin_username'),
      props: {
        placeholder: t('project.node.zeppelin_username_tips')
      }
    },
    {
      type: 'input',
      field: 'password',
      name: t('project.node.zeppelin_password'),
      props: {
        placeholder: t('project.node.zeppelin_password_tips')
      }
    },
    {
      type: 'input',
      field: 'parameters',
      name: t('project.node.zeppelin_parameters'),
      props: {
        placeholder: t('project.node.zeppelin_parameters_tips')
      }
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}
