## Radio

用于一组可选项单项选择，或者单独用于切换到选中状态。

### Radio props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
value | 单独使用时有效，可用于v-model双向绑定 | Boolean | - | false
name | 原生name属性 | String | - | -
label | 组合使用时有效，指定当前选项value值 | String / Number | - | -
disabled | 是否禁用 | Boolean | - | false
size | 尺寸 | String | large / default / small | default
true-value | 自定义选中时的值，当使用类似 1 和 0 来判断是否选中时会很有用 | String / Number / Boolean | - | true
false-value | 自定义未选中时的值，当使用类似 1 和 0 来判断是否选中时会很有用 | String / Number / Boolean | - | false

### Radio events

事件名称 | 说明 | 返回值
--- | --- | ---
on-change | 在选项状态发生改变时触发，返回当前状态。通过修改外部的数据改变时不会触发 | 选中的 Radio value 值

### RadioGroup props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
value | 当前选中的值，可用于v-model双向绑定 | String / Number / Boolean	 | - | -
name | 原生name属性，优先级小于radio的 name 属性 | String | - | -
size | 尺寸 | String | large、default、small | default
vertical | 是否垂直显示 | Boolean | - | false

### RadioGroup events

事件名称 | 说明 | 返回值
--- | --- | ---
on-change | 在选项状态发生改变时触发，返回当前状态。通过修改外部的数据改变时不会触发 | 选中的 Radio label 值