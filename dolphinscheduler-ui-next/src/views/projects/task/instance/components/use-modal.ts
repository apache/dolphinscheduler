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

import { reactive, ref } from 'vue'
import { useAsyncState } from '@vueuse/core'
import { queryLog } from '@/service/modules/log'

export function useModal() {
  const variables = reactive({
    id: ref(''),
    loadingRef: ref(true),
    logRef: ref(''),
    skipLineNum: ref(0),
    limit: ref(1000)
  })

  const getLogs = () => {
    const { state } = useAsyncState(
      queryLog({
        taskInstanceId: Number(variables.id),
        limit: variables.limit,
        skipLineNum: variables.skipLineNum
      }).then((res: string) => {
        variables.logRef += res

        if (res) {
          variables.limit += 1000
          variables.skipLineNum += 1000
          getLogs()
        } else {
          variables.loadingRef = false
        }
      }),
      {}
    )

    return state
  }

  return {
    variables,
    getLogs
  }
}
