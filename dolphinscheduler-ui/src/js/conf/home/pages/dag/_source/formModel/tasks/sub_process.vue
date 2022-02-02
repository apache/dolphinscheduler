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
  <div class="sub_process-model">
    <m-list-box>
      <div slot="text">{{$t('Child Node')}}</div>
      <div slot="content">
        <el-select
                style="width: 100%;"
                size="small"
                filterable
                v-model="wdiCurr"
                :disabled="isDetails"
                @change="_handleWdiChanged">
          <el-option
                v-for="city in processDefinitionList"
                :key="city.code"
                :value="city.code"
                :label="city.name">
          </el-option>
        </el-select>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import disabledState from '@/module/mixin/disabledState'
  import mListBox from './_source/listBox'
  import { mapActions, mapState } from 'vuex'

  export default {
    name: 'sub_process',
    data () {
      return {
        // Process definition(List)
        processDefinitionList: [],
        // Process definition
        wdiCurr: null
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    computed: {
      ...mapState('dag', ['processListS'])
    },
    methods: {
      ...mapActions('dag', ['getProcessList']),
      /**
       * Node unified authentication parameters
       */
      _verification () {
        if (!this.wdiCurr) {
          this.$message.warning(`${i18n.$t('Please select a sub-Process')}`)
          return false
        }
        this.$emit('on-params', {
          processDefinitionCode: this.wdiCurr
        })
        return true
      },
      /**
       * The selected process defines the upper component name padding
       */
      _handleWdiChanged (id) {
        this.$emit('on-set-process-name', this._handleName(id))
      },
      /**
       * Return the name according to the process definition id
       */
      _handleName (code) {
        return _.filter(this.processDefinitionList, v => code === v.code)[0].name
      },
      /**
       * Get all processDefinition list
       */
      getAllProcessDefinitions () {
        if (!this.processListS || this.processListS.length === 0) {
          return this.getProcessList()
        }
        return Promise.resolve(this.processListS)
      }
    },
    watch: {
      wdiCurr (val) {
        this.$emit('on-cache-params', {
          processDefinitionCode: this.wdiCurr
        })
      }
    },
    created () {
      let code = null
      if (this.router.history.current.name === 'projects-instance-details') {
        code = this.router.history.current.query.code || null
      } else {
        code = this.router.history.current.params.code || null
      }
      this.getAllProcessDefinitions().then((processListS) => {
        this.processDefinitionList = processListS.map(def => {
          return {
            id: def.id,
            code: def.code,
            name: def.name,
            disabled: false
          }
        }).filter(a => (a.code + '') !== code)
        let o = this.backfillItem
        // Non-null objects represent backfill
        if (!_.isEmpty(o)) {
          this.wdiCurr = o.params.processDefinitionCode
        } else {
          if (this.processDefinitionList.length) {
            this.wdiCurr = this.processDefinitionList[0].code
            this.$emit('on-set-process-name', this._handleName(this.wdiCurr))
          }
        }
      })
    },
    mounted () {
    },
    components: { mListBox }
  }
</script>
