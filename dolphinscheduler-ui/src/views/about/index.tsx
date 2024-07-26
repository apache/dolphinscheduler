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

import {useI18n} from 'vue-i18n'
import {defineComponent, onMounted, ref} from 'vue'
import Card from '@/components/card'
import {NSpace} from 'naive-ui'
import {queryProductInfo} from '@/service/modules/ui-plugins'
import {style} from "@antv/x6/es/registry/attr/style";
import {size} from "lodash";

const about = defineComponent({
    name: 'about',
    setup() {
        const info: any = ref('')
        const queryProduct = async () => {
            const productInfo = await queryProductInfo()
            if (!productInfo) throw Error()
            info.value = productInfo.version
        }
        onMounted(() => {
            queryProduct()
        })

        return {queryProduct, info}
    },
    render() {
        const { t } = useI18n();
        const { info } = this;

        return (
            <div style={{ padding: '20px' }}>
                <Card style={{width: '50%', background: 'white', border: 'none'}}>
                    <div slot="title" style="font-size: 40px">
                        {t('about.about')}
                    </div>
                    <NSpace vertical style={{padding: '20px'}}>
                        <NSpace align='center' style={{marginBottom: '10px'}}>
                            <img src="../../images/logo.png" alt="DolphinScheduler Logo"
                                 style={{maxWidth: '100%', width: '66%', height: 'auto'}}/>
                        </NSpace>
                        <NSpace align='center' style={{marginBottom: '10px'}}>
                            <span style={{fontSize: '1.2em', fontWeight: 'bold'}}>{t('about.about_version')}</span>
                            <div style={{fontSize: '1.5em'}}>{info}</div>
                        </NSpace>
                        <NSpace align='center' style={{marginBottom: '10px'}}>
                            <span style={{fontSize: '1.2em', fontWeight: 'bold'}}>{t('about.homepage')}</span>
                            <a href="https://dolphinscheduler.apache.org/" target="_blank"
                               style={{fontSize: '1.5em', color: '#1890ff'}}>dolphinscheduler.apache.org</a>
                        </NSpace>
                        <NSpace align='center' style={{marginBottom: '10px'}}>
                            <span style={{fontSize: '1.2em', fontWeight: 'bold'}}>{t('about.github_page')}</span>
                            <a href="https://github.com/apache/dolphinscheduler" target="_blank"
                               style={{fontSize: '1.5em', color: '#1890ff'}}>dolphinscheduler</a>
                        </NSpace>
                        <NSpace align='center' style={{marginBottom: '10px'}}>
                            <span
                                style={{fontSize: '1.2em', fontWeight: 'bold'}}>{t('about.product_introduction')}</span>
                            <p style={{fontSize: '1.5em'}}>{t('about.Apache DolphinScheduler is a distributed and extensible open-source workflow orchestration platform with powerful DAG visual interfaces')}</p>
                        </NSpace>
                        <NSpace align='center' style={{marginBottom: '10px'}}>
                            <span style={{fontSize: '1.2em', fontWeight: 'bold'}}>{t('about.license')}</span>
                            <a href="http://www.apache.org/licenses/LICENSE-2.0" target="_blank"
                               style={{fontSize: '1.5em', color: '#1890ff'}}>LICENSE-2.0</a>
                        </NSpace>

                    </NSpace>
                </Card>
            </div>
        );
    }

})
export default about

