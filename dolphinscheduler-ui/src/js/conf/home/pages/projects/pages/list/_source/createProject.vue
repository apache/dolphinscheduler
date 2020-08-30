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
  <m-popup ref="popup" :ok-text="item ? $t('Edit') : $t('Submit')" :nameText="item ? $t('Edit') : $t('Create Project')" @ok="_ok">
    <template slot="content">
      <div class="projects-create-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Project Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="projectName"
                    maxlength="60"
                    :placeholder="$t('Please enter name')"
                    autocomplete="off">
            </x-input>
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
    name: 'projects-create',
    data () {
      return {
        store,
        description: '',
        projectName: ''
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
          projectName: _.trim(this.projectName),
          description: _.trim(this.description)
        }

        // edit
        if (this.item) {
          param.projectId = this.item.id
        }

        this.$refs['popup'].spinnerLoading = true

        this.store.dispatch(`projects/${this.item ? 'updateProjects' : 'createProjects'}`, param).then(res => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          setTimeout(() => {
            this.$refs['popup'].spinnerLoading = false
          }, 800)
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs['popup'].spinnerLoading = false
        })
      },
      _verification () {
        if (!this.projectName) {
          this.$message.warning(`${i18n.$t('Please enter name')}`)
          return false
        }
        return true
      }
    },
    watch: {},
    created () {
      if (this.item) {
        this.projectName = this.item.name
        this.description = this.item.description
      }
    },
    mounted () {
    },
    components: { mPopup, mListBoxF }
  }
</script>
