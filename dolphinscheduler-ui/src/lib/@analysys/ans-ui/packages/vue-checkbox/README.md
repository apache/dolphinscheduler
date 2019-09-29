## Checkbox

用于一组可选项多项选择，或者单独用于标记切换某种状态。

### Checkbox props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
value | 单独使用时有效，可用于v-model双向绑定 | String / Number / Boolean | - | -
label | 组合使用时有效，指定当前选项value值 | String / Number / Boolean | - | -
disabled | 是否禁用 | Boolean | - | false
true-value | 自定义选中时的值 | String / Number / Boolean | - | true
false-value | 自定义未选中时的值 | String / Number / Boolean | - | false

### Checkbox events

事件名称 | 说明 | 回调参数
--- | --- | ---
on-change | 在选项状态发生改变时触发，返回当前状态。通过修改外部的数据改变时不会触发 | 选中的 Checkbox value 值

### CheckboxGroup props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
value | 当前选中的值，可用于v-model双向绑定 | Array | - | []

### CheckboxGroup events

事件名称 | 说明 | 回调参数
--- | --- | ---
on-change | 在选项状态发生改变时触发，返回当前状态。通过修改外部的数据改变时不会触发 | 选中的 Checkbox label 值