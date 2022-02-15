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
                    v-model="namespace"
                    maxlength="60"
                    size="mini"
                    :disabled="item ? true: false"
                    :placeholder="$t('Please enter name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('K8s Cluster')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="k8s"
                    maxlength="60"
                    size="mini"
                    :disabled="item ? true: false"
                    :placeholder="$t('Please enter k8s cluster')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('K8s Tag')}}</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="tag"
              maxlength="60"
              size="mini"
              :placeholder="$t('Please enter k8s cluster')">
            </el-input>
          </template>
        </m-list-box-f>

        <m-list-box-f>
          <template slot="name">{{$t('Limits Cpu')}}</template>
          <template slot="content">
            <el-input
              v-model="limitsCpu"
              size="small"
            >
              <template slot="append">Core</template>
            </el-input>
          </template>
        </m-list-box-f>

        <m-list-box-f>
          <template slot="name">{{$t('Limits Memory')}}</template>
          <template slot="content">
            <el-input
              v-model="limitsMemory"
              size="small"
            >
              <template slot="append">GB</template>
            </el-input>
          </template>
        </m-list-box-f>

        <m-list-box-f>
          <template slot="name">{{$t('Namespace Owner')}}</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="owner"
              maxlength="60"
              size="mini"
              :placeholder="$t('Please enter owner')">
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
    name: 'create-namespace',
    data () {
      return {
        store,
        namespace: '',
        k8s: '',
        owner: '',
        tag: '',
        limitsCpu: '',
        limitsMemory: ''
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
          namespace: _.trim(this.namespace),
          k8s: _.trim(this.k8s),
          owner: _.trim(this.owner),
          tag: _.trim(this.tag),
          limitsCpu: _.trim(this.limitsCpu),
          limitsMemory: _.trim(this.limitsMemory)
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
          this.store.dispatch('security/updateNamespace', param).then(res => {
            $then(res)
          }).catch(e => {
            $catch(e)
          })
        } else {
          this._verifyName(param).then(() => {
            this.$refs.popover.spinnerLoading = true
            this.store.dispatch('security/createNamespace', param).then(res => {
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
        if (!this.namespace.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter name')}`)
          return false
        }
        if (!this.k8s.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter namespace')}`)
          return false
        }
        return true
      },
      _verifyName (param) {
        return new Promise((resolve, reject) => {
          this.store.dispatch('security/verifyNamespaceK8s', param).then(res => {
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
        this.namespace = this.item.namespace
        this.k8s = this.item.k8s
        this.owner = this.item.owner
        this.tag = this.item.tag
        this.limitsCpu = this.item.limitsCpu
        this.limitsMemory = this.item.limitsMemory
      }
    },
    mounted () {

    },
    components: { mPopover, mListBoxF }
  }
</script>
