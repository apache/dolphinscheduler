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

import { NButton, NForm, NFormItem, NSelect } from "naive-ui";
import { defineComponent } from "vue";
import { useLogTimerStore } from '@/store/logTimer/logTimer'

const setting = defineComponent({
    name: 'ui-setting',
    setup() {
        const logTimerStore = useLogTimerStore()
        const logTimerMap = {
            0: "Off",
        } as any

        const logTimerOptions = [
            {
                label: "Off",
                value: 0,
            },
            {
                label: "10 Seconds",
                value: '10',
            },
            {
                label: "30 Seconds",
                value: '30',
            },
            {
                label: "1 Minute",
                value: '60',
            },
            {
                label: "5 Minute",
                value: '300',
            },
            {
                label: "30 Minute",
                value: '1800',
            },
            ]
        return {logTimerStore, logTimerMap, logTimerOptions}
    },
    render() {
        const defaultLogTimer = this.logTimerStore.getLogTimer;

        return ( 
            <>
            <div>UI Setting</div>
            <NForm>
                <NFormItem label="Log Auto Refresh Time">
                <NSelect
                    default-value={this.logTimerMap[defaultLogTimer]}
                    options={this.logTimerOptions} />
                </NFormItem>
            </NForm>
            <NButton type='primary'>
                Save
            </NButton>
            </>
        )
    }
})

export default setting