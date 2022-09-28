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
import { useCustomParams, useResources } from '.'
import type { IJsonItem } from '../types'

export function useOpenmldb(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()
  const options = [
    {
      label: t('project.node.openmldb_execute_mode_offline'),
      value: 'offline'
    },
    {
      label: t('project.node.openmldb_execute_mode_online'),
      value: 'online'
    }
  ]
  return [
    {
      type: 'input',
      field: 'zk',
      name: t('project.node.openmldb_zk_address'),
      props: {
        placeholder: t('project.node.openmldb_zk_address_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.openmldb_zk_address_tips'))
          }
        }
      }
    },
    {
      type: 'input',
      field: 'zkPath',
      name: t('project.node.openmldb_zk_path'),
      props: {
        placeholder: t('project.node.openmldb_zk_path_tips')
      },
      validate: {
        trigger: ['input', 'blur'],
        required: true,
        validator(validate: any, value: string) {
          if (!value) {
            return new Error(t('project.node.openmldb_zk_path_tips'))
          }
        }
      }
    },
    {
      type: 'radio',
      field: 'executeMode',
      name: t('project.node.openmldb_execute_mode'),
      options: options
    },
    {
      type: 'editor',
      field: 'sql',
      name: t('project.node.sql_statement'),
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: t('project.node.sql_empty_tips')
      }
    },
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}
