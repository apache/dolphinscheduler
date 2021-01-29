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
      <div class="create-warning-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Group Name')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="groupName"
                    maxlength="60"
                    size="small"
                    :placeholder="$t('Please enter group name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Alarm plugin instance')}}</template>
          <template slot="content">
            <el-select v-model="alertInstanceIds" size="small" style="width: 100%" multiple>
              <el-option
                      v-for="items in allAlertPluginInstance"
                      :key="items.id"
                      :value="items.id"
                      :label="items.instanceName">
              </el-option>
            </el-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Remarks')}}</template>
          <template slot="content">
            <el-input
                type="textarea"
                v-model="description"
                size="small"
                :placeholder="$t('Please enter description')">
            </el-input>
          </template>
        </m-list-box-f>
      </div>
    </template>
  </m-popover>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import mPopover from '@/module/components/popup/popover'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-warning',
    data () {
      return {
        store,
        groupName: '',
        alertInstanceIds: [],
        description: ''
      }
    },
    props: {
      item: Object,
      allAlertPluginInstance: Array
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
          this.store.dispatch('security/verifyName', {
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
        if (!this.groupName.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter group name')}`)
          return false
        }
        return true
      },
      _submit () {
        let param = {
          groupName: this.groupName,
          alertInstanceIds: this.alertInstanceIds.join(','),
          description: this.description
        }
        if (this.item) {
          param.id = this.item.id
        }
        this.$refs.popover.spinnerLoading = true
        this.store.dispatch(`security/${this.item ? 'updateAlertgrou' : 'createAlertgrou'}`, param).then(res => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          this.$refs.popover.spinnerLoading = false
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs.popover.spinnerLoading = false
        })
      },
      close () {
        this.$emit('close')
      }
    },
    watch: {},
    created () {
      if (this.item) {
        this.groupName = this.item.groupName
        let dataStrArr = this.item.alertInstanceIds.split(',')
        this.alertInstanceIds = _.map(dataStrArr, v => {
          return +v
        })
        this.description = this.item.description
      }
    },
    mounted () {
    },
    components: { mPopover, mListBoxF }
  }
</script>
