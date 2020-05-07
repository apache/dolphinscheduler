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

import _ from 'lodash'
import Vue from 'vue'
import store from '@/conf/home/store'

const Permissions = function () {
  this.isAuth = true
}

Permissions.prototype = {
  // Get permission based on userInfo
  request () {
    return new Promise((resolve, reject) => {
      store.dispatch('user/getUserInfo').then(res => {
        if (res.userType !== 'GENERAL_USER') {
          this.isAuth = false
        }
        this.ps(res)
        resolve()
      })
    })
  },
  // Command authority status
  ps (res) {
    Vue.directive('ps', {
      bind: function (el, binding, vnode) {
        if (!Vue.prototype.$_ps(binding.value)) {
          if ($(el).prop('tagName') === 'BUTTON') {
            $(el).attr('disabled', true)
          } else {
            setTimeout(function () { el.parentNode.removeChild(el) }, 100)
          }
        }
      }
    })
    // Permission check method
    Vue.prototype.$_ps = function (valueArr) {
      return _.indexOf(valueArr, res.userType) !== -1
    }
  },
  // External access permission status
  getAuth () {
    return this.isAuth
  }
}

export default new Permissions()
