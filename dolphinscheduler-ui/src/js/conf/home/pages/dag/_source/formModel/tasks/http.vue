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
  <div class="http-model">
    <m-list-box>
      <div slot="text">{{$t('Http Url')}}</div>
      <div slot="content">
        <x-input
          :autosize="{minRows:2}"
          :disabled="isDetails"
          type="textarea"
          v-model="url"
          :placeholder="$t('Please Enter Http Url')"
          autocomplete="off">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Http Method')}}</div>
      <div slot="content">
        <x-select
          style="width: 150px;"
          v-model="httpMethod"
          :disabled="isDetails">
          <x-option
            v-for="city in httpMethodList"
            :key="city.code"
            :value="city.code"
            :label="city.code">
          </x-option>
        </x-select>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Http Parameters')}}</div>
      <div slot="content">
        <m-http-params
          ref="refHttpParams"
          @on-http-params="_onHttpParams"
          :udp-list="httpParams"
          :hide="false">
        </m-http-params>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Http Check Condition')}}</div>
      <div slot="content">
        <x-select
          style="width: 230px;"
          v-model="httpCheckCondition"
          :disabled="isDetails">
          <x-option
            v-for="city in httpCheckConditionList"
            :key="city.code"
            :value="city.code"
            :label="city.value">
          </x-option>
        </x-select>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Http Condition')}}</div>
      <div slot="content">
        <x-input
          :autosize="{minRows:2}"
          :disabled="isDetails"
          type="textarea"
          v-model="condition"
          :placeholder="$t('Please Enter Http Condition')"
          autocomplete="off">
        </x-input>
      </div>
    </m-list-box>
    <m-list-box>
      <div slot="text">{{$t('Custom Parameters')}}</div>
      <div slot="content">
        <m-local-params
          ref="refLocalParams"
          @on-local-params="_onLocalParams"
          :udp-list="localParams"
          :hide="false">
        </m-local-params>
      </div>
    </m-list-box>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import cookies from 'js-cookie'
  import mLocalParams from './_source/localParams'
  import mHttpParams from './_source/httpParams'
  import mListBox from './_source/listBox'
  import disabledState from '@/module/mixin/disabledState'
  export default {
    name: 'http',
    data () {
      return {
        url: '',
        condition: '',
        localParams: [],
        httpParams: [],
        httpMethod: 'GET',
        httpMethodList: [{ code: 'GET' }, { code: 'POST' }, { code: 'HEAD' }, { code: 'PUT' }, { code: 'DELETE' }],
        httpCheckCondition: 'STATUS_CODE_DEFAULT',
        httpCheckConditionList: cookies.get('language') == 'en_US'? [{ code: 'STATUS_CODE_DEFAULT',value:'Default response code 200' }, { code: 'STATUS_CODE_CUSTOM',value:'Custom response code' }, { code: 'BODY_CONTAINS',value:'Content includes' }, { code: 'BODY_NOT_CONTAINS',value:'Content does not contain' }]:[{ code: 'STATUS_CODE_DEFAULT',value:'默认响应码200' }, { code: 'STATUS_CODE_CUSTOM',value:'自定义响应码' }, { code: 'BODY_CONTAINS',value:'内容包含' }, { code: 'BODY_NOT_CONTAINS',value:'内容不包含' }]
      }
    },
    props: {
      backfillItem: Object
    },
    mixins: [disabledState],
    methods: {
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },
      _onHttpParams (a) {
        this.httpParams = a
      },
      /**
       * verification
       */
      _verification () {
        if (!this.url) {
          this.$message.warning(`${i18n.$t('Please Enter Http Url')}`)
          return false
        }
        // localParams Subcomponent verification
        if (!this.$refs.refLocalParams._verifProp()) {
          return false
        }
        if (!this.$refs.refHttpParams._verifProp()) {
          return false
        }
        if (!this.$refs.refHttpParams._verifValue()) {
          return false
        }
        // storage
        this.$emit('on-params', {
          localParams: this.localParams,
          httpParams: this.httpParams,
          url: this.url,
          httpMethod: this.httpMethod,
          httpCheckCondition: this.httpCheckCondition,
          condition: this.condition
        })
        return true
      }
    },
    computed: {
      cacheParams () {
        return {
          localParams: this.localParams,
          httpParams: this.httpParams,
          url: this.url,
          httpMethod: this.httpMethod,
          httpCheckCondition: this.httpCheckCondition,
          condition: this.condition
        }
      }
    },
    watch: {
      /**
       * Watch the cacheParams
       * @param val
       */
      cacheParams (val) {
        this.$emit('on-cache-params', val);
      }
    },
    created () {
        let o = this.backfillItem
        // Non-null objects represent backfill
        if (!_.isEmpty(o)) {
          this.url = o.params.url || ''
          this.httpMethod = o.params.httpMethod || 'GET'
          this.httpCheckCondition = o.params.httpCheckCondition || 'DEFAULT'
          this.condition = o.params.condition || ''
          // backfill localParams
          let localParams = o.params.localParams || []
          if (localParams.length) {
            this.localParams = localParams
          }
          let httpParams = o.params.httpParams || []
          if (httpParams.length) {
            this.httpParams = httpParams
          }
        }
    },
    mounted () {
    },
    components: { mLocalParams, mHttpParams, mListBox }
  }
</script>
