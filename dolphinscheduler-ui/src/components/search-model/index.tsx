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

import { defineComponent, PropType, withKeys} from 'vue'
import {NInput} from 'naive-ui'


const placeholder = 'Please enter keyword'

const clearable = true

const props = {
  placeholder: {
    type: String as PropType<string>,
    required: false
  },
  clearable: {
    type: Boolean as PropType<boolean>,
    required: false
  },
  searchVal: {
    type: Object as PropType<any>,
    required: true
  }
}

const Search = defineComponent({
  name: 'Search',
  props,
  emits: ['keyDown'],
  setup(props, ctx) {


    const onKeyDown = () => {
      ctx.emit('keyDown')
    }
    return {
      onKeyDown
    }
  },
  render() {
    const {  $slots } = this
    return (
        <NInput
            size='small'
            placeholder={this.placeholder?this.placeholder:placeholder}
            onKeydown={withKeys(this.onKeyDown,["enter"])}
            clearable={this.clearable ? this.clearable : clearable}
        />
    )
  }
})

export default Search
