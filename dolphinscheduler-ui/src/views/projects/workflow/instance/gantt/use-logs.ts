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

import { onBeforeUnmount, ref } from 'vue'
import { queryLog } from '@/service/modules/log'
import { downloadLog } from '@/service/modules/task-instances'
import type { GanttRow } from './type'

export function useGanttLogs(t: (key: string) => string) {
  const selected = ref<GanttRow>()
  const visible = ref(false)
  const text = ref('')
  const loading = ref(false)
  let generation = 0
  const close = () => {
    generation++
    visible.value = false
    loading.value = false
  }
  const refresh = async () => {
    const id = selected.value?.id
    if (!id) return
    const request = ++generation
    text.value = ''
    loading.value = true
    let skipLineNum = 0
    try {
      // Bound the live preview; the existing download action returns the complete log.
      while (skipLineNum < 20000) {
        const result = await queryLog({
          taskInstanceId: id,
          limit: 1000,
          skipLineNum
        })
        if (request !== generation) return
        text.value += result.message || ''
        if (!result.message || !result.lineNum) break
        skipLineNum += result.lineNum
      }
      if (skipLineNum >= 20000)
        text.value += `\n${t('project.workflow.gantt_log_limit')}`
      if (!text.value) text.value = t('project.workflow.gantt_no_log')
    } catch {
      if (request === generation)
        text.value += `\n${t('project.workflow.gantt_log_error')}`
    } finally {
      if (request === generation) loading.value = false
    }
  }
  const open = (row: GanttRow) => {
    if (!row.logAvailable) return
    selected.value = row
    visible.value = true
    void refresh()
  }
  const download = () => {
    if (selected.value?.id) downloadLog(selected.value.id)
  }
  onBeforeUnmount(close)
  return { selected, visible, text, loading, open, close, refresh, download }
}
