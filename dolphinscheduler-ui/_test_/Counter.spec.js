
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
import { mount } from '@vue/test-utils'
import Counter from '../src/components/Counter.vue'

describe("Counter.vue", () => {
    it("渲染Counter组件", () => {
        const wrapper = mount(Counter)
        expect(wrapper.element).toMatchSnapshot()
    })

    it("初始化之为0", () => {
        const wrapper = mount(Counter)
        expect(wrapper.vm.count).toEqual(0)
    })

    it("加1", () => {
        const wrapper = mount(Counter)
        wrapper.vm.inc()
        expect(wrapper.vm.count).toEqual(1)
    })

    it("减1", () => {
        const wrapper = mount(Counter)
        wrapper.vm.dec()
        expect(wrapper.vm.count).toEqual(-1)
    })

    it("重置", () => {
        const wrapper = mount(Counter)
        wrapper.vm.reset()
        expect(wrapper.vm.count).toEqual(0)
    })

    it("因数为10加1操作", () => {
        const wrapper = mount(Counter, { propsData: { factor: 10 } })
        wrapper.vm.inc()
        expect(wrapper.vm.computedCount).toEqual(10)
    })
})