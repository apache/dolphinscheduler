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
  <m-popover ref="popover" :nameText="item ? $t('Edit') : $t('Create Project')" :ok-text="item ? $t('Edit') : $t('Submit')"
           @close="_close" @ok="_ok">
    <template slot="content">
      <div class="projects-create-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{ $t('Project Name') }}</template>
          <template slot="content">
            <el-input
              v-model="projectName"
              :placeholder="$t('Please enter name')"
              maxlength="60"
              size="small"
              type="input">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{ $t('Description') }}</template>
          <template slot="content">
            <el-input
              v-model="description"
              :placeholder="$t('Please enter description')"
              size="small"
              type="textarea">
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

        this.$refs.popover.spinnerLoading = true

        this.store.dispatch(`projects/${this.item ? 'updateProjects' : 'createProjects'}`, param).then(res => {
          this.$emit('_onUpdate')
          this.$message({
            message: res.msg,
            type: 'success',
            offset: 70
          })
          this.$refs.popover.spinnerLoading = false
        }).catch(e => {
          this.$message.error(e.msg || '')
          this.$refs.popover.spinnerLoading = false
        })
      },
      _close () {
        this.$emit('close')
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
    components: { mPopover, mListBoxF }
  }
</script>
