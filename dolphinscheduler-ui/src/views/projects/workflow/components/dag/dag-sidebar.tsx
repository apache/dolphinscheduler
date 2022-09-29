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

import { defineComponent, onMounted, reactive } from 'vue'
import styles from './dag.module.scss'
import type { TaskType } from './types'
import { NCollapse, NCollapseItem, NIcon } from 'naive-ui'
import { StarFilled, StarOutlined } from '@vicons/antd'
import {
  CancelCollection,
  Collection,
  getDagMenu
} from '@/service/modules/dag-menu'
import { useI18n } from 'vue-i18n'

export default defineComponent({
  name: 'workflow-dag-sidebar',
  emits: ['dragStart'],
  setup(props, context) {
    const variables = reactive({
      dataList: [],
      universal: [],
      cloud: [],
      logic: [],
      di: [],
      dq: [],
      ml: [],
      other: [],
      fav: []
    })

    const { t } = useI18n()

    const handleDagMenu = () => {
      getDagMenu().then((res: any) => {
        variables.dataList = res.map((item: any) => {
          return {
            ...item,
            starHover: false,
            type: item.taskName
          }
        })
        variables.universal = variables.dataList.filter(
          (item: any) => item.taskType === 'Universal'
        )
        variables.cloud = variables.dataList.filter(
          (item: any) => item.taskType === 'Cloud'
        )
        variables.logic = variables.dataList.filter(
          (item: any) => item.taskType === 'Logic'
        )
        variables.di = variables.dataList.filter(
          (item: any) => item.taskType === 'DataIntegration'
        )
        variables.dq = variables.dataList.filter(
          (item: any) => item.taskType === 'DataQuality'
        )
        variables.ml = variables.dataList.filter(
          (item: any) => item.taskType === 'MachineLearning'
        )
        variables.other = variables.dataList.filter(
          (item: any) => item.taskType === 'Other'
        )
        variables.fav = variables.dataList.filter(
          (item: any) => item.collection === true
        )
      })
    }

    const handleCollection = (item: any) => {
      item.collection
        ? CancelCollection(item.taskName).then(() => {
            handleDagMenu()
          })
        : Collection(item.taskName).then(() => {
            handleDagMenu()
          })
      item.collection = !item.collection
    }

    onMounted(() => {
      handleDagMenu()
    })

    return () =>
      <div class={styles.sidebar}>
        <NCollapse default-expanded-names='1' accordion>
          {variables.fav.length > 0 && (
            <NCollapseItem
              title={t('project.menu.fav')}
              name='0'
              class='task-cate-fav'
              v-slots={{
                default: () => {
                  return variables.fav.map((task: any) => (
                    <div
                      class={[styles.draggable, `task-item-${task.type}`]}
                      draggable='true'
                      onDragstart={(e) => {
                        context.emit('dragStart', e, task.type as TaskType)
                      }}
                    >
                      <em
                        class={[
                          styles['sidebar-icon'],
                          styles['icon-' + task.type.toLocaleLowerCase()]
                        ]}
                      />
                      <span>{task.taskName}</span>
                      <div
                        class={styles.stars}
                        onMouseenter={() => {
                          task.starHover = true
                        }}
                        onMouseleave={() => {
                          task.starHover = false
                        }}
                        onClick={() => handleCollection(task)}
                      >
                        <NIcon
                          size='20'
                          color={
                            task.collection || task.starHover
                              ? '#288FFF'
                              : '#ccc'
                          }
                        >
                          {task.collection ? (
                            <StarFilled />
                          ) : (
                            <StarOutlined />
                          )}
                        </NIcon>
                      </div>
                    </div>
                  ))
                }
              }}
            ></NCollapseItem>
          )}
          {variables.universal.length > 0 && (
            <NCollapseItem
              title={t('project.menu.universal')}
              name='1'
              class='task-cate-universal'
              v-slots={{
                default: () => {
                  return variables.universal.map((task: any) => (
                    <div
                      class={[styles.draggable, `task-item-${task.type}`]}
                      draggable='true'
                      onDragstart={(e) => {
                        context.emit('dragStart', e, task.type as TaskType)
                      }}
                    >
                      <em
                        class={[
                          styles['sidebar-icon'],
                          styles['icon-' + task.type.toLocaleLowerCase()]
                        ]}
                      />
                      <span>{task.taskName}</span>
                      <div
                        class={styles.stars}
                        onMouseenter={() => {
                          task.starHover = true
                        }}
                        onMouseleave={() => {
                          task.starHover = false
                        }}
                        onClick={() => handleCollection(task)}
                      >
                        <NIcon
                          size='20'
                          color={
                            task.collection || task.starHover
                              ? '#288FFF'
                              : '#ccc'
                          }
                        >
                          {task.collection ? (
                            <StarFilled />
                          ) : (
                            <StarOutlined />
                          )}
                        </NIcon>
                      </div>
                    </div>
                  ))
                }
              }}
            ></NCollapseItem>
          )}
          {variables.cloud.length > 0 && (
            <NCollapseItem
              title={t('project.menu.cloud')}
              name='2'
              class='task-cate-cloud'
              v-slots={{
                default: () => {
                  return variables.cloud.map((task: any) => (
                    <div
                      class={[styles.draggable, `task-item-${task.type}`]}
                      draggable='true'
                      onDragstart={(e) => {
                        context.emit('dragStart', e, task.type as TaskType)
                      }}
                    >
                      <em
                        class={[
                          styles['sidebar-icon'],
                          styles['icon-' + task.type.toLocaleLowerCase()]
                        ]}
                      />
                      <span>{task.taskName}</span>
                      <div
                        class={styles.stars}
                        onMouseenter={() => {
                          task.starHover = true
                        }}
                        onMouseleave={() => {
                          task.starHover = false
                        }}
                        onClick={() => handleCollection(task)}
                      >
                        <NIcon
                          size='20'
                          color={
                            task.collection || task.starHover
                              ? '#288FFF'
                              : '#ccc'
                          }
                        >
                          {task.collection ? (
                            <StarFilled />
                          ) : (
                            <StarOutlined />
                          )}
                        </NIcon>
                      </div>
                    </div>
                  ))
                }
              }}
            ></NCollapseItem>
          )}
          {variables.logic.length > 0 && (
            <NCollapseItem
              title={t('project.menu.logic')}
              name='3'
              class='task-cate-logic'
              v-slots={{
                default: () => {
                  return variables.logic.map((task: any) => (
                    <div
                      class={[styles.draggable, `task-item-${task.type}`]}
                      draggable='true'
                      onDragstart={(e) => {
                        context.emit('dragStart', e, task.type as TaskType)
                      }}
                    >
                      <em
                        class={[
                          styles['sidebar-icon'],
                          styles['icon-' + task.type.toLocaleLowerCase()]
                        ]}
                      />
                      <span>{task.taskName}</span>
                      <div
                        class={styles.stars}
                        onMouseenter={() => {
                          task.starHover = true
                        }}
                        onMouseleave={() => {
                          task.starHover = false
                        }}
                        onClick={() => handleCollection(task)}
                      >
                        <NIcon
                          size='20'
                          color={
                            task.collection || task.starHover
                              ? '#288FFF'
                              : '#ccc'
                          }
                        >
                          {task.collection ? (
                            <StarFilled />
                          ) : (
                            <StarOutlined />
                          )}
                        </NIcon>
                      </div>
                    </div>
                  ))
                }
              }}
            ></NCollapseItem>
          )}
          {variables.di.length > 0 && (
            <NCollapseItem
              title={t('project.menu.di')}
              name='4'
              class='task-cate-di'
              v-slots={{
                default: () => {
                  return variables.di.map((task: any) => (
                    <div
                      class={[styles.draggable, `task-item-${task.type}`]}
                      draggable='true'
                      onDragstart={(e) => {
                        context.emit('dragStart', e, task.type as TaskType)
                      }}
                    >
                      <em
                        class={[
                          styles['sidebar-icon'],
                          styles['icon-' + task.type.toLocaleLowerCase()]
                        ]}
                      />
                      <span>{task.taskName}</span>
                      <div
                        class={styles.stars}
                        onMouseenter={() => {
                          task.starHover = true
                        }}
                        onMouseleave={() => {
                          task.starHover = false
                        }}
                        onClick={() => handleCollection(task)}
                      >
                        <NIcon
                          size='20'
                          color={
                            task.collection || task.starHover
                              ? '#288FFF'
                              : '#ccc'
                          }
                        >
                          {task.collection ? (
                            <StarFilled />
                          ) : (
                            <StarOutlined />
                          )}
                        </NIcon>
                      </div>
                    </div>
                  ))
                }
              }}
            ></NCollapseItem>
          )}
          {variables.dq.length > 0 && (
            <NCollapseItem
              title={t('project.menu.dq')}
              name='5'
              class='task-cate-dq'
              v-slots={{
                default: () => {
                  return variables.dq.map((task: any) => (
                    <div
                      class={[styles.draggable, `task-item-${task.type}`]}
                      draggable='true'
                      onDragstart={(e) => {
                        context.emit('dragStart', e, task.type as TaskType)
                      }}
                    >
                      <em
                        class={[
                          styles['sidebar-icon'],
                          styles['icon-' + task.type.toLocaleLowerCase()]
                        ]}
                      />
                      <span>{task.taskName}</span>
                      <div
                        class={styles.stars}
                        onMouseenter={() => {
                          task.starHover = true
                        }}
                        onMouseleave={() => {
                          task.starHover = false
                        }}
                        onClick={() => handleCollection(task)}
                      >
                        <NIcon
                          size='20'
                          color={
                            task.collection || task.starHover
                              ? '#288FFF'
                              : '#ccc'
                          }
                        >
                          {task.collection ? (
                            <StarFilled />
                          ) : (
                            <StarOutlined />
                          )}
                        </NIcon>
                      </div>
                    </div>
                  ))
                }
              }}
            ></NCollapseItem>
          )}
          {variables.ml.length > 0 && (
            <NCollapseItem
              title={t('project.menu.ml')}
              name='5'
              class='task-cate-ml'
              v-slots={{
                default: () => {
                  return variables.ml.map((task: any) => (
                    <div
                      class={[styles.draggable, `task-item-${task.type}`]}
                      draggable='true'
                      onDragstart={(e) => {
                        context.emit('dragStart', e, task.type as TaskType)
                      }}
                    >
                      <em
                        class={[
                          styles['sidebar-icon'],
                          styles['icon-' + task.type.toLocaleLowerCase()]
                        ]}
                      />
                      <span>{task.taskName}</span>
                      <div
                        class={styles.stars}
                        onMouseenter={() => {
                          task.starHover = true
                        }}
                        onMouseleave={() => {
                          task.starHover = false
                        }}
                        onClick={() => handleCollection(task)}
                      >
                        <NIcon
                          size='20'
                          color={
                            task.collection || task.starHover
                              ? '#288FFF'
                              : '#ccc'
                          }
                        >
                          {task.collection ? (
                            <StarFilled />
                          ) : (
                            <StarOutlined />
                          )}
                        </NIcon>
                      </div>
                    </div>
                  ))
                }
              }}
            ></NCollapseItem>
          )}
          {variables.other.length > 0 && (
            <NCollapseItem
              title={t('project.menu.other')}
              name='6'
              class='task-cate-other'
              v-slots={{
                default: () => {
                  return variables.other.map((task: any) => (
                    <div
                      class={[styles.draggable, `task-item-${task.type}`]}
                      draggable='true'
                      onDragstart={(e) => {
                        context.emit('dragStart', e, task.type as TaskType)
                      }}
                    >
                      <em
                        class={[
                          styles['sidebar-icon'],
                          styles['icon-' + task.type.toLocaleLowerCase()]
                        ]}
                      />
                      <span>{task.taskName}</span>
                      <div
                        class={styles.stars}
                        onMouseenter={() => {
                          task.starHover = true
                        }}
                        onMouseleave={() => {
                          task.starHover = false
                        }}
                        onClick={() => handleCollection(task)}
                      >
                        <NIcon
                          size='20'
                          color={
                            task.collection || task.starHover
                              ? '#288FFF'
                              : '#ccc'
                          }
                        >
                          {task.collection ? (
                            <StarFilled />
                          ) : (
                            <StarOutlined />
                          )}
                        </NIcon>
                      </div>
                    </div>
                  ))
                }
              }}
            ></NCollapseItem>
          )}
        </NCollapse>
      </div>
  }
})
