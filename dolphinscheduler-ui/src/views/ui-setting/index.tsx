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
import { NForm, NFormItem, NSelect } from "naive-ui";
import { defineComponent } from "vue";
import { useLogTimerStore } from '@/store/logTimer/logTimer'
 
// Update LogTimer store when select value is updated
const handleUpdateValue = (logTimer: number) => {
    const logTimerStore = useLogTimerStore()
    logTimerStore.setLogTimer(logTimer)
}

const setting = defineComponent({
    name: 'ui-setting',
    setup() {
        const logTimerStore = useLogTimerStore()
        const defaultLogTimer = logTimerStore.getLogTimer;

        const logTimerMap = {
            0: "Off",
            10: '10 Seconds',
            30: '30 Seconds',
            60: '1 Minute',
            300: '5 Minutes',
            1800: '30 Minutes'
        } as any

        const logTimerOptions = [
            {
                label: "Off",
                value: 0,
            },
            {
                label: "10 Seconds",
                value: 10,
            },
            {
                label: "30 Seconds",
                value: 30,
            },
            {
                label: "1 Minute",
                value: 60,
            },
            {
                label: "5 Minutes",
                value: 300,
            },
            {
                label: "30 Minutes",
                value: 1800,
            },
            ]
        return {defaultLogTimer, logTimerMap, logTimerOptions}
    },
    render() {
        const { t } = useI18n()

        return ( 
            <>
            <NForm>
                <NFormItem label={t('ui_setting.log.refresh_time')}>
                <NSelect
                    default-value={this.logTimerMap[this.defaultLogTimer]}
                    options={this.logTimerOptions} 
                    onUpdateValue={handleUpdateValue}
                    />
                </NFormItem>
            </NForm>
            </>
        )
    }
})

export default setting