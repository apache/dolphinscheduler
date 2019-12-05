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

if (typeof window === 'undefined') {
  throw new Error('For browser use only.')
}

const root = window
const storage = root.localStorage

const localStorage = {
  setItem: function (skey, sval) {
    try {
      return storage.setItem(skey, sval)
    } catch (e) {
      console.info(e)
    }
  },
  getItem: function (skey) {
    try {
      return storage.getItem(skey)
    } catch (e) {
      console.info(e)
      return null
    }
  },
  removeItem: function (skey) {
    try {
      return storage.removeItem(skey)
    } catch (e) {
      console.info(e)
      return null
    }
  },
  getJSON: function (skey, p) {
    try {
      var d = storage.getItem(skey)
      if (d) {
        d = JSON.parse(d)
        return d[p]
      }
    } catch (e) {
      console.info(e)
    }
  },
  setJSON: function (skey, p, val) {
    try {
      var f = storage.getItem(skey)
      f = f ? JSON.parse(f) : {}
      f[p] = val
      storage.setItem(skey, JSON.stringify(f))
    } catch (e) {
      console.info(e)
    }
  },
  removeJSON: function (skey, p) {
    try {
      var d = storage.getItem(skey)
      if (d) {
        d = JSON.parse(d)
        delete d[p]
        storage.setItem(skey, JSON.stringify(d))
      }
    } catch (e) {
      console.info(e)
    }
  }
}

export default localStorage
