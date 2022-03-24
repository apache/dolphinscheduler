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
import { watch, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useDeployMode, useResources, useCustomParams } from '.'
import type { IJsonItem } from '../types'

export function useSeaTunnel(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const masterTypeOptions = [
    {
      label: 'yarn',
      value: 'yarn'
    },
    {
      label: 'local',
      value: 'local'
    },
    {
      label: 'spark://',
      value: 'spark://'
    },
    {
      label: 'mesos://',
      value: 'mesos://'
    }
  ]

  const queueOptions = [
    {
      label: 'default',
      value: 'default'
    }
  ]

  const masterSpan = computed(() => (model.deployMode === 'local' ? 0 : 12))
  const queueSpan = computed(() =>
    model.deployMode === 'local' || model.master != 'yarn' ? 0 : 12
  )
  const masterUrlSpan = computed(() =>
    model.deployMode === 'local' ||
    (model.master != 'spark://' && model.master != 'mesos://')
      ? 0
      : 12
  )

  const baseScript = 'sh ${WATERDROP_HOME}/bin/start-waterdrop.sh'

  const parseRawScript = () => {
    if (model.rawScript) {
      model.rawScript.split('\n').forEach((script: string) => {
        const params = script.replace(baseScript, '').split('--')
        params?.forEach((param: string) => {
          const pair = param.split(' ')
          if (pair && pair.length >= 2) {
            if (pair[0] === 'master') {
              const prefix = pair[1].substring(0, 8)
              if (pair[1] && (prefix === 'mesos://' || prefix === 'spark://')) {
                model.master = prefix
                model.masterUrl = pair[1].substring(8, pair[1].length)
              } else {
                model.master = pair[1]
              }
            } else if (pair[0] === 'deploy-mode') {
              model.deployMode = pair[1]
            } else if (pair[0] === 'queue') {
              model.queue = pair[1]
            }
          }
        })
      })
    }
  }

  watch(
    () => model.rawScript,
    () => {
      parseRawScript()
    }
  )

  return [
    useDeployMode(),
    {
      type: 'select',
      field: 'master',
      name: t('project.node.sea_tunnel_master'),
      options: masterTypeOptions,
      value: model.master,
      span: masterSpan
    },
    {
      type: 'input',
      field: 'masterUrl',
      name: t('project.node.sea_tunnel_master_url'),
      value: model.masterUrl,
      span: masterUrlSpan,
      props: {
        placeholder: t('project.node.sea_tunnel_master_url_tips')
      }
    },
    {
      type: 'select',
      field: 'queue',
      name: t('project.node.sea_tunnel_queue'),
      options: queueOptions,
      value: model.queue,
      span: queueSpan
    },
    useResources(),
    ...useCustomParams({ model, field: 'localParams', isSimple: true })
  ]
}
