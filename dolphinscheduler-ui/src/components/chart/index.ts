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

import { getCurrentInstance, onMounted, onBeforeUnmount, watch } from 'vue'
import { useThemeStore } from '@/store/theme/theme'
import { throttle } from 'echarts'
import { useI18n } from 'vue-i18n'
import type { Ref } from 'vue'
import type { ECharts } from 'echarts'
import type { ECBasicOption } from 'echarts/types/dist/shared'

function initChart<Opt extends ECBasicOption>(
  domRef: Ref<HTMLDivElement | null>,
  option: Opt
): ECharts | null {
  let chart: ECharts | null = null
  const themeStore = useThemeStore()
  const { locale } = useI18n()
  const globalProperties =
    getCurrentInstance()?.appContext.config.globalProperties

  option['backgroundColor'] = ''

  const init = () => {
    chart = globalProperties?.echarts.init(
      domRef.value,
      themeStore.darkTheme ? 'dark-bold' : 'macarons'
    )
    chart && chart.setOption(option)
  }

  const resize = throttle(() => {
    chart && chart.resize()
  }, 20)

  watch(
    () => themeStore.darkTheme,
    () => {
      chart?.dispose()
      init()
    }
  )

  watch(
    () => locale.value,
    () => {
      chart?.dispose()
      init()
    }
  )

  watch(
    () => option,
    () => {
      chart?.dispose()
      init()
    },
    {
      deep: true
    }
  )

  onMounted(() => {
    init()
    addEventListener('resize', resize)
  })

  onBeforeUnmount(() => {
    removeEventListener('resize', resize)
  })

  return chart
}

export default initChart
