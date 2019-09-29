export default {
  bind (el, binding, vnode) {
    // vue 中 v-popover:argument 和 v-popover="variate || expression" 得到的 binding 数据是不同的。
    // 后者可以指向动态 popver 组件，可极大的增强popover指令的灵活程度。
    const ref = binding.expression ? binding.value : binding.arg
    ref && vnode.context.$refs[ref] && (vnode.context.$refs[ref].$refs.reference = el)
  }
}
