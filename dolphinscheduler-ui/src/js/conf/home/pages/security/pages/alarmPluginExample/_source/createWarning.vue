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
          :nameText="item ? $t('Edit alarm group') : $t('Create alarm group')"
          @ok="_ok">
    <template slot="content">
      <div class="create-warning-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Group Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="groupName"
                    maxlength="60"
                    :placeholder="$t('Please enter group name')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Group Type')}}</template>
          <template slot="content">
            <x-select v-model="groupType">
              <x-option
                      v-for="city in options"
                      :key="city.id"
                      :value="city.id"
                      :label="city.code">
              </x-option>
            </x-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Remarks')}}</template>
          <template slot="content">
            <x-input
                    type="textarea"
                    v-model="description"
                    :placeholder="$t('Please enter description')">
            </x-input>
          </template>
        </m-list-box-f>
      </div>
    </template>
  </m-popup>
</template>
<script>
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import mPopup from '@/module/components/popup/popup'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-warning',
    data () {
      return {
        store,
        groupName: '',
        groupType: 'EMAIL',
        description: '',
        options: [{ code: `${i18n.$t('Email')}`, id: 'EMAIL' }, { code: `${i18n.$t('SMS')}`, id: 'SMS' }]
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
            type: 'alertgroup',
            groupName: this.groupName
          }).then(res => {
            this._submit()
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      _verification () {
        // group name
        if (!this.groupName.replace(/\s*/g,"")) {
          this.$message.warning(`${i18n.$t('Please enter group name')}`)
          return false
        }
        return true
      },
      _submit () {
        let param = {
          groupName: this.groupName,
          groupType: this.groupType,
          description: this.description
        }
        if (this.item) {
          param.id = this.item.id
        }
        this.$refs['popup'].spinnerLoading = true
        this.store.dispatch(`security/${this.item ? 'updateAlertgrou' : 'createAlertgrou'}`, param).then(res => {
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
    watch: {},
    created () {
      if (this.item) {
        this.groupName = this.item.groupName
        this.groupType = this.item.groupType
        this.description = this.item.description
      }
    },
    mounted () {
    },
    components: { mPopup, mListBoxF }
  }
</script>