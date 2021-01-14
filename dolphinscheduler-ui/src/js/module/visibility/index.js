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

let hidden, visibilityChange

if (typeof document.hidden !== 'undefined') { // Opera 12.10 and Firefox 18 and later support
  hidden = 'hidden'
  visibilityChange = 'visibilitychange'
} else if (typeof document.msHidden !== 'undefined') {
  hidden = 'msHidden'
  visibilityChange = 'msvisibilitychange'
} else if (typeof document.webkitHidden !== 'undefined') {
  hidden = 'webkitHidden'
  visibilityChange = 'webkitvisibilitychange'
} else if (typeof document.mozHidden !== 'undefined') {
  hidden = 'mozHidden'
  visibilityChange = 'mozvisibilitychange'
}

const visibility = {
  /**
   * Callback when visibility changed
   *
   * @param {Function (event, hidden)} callback
   *  - {Event} event
   *  - {Boolean} hidden
   */
  change (callback) {
    if (visibility.isSupported()) {
      document.addEventListener(visibilityChange, evt => callback(evt, document[hidden]), false)
    } else {
      // fallback
      if (document.addEventListener) {
        window.addEventListener('focus', evt => callback(evt, false), false)
        window.addEventListener('blur', evt => callback(evt, true), false)
      } else {
        document.attachEvent('onfocusin', evt => callback(evt, false))
        document.attachEvent('onfocusout', evt => callback(evt, true))
      }
    }
  },
  /**
   * Return true if browser support Page Visibility API.
   */
  isSupported () {
    return hidden !== undefined
  },
  /**
   * Return true if page now isn’t visible to user.
   */
  hidden () {
    return document[hidden]
  }
}

export default visibility
