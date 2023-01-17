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
import {
  queryDataFactoryFactories,
  queryDataFactoryPipelines,
  queryDataFactoryResourceGroups
} from '@/service/modules/azure'
import { onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useCustomParams } from '.'
import type { IJsonItem } from '../types'

export function useDataFactory(model: { [field: string]: any }): IJsonItem[] {
  const { t } = useI18n()

  const factoryOptions = ref([] as { label: string; value: number }[])
  const resourceGroupOptions = ref([] as { label: string; value: number }[])
  const pipelineOptions = ref([] as { label: string; value: number }[])

  const getFactoryOptions = async () => {
    const factories = await queryDataFactoryFactories()
    factoryOptions.value = factories.map((factory: string) => ({
      label: factory,
      value: factory
    }))
  }

  const getResourceGroupName = async () => {
    const groupNames = await queryDataFactoryResourceGroups()
    resourceGroupOptions.value = groupNames.map((groupName: string) => ({
      label: groupName,
      value: groupName
    }))
  }

  const getPipelineName = async (
    factoryName: string,
    resourceGroupName: string
  ) => {
    const pipelineNames = await queryDataFactoryPipelines({
      factoryName,
      resourceGroupName
    })
    pipelineOptions.value = pipelineNames.map((pipelineName: string) => ({
      label: pipelineName,
      value: pipelineName
    }))
  }

  const onChange = () => {
    model['pipelineName'] = ''
    if (model['factoryName'] && model['resourceGroupName']) {
      getPipelineName(model['factoryName'], model['resourceGroupName'])
    }
  }

  watch(
    () => model['pipelineName'],
    () => {
      if (model['pipelineName'] && pipelineOptions.value.length === 0) {
        getPipelineName(model['factoryName'], model['resourceGroupName'])
      }
    }
  )

  onMounted(() => {
    getFactoryOptions()
    getResourceGroupName()
  })

  return [
    {
      type: 'select',
      field: 'factoryName',
      span: 24,
      name: t('project.node.factory_name'),
      options: factoryOptions,
      props: {
        'on-update:value': onChange
      },
      validate: {
        required: true
      }
    },
    {
      type: 'select',
      field: 'resourceGroupName',
      span: 24,
      name: t('project.node.resource_group_name'),
      options: resourceGroupOptions,
      props: {
        'on-update:value': onChange
      },
      validate: {
        required: true
      }
    },
    {
      type: 'select',
      field: 'pipelineName',
      span: 24,
      name: t('project.node.pipeline_name'),
      options: pipelineOptions,
      validate: {
        required: true
      }
    },
    ...useCustomParams({
      model,
      field: 'localParams',
      isSimple: true
    })
  ]
}
