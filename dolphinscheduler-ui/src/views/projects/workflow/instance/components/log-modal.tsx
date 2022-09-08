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

import _ from 'lodash'
import {
  defineComponent,
  PropType,
  Transition,
  toRefs,
  ref,
  onMounted,
  computed,
  reactive,
  renderSlot
} from 'vue'
import { useI18n } from 'vue-i18n'
import { NButton, NIcon, NTooltip } from 'naive-ui'
import { queryLog } from '@/service/modules/log'
import {
  DownloadOutlined,
  SyncOutlined,
  FullscreenOutlined,
  FullscreenExitOutlined
} from '@vicons/antd'
import { downloadFile } from '@/service/service'
import styles from './log.module.scss'

const props = {
  taskInstanceId: {
    type: Number as PropType<number>,
    default: -1
  },
  taskInstanceType: {
    type: String as PropType<string>,
    default: ''
  }
}

export default defineComponent({
  name: 'workflow-instance-log',
  props,
  emits: ['hideLog'],
  setup(props, ctx) {
    const { t } = useI18n()

    const loadingRef = ref(false)
    const loadingIndex = ref(0)
    const isDataRef = ref(true)
    const logBox = ref()
    const logContent = ref()
    const logContentBox = ref()
    const textareaLog = ref()
    const isScreen = ref(false)
    const textareaHeight = computed(() =>
      logContentBox.value ? logContentBox.value.clientHeight : 0
    )
    const contentRef = ref()

    const boxRef = reactive({
      width: '',
      height: '',
      marginLeft: '',
      marginRight: '',
      marginTop: ''
    })

    const refreshLog = () => {
      loadingRef.value = true
      queryLog({
        taskInstanceId: props.taskInstanceId,
        skipLineNum: loadingIndex.value * 1000,
        limit: loadingIndex.value === 0 ? 1000 : (loadingIndex.value + 1) * 1000
      }).then((res: any) => {
        setTimeout(() => {
          loadingRef.value = false
          if (res) {
            window.$message.success(t('project.workflow.update_log_success'))
          } else {
            window.$message.warning(t('project.workflow.no_more_log'))
          }
        }, 1500)
        textareaLog.value.innerHTML = res || t('project.workflow.no_log')
      })
    }

    const showLog = () => {
      queryLog({
        taskInstanceId: props.taskInstanceId,
        skipLineNum: loadingIndex.value * 1000,
        limit: loadingIndex.value === 0 ? 1000 : (loadingIndex.value + 1) * 1000
      }).then((res: any) => {
        if (!res) {
          isDataRef.value = false
          setTimeout(() => {
            window.$message.warning(t('project.workflow.no_more_log'))
          }, 1000)
          textareaLog.value.innerHTML =
            contentRef.value || t('project.workflow.no_log')
        } else {
          isDataRef.value = true
          contentRef.value = res
          textareaLog.value.innerHTML =
            contentRef.value || t('project.workflow.no_log')
          setTimeout(() => {
            textareaLog.value.scrollTop = 2
          }, 800)
        }
      })
    }

    const initLog = () => {
      window.$message.info(t('project.workflow.loading_log'))
      showLog()
    }

    const downloadLog = () => {
      downloadFile('log/download-log', {
        taskInstanceId: props.taskInstanceId
      })
    }

    const screenOpen = () => {
      isScreen.value = true
      const winW = window.innerWidth - 40
      const winH = window.innerHeight - 40

      boxRef.width = `${winW}px`
      boxRef.height = `${winH}px`
      boxRef.marginLeft = `-${winW / 2}px`
      boxRef.marginRight = `-${winH / 2}px`
      boxRef.marginTop = `-${winH / 2}px`

      logContent.value.animate({ scrollTop: 0 }, 0)
    }

    const screenClose = () => {
      isScreen.value = false
      boxRef.width = ''
      boxRef.height = ''
      boxRef.marginLeft = ''
      boxRef.marginRight = ''
      boxRef.marginTop = ''

      logContent.value.animate({ scrollTop: 0 }, 0)
    }

    const toggleScreen = () => {
      if (isScreen.value) {
        screenClose()
      } else {
        screenOpen()
      }
    }

    const close = () => {
      ctx.emit('hideLog')
    }

    /**
     * up
     */
    const onUp = _.throttle(
      function () {
        loadingIndex.value = loadingIndex.value - 1
        showLog()
      },
      1000,
      {
        trailing: false
      }
    )

    /**
     * down
     */
    const onDown = _.throttle(
      function () {
        loadingIndex.value = loadingIndex.value + 1
        showLog()
      },
      1000,
      {
        trailing: false
      }
    )

    const onTextareaScroll = () => {
      textareaLog.value.onscroll = () => {
        // Listen for scrollbar events
        if (
          textareaLog.value.scrollTop + textareaLog.value.clientHeight ===
          textareaLog.value.clientHeight
        ) {
          if (loadingIndex.value > 0) {
            window.$message.info(t('project.workflow.loading_log'))
            onUp()
          }
        }
        // Listen for scrollbar events
        if (
          textareaLog.value.scrollHeight ===
          textareaLog.value.clientHeight + textareaLog.value.scrollTop
        ) {
          // No data is not requested
          if (isDataRef.value) {
            window.$message.info(t('project.workflow.loading_log'))
            onDown()
          }
        }
      }
    }

    onMounted(() => {
      initLog()
      onTextareaScroll()
    })

    return {
      t,
      logBox,
      logContentBox,
      loadingRef,
      textareaLog,
      logContent,
      textareaHeight,
      isScreen,
      boxRef,
      showLog,
      downloadLog,
      refreshLog,
      toggleScreen,
      close,
      ...toRefs(props)
    }
  },
  render() {
    return (
      <div>
        <span>
          {this.taskInstanceId && this.taskInstanceType !== 'SUB_PROCESS' && (
            <span>
              {renderSlot(this.$slots, 'history')}
              <slot name='history'></slot>
              <span onClick={this.showLog}>
                {renderSlot(this.$slots, 'log')}
              </span>
            </span>
          )}
          <Transition name='fade'>
            {
              <div class={styles['log-pop']}>
                <div class={styles['log-box']} style={{ ...this.boxRef }}>
                  <div class={styles['title']}>
                    <div class={styles['left-item']}>
                      {this.t('project.workflow.view_log')}
                    </div>
                    <div class={styles['right-item']}>
                      <NTooltip>
                        {{
                          trigger: () => (
                            <NButton
                              strong
                              secondary
                              circle
                              type='info'
                              class={styles.button}
                              onClick={this.downloadLog}
                            >
                              <NIcon>
                                <DownloadOutlined />
                              </NIcon>
                            </NButton>
                          ),
                          default: () => this.t('project.workflow.download_log')
                        }}
                      </NTooltip>
                      <NTooltip>
                        {{
                          trigger: () => (
                            <NButton
                              strong
                              secondary
                              circle
                              type='info'
                              class={styles.button}
                              onClick={() =>
                                !this.loadingRef && this.refreshLog()
                              }
                            >
                              <NIcon>
                                <SyncOutlined />
                              </NIcon>
                            </NButton>
                          ),
                          default: () => this.t('project.workflow.refresh_log')
                        }}
                      </NTooltip>
                      <NTooltip>
                        {{
                          trigger: () => (
                            <NButton
                              strong
                              secondary
                              circle
                              type='info'
                              class={styles.button}
                              onClick={this.toggleScreen}
                            >
                              <NIcon>
                                {this.isScreen ? (
                                  <FullscreenExitOutlined />
                                ) : (
                                  <FullscreenOutlined />
                                )}
                              </NIcon>
                            </NButton>
                          ),
                          default: () =>
                            this.isScreen
                              ? this.t('project.workflow.cancel_full_screen')
                              : this.t('project.workflow.enter_full_screen')
                        }}
                      </NTooltip>
                    </div>
                  </div>
                  <div class={styles['content']} ref='logContent'>
                    <div class={styles['content-log-box']} ref='logContentBox'>
                      <textarea
                        style={`width: 100%; height: ${this.textareaHeight}px`}
                        spellcheck='false'
                        ref='textareaLog'
                        readonly
                      ></textarea>
                    </div>
                  </div>
                  <div class={styles['operation']}>
                    <NButton
                      type='primary'
                      size='small'
                      round
                      onClick={this.close}
                    >
                      {this.t('project.workflow.close')}
                    </NButton>
                  </div>
                </div>
              </div>
            }
          </Transition>
        </span>
      </div>
    )
  }
})
