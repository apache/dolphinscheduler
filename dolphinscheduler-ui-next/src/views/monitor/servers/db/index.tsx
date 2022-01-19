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
import { NGrid, NGi, NNumberAnimation, NIcon } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useDatabase } from '@/views/monitor/servers/db/use-database'
import { CheckCircleOutlined, CloseCircleOutlined } from '@vicons/antd'
import Card from '@/components/card'
import styles from './index.module.scss'
import type { Ref } from 'vue'
import type { DatabaseRes } from '@/service/modules/monitor/types'

const db = defineComponent({
  name: 'db',
  setup() {
    const { t } = useI18n()
    const { getDatabase } = useDatabase()
    const databaseRef: Ref<DatabaseRes[]> = ref(getDatabase())

    return { t, databaseRef }
  },
  render() {
    const { t, databaseRef } = this

    return (
      <NGrid x-gap='12' y-gap='8' cols='2 2xl:4' responsive='screen'>
        <NGi>
          <Card title={t('monitor.db.health_state')}>
            <div class={styles.health}>
              {databaseRef[0] &&
                (databaseRef[0].state ? (
                  <NIcon class={styles['health-success']}>
                    <CheckCircleOutlined />
                  </NIcon>
                ) : (
                  <NIcon class={styles['health-error']}>
                    <CloseCircleOutlined />
                  </NIcon>
                ))}
            </div>
          </Card>
        </NGi>
        <NGi>
          <Card
            title={`${t('monitor.db.max_connections')}${
              databaseRef[0] ? ' - ' + databaseRef[0].date : ''
            }`}
          >
            <div class={styles.connections}>
              {databaseRef[0] && (
                <NNumberAnimation from={0} to={databaseRef[0].maxConnections} />
              )}
            </div>
          </Card>
        </NGi>
        <NGi>
          <Card title={t('monitor.db.threads_connections')}>
            <div class={styles.connections}>
              {databaseRef[0] && (
                <NNumberAnimation
                  from={0}
                  to={databaseRef[0].threadsConnections}
                />
              )}
            </div>
          </Card>
        </NGi>
        <NGi>
          <Card title={t('monitor.db.threads_running_connections')}>
            <div class={styles.connections}>
              {databaseRef[0] && (
                <NNumberAnimation
                  from={0}
                  to={databaseRef[0].threadsRunningConnections}
                />
              )}
            </div>
          </Card>
        </NGi>
      </NGrid>
    )
  }
})

export default db
