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
      <div class="create-tenement-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Name')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="queueName"
                    maxlength="60"
                    size="mini"
                    :placeholder="$t('Please enter name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Queue value')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="queue"
                    maxlength="60"
                    size="mini"
                    :placeholder="$t('Please enter queue value')">
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
          this.$refs.popover.spinnerLoading = false
        }

        let $catch = (e) => {
          this.$message.error(e.msg || '')
          this.$refs.popover.spinnerLoading = false
        }

        if (this.item) {
          this.$refs.popover.spinnerLoading = true
          this.store.dispatch('security/updateQueueQ', param).then(res => {
            $then(res)
          }).catch(e => {
            $catch(e)
          })
        } else {
          this._verifyName(param).then(() => {
            this.$refs.popover.spinnerLoading = true
            this.store.dispatch('security/createQueueQ', param).then(res => {
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
        if (!this.queueName.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter name')}`)
          return false
        }
        if (!this.queue.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter queue value')}`)
          return false
        }
        return true
      },
      _verifyName (param) {
        return new Promise((resolve, reject) => {
          this.store.dispatch('security/verifyQueueQ', param).then(res => {
            resolve()
          }).catch(e => {
            reject(e)
          })
        })
      },
      close () {
        this.$emit('close')
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
    components: { mPopover, mListBoxF }
  }
</script>
