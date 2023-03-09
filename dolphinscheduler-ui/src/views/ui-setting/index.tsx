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

import { useI18n } from 'vue-i18n'
import { NSelect, NSpace, NSwitch } from 'naive-ui'
import { defineComponent } from 'vue'
import { useUISettingStore } from '@/store/ui-setting/ui-setting'
import Card from '@/components/card'

// Update LogTimer store when select value is updated
const handleUpdateValue = (logTimer: number) => {
  const uiSettingStore = useUISettingStore()
  uiSettingStore.setLogTimer(logTimer)
}

const setting = defineComponent({
  name: 'ui-setting',
  setup() {
    const uiSettingStore = useUISettingStore()

    const logTimerMap = {
      0: 'Off',
      10: '10 Seconds',
      30: '30 Seconds',
      60: '1 Minute',
      300: '5 Minutes',
      1800: '30 Minutes'
    } as any

    const logTimerOptions = [
      {
        label: 'Off',
        value: 0
      },
      {
        label: '10 Seconds',
        value: 10
      },
      {
        label: '30 Seconds',
        value: 30
      },
      {
        label: '1 Minute',
        value: 60
      },
      {
        label: '5 Minutes',
        value: 300
      },
      {
        label: '30 Minutes',
        value: 1800
      }
    ]
    return { uiSettingStore, logTimerMap, logTimerOptions }
  },
  render() {
    const { t } = useI18n()

    return (
      <Card
        style={{ marginLeft: '25%', width: '50%' }}
        title={t('menu.ui_setting')}
      >
        <h4>{t('ui_setting.request_settings')}</h4>
        <NSpace vertical>
          <NSpace align='center' justify='space-between'>
            <span>{t('ui_setting.api_timeout')}</span>
            <NSelect
              style={{ width: '200px' }}
              default-value={this.uiSettingStore.getApiTimer}
              options={[
                { label: '10000 ' + t('ui_setting.millisecond'), value: 10000 },
                { label: '20000 ' + t('ui_setting.millisecond'), value: 20000 },
                { label: '30000 ' + t('ui_setting.millisecond'), value: 30000 },
                { label: '40000 ' + t('ui_setting.millisecond'), value: 40000 },
                { label: '50000 ' + t('ui_setting.millisecond'), value: 50000 },
                { label: '60000 ' + t('ui_setting.millisecond'), value: 60000 }
              ]}
              onUpdateValue={(t) => this.uiSettingStore.setApiTimer(t)}
            />
          </NSpace>
          <NSpace align='center' justify='space-between'>
            <span>{t('ui_setting.refresh_time')}</span>
            <NSelect
              style={{ width: '200px' }}
              default-value={this.logTimerMap[this.uiSettingStore.getLogTimer]}
              options={this.logTimerOptions}
              onUpdateValue={handleUpdateValue}
            />
          </NSpace>
        </NSpace>
        <h4>{t('ui_setting.experimental_feature')}</h4>
        <NSpace align='center' justify='space-between'>
          <span>{t('ui_setting.dynamic_task_component')}</span>
          <NSwitch
            round={false}
            defaultValue={this.uiSettingStore.getDynamicTask}
            onUpdateValue={() => this.uiSettingStore.setDynamicTask()}
          ></NSwitch>
        </NSpace>
      </Card>
    )
  }
})

export default setting
