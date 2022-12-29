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
import { useCustomParams, useNamespace } from '.'

export function useKubeflow(model: { [field: string]: any }): IJsonItem[] {
  return [
    useNamespace(),
    {
      type: 'editor',
      field: 'yamlContent',
      name: 'yamlContent',
      props: {
        language: 'yaml'
      },
      validate: {
        trigger: ['input', 'trigger'],
        required: true,
        message: 'requestJson'
      }
    },
    ...useCustomParams({ model, field: 'localParams', isSimple: false })
  ]
}
