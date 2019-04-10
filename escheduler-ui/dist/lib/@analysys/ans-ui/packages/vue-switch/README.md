## Switch

在两种状态间切换时用到的开关选择器。

### Switch props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
name | 表单原始name属性 | String / Number / Boolean | - | -
value | 开关当前值 | Boolean | - | false
text | 开关内显示的文本 | Object | {on: '',off: ''} | {on: '',off: ''}
readonly | 只读状态 | Boolean | - | false
disabled | 禁用状态 | Boolean | - | false
size | 开关大小 | String | large / default / small | default
true-value | 自定义选中时的值，当使用类似 1 和 0 来判断是否选中时会很有用 | String / Number / Boolean | - | true
false-value | 自定义未选中时的值，当使用类似 1 和 0 来判断是否选中时会很有用 | String / Number / Boolean | - | false

### Switch events

事件名称 | 说明 | 回调参数
--- | --- | ---
on-click | 点击开关时触发 | 当前 value 值
on-change | 开关变化时触发，显示当前状态 | 当前 value 值