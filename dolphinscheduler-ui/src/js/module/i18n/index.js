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

/* eslint-disable */
import Vue from 'vue'
import { findLocale } from './config'
import { template } from '@/module/util'
import cookies from 'js-cookie'

const globalScope = typeof window !== 'undefined' && window.document ? window : global

const $t = (str, data) => {
  // test
  // return str

  if (!str) return ''

  if (!globalScope.LOCALE) {
    init()
  }

  let language = findLocale(globalScope.LOCALE).locale

  /**
   * $t('等待查询aaa',{aaa:111})
   */
  return template(language[str], data)
}

const locale = (lang) => {
  // global
  globalScope.LOCALE = lang
  // cookies
  cookies.set('language', lang,{ path: '/' })
}

const init = () => {
  let language = cookies.get('language')
  if (language) {
    locale(language)
  }else{
    /**
     * Browser language
     */
    let lang = (navigator.language || navigator.userLanguage).indexOf("CN") !== -1
    locale(lang ? 'zh_CN' : 'en_US')
  }
}

// Expose to global scope for quick access.
globalScope.$t = $t

Vue.prototype.$t = $t

export default {
  init,
  $t,
  locale,
  globalScope
}
