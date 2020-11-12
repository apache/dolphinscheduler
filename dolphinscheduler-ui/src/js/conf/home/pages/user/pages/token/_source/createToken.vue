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
  <m-popup
          ref="popup"
          :ok-text="item ? $t('Edit') : $t('Submit')"
          :nameText="item ? $t('Edit token') : $t('Create token')"
          @ok="_ok">
    <template slot="content">
      <div class="create-token-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Expiration time')}}</template>
          <template slot="content">
            <x-datepicker
                    :disabled-date="disabledDate"
                    v-model="expireTime"
                    @on-change="_onChange"
                    format="YYYY-MM-DD HH:mm:ss"
                    :panelNum="1">
            </x-datepicker>
          </template>
        </m-list-box-f>
        <m-list-box-f v-if="auth">
          <template slot="name"><strong>*</strong>{{$t('User')}}</template>
          <template slot="content">
            <x-select v-model="userId" @on-change="_onChange">
              <x-option
                      v-for="city in userIdList"
                      :key="city.id"
                      :value="city.id"
                      :label="city.userName">
              </x-option>
            </x-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">Token</template>
          <template slot="content">
            <x-input
                    readonly
                    style="width: 306px;"
                    type="input"
                    v-model="token"
                    :placeholder="$t('Please enter token')">
            </x-input>
            <x-button type="ghost" @click="_generateToken" :loading="tokenLoading">{{$t('Generate token')}}</x-button>
          </template>
        </m-list-box-f>
      </div>
    </template>
  </m-popup>
</template>
<script>
  import _ from 'lodash'
  import dayjs from 'dayjs'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import Permissions from '@/module/permissions'
  import mPopup from '@/module/components/popup/popup'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-token',
    data () {
      return {
        store,
        expireTime: dayjs().format('YYYY-MM-DD 23:59:59'),
        userId: null,
        disabledDate: date => (date.getTime() - new Date(new Date().getTime() - 24 * 60 * 60 * 1000)) < 0,
        token: '',
        userIdList: [],
        tokenLoading: false,
        auth: !Permissions.getAuth()
      }
    },
    props: {
      item: Object
    },
    methods: {
      _ok () {
        if (this._verification()) {
          this._submit()
        }
      },
      _verification () {
        if (!this.token) {
          this.$message.warning(`${i18n.$t('Please generate token')}`)
          return false
        }
        return true
      },
      _submit () {
        let param = {
          expireTime: dayjs(this.expireTime).format('YYYY-MM-DD HH:mm:ss'),
          userId: this.userId,
          token: this.token
        }
        if (this.item) {
          param.id = this.item.id
        }
        this.$refs['popup'].spinnerLoading = true
        this.store.dispatch(`user/${this.item ? 'updateToken' : 'createToken'}`, param).then(res => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          setTimeout(() => {
            this.$refs['popup'].spinnerLoading = false
          }, 800)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs['popup'].spinnerLoading = false
        })
      },
      _generateToken () {
        this.tokenLoading = true
        this.store.dispatch(`user/generateToken`, {
          userId: this.userId,
          expireTime: this.expireTime
        }).then(res => {
          setTimeout(() => {
            this.tokenLoading = false
            this.token = res
          }, 1200)
        }).catch(e => {
          this.token = ''
          this.$message.error(e.msg || '')
          this.tokenLoading = false
        })
      },
      _onChange () {
        this.token = ''
      }
    },
    watch: {},
    created () {
      const d = (userId) => {
        if (this.item) {
          this.expireTime = this.item.expireTime
          this.userId = this.item.userId
          this.token = this.item.token
        } else {
          this.userId = userId
        }
      }
      if (this.auth) {
        this.store.dispatch(`security/getUsersAll`).then(res => {
          this.userIdList = _.map(res, v => _.pick(v, ['id', 'userName']))
          d(this.userIdList[0].id)
        })
      } else {
        d(this.store.state.user.userInfo.id)
      }
    },
    mounted () {
    },
    components: { mPopup, mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .create-token-model {
    width: 640px;
  }
</style>
