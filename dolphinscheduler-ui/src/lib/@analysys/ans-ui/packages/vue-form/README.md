## Form

表单验证组件，基于`async-validator`开发。

### Form props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
model | 表单数据对象 | Object | - | -
rules | 表单验证规则，详见`async-validator` | Object | - | -
label-width | 表单域标签的的宽度 | Number / String | - | -
label-height | 表单域标签的的高度 | Number / String | - | -

### Form methods

方法名 | 说明 | 参数
--- | --- | ---
validate | 对整个表单进行校验，参数为检验完的回调，会返回一个 Boolean 表示成功与失败，支持 Promise | callback
resetFields | 对整个表单进行重置，将所有字段值重置为空并移除校验结果 | —
validateField | 对部分表单字段进行校验的方法，参数1为需校验的 prop，参数2为检验完回调，返回错误信息 | prop, callback

### FormItem props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
prop | 对应表单域 model 里的字段 | String | - | -
rules | 表单验证规则，会合并父级的规则 | Object / Array | - | -
label | 标签文本 | String | - | -
required | 是否必填，如不设置，则会根据校验规则自动生成 | Boolean | - | false
label-width | 表单域标签的的宽度 | Number / String | - | -
label-height | 表单域标签的的高度 | Number / String | - | -
label-for | 指定原生的 label 标签的 for 属性 | String | - | -

### FormItem slots

名称 | 说明 | slot-scope 属性
--- | --- | ---
default | 内容 | -
label | label内容 | -
input | 使用原生input时用这个插入内容 | -
select | 使用原生select时用这个插入内容 | -