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
import { useI18n } from 'vue-i18n'
import { defineComponent, ref } from 'vue'
import { SearchOutlined } from '@vicons/antd'
import { NButton, NIcon, NInput, NSpace } from 'naive-ui'
import Card from '@/components/card'
import styles from './index.module.scss'

const Conditions = defineComponent({
  name: 'Conditions',
  emits: ['conditions'],
  setup(props, ctx) {
    const searchVal = ref()
    const handleConditions = () => {
      ctx.emit('conditions', _.trim(searchVal.value))
    }

    return { searchVal, handleConditions }
  },
  render() {
    const { t } = useI18n()
    const { $slots, handleConditions } = this
    return (
      <Card style={{ marginBottom: '5px' }}>
        <div class={styles['conditions-model']}>
          <NSpace>{$slots}</NSpace>
          <div class={styles.right}>
            <div class={styles['form-box']}>
              <div class={styles.list}>
                <NButton onClick={handleConditions}>
                  <NIcon>
                    <SearchOutlined />
                  </NIcon>
                </NButton>
              </div>
              <div class={styles.list}>
                <NInput
                  placeholder={t('resource.file.enter_keyword_tips')}
                  v-model={[this.searchVal, 'value']}
                />
              </div>
            </div>
          </div>
        </div>
      </Card>
    )
  },
})

export default Conditions
