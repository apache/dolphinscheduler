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
import _ from 'lodash'
import { setUrlParams } from '@/module/util/routerUtil'
/**
 * Mainly used for data list paging url param handle
 * @param _getList => api function(required)
 */
export default {
  watch: {
    // watch pageNo
    searchParams: {
      deep: true,
      handler () {
        setUrlParams(this.searchParams)
        this._debounceGET()
      }
    }
  },
  created () {
    // Routing parameter merging
    if (!_.isEmpty(this.$route.query)) {
      this.searchParams = _.assign(this.searchParams, this.$route.query)
    }
  },
  mounted () {
    this._debounceGET()
  },
  methods: {
    /**
     * Anti-shake request interface
     * @desc Prevent function from being called multiple times
     */
    _debounceGET: _.debounce(function (flag) {
      this._getList(flag)
    }, 100, {
      leading: false,
      trailing: true
    })
  }
}
