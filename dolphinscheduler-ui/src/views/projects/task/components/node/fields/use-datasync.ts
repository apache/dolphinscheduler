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

import type { IJsonItem } from '../types'
import { watch, ref } from 'vue'
import { useCustomParams } from '.'

export function useDatasync(model: { [field: string]: any }): IJsonItem[] {
  const jsonSpan = ref(0)
  const destinationLocationArnSpan = ref(0)
  const sourceLocationArnSpan = ref(0)
  const nameSpan = ref(0)
  const cloudWatchLogGroupArnSpan = ref(0)

  const resetSpan = () => {
    jsonSpan.value = model.jsonFormat ? 24 : 0
    destinationLocationArnSpan.value = model.jsonFormat ? 0 : 24
    sourceLocationArnSpan.value = model.jsonFormat ? 0 : 24
    nameSpan.value = model.jsonFormat ? 0 : 24
    cloudWatchLogGroupArnSpan.value = model.jsonFormat ? 0 : 24
  }

  watch(
    () => [model.jsonFormat],
    () => {
      resetSpan()
    }
  )

  resetSpan()

  return [
    {
      type: 'switch',
      field: 'jsonFormat',
      name: 'jsonFormat',
      span: 12
    },
    {
      type: 'editor',
      field: 'json',
      name: 'json',
      span: jsonSpan
    },
    {
      type: 'input',
      field: 'destinationLocationArn',
      name: 'destinationLocationArn',
      span: destinationLocationArnSpan
    },
    {
      type: 'input',
      field: 'sourceLocationArn',
      name: 'sourceLocationArn',
      span: sourceLocationArnSpan
    },
    {
      type: 'input',
      field: 'name',
      name: 'name',
      span: nameSpan
    },
    {
      type: 'input',
      field: 'cloudWatchLogGroupArn',
      name: 'cloudWatchLogGroupArn',
      span: cloudWatchLogGroupArnSpan
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}
