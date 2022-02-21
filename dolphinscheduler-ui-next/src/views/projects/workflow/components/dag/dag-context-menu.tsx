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

import { defineComponent, onMounted, PropType } from 'vue'
import { useI18n } from 'vue-i18n'
import styles from './menu.module.scss'

const props = {
  visible: {
    type: Boolean as PropType<boolean>,
    default: true
  },
  left: {
    type: Number as PropType<number>,
    default: 0
  },
  top: {
    type: Number as PropType<number>,
    default: 0
  }
}

export default defineComponent({
  name: 'dag-context-menu',
  props,
  emits: ['hide'],
  setup(props, ctx) {
    const hide = () => {
      ctx.emit('hide', false)
    }

    onMounted(() => {
      document.addEventListener('click', () => {
        hide()
      })
    })
  },
  render() {
    const { t } = useI18n()

    return (
      this.visible && (
        <div
          class={styles['dag-context-menu']}
          style={{ left: `${this.left}px`, top: `${this.top}px` }}
        >
          <div class={styles['menu-item']}> {t('project.node.start')}</div>
          <div class={styles['menu-item']}> {t('project.node.edit')}</div>
          <div class={styles['menu-item']}> {t('project.node.copy')}</div>
          <div class={styles['menu-item']}> {t('project.node.delete')}</div>
          <div class={styles['menu-item']}> {t('project.node.view_log')}</div>
        </div>
      )
    )
  }
})
