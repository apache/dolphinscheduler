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
import { NGrid, NGi, NNumberAnimation } from 'naive-ui'
import { useStatistics } from './use-statistics'
import { useI18n } from 'vue-i18n'
import Card from '@/components/card'
import styles from './index.module.scss'

const statistics = defineComponent({
  name: 'statistics',
  setup() {
    const { t } = useI18n()
    const { getStatistics } = useStatistics()
    const statisticsRef = ref(getStatistics())

    return { t, statisticsRef }
  },
  render() {
    const { t, statisticsRef } = this

    return (
      <NGrid x-gap='12' y-gap='8' cols='2' responsive='screen'>
        <NGi>
          <Card
            title={t(
              'monitor.statistics.command_number_of_waiting_for_running'
            )}
          >
            <div class={styles.connections}>
              {statisticsRef.command.length > 0 && (
                <NNumberAnimation
                  from={0}
                  to={statisticsRef.command
                    .map((item) => item.normalCount)
                    .reduce((prev, next) => prev + next)}
                />
              )}
            </div>
          </Card>
        </NGi>
        <NGi>
          <Card title={t('monitor.statistics.failure_command_number')}>
            <div class={styles.connections}>
              {statisticsRef.command.length > 0 && (
                <NNumberAnimation
                  from={0}
                  to={statisticsRef.command
                    .map((item) => item.errorCount)
                    .reduce((prev, next) => prev + next)}
                />
              )}
            </div>
          </Card>
        </NGi>
      </NGrid>
    )
  }
})

export default statistics
