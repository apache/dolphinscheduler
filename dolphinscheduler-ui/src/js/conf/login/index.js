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
// import $ from 'jquery'
import Vue from 'vue'
import App from './App'
import i18n from '@/module/i18n'
import 'ans-ui/lib/ans-ui.min.css'
import ans from 'ans-ui/lib/ans-ui.min'

import 'sass/conf/login/index.scss'
import 'bootstrap/dist/js/bootstrap.min.js'

Vue.use(ans)

Vue.config.devtools = true
Vue.config.productionTip = true
Vue.config.silent = true

/* eslint-disable no-new */
new Vue({
  el: '#app',
  render: h => h(App),
  mounted () {
  },
  methods: {
    initApp () {
      const bootstrapTooltip = $.fn.tooltip.noConflict()
      $.fn.tooltip = bootstrapTooltip
      $('body').tooltip({
        selector: '[data-toggle="tooltip"]',
        trigger: 'hover'
      })
      // Component internationalization
      i18n.init()
    }
  },
  created () {
    this.initApp()
  }
})
