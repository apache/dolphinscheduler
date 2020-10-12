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
          :nameText="item ? $t('Edit Tenant') : $t('Create Tenant')"
          @ok="_ok">
    <template slot="content">
      <div class="create-tenement-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Tenant Code')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    :disabled="item ? true : false"
                    v-model="tenantCode"
                    maxlength="60"
                    :placeholder="$t('Please enter tenant code')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Tenant Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="tenantName"
                    maxlength="60"
                    :placeholder="$t('Please enter tenant Name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Queue')}}</template>
          <template slot="content">
            <x-select v-model="queueId">
              <x-option
                      v-for="city in queueList"
                      :key="city.id"
                      :value="city.id"
                      :label="city.code">
              </x-option>
            </x-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Description')}}</template>
          <template slot="content">
            <x-input
                    type="textarea"
                    v-model="description"
                    :placeholder="$t('Please enter description')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
      </div>
    </template>
  </m-popup>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import mPopup from '@/module/components/popup/popup'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-tenement',
    data () {
      return {
        store,
        queueList: [],
        queueId: '',
        tenantCode: null,
        tenantName: '',
        description: '',
      }
    },
    props: {
      item: Object
    },
    methods: {
      _ok () {
        if (this._verification()) {
          // The name is not verified
          if (this.item && this.item.groupName === this.groupName) {
            this._submit()
            return
          }
          // Verify username
          this.store.dispatch(`security/verifyName`, {
            type: 'tenant',
            tenantCode: this.tenantCode
          }).then(res => {
            this._submit()
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      _getQueueList () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('security/getQueueList').then(res => {
            this.queueList = _.map(res, v => {
              return {
                id: v.id,
                code: v.queueName
              }
            })
            this.$nextTick(() => {
              this.queueId = this.queueList[0].id
            })
            resolve()
          })
        })
      },
      _verification () {
        let isEn = /^[0-9a-zA-Z_.-]{1,}$/

        if (!this.tenantCode.replace(/\s*/g,"")) {
          this.$message.warning(`${i18n.$t('Please enter the tenant code in English')}`)
          return false
        }

        if (!isEn.test(this.tenantCode) || _.startsWith(this.tenantCode, '_', 0) || _.startsWith(this.tenantCode, '.', 0)) {
          this.$message.warning(`${i18n.$t('Please enter tenant code in English')}`)
          return false
        }

        if (!this.tenantName.replace(/\s*/g,"")) {
          this.$message.warning(`${i18n.$t('Please enter tenant Name')}`)
          return false
        }
        return true
      },
      _submit () {
        // 提交
        let param = {
          tenantCode: this.tenantCode,
          tenantName: this.tenantName,
          queueId: this.queueId,
          description: this.description
        }
        if (this.item) {
          param.id = this.item.id
        }

        this.$refs['popup'].spinnerLoading = true
        this.store.dispatch(`security/${this.item ? 'updateQueue' : 'createQueue'}`, param).then(res => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          setTimeout(() => {
            this.$refs['popup'].spinnerLoading = false
          }, 800)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs['popup'].spinnerLoading = false
        })
      }
    },
    watch: {
    },
    created () {
      this._getQueueList().then(res => {
        if (this.item) {
          this.$nextTick(() => {
            this.queueId = this.item.queueId
          })
          this.tenantCode = this.item.tenantCode
          this.tenantName = this.item.tenantName
          this.description = this.item.description
        }
      })
    },
    mounted () {

    },
    components: { mPopup, mListBoxF }
  }
</script>