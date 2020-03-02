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
          :nameText="item ? $t('Edit queue') : $t('Create queue')"
          @ok="_ok">
    <template slot="content">
      <div class="create-tenement-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="queueName"
                    maxlength="60"
                    :placeholder="$t('Please enter name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Queue value')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="queue"
                    maxlength="60"
                    :placeholder="$t('Please enter queue value')"
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
        queue: '',
        queueName: ''
      }
    },
    props: {
      item: Object
    },
    methods: {
      _ok () {
        if (!this._verification()) {
          return
        }

        let param = {
          queue: _.trim(this.queue),
          queueName: _.trim(this.queueName)
        }
        // edit
        if (this.item) {
          param.id = this.item.id
        }

        let $then = (res) => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          setTimeout(() => {
            this.$refs['popup'].spinnerLoading = false
          }, 800)
        }

        let $catch = (e) => {
          this.$message.error(e.msg || '')
          this.$refs['popup'].spinnerLoading = false
        }

        if (this.item) {
          this.$refs['popup'].spinnerLoading = true
          this.store.dispatch(`security/updateQueueQ`, param).then(res => {
            $then(res)
          }).catch(e => {
            $catch(e)
          })
        } else {
          this._verifyName(param).then(() => {
            this.$refs['popup'].spinnerLoading = true
            this.store.dispatch(`security/createQueueQ`, param).then(res => {
              $then(res)
            }).catch(e => {
              $catch(e)
            })
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      _verification () {
        if (!this.queueName.replace(/\s*/g,"")) {
          this.$message.warning(`${i18n.$t('Please enter name')}`)
          return false
        }
        if (!this.queue.replace(/\s*/g,"")) {
          this.$message.warning(`${i18n.$t('Please enter queue value')}`)
          return false
        }
        return true
      },
      _verifyName (param) {
        return new Promise((resolve, reject) => {
          this.store.dispatch(`security/verifyQueueQ`, param).then(res => {
            resolve()
          }).catch(e => {
            reject(e)
          })
        })
      }
    },
    watch: {
    },
    created () {
      if (this.item) {
        this.queueName = this.item.queueName
        this.queue = this.item.queue
      }
    },
    mounted () {

    },
    components: { mPopup, mListBoxF }
  }
</script>