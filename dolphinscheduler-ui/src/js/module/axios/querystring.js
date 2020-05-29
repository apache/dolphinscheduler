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
/* istanbul ignore next */
var param = function (a) {
  var s = []
  var rbracket = /\[\]$/
  var isArray = function (obj) {
    return Object.prototype.toString.call(obj) === '[object Array]'
  }
  var add = function (k, v) {
    v = typeof v === 'function' ? v() : v === null ? '' : v === undefined ? '' : v
    s[s.length] = encodeURIComponent(k) + '=' + encodeURIComponent(v)
  }
  var buildParams = function (prefix, obj) {
    var i, len, key

    if (prefix) {
      if (isArray(obj)) {
        for (i = 0, len = obj.length; i < len; i++) {
          if (rbracket.test(prefix)) {
            add(prefix, obj[i])
          } else {
            buildParams(prefix + '[' + (typeof obj[i] === 'object' ? i : '') + ']', obj[i])
          }
        }
      } else if (obj && String(obj) === '[object Object]') {
        for (key in obj) {
          buildParams(prefix + '[' + key + ']', obj[key])
        }
      } else {
        add(prefix, obj)
      }
    } else if (isArray(obj)) {
      for (i = 0, len = obj.length; i < len; i++) {
        add(obj[i].name, obj[i].value)
      }
    } else {
      for (key in obj) {
        buildParams(key, obj[key])
      }
    }
    return s
  }
  return buildParams('', a).join('&').replace(/%20/g, '+')
}

module.exports = param
