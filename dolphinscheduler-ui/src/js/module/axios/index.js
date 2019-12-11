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
const _ = require('lodash')
const axios = require('axios')
const combineURLs = require('axios/lib/helpers/combineURLs')
const buildURL = require('axios/lib/helpers/buildURL')

const qs = require('./querystring')
const jsonp = require('./jsonp')

const preflightDataMethods = ['post', 'put', 'patch']
const API_ASSERT_OK = 0

const def = (o, p, v, desc) =>
  Object.defineProperty(o, p,
    Object.assign({ writable: false, enumerable: false, configurable: false }, desc, { value: v }))

const normalizeArgs = (method, url, data, success, fail, config) => {
  if (_.isFunction(data)) {
    config = fail
    fail = success
    success = data
  }
  if (_.isPlainObject(data)) {
    if (!_.includes(preflightDataMethods, method)) {
      config = _.merge({}, config, { params: data })
    } else {
      config = _.merge({}, config, { data })
    }
  } else {
    config = config || {}
  }
  config.method = method
  config.url = url
  return {
    success, fail, config
  }
}

const generalHandle = (data, res, resolve, reject, success, fail) => {
  if (!data || +(data.code || 0) !== API_ASSERT_OK) {
    fail && fail(data)
    reject(data)
  } else {
    success && success(data)
    resolve(data)
  }
}

const isAbsUrl = (url) => {
  return /^(https?:)?\/\//i.test(url)
}

const resolveURL = (base, path) => {
  if (!base || (path && isAbsUrl(path))) {
    return path
  }
  return combineURLs(base, path)
}

const create = (cfg) => new InnerCtor(cfg)

class InnerCtor {
  constructor (defaults) {
    const inter = axios.create(defaults)

    // { baseURL, timeout, ... }
    this.config = Object.assign(
      {
        baseURL: '',
        timeout: 0,
        resolveURL: u => u
      },
      defaults
    )

    this.inter = inter
    this.interceptors = inter.interceptors

    this.jsonp = this.jsonp.bind(this)

    // Exporse the internal json api
    this.jsonp.inter = jsonp

    // Generates shortcuts by http method.
    ;['get', 'delete', 'head', 'options', 'post', 'put', 'patch'].forEach((method) => {
      this[method] = function (url, data, success, fail, config) {
        return this.request({ url, method, data, success, fail, config })
      }.bind(this)
    })
  }

  request ({ url, method, data, success, fail, config }) {
    const configs = normalizeArgs(method, this.config.resolveURL(url), data, success, fail, config)
    configs.config = _.merge({}, this.config, configs.config)

    // fallback application/json to application/x-www-form-urlencoded
    if (configs.config.emulateJSON !== false) {
      configs.config.data = qs(configs.config.data)
    }

    return new Promise((resolve, reject) => {
      this.inter.request(configs.config)
        .then((res) => {
          if (method === 'head' || method === 'options') {
            res.data = res.headers
          }
          generalHandle(res.data, res, resolve, reject, configs.success, configs.fail)
        })
        .catch(err => {
          let ret, code
          /* istanbul ignore else */
          if (err.response && err.response.status) {
            code = err.response.status
          } else {
            code = 500
          }
          if (err.response && (method === 'head' || method === 'options')) {
            err.response.data = err.response.headers
          }
          /* istanbul ignore else */
          if (err.response && err.response.data) {
            if (_.isString(err.response.data)) {
              ret = {
                message: err.message,
                code,
                data: err.response.data
              }
            } else {
              ret = err.response.data
            }
          } else {
            ret = {
              code,
              message: err.message,
              data: null
            }
          }
          def(ret, '$error', err)
          reject(ret)
        })
    })
  }

  jsonp (url, data, success, fail, config) {
    const configs = normalizeArgs('jsonp', this.config.resolveURL(url), data, success, fail, config)

    configs.config = _.merge({}, this.config, configs.config)
    configs.url = buildURL(resolveURL(configs.config.baseURL, configs.config.url), configs.config.params)

    return new Promise((resolve, reject) => {
      jsonp(configs.url, configs.config, (err, data) => {
        if (err) {
          const ret = {
            code: 500,
            message: err.message,
            data: null
          }
          def(ret, '$error', err)
          reject(ret)
        } else {
          generalHandle(data, data, resolve, reject, configs.success, configs.fail)
        }
      })
    })
  }
}

module.exports = Object.assign(create({}), { create, axios })
