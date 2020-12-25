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

import io from '@/module/axios/index'
import cookies from 'js-cookie'

const apiPrefix = '/dolphinscheduler'
const reSlashPrefix = /^\/+/

const resolveURL = (url) => {
  if (url.indexOf('http') !== -1) {
    return url
  }
  if (url.charAt(0) !== '/') {
    return `${apiPrefix}/${url.replace(reSlashPrefix, '')}`
  }

  return url
}

/**
 * Resolve backend api url
 */
export { resolveURL }

/**
 * Set io default instance resolveUrl globally
 */
io.config.resolveURL = resolveURL
io.config.timeout = 0
io.config.maxContentLength = 200000
io.config.validateStatus = function (status) {
  if (status === 401 || status === 504) {
    window.location.href = `${PUBLIC_PATH}/view/login/index.html`
    return
  }
  return status
}

// io.config.emulateJSON = false
const _propRequest = io.request

// Add a local request interceptor
io.request = (spec) => {
  return _propRequest.call(io, spec)
}

// Global response interceptor registion
io.interceptors.response.use(
  response => {
    return response
  }, error => {
    // Do something with response error
    return Promise.reject(error)
  }
)

// Global request interceptor registion
io.interceptors.request.use(
  config => {
    const sIdCookie = cookies.get('sessionId')
    const sessionId = sessionStorage.getItem('sessionId')
    const requstUrl = config.url.substring(config.url.lastIndexOf('/') + 1)
    if (sIdCookie !== null && requstUrl !== 'login' && sIdCookie !== sessionId) {
      window.location.href = `${PUBLIC_PATH}/view/login/index.html`
    } else {
      const { method } = config
      if (method === 'get') {
        config.params = Object.assign({}, config.params, {
          _t: Math.random()
        })
      }
      return config
    }
  }, error => {
    // Do something with request error
    return Promise.reject(error)
  }
)

export default io
