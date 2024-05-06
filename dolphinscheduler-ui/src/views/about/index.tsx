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
import {defineComponent, getCurrentInstance, onMounted, ref, toRefs} from 'vue'
import Card from '@/components/card'
import { NSelect, NSpace, NSwitch } from 'naive-ui'
import { useUserStore } from '@/store/user/user'
import { queryProductInfo } from '@/service/modules/ui-plugins'
import {UserInfoRes} from "@/service/modules/users/types";

const userStore = useUserStore()
const about = defineComponent({
  name: 'about',
  setup() {
    const info: any = ref('')
    const queryProduct = async (userId: number) => {
      const productInfo = await queryProductInfo(
          { userId })
      if (!productInfo) throw Error()
      info.value = productInfo.version
    }
    onMounted( () => {
      queryProduct((userStore.getUserInfo as UserInfoRes).id)
    })

    return { queryProduct, info }
  },
  render() {
    const { t } = useI18n()
    const { info } = this
    return (
      <div>
        <Card
          title={t('about.about')}
        >
          <NSpace vertical>
            <NSpace align='center' >
              <span>{t('about.about_version')}</span>
              <div>{ info }</div>
            </NSpace>
          </NSpace>
        </Card>
      </div>
    )
  }
})
export default about
