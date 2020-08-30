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
  <div class="update-udf-model">
    <div class="update-udf-box">
      <ul>
        <li>
          <div class="update-pbx">
            <x-input
                    type="input"
                    size="small"
                    v-model="udfName"
                    :disabled="progress !== 0"
                    style="width: 535px"
                    :placeholder="$t('Please enter name')"
                    autocomplete="off">
            </x-input>
            <div class="p1" style="position: absolute;">
              <input name="file" id="file" type="file" class="file-update" v-if="!progress">
              <x-button type="dashed" size="small" :disabled="progress !== 0"> {{$t('Upload')}} </x-button>
            </div>
          </div>
        </li>
        <li>
          <x-input
                  type="textarea"
                  size="small"
                  v-model="udfDesc"
                  :disabled="progress !== 0"
                  :placeholder="$t('Please enter description')"
                  autocomplete="off">
          </x-input>
        </li>
        <li style="margin-top: -4px;margin-bottom: 8px;">
          <x-button type="success" size="xsmall" long @click="_ok" :loading="spinnerLoading">{{spinnerLoading ? `Loading... (${progress}%)` : $t('Upload UDF Resources')}}</x-button>
        </li>
      </ul>
    </div>
  </div>
</template>
<script>
  import io from '@/module/io'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'

  export default {
    name: 'udf-update',
    data () {
      return {
        store,
        udfName: '',
        udfDesc: '',
        file: '',
        progress: 0,
        spinnerLoading: false,
        pid: null,
        currentDir: ''
      }
    },
    props: {

    },
    methods: {
      /**
       * validation
       */
      _validation () {
        if (!this.currentDir) {
          this.$message.warning(`${i18n.$t('Please select UDF resources directory')}`)
          return false
        }
        if (!this.udfName) {
          this.$message.warning(`${i18n.$t('Please enter file name')}`)
          return false
        }
        if (!this.file) {
          this.$message.warning(`${i18n.$t('Please select the file to upload')}`)
          return false
        }
        return true
      },
      _verifyName () {
        return new Promise((resolve, reject) => {
          this.store.dispatch('resource/resourceVerifyName', {
            fullName: '/'+this.currentDir+'/'+this.udfName,
            type: 'UDF'
          }).then(res => {
            resolve()
          }).catch(e => {
            this.$message.error(e.msg || '')
            reject(e)
          })
        })
      },
      receivedValue(pid,name) {
        this.pid = pid
        this.currentDir = name
      },
      _formDataUpdate () {
        let self = this
        let formData = new FormData()
        formData.append('file', this.file)
        formData.append('type', 'UDF')
        formData.append('pid', this.pid)
        formData.append('currentDir', this.currentDir)
        formData.append('name', this.udfName)
        formData.append('description', this.udfDesc)
        this.spinnerLoading = true
        this.$emit('on-update-present', false)
        io.post(`resources/create`, res => {
          this.$message.success(res.msg)
          this.spinnerLoading = false
          this.progress = 0
          this.$emit('on-update', res.data)
        }, e => {
          this.spinnerLoading = false
          this.progress = 0
          this.$message.error(e.msg || '')
          this.$emit('on-update', e)
        }, {
          data: formData,
          emulateJSON: false,
          timeout: 99999999,
          onUploadProgress (progressEvent) {
            // Size has been uploaded
            let loaded = progressEvent.loaded
            // Total attachment size
            let total = progressEvent.total
            self.progress = Math.floor(100 * loaded / total)
          }
        })
      },
      _ok () {
        if (this._validation()) {
          this._verifyName().then(res => {
            this._formDataUpdate()
          })
        }
      }
    },
    watch: {},
    created () {
    },
    mounted () {
      $('#file').change(() => {
        let file = $('#file')[0].files[0]
        this.file = file
        this.udfName = file.name
      })
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {},
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .update-udf-model {
    min-height: 40px;
    border-radius: 3px;
    margin-top: -10px;
    margin-bottom: -10px;
    .update-udf-box {
      ul {
        li {
          margin-bottom: 8px;
          .v-input {
            textarea {
              min-height: 60px !important;
            }
          }
          .update-pbx {
            position: relative;
            .p1 {
              right: 0;
              top: 0;
              width: 82px;
              height: 28px;
              overflow: hidden;
              .file-update {
                position: absolute;
                left: 0;
                top: 0;
                opacity: 0;
                cursor: pointer;
              }
            }
          }
        }
      }
    }
  }
</style>
