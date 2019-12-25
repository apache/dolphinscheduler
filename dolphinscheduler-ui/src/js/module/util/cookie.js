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
let document = window.document
let trim = function (s) {
  if (typeof s !== 'string') {
    throw new Error('trim need a string as parameter')
  }
  let len = s.length, i = 0, j = len - 1, re = /(\u3000|\s|\t|\u00A0)/
  while (i < len && re.test(s.charAt(i))) ++i
  while (j >= 0 && re.test(s.charAt(j))) --j
  return s.substring(i, j + 1)
}
let copy = function (o) {
  let d = {}
  for (let k in o) { if (o.hasOwnProperty(k)) d[k] = o[k] }
  return d
}
/**
 * Cookie setter & setter
 *
 * @param {String} name The identify name of cookie.
 * @param {String} value (Optional) String to set cookie value. (`null` to remove cookie)
 * @param {Object} options (Optional) Set the cooke native options, (path domain, secure, expires)
 */
let cookie = function (name, value, options) {
  options = options || {}
  if (value !== undefined) { // set cookie
    options = copy(options)
    if (value === null) {
      value = ''
      options.expires = -1
    }
    if (typeof options.expires === 'number') {
      let days = options.expires, t = options.expires = new Date()
      t.setTime(t.getTime() + days * 864e+5) // 24 * 60 * 60 * 1000
    }
    let encode = function (s) {
      try {
        return options.raw ? s : encodeURIComponent(s)
      } catch (e) {
      }
      return s
    }
    return (document.cookie = [
      encode(name), '=', encode(value),
      options.expires ? '; expires=' + options.expires.toUTCString() : '', // use expires attribute, max-age is not supported by IE
      options.path ? '; path=' + options.path : '',
      options.domain ? '; domain=' + options.domain : '',
      options.secure ? '; secure' : ''
    ].join(''))
  } else {
    let value = null,
      cookie = document.cookie,
      decode = function (s) {
        return options.raw ? s : decodeURIComponent(s)
      },
      cookies = cookie ? cookie.split('; ') : []
    for (let i = -1, l = cookies.length, c = name.length + 1; ++i < l;) {
      cookie = trim(cookies[i])
      if (cookie.substring(0, c) === (name + '=')) {
        value = decode(cookie.substring(c))
        break
      }
    }
    return value
  }
}
cookie.set = function (k, v, opts) {
  return cookie(k, v, opts)
}
cookie.get = function (k) {
  return cookie(k)
}
export default cookie
