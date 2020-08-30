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
<template>
  <div class="login-model">
    <div class="text-1">
      <a href="javascript:"></a>
    </div>
    <div class="from-model">
      <div class="list">
        <label>{{$t('User Name')}}</label>
        <div>
          <x-input
                  size="large"
                  type="text"
                  v-model.trim="userName"
                  :placeholder="$t('Please enter user name')"
                  maxlength="60"
                  @on-enterkey="_ok">
          </x-input>
        </div>
        <p class="error" v-if="isUserPassword">
          {{userNameText}}
        </p>
      </div>
      <div class="list">
        <label>{{$t('Password')}}</label>
        <div>
          <x-input
                  type="password"
                  size="large"
                  v-model="userPassword"
                  :placeholder="$t('Please enter your password')"
                  maxlength="20"
                  @on-enterkey="_ok">
          </x-input>
        </div>
        <p class="error" v-if="isUserPassword">
          {{userPasswordText}}
        </p>
      </div>
      <div class="list" style="margin-top: 10px;">
        <x-button type="primary" shape="circle" size="large" :loading="spinnerLoading" long @click="_ok">{{spinnerLoading ? 'Loading...' : ` ${$t('Login')} `}} </x-button>
      </div>
    </div>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import io from '@/module/io'
  import cookies from 'js-cookie'

  export default {
    name: 'login-model',
    data () {
      return {
        spinnerLoading: false,
        userName: '',
        userPassword: '',
        isUserName: false,
        isUserPassword: false,
        userNameText: '',
        userPasswordText: ''
      }
    },
    props: {},
    methods: {
      _ok () {
        if (this._verification()) {
          this.spinnerLoading = true
          this._gLogin().then(res => {
            setTimeout(() => {
              this.spinnerLoading = false
              sessionStorage.setItem('isLeft',1);
              if (res.data.hasOwnProperty("sessionId")) {
                let sessionId=res.data.sessionId
                sessionStorage.setItem("sessionId", sessionId)
                cookies.set('sessionId', sessionId,{ path: '/' })
              }
              
              if (this.userName === 'admin') {
                window.location.href = `${PUBLIC_PATH}/#/security/tenant`
              } else {
                window.location.href = `${PUBLIC_PATH}/#/home`
              }
            }, 1000)
          }).catch(e => {
            this.userPasswordText = e.msg
            this.isUserPassword = true
            this.spinnerLoading = false
          })
        }
      },
      _verification () {
        let flag = true
        if (!this.userName) {
          this.userNameText = `${i18n.$t('Please enter user name')}`
          this.isUserName = true
          flag = false
        }
        if (!this.userPassword) {
          this.userPasswordText = `${i18n.$t('Please enter your password')}`
          this.isUserPassword = true
          flag = false
        }
        return flag
      },
      _gLogin () {
        return new Promise((resolve, reject) => {
          io.post(`login`, {
            userName: this.userName,
            userPassword: this.userPassword
          }, res => {
            resolve(res)
          }).catch(e => {
            reject(e)
          })
        })
      }
    },
    watch: {
      userName () {
        this.isUserName = false
      },
      userPassword () {
        this.isUserPassword = false
      }
    },
    created () {
    },
    mounted () {
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .login-model {
    width: 400px;
    min-height: 260px;
    background: #fff;
    border-radius: 3px;
    position: fixed;
    left:50%;
    top: 50%;
    margin-left: -200px;
    margin-top: -200px;
    z-index: 1;
    box-shadow: 0px 2px 25px 0px rgba(0, 0, 0, .3);
    .text-1 {
      padding-top: 30px;
      margin-bottom: -6px;
      a {
        width: 280px;
        height: 60px;
        display: block;
        background: url("img/login-logo.svg") no-repeat 23px;
        margin: 0 auto;
      }
    }
    .from-model {
      padding: 30px 20px;
      .list {
        margin-bottom: 24px;
        >label {
          font-size: 14px;
          display: block;
          height: 24px;
          line-height: 24px;
          font-weight: normal;
          color: #333;
        }
        >.error {
          font-size: 12px;
          color: #ff0000;
          padding-top: 6px;
        }
        &:last-child {
          margin-bottom: 6px;
        }
      }
    }
    .ctr {
      width: 400px;
      text-align: center;
      position: absolute;
      left: 0;
      bottom: -80px;
      color: #fff;
    }
  }
</style>
