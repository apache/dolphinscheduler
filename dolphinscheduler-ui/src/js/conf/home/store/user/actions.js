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

import io from '@/module/io'

export default {
  /**
   * get userInfo
   */
  getUserInfo ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('users/get-user-info', payload, res => {
        state.userInfo = res.data
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * sign out
   */
  signOut () {
    io.post('signOut', res => {
      setTimeout(() => {
        window.location.href = `${PUBLIC_PATH}/view/login/index.html`
      }, 100)
    }).catch(e => {
      console.log(e)
    })
  },
  /**
   * get token list
   * User loginUser,
   * Integer pageNo,
   * String searchVal,
   * Integer pageSize
   */
  getTokenListP ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.get('access-token/list-paging', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * create token
   * User loginUser,
   * int userId,
   * String expireTime,
   * String token
   */
  createToken ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('access-token/create', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * update token
   * User loginUser,
   * int userId,
   * String expireTime,
   * String token
   */
  updateToken ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('access-token/update', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * create token
   * User loginUser,
   * int userId,
   * String expireTime
   */
  generateToken ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('access-token/generate', payload, res => {
        resolve(res.data)
      }).catch(e => {
        reject(e)
      })
    })
  },
  /**
   * delete token
   * User loginUser,
   * int  id
   */
  deleteToken ({ state }, payload) {
    return new Promise((resolve, reject) => {
      io.post('access-token/delete', payload, res => {
        resolve(res)
      }).catch(e => {
        reject(e)
      })
    })
  }
}
