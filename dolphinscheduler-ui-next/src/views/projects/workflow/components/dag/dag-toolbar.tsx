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

import { defineComponent, ref, inject, PropType, Ref } from 'vue'
import { useI18n } from 'vue-i18n'
import Styles from './dag.module.scss'
import {
  NTooltip,
  NIcon,
  NButton,
  NSelect,
  NPopover,
  NText,
  NTag
} from 'naive-ui'
import {
  SearchOutlined,
  DownloadOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined,
  InfoCircleOutlined,
  FormatPainterOutlined,
  CopyOutlined,
  DeleteOutlined,
  RightCircleOutlined,
  FundViewOutlined,
  SyncOutlined
} from '@vicons/antd'
import { useNodeSearch, useTextCopy } from './dag-hooks'
import { DataUri } from '@antv/x6'
import { useFullscreen } from '@vueuse/core'
import { useRoute, useRouter } from 'vue-router'
import { useThemeStore } from '@/store/theme/theme'
import type { Graph } from '@antv/x6'
import StartupParam from './dag-startup-param'
import VariablesView from '@/views/projects/workflow/instance/components/variables-view'
import { WorkflowDefinition, WorkflowInstance } from './types'

const props = {
  layoutToggle: {
    type: Function as PropType<(bool?: boolean) => void>,
    default: () => {}
  },
  // If this prop is passed, it means from definition detail
  instance: {
    type: Object as PropType<WorkflowInstance>,
    default: null
  },
  definition: {
    // The same as the structure responsed by the queryProcessDefinitionByCode api
    type: Object as PropType<WorkflowDefinition>,
    default: null
  }
}

export default defineComponent({
  name: 'workflow-dag-toolbar',
  props,
  emits: ['versionToggle', 'saveModelToggle', 'removeTasks', 'refresh'],
  setup(props, context) {
    const { t } = useI18n()

    const startupPopoverRef = ref(false)
    const paramPopoverRef = ref(false)

    const themeStore = useThemeStore()

    const graph = inject<Ref<Graph | undefined>>('graph', ref())
    const router = useRouter()
    const route = useRoute()

    /**
     * Node search and navigate
     */
    const {
      navigateTo,
      toggleSearchInput,
      searchInputVisible,
      reQueryNodes,
      nodesDropdown
    } = useNodeSearch({ graph })

    /**
     * Download Workflow Image
     * @param {string} fileName
     * @param {string} bgColor
     */
    const downloadPNG = (options = { fileName: 'dag', bgColor: '#f2f3f7' }) => {
      const { fileName, bgColor } = options
      graph.value?.toPNG(
        (dataUri: string) => {
          DataUri.downloadDataUri(dataUri, `${fileName}.png`)
        },
        {
          padding: {
            top: 50,
            right: 50,
            bottom: 50,
            left: 50
          },
          backgroundColor: bgColor
        }
      )
    }

    /**
     * Toggle fullscreen
     */
    const { isFullscreen, toggle } = useFullscreen()

    /**
     * Open workflow version modal
     */
    const openVersionModal = () => {
      context.emit('versionToggle', true)
    }

    /**
     * Open DAG format modal
     */
    const onFormat = () => {
      props.layoutToggle(true)
    }

    /**
     * Back to the entrance
     */
    const onClose = () => {
      if (history.state.back !== '/login') {
        router.go(-1)
        return
      }
      if (history.state.current.includes('workflow/definitions')) {
        router.push({
          path: `/projects/${route.params.projectCode}/workflow-definition`
        })
        return
      }
      if (history.state.current.includes('workflow/instances')) {
        router.push({
          path: `/projects/${route.params.projectCode}/workflow/instances`
        })
        return
      }
    }

    /**
     *  Copy workflow name
     */
    const { copy } = useTextCopy()

    /**
     * Delete selected edges and nodes
     */
    const removeCells = () => {
      if (graph.value) {
        const cells = graph.value.getSelectedCells()
        if (cells) {
          graph.value?.removeCells(cells)
          const codes = cells
            .filter((cell) => cell.isNode())
            .map((cell) => +cell.id)
          context.emit('removeTasks', codes)
        }
      }
    }

    return () => (
      <div
        class={[
          Styles.toolbar,
          Styles[themeStore.darkTheme ? 'toolbar-dark' : 'toolbar-light']
        ]}
      >
        <span class={Styles['workflow-name']}>
          {route.name === 'workflow-instance-detail'
            ? props.instance?.name
            : props.definition?.processDefinition?.name ||
              t('project.dag.create')}
        </span>
        {props.definition?.processDefinition?.name && (
          <NTooltip
            v-slots={{
              trigger: () => (
                <NButton
                  quaternary
                  circle
                  onClick={() => {
                    const name =
                      route.name === 'workflow-instance-detail'
                        ? props.instance?.name
                        : props.definition?.processDefinition?.name
                    copy(name)
                  }}
                  class={Styles['toolbar-btn']}
                >
                  <NIcon>
                    <CopyOutlined />
                  </NIcon>
                </NButton>
              ),
              default: () => t('project.dag.copy_name')
            }}
          ></NTooltip>
        )}
        <div class={Styles['toolbar-left-part']}>
          {route.name !== 'workflow-instance-detail' &&
            props.definition?.processDefinition?.releaseState === 'ONLINE' && (
              <NTag round size='small' type='info'>
                {t('project.dag.online')}
              </NTag>
            )}
          {route.name === 'workflow-instance-detail' && (
            <>
              <NTooltip
                v-slots={{
                  trigger: () => (
                    <NPopover
                      show={paramPopoverRef.value}
                      placement='bottom'
                      trigger='manual'
                    >
                      {{
                        trigger: () => (
                          <NButton
                            quaternary
                            circle
                            onClick={() =>
                              (paramPopoverRef.value = !paramPopoverRef.value)
                            }
                            class={Styles['toolbar-btn']}
                          >
                            <NIcon>
                              <FundViewOutlined />
                            </NIcon>
                          </NButton>
                        ),
                        header: () => (
                          <NText strong depth={1}>
                            {t('project.workflow.parameters_variables')}
                          </NText>
                        ),
                        default: () => <VariablesView onCopy={copy} />
                      }}
                    </NPopover>
                  ),
                  default: () => t('project.dag.view_variables')
                }}
              ></NTooltip>
              <NTooltip
                v-slots={{
                  trigger: () => (
                    <NPopover
                      show={startupPopoverRef.value}
                      placement='bottom'
                      trigger='manual'
                    >
                      {{
                        trigger: () => (
                          <NButton
                            quaternary
                            circle
                            onClick={() =>
                              (startupPopoverRef.value =
                                !startupPopoverRef.value)
                            }
                            class={Styles['toolbar-btn']}
                          >
                            <NIcon>
                              <RightCircleOutlined />
                            </NIcon>
                          </NButton>
                        ),
                        header: () => (
                          <NText strong depth={1}>
                            {t('project.workflow.startup_parameter')}
                          </NText>
                        ),
                        default: () => (
                          <StartupParam startupParam={props.instance} />
                        )
                      }}
                    </NPopover>
                  ),
                  default: () => t('project.dag.startup_parameter')
                }}
              ></NTooltip>
            </>
          )}
        </div>
        <div class={Styles['toolbar-right-part']}>
          {/* Search node */}
          <NTooltip
            v-slots={{
              trigger: () => (
                <NButton
                  class={Styles['toolbar-right-item']}
                  strong
                  secondary
                  circle
                  type='info'
                  onClick={toggleSearchInput}
                  v-slots={{
                    icon: () => (
                      <NIcon>
                        <SearchOutlined />
                      </NIcon>
                    )
                  }}
                />
              ),
              default: () => t('project.dag.search')
            }}
          ></NTooltip>
          <div
            class={`${Styles['toolbar-right-item']} ${
              Styles['node-selector']
            } ${searchInputVisible.value ? Styles['visible'] : ''}`}
          >
            <NSelect
              size='small'
              options={nodesDropdown.value}
              onFocus={reQueryNodes}
              onUpdateValue={navigateTo}
              filterable
            />
          </div>
          {/* Download workflow PNG */}
          <NTooltip
            v-slots={{
              trigger: () => (
                <NButton
                  class={Styles['toolbar-right-item']}
                  strong
                  secondary
                  circle
                  type='info'
                  onClick={() => downloadPNG()}
                  v-slots={{
                    icon: () => (
                      <NIcon>
                        <DownloadOutlined />
                      </NIcon>
                    )
                  }}
                />
              ),
              default: () => t('project.dag.download_png')
            }}
          ></NTooltip>
          {/* Refresh */}
          {props.instance && (
            <NTooltip
              v-slots={{
                trigger: () => (
                  <NButton
                    class={Styles['toolbar-right-item']}
                    strong
                    secondary
                    circle
                    type='info'
                    onClick={() => {
                      context.emit('refresh')
                    }}
                    v-slots={{
                      icon: () => (
                        <NIcon>
                          <SyncOutlined />
                        </NIcon>
                      )
                    }}
                  />
                ),
                default: () => t('project.dag.refresh_dag_status')
              }}
            ></NTooltip>
          )}
          {/* Delete */}
          <NTooltip
            v-slots={{
              trigger: () => (
                <NButton
                  class={Styles['toolbar-right-item']}
                  strong
                  secondary
                  circle
                  type='info'
                  onClick={() => removeCells()}
                  v-slots={{
                    icon: () => (
                      <NIcon>
                        <DeleteOutlined />
                      </NIcon>
                    )
                  }}
                />
              ),
              default: () => t('project.dag.delete_cell')
            }}
          ></NTooltip>
          {/* Toggle fullscreen */}
          <NTooltip
            v-slots={{
              trigger: () => (
                <NButton
                  class={Styles['toolbar-right-item']}
                  strong
                  secondary
                  circle
                  type='info'
                  onClick={toggle}
                  v-slots={{
                    icon: () => (
                      <NIcon>
                        {isFullscreen.value ? (
                          <FullscreenExitOutlined />
                        ) : (
                          <FullscreenOutlined />
                        )}
                      </NIcon>
                    )
                  }}
                />
              ),
              default: () =>
                isFullscreen.value
                  ? t('project.dag.fullscreen_close')
                  : t('project.dag.fullscreen_open')
            }}
          ></NTooltip>
          {/* DAG Format */}
          <NTooltip
            v-slots={{
              trigger: () => (
                <NButton
                  class={Styles['toolbar-right-item']}
                  strong
                  secondary
                  circle
                  type='info'
                  onClick={onFormat}
                  v-slots={{
                    icon: () => (
                      <NIcon>
                        <FormatPainterOutlined />
                      </NIcon>
                    )
                  }}
                />
              ),
              default: () => t('project.dag.format')
            }}
          ></NTooltip>
          {/* Version info */}
          {!!props.definition && (
            <NTooltip
              v-slots={{
                trigger: () => (
                  <NButton
                    class={Styles['toolbar-right-item']}
                    strong
                    secondary
                    circle
                    type='info'
                    onClick={openVersionModal}
                    v-slots={{
                      icon: () => (
                        <NIcon>
                          <InfoCircleOutlined />
                        </NIcon>
                      )
                    }}
                  />
                ),
                default: () => t('project.workflow.version_info')
              }}
            ></NTooltip>
          )}
          {/* Save workflow */}
          <NButton
            class={[Styles['toolbar-right-item'], 'btn-save']}
            type='info'
            secondary
            round
            disabled={
              props.definition?.processDefinition?.releaseState === 'ONLINE' &&
              !props.instance
            }
            onClick={() => {
              context.emit('saveModelToggle', true)
            }}
          >
            {t('project.dag.save')}
          </NButton>
          {/* Return to previous page */}
          <NButton secondary round onClick={onClose} class='btn-close'>
            {t('project.dag.close')}
          </NButton>
        </div>
      </div>
    )
  }
})
