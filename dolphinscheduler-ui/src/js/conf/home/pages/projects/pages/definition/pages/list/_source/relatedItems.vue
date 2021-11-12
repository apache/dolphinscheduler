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
          :ok-text="$t('Confirm')"
          :nameText="$t('Related items')"
          @close="_close"
          @ok="_ok">
    <template slot="content">
      <div class="create-tenement-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Project Name')}}</template>
          <template slot="content">
            <el-select v-model="selected" size="small">
              <el-option
                      v-for="item in itemList"
                      :key="item.code"
                      :value="item.code"
                      :label="item.name">
              </el-option>
            </el-select>
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

  export default {
    name: 'create-tenement',
    data () {
      return {
        store,
        itemList: [],
        selected: ''
      }
    },
    props: {
      tmp: Boolean
    },
    methods: {
      _close () {
        this.$emit('closeRelatedItems')
      },
      _ok () {
        if (this._verification()) {
          if (this.tmp) {
            this.$emit('onBatchMove', this.selected)
          } else {
            this.$emit('onBatchCopy', this.selected)
          }
        }
      },
      _verification () {
        if (!this.selected) {
          this.$message.warning(`${i18n.$t('Project name is required')}`)
          return false
        }
        return true
      }

    },
    watch: {
    },
    created () {
      this.store.dispatch('dag/getAllItems', {}).then(res => {
        if (res.data.length > 0) {
          this.itemList = res.data
        }
      })
    },
    mounted () {

    },
    components: { mPopup, mListBoxF }
  }
</script>
