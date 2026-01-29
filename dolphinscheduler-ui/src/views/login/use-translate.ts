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

import { WritableComputedRef } from 'vue'
import { useLocalesStore } from '@/store/locales/locales'
import type { Locales } from '@/store/locales/types'
import cookies from 'js-cookie'

export function useTranslate(locale: WritableComputedRef<string>) {
  const localesStore = useLocalesStore()

  const handleChange = (value: Locales) => {
    locale.value = value
    localesStore.setLocales(value)
    cookies.set('language', locale.value, { path: '/' })
  }
  return {
    handleChange
  }
}
