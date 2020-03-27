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
  <m-list-construction :title="$t('Create folder')">
    <template slot="content">
      <div class="resource-create-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Folder Name')}}</template>
          <template slot="content">
            <x-input
                    type="input"
                    v-model="name"
                    maxlength="60"
                    style="width: 300px;"
                    :placeholder="$t('Please enter name')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <!-- <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Folder Format')}}</template>
          <template slot="content">
            <x-select v-model="type" style="width: 100px;">
              <x-option
                      v-for="item in folderList"
                      :key="item.value"
                      :value="item.value"
                      :label="item.label">
              </x-option>
            </x-select>
          </template>
        </m-list-box-f> -->
        <m-list-box-f>
          <template slot="name">{{$t('Description')}}</template>
          <template slot="content">
            <x-input
                    type="textarea"
                    v-model="description"
                    style="width: 430px;"
                    :placeholder="$t('Please enter description')"
                    autocomplete="off">
            </x-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">&nbsp;</template>
          <template slot="content">
            <div class="submit">
              <x-button type="primary" shape="circle" :loading="spinnerLoading" @click="ok()">{{spinnerLoading ? 'Loading...' : $t('Create')}} </x-button>
              <x-button type="text" @click="() => $router.push({name: 'file'})"> {{$t('Cancel')}} </x-button>
            </div>
          </template>
        </m-list-box-f>
      </div>
    </template>
  </m-list-construction>
</template>
<script>
  import i18n from '@/module/i18n'
  import { mapActions } from 'vuex'
  import { folderList } from '../_source/common'
  import { handlerSuffix } from '../details/_source/utils'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'
  import mSpin from '@/module/components/spin/spin'
  import mConditions from '@/module/components/conditions/conditions'
  import localStore from '@/module/util/localStorage'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'resource-list-create-FILE',
    data () {
      return {
        type: '',
        name: '',
        description: '',
        folderList: folderList,
        spinnerLoading: false
      }
    },
    props: {},
    methods: {
      ...mapActions('resource', ['createResourceFolder']),
      ok () {
        if (this._validation()) {
          this.spinnerLoading = true
          this.createResourceFolder({
            type: 'FILE',
            name: this.name,
            currentDir: localStore.getItem('currentDir'),
            pid: this.$route.params.id,
            description: this.description
          }).then(res => {
            this.$message.success(res.msg)
            setTimeout(() => {
              this.spinnerLoading = false
              this.$router.push({ path: `/resource/file/subdirectory/${this.$route.params.id}`})
            }, 800)
          }).catch(e => {
            this.$message.error(e.msg || '')
            this.spinnerLoading = false
          })
        }
      },
      _validation () {
        if (!this.name) {
          this.$message.warning(`${i18n.$t('Please enter resource folder name')}`)
          return false
        }

        return true
      },
    },
    watch: {},
    created () {
    },
    mounted () {
      this.$modal.destroy()
    },
    destroyed () {
    },
    computed: {},
    components: { mListConstruction, mConditions, mSpin, mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .resource-create-model {
    padding: 30px;
  }
</style>
