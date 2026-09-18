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

import { computed, defineComponent, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import {
  NAlert,
  NButton,
  NEmpty,
  NSpin,
  NSwitch,
  NTag,
  useThemeVars
} from 'naive-ui'
import Card from '@/components/card'
import LogModal from '@/components/log-modal'
import TaskModal from '@/views/projects/task/components/node/detail-modal'
import GanttChart from './components/gantt-chart'
import { useGantt } from './use-gantt'
import { useGanttLogs } from './use-logs'
import { isWorkflowActive } from './model'
import type { GanttRow } from './type'
import styles from './index.module.scss'

export default defineComponent({
  name: 'WorkflowInstanceGantt',
  setup() {
    const { t } = useI18n()
    const route = useRoute()
    const theme = useThemeVars()
    const { workflow, model, loading, error, autoRefresh, updatedAt, refresh } =
      useGantt(() => [
        Number(route.params.id),
        Number(route.params.projectCode)
      ])
    const logs = useGanttLogs(t)
    const configVisible = ref(false)
    const configTask = ref<GanttRow['definition']>()
    const workflowDefinition = computed(() => workflow.value?.dagData as any)
    const openConfig = (row: GanttRow) => {
      if (!row.definition) return
      configTask.value = row.definition
      configVisible.value = true
    }
    const closeConfig = () => {
      configVisible.value = false
      configTask.value = undefined
    }
    const statistics = [
      'total',
      'submitted',
      'pending',
      'waiting',
      'running',
      'success',
      'failed',
      'stopped'
    ] as const
    const statColor = (key: string) =>
      ({
        total: theme.value.textColor1,
        submitted: '#7c6bc4',
        pending: theme.value.textColor3,
        waiting: theme.value.warningColor,
        running: theme.value.infoColor,
        success: theme.value.successColor,
        failed: theme.value.errorColor,
        stopped: '#b7815b'
      }[key])
    return () => (
      <Card title={t('project.workflow.gantt')}>
        <div
          class={styles.page}
          style={{
            '--gantt-border': theme.value.borderColor,
            '--gantt-surface': theme.value.cardColor,
            '--gantt-muted': theme.value.textColor3,
            '--gantt-text': theme.value.textColor1,
            '--gantt-primary': theme.value.primaryColor,
            '--gantt-hover': theme.value.tableHeaderColor
          }}
        >
          <div class={styles.heading}>
            <div>
              <h3>
                {workflow.value?.name || t('project.workflow.gantt_timeline')}
              </h3>
              <div class={styles.metadata}>
                <span>
                  {t('project.workflow.gantt_instance')} #{route.params.id}
                </span>
                {updatedAt.value && (
                  <span class={styles.updatedAt}>
                    {t('project.workflow.gantt_updated')}{' '}
                    {new Date(updatedAt.value).toLocaleTimeString()}
                  </span>
                )}
              </div>
            </div>
            <div class={styles.controls}>
              {workflow.value && (
                <NTag
                  size='small'
                  bordered={false}
                  type={
                    workflow.value.state === 'SUCCESS'
                      ? 'success'
                      : workflow.value.state === 'FAILURE'
                      ? 'error'
                      : 'info'
                  }
                >
                  {t(
                    `project.workflow.gantt_${
                      isWorkflowActive(workflow.value.state)
                        ? 'live'
                        : 'finished'
                    }`
                  )}
                </NTag>
              )}
              <label class={styles.autoRefresh}>
                <NSwitch
                  size='small'
                  value={autoRefresh.value}
                  onUpdateValue={(value) => {
                    autoRefresh.value = value
                  }}
                />
                <span>{t('project.workflow.gantt_auto_refresh')}</span>
              </label>
              <NButton
                size='small'
                loading={loading.value}
                onClick={() => refresh()}
              >
                {t('project.task.refresh')}
              </NButton>
            </div>
          </div>
          {error.value && (
            <NAlert type='error' class={styles.alert}>
              {t(
                workflow.value
                  ? 'project.workflow.gantt_stale'
                  : 'project.workflow.gantt_load_error'
              )}
            </NAlert>
          )}
          <NSpin show={loading.value && !workflow.value}>
            {workflow.value && (
              <>
                <div class={styles.stats}>
                  <div class={styles.statOverview}>
                    {statistics.slice(0, 2).map((key) => (
                      <div
                        key={key}
                        class={styles.statPrimary}
                        style={{ '--stat-color': statColor(key) }}
                      >
                        <span>{t(`project.workflow.gantt_${key}`)}</span>
                        <strong>{model.value.stats[key]}</strong>
                      </div>
                    ))}
                  </div>
                  <div class={styles.statStates}>
                    {statistics.slice(2).map((key) => (
                      <div
                        key={key}
                        class={[
                          styles.stat,
                          model.value.stats[key] === 0 && styles.statEmpty
                        ]}
                        style={{ '--stat-color': statColor(key) }}
                      >
                        <span>
                          <i />
                          {t(`project.workflow.gantt_${key}`)}
                        </span>
                        <strong>{model.value.stats[key]}</strong>
                      </div>
                    ))}
                  </div>
                </div>
                {model.value.rows.length ? (
                  <GanttChart
                    model={model.value}
                    onViewLog={logs.open}
                    onViewConfig={openConfig}
                  />
                ) : (
                  <NEmpty
                    class={styles.empty}
                    description={t('project.workflow.gantt_empty')}
                  />
                )}
              </>
            )}
            {!workflow.value && (
              <div class={styles.empty}>
                {error.value ? (
                  <NButton onClick={() => refresh()}>
                    {t('project.task.refresh')}
                  </NButton>
                ) : (
                  t('project.workflow.gantt_loading')
                )}
              </div>
            )}
          </NSpin>
          {logs.visible.value && (
            <LogModal
              showModalRef={logs.visible.value}
              logRef={logs.text.value}
              logLoadingRef={logs.loading.value}
              row={logs.selected.value}
              showDownloadLog={true}
              onConfirmModal={logs.close}
              onRefreshLogs={logs.refresh}
              onDownloadLogs={logs.download}
            />
          )}
          <TaskModal
            readonly
            confirmShow={false}
            show={configVisible.value}
            from={1}
            projectCode={Number(route.params.projectCode)}
            data={
              (configTask.value || {
                code: 0,
                name: '',
                taskType: 'SHELL'
              }) as any
            }
            definition={workflowDefinition}
            onCancel={closeConfig}
          />
        </div>
      </Card>
    )
  }
})
