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

import { defineComponent, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { NTabPane, NTabs } from 'naive-ui'
import { useRouter } from 'vue-router'
import BatchTaskInstance from './batch-task'
import StreamTaskInstance from './stream-task'
import type { Router } from 'vue-router'


const TaskDefinition = defineComponent({
  name: 'task-instance',
  setup() {
    const { t } = useI18n()
    const selectedTab = ref<string>()
    const router: Router = useRouter()
    if (router.currentRoute.value.query.taskExecuteType) {
      selectedTab.value = router.currentRoute.value.query.taskExecuteType.toString()
    }
    const onTabChange = (newSelectedTab: string) => {
      if (router.currentRoute.value.query.taskExecuteType) {
        selectedTab.value = newSelectedTab
      }
    }
    return () => (
      <NTabs value={selectedTab.value} type='line' animated onUpdate:value={onTabChange}>
        <NTabPane name='Batch' tab={t('project.task.batch_task')}>
          <BatchTaskInstance />
        </NTabPane>
        <NTabPane name='Stream' tab={t('project.task.stream_task')}>
          <StreamTaskInstance />
        </NTabPane>
      </NTabs>
    )
  }
})

export default TaskDefinition
