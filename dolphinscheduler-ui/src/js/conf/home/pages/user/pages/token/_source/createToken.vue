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
  <m-popover
          ref="popover"
          :ok-text="item ? $t('Edit') : $t('Submit')"
          @ok="_ok"
          @close="close">
    <template slot="content">
      <div class="create-token-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Expiration time')}}</template>
          <template slot="content">
            <el-date-picker
                type="datetime"
                :picker-options="pickerOptions"
                v-model="expireTime"
                @on-change="_onChange"
                format="yyyy-MM-dd HH:mm:ss"
                size="small">
            </el-date-picker>
          </template>
        </m-list-box-f>
        <m-list-box-f v-if="auth">
          <template slot="name"><strong>*</strong>{{$t('User')}}</template>
          <template slot="content">
            <el-select v-model="userId" @change="_onChange" size="small">
              <el-option
                      v-for="city in userIdList"
                      :key="city.id"
                      :value="city.id"
                      :label="city.userName">
              </el-option>
            </el-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">Token</template>
          <template slot="content">
            <el-input
                    readonly
                    style="width: 306px;"
                    type="input"
                    size="small"
                    v-model="token"
                    :placeholder="$t('Please enter token')">
            </el-input>
            <el-button size="small" @click="_generateToken" :loading="tokenLoading">{{$t('Generate token')}}</el-button>
          </template>
        </m-list-box-f>
      </div>
    </template>
  </m-popover>
</template>
<script>
  import _ from 'lodash'
  import dayjs from 'dayjs'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import Permissions from '@/module/permissions'
  import mPopover from '@/module/components/popup/popover'
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
        auth: !Permissions.getAuth(),
        pickerOptions: {
          disabledDate (time) {
            return time.getTime() < Date.now() - 8.64e7 // 当前时间以后可以选择当前时间
          }
        }
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
        this.$refs.popover.spinnerLoading = true
        this.store.dispatch(`user/${this.item ? 'updateToken' : 'createToken'}`, param).then(res => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          this.$refs.popover.spinnerLoading = false
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs.popover.spinnerLoading = false
        })
      },
      _generateToken () {
        this.tokenLoading = true
        this.store.dispatch('user/generateToken', {
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
      },
      close () {
        this.$emit('close')
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
        this.store.dispatch('security/getUsersAll').then(res => {
          this.userIdList = _.map(res, v => _.pick(v, ['id', 'userName']))
          d(this.userIdList[0].id)
        })
      } else {
        d(this.store.state.user.userInfo.id)
      }
    },
    mounted () {
    },
    components: { mPopover, mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .create-token-model {
    width: 640px;
  }
</style>
