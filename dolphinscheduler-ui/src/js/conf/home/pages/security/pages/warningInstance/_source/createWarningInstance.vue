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
          <template slot="name"><strong>*</strong>{{$t('Alarm instance name')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="instanceName"
                    maxlength="60"
                    size="small"
                    :placeholder="$t('Please enter group name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Select plugin')}}</template>
          <template slot="content">
            <el-select v-model="pluginDefineId" size="small" style="width: 100%" @change="changePlugin" disabled="true" v-if="item.id">
              <el-option
                      v-for="items in pulginInstance"
                      :key="items.id"
                      :value="items.id"
                      :label="items.pluginName">
              </el-option>
            </el-select>
            <el-select v-model="pluginDefineId" size="small" style="width: 100%" @change="changePlugin" v-else>
              <el-option
                      v-for="items in pulginInstance"
                      :key="items.id"
                      :value="items.id"
                      :label="items.pluginName">
              </el-option>
            </el-select>
          </template>
        </m-list-box-f>
        <div  class="alertForm">
          <template>
            <form-create v-model="$f" :rule="rule" :option="{submitBtn:false}" size="mini"></form-create>
          </template>
        </div>
      </div>
    </template>
  </m-popover>
</template>
<script>
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import mPopover from '@/module/components/popup/popover'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-warning',
    data () {
      return {
        store,
        instanceName: '',
        pluginDefineId: null,
        $f: {},
        rule: []
      }
    },
    props: {
      item: Object,
      pulginInstance: Array
    },
    methods: {
      _ok () {
        if (this._verification()) {
          // The name is not verified
          if (this.item && this.item.instanceName === this.instanceName) {
            this._submit()
            return
          }

          // Verify username
          this.store.dispatch('security/verifyName', {
            type: 'alarmInstance',
            instanceName: this.instanceName
          }).then(res => {
            this._submit()
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      _verification () {
        // group name
        if (!this.instanceName.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter group name')}`)
          return false
        }
        return true
      },
      // Select plugin
      changePlugin () {
        this.store.dispatch('security/getUiPluginById', {
          pluginId: this.pluginDefineId
        }).then(res => {
          this.rule = JSON.parse(res.pluginParams)
          this.rule.forEach(item => {
            if (item.title.indexOf('$t') !== -1) {
              item.title = $t(item.field)
            }
          })
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      },
      _submit () {
        this.$f.rule.forEach(item => {
          item.title = item.name
        })
        let param = {
          instanceName: this.instanceName,
          pluginDefineId: this.pluginDefineId,
          pluginInstanceParams: JSON.stringify(this.$f.rule)
        }
        if (this.item) {
          param.alertPluginInstanceId = this.item.id
          param.pluginDefineId = null
        }
        this.$refs.popover.spinnerLoading = true
        this.store.dispatch(`security/${this.item ? 'updateAlertPluginInstance' : 'createAlertPluginInstance'}`, param).then(res => {
          this.$refs.popover.spinnerLoading = false
          this.$emit('onUpdate')
          this.$message.success(res.msg)
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
      let pluginInstanceParams = []
      if (this.item) {
        this.instanceName = this.item.instanceName
        this.pluginDefineId = this.item.pluginDefineId
        JSON.parse(this.item.pluginInstanceParams).forEach(item => {
          if (item.title.indexOf('$t') !== -1) {
            item.title = $t(item.field)
          }
          pluginInstanceParams.push(item)
        })
        this.rule = pluginInstanceParams
      }
    },
    mounted () {
    },
    components: { mPopover, mListBoxF }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  .alertForm {
    label {
      span {
        font-weight: 10!important;
      }
    }
    .el-row {
      width: 520px;
    }
    .el-form-item__label {
      width: 144px!important;
      color: #606266!important;
    }
    .el-form-item__content {
      margin-left: 144px!important;
      width: calc(100% - 162px);
    }
  }
</style>
