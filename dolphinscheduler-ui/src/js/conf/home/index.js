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

// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import store from './store'
import i18n from '@/module/i18n'
import { sync } from 'vuex-router-sync'
import Chart from '@/module/ana-charts'
import '@/module/filter/formatDate'
import themeData from '@/module/echarts/themeData.json'
import Permissions from '@/module/permissions'
import 'ans-ui/lib/ans-ui.min.css'
import ans from 'ans-ui/lib/ans-ui.min'
import en_US from 'ans-ui/lib/locale/en' // eslint-disable-line
import 'sass/conf/home/index.scss'
import 'bootstrap/dist/css/bootstrap.min.css'

import 'bootstrap/dist/js/bootstrap.min.js'
import 'canvg/dist/browser/canvg.min.js'

// Component internationalization
const useOpt = i18n.globalScope.LOCALE === 'en_US' ? { locale: en_US } : {}

// Vue.use(ans)
Vue.use(ans, useOpt)

sync(store, router)

Vue.config.devtools = true
Vue.config.productionTip = true
Vue.config.silent = true

Chart.config({
  theme: {
    name: 'themeName',
    data: themeData,
    default: true
  }
})

/* eslint-disable no-new */
Permissions.request().then(res => {
  // instance
  new Vue({
    el: '#app',
    router,
    store,
    render: h => h(App),
    mounted () {
      document.addEventListener('click', (e) => {
        $('#contextmenu').css('visibility', 'hidden')
      })
    },
    methods: {
      initApp () {
        $('.global-loading').hide()
        const bootstrapTooltip = $.fn.tooltip.noConflict()
        $.fn.tooltip = bootstrapTooltip
        $('body').tooltip({
          selector: '[data-toggle="tooltip"]',
          trigger: 'hover'
        })
        // init
        i18n.init()
      }
    },
    created () {
      this.initApp()
    }
  })
})
