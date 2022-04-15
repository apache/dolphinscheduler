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

import { useRoute } from 'vue-router'
import { defineComponent, onMounted, ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { viewVariables } from '@/service/modules/process-instances'
import styles from './variables.module.scss'
import { NButton } from 'naive-ui'

export default defineComponent({
  name: 'variables-view',
  emits: ['copy'],
  setup(props, ctx) {
    const paramsRef = ref<any>()
    const route = useRoute()

    const projectCode = Number(route.params.projectCode)
    const instanceId = Number(route.params.id)

    const globalParams = computed(() => {
      return paramsRef.value ? paramsRef.value.globalParams : []
    })

    const localParams = computed(() => {
      return paramsRef.value ? paramsRef.value.localParams : {}
    })

    const getViewVariables = () => {
      viewVariables(instanceId, projectCode).then((res: any) => {
        paramsRef.value = res
      })
    }

    const handleCopy = (text: string) => {
      ctx.emit('copy', text)
    }

    /**
     * Copyed text processing
     */
    const rtClipboard = (el: any, taskType: string) => {
      const arr: Array<string> = []
      Object.keys(el).forEach((key) => {
        if (taskType === 'SQL' || taskType === 'PROCEDURE') {
          if (key !== 'direct' && key !== 'type') {
            arr.push(`${key}=${el[key]}`)
          }
        } else {
          arr.push(`${key}=${el[key]}`)
        }
      })
      return arr.join(' ')
    }

    const localButton = (index: number, taskType: string, el: any) => {
      return (
        <NButton
          key={index}
          type='primary'
          style={'margin-right: 10px'}
          onClick={() => handleCopy(rtClipboard(el, taskType))}
        >
          {Object.keys(el).map((key: string) => {
            if (taskType === 'SQL' || taskType === 'PROCEDURE') {
              return key !== 'direct' && key !== 'type' ? (
                <span style={'margin-right: 5px'}>
                  <strong style='color: #2A455B;'>{key}</strong> = {el[key]}
                </span>
              ) : (
                ''
              )
            } else {
              return (
                <span style={'margin-right: 5px'}>
                  <strong style='color: #2A455B;'>{key}</strong> = {el[key]}
                </span>
              )
            }
          })}
        </NButton>
      )
    }

    onMounted(() => {
      getViewVariables()
    })

    return {
      globalParams,
      localParams,
      localButton,
      handleCopy
    }
  },
  render() {
    const { t } = useI18n()

    return (
      <div class={styles.variable}>
        <div class={styles.list}>
          <div class={styles.name}>
            <em class='ri-code-s-slash-line'></em>
            <strong style='padding-top: 3px;display: inline-block'>
              {t('project.workflow.global_parameters')}
            </strong>
          </div>
          <div class={styles['var-cont']}>
            {this.globalParams.map((item: any, index: number) => (
              <NButton
                key={index}
                type='primary'
                style={{ marginRight: '5px' }}
                onClick={() => this.handleCopy(`${item.prop}=${item.value}`)}
              >
                <strong style='color: #2A455B; margin-right: 4px'>
                  {item.prop}
                </strong>{' '}
                = {item.value}
              </NButton>
            ))}
          </div>
        </div>
        <div class={styles.list} style='height: 30px;'>
          <div class={styles.name}>
            <em class='ri-code-s-slash-line'></em>
            <strong style='padding-top: 3px;display: inline-block'>
              {t('project.workflow.local_parameters')}
            </strong>
          </div>
          <div class={styles['var-cont']}>&nbsp;</div>
        </div>
        {Object.keys(this.localParams).map(
          (node_name: string, index: number) => (
            <div class={`${styles['list']} ${styles['list-t']}`}>
              <div class={styles['task-name']}>
                Task({index}): {node_name}
              </div>
              <div class={styles['var-cont']}>
                {this.localParams[node_name].localParamsList.map(
                  (el: any, index: number) =>
                    this.localButton(
                      index,
                      this.localParams[node_name].taskType,
                      el
                    )
                )}
              </div>
            </div>
          )
        )}
      </div>
    )
  }
})
