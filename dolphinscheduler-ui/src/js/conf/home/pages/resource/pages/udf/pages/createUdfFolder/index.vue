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
            <el-input
                    type="input"
                    v-model="name"
                    maxlength="60"
                    style="width: 300px;"
                    size="small"
                    :placeholder="$t('Please enter name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Description')}}</template>
          <template slot="content">
            <el-input
                    type="textarea"
                    v-model="description"
                    style="width: 430px;"
                    size="small"
                    :placeholder="$t('Please enter description')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">&nbsp;</template>
          <template slot="content">
            <div class="submit">
              <el-button type="primary" size="mini" round :loading="spinnerLoading" @click="ok()">{{spinnerLoading ? $t('Loading...') : $t('Create')}} </el-button>
              <el-button type="text" size="mini" @click="() => $router.push({name: 'resource-udf'})"> {{$t('Cancel')}} </el-button>
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
  import mListBoxF from '@/module/components/listBoxF/listBoxF'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'resource-list-create-udf',
    data () {
      return {
        type: '',
        name: '',
        description: '',
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
            type: 'UDF',
            name: this.name,
            currentDir: '/',
            pid: -1,
            description: this.description
          }).then(res => {
            this.$message.success(res.msg)
            setTimeout(() => {
              this.spinnerLoading = false
              this.$router.push({ path: '/resource/udf' })
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
      }
    },
    watch: {},
    created () {
    },
    mounted () {
    },
    destroyed () {
    },
    computed: {},
    components: { mListConstruction, mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .resource-create-model {
    padding: 30px;
  }
</style>
