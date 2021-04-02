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
          :nameText="item ? $t('Edit worker group') : $t('Create worker group')"
          @ok="_ok"
          style="width: 700px;">
    <template slot="content">
      <div class="create-worker-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Group Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="name"
                    maxlength="60"
                    :placeholder="$t('Please enter group name')">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Worker Addresses')}}</template>
          <template slot="content">
            <treeselect :options="this.workerAddressList" v-model="addrList" :multiple="true" :placeholder="$t('Please select the worker addresses')"></treeselect>
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
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'

  export default {
    name: 'create-warning',
    data () {
      return {
        store,
        id: 0,
        name: '',
        addrList: []
      }
    },
    props: {
      item: Object,
      workerAddressList: Object
    },
    methods: {
      _ok () {
        if (this._verification()) {
          // Verify username
          this._submit()
        }
      },
      _verification () {
        // group name
        if (!this.name) {
          this.$message.warning(`${i18n.$t('Please enter group name')}`)
          return false
        }
        if (!this.addrList.length) {
          this.$message.warning(`${i18n.$t('Worker addresses cannot be empty')}`)
          return false
        }
        return true
      },
      _submit () {
        let param = {
          id: this.id,
          name: this.name,
          addrList: this.addrList.join(',')
        }
        if (this.item) {
          param.id = this.item.id
        }
        this.$refs['popup'].spinnerLoading = true
        this.store.dispatch(`security/saveWorkerGroups`, param).then(res => {
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
        this.id = this.item.id
        this.name = this.item.name
        this.addrList = this.item.addrList.split(',')
      }
    },
    mounted () {
    },
    components: { mPopup, mListBoxF, Treeselect }
  }
</script>