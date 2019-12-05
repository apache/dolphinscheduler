## Input

基本表单组件，支持 input 和 textarea，并在原生控件基础上进行了功能扩展，可以组合使用。

### Input props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
type | 类型 | String | text、textarea 和其他原生 input 的 type 值 | text
size | 尺寸，type="textarea" 时无效 | String | large / default / small | default
value | 绑定值 | String / Number | — | —
clearable | 是否可清空 | Boolean | — | false
suffix-icon | 前置图标 | String | — | —
prefix-icon | 后置图标 | String | — | —
label | aria-label 属性 | String | — | —
no-border | 是否无边框 | Boolean | — | false
name | 原生属性 | String | — | —
placeholder | 原生属性 | String | — | 请输入...
disabled | 原生属性 | Boolean | — | false
readonly | 原生属性 | Boolean | — | false
autofocus | 原生属性 | Boolean | — | false
autocomplete | 原生属性 | String | — | off
maxlength | 原生属性 | Number | — | —
minlength | 原生属性 | Number | — | —
tabindex | 原生属性 | Number | — | —
resize | 文本域是否可以拉伸 | String | — | —
autosize | textarea 自适应，如 { minRows: 2, maxRows: 6 } | Boolean / Object | — | false
rows | textarea 原生属性 | Number | — | 2

### Input slots

名称 | 说明
--- | ---
prefix | 前置图标插槽，插槽显示在输入框内
suffix | 后置图标插槽，插槽显示在输入框内
prepend | 前置内容插槽
append | 后置内容插槽

### Input events

事件名称 | 说明 | 回调参数
--- | --- | ---
on-enterkey | 输入回车时触发 | event: Event
on-click | 点击时触发 | event: Event
on-blur | 失去焦点时触发 | event: Event
on-focus | 获得焦点时触发 | event: Event
on-change | 失去焦点并且输入值改变时触发 | value: String
on-clear | 点击清空图标时触发 | —
on-click-icon | 点击前置/后置图标时触发 | event: Event

### Input methods

方法名 | 说明 | 参数
--- | --- | ---
focus | 使 Input 组件获得焦点 | —
blur | 使 Input 组件失去焦点 | —
clear | 清空文本并且获得焦点 | —