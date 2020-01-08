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