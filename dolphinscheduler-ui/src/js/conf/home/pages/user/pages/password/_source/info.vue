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
  <div class="user-info-model password-model">
    <m-list-box-f>
      <template slot="name">{{$t('User Name')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.userName}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('Password')}}</template>
      <template slot="content">
        <x-input
                style="width: 320px;"
                type="password"
                v-model="userPassword"
                :placeholder="$t('Please enter your password')">
        </x-input>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('Confirm Password')}}</template>
      <template slot="content">
        <x-input
                style="width: 320px;"
                type="password"
                v-model="oldUserPassword"
                :placeholder="$t('Please enter confirm password')">
        </x-input>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">&nbsp;</template>
      <template slot="content">
        <x-button type="primary" shape="circle" @click="_edit()" :loading="spinnerLoading">{{spinnerLoading ? 'Loading...' : $t('Edit')}}</x-button>
      </template>
    </m-list-box-f>
  </div>
</template>
<script>
  import i18n from '@/module/i18n'
  import { mapState, mapActions } from 'vuex'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'password-info',
    data () {
      return {
        // loading
        spinnerLoading: false,
        // user password
        userPassword: '',
        // Confirm password
        oldUserPassword: ''
      }
    },
    props: {},
    methods: {
      ...mapActions('user', ['signOut']),
      ...mapActions('security', ['updateUser']),
      /**
       * edit
       */
      _edit () {
        // verification
        if (this._verification()) {
          let param = {
            id: this.userInfo.id,
            userName: this.userInfo.userName,
            userPassword: this.userPassword,
            tenantId: this.userInfo.tenantId,
            email: this.userInfo.email,
            phone: this.userInfo.phone
          }
          this.spinnerLoading = true
          this.updateUser(param).then(res => {
            this.$message.success(res.msg)
            setTimeout(() => {
              this.spinnerLoading = false
              this.signOut()
            }, 1500)
          }).catch(e => {
            this.$message.error(e.msg || '')
            this.spinnerLoading = false
          })
        }
      },
      /**
       * verification
       */
      _verification () {
        let regCn = new RegExp('[\\u4E00-\\u9FFF]+', 'g')

        // password
        if (regCn.test(this.userPassword)) {
          this.$message.warning(`${i18n.$t('Password cannot be in Chinese')}`)
          return false
        }
        if (!this.userPassword) {
          this.$message.warning(`${i18n.$t('Please enter a password (6-22) character password')}`)
          return false
        }
        if (this.userPassword.length < 6 || this.userPassword.length > 22) {
          this.$message.warning(`${i18n.$t('Please enter a password (6-22) character password')}`)
          return false
        }

        // confirm password
        if (regCn.test(this.oldUserPassword)) {
          this.$message.warning(`${i18n.$t('Confirmation password cannot be in Chinese')}`)
          return false
        }
        if (!this.oldUserPassword) {
          this.$message.warning(`${i18n.$t('Please enter a confirmation password (6-22) character password')}`)
          return false
        }
        if (this.oldUserPassword.length < 6 || this.oldUserPassword.length > 22) {
          this.$message.warning(`${i18n.$t('Please enter a confirmation password (6-22) character password')}`)
          return false
        }
        if (this.userPassword !== this.oldUserPassword) {
          this.$message.warning(`${i18n.$t('The password is inconsistent with the confirmation password')}`)
          return false
        }
        return true
      }
    },
    computed: {
      ...mapState('user', ['userInfo'])
    },
    components: { mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .password-model {
    .list-box-f {
      margin-bottom: 30px;
    }
  }
</style>
