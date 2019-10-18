## Select

使用模拟的增强下拉选择器来代替浏览器原生的选择器。

### Select props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
value | 绑定值 | String / Number / Object / Array | — | —
value-key | 当绑定值为对象或者对象数组时，通过该属性值判断 Option 是否被选中，否则将比较对象是否相等 | String | — | —
name | select 组件中 input 的原生属性 | String | — | —
placeholder | select 组件中 input 的原生属性 | String | — | 请选择
input-props | select 组件中 input 组件的属性 | Object | — | —
width | 下拉框的宽度，默认与触发元素的宽度相同 | String / Number | — | —
height | 下拉框的高度，超出该高度出现滚动条 | String / Number | — | —
max-height | 下拉框的最大高度，超出该高度出现滚动条 | String / Number | — | 300
add-title | 是否在选项中增加 title 属性，显示超长的文本 | Boolean | — | false
disabled | select 组件中 input 的原生属性 | Boolean | — | false
clearable | 是否可清空已选项 | Boolean | — | false
multiple | 是否开启多选 | Boolean | — | false
filterable | 是否开启搜索功能 | Boolean | — | false
filter-props | 当 option 的绑定值为对象时，搜索查找的对应属性列表 | Array | — | —
no-data-text | 选项为空时显示的文字 | String | — | 暂无数据
highlight-matched-text | 搜索时是否高亮选项中匹配的文字，(仅当未设置 filter-props 时可用) | Boolean | — | false
no-match-text | 搜索没有任何匹配项时显示的文字 | String | — | 搜索无结果
has-arrow | 下拉框是否显示指示箭头 | Boolean | — | false
append-to-body | 下拉框是否插入 body | Boolean | — | false
position-fixed | 下拉框是否 fixed 定位 | Boolean | — | false
viewport | 下拉框是否基于 viewport 定位 | Boolean | — | false
popper-options | Popper.js 的可选项 | Object | — | —
scrollbar-class | 滚动条样式 | String | — | —
drop-animation | 下拉框动画 | String | — | —

### Select events

事件名称 | 说明 | 回调参数
--- | --- | ---
on-change | 选中值发生变化时触发 | 目前的选中值，{ value: value, label: label }
on-visible-change | 下拉框状态改变时触发 | 出现则为 true，隐藏则为 false

### Select methods

方法名 | 说明 | 参数
--- | --- | ---
focus | 使 Input 组件获得焦点 | —
blur | 使 input 失去焦点并且隐藏下拉框 | —
search | 搜索 | keyword
updateScrollbar | 更新下拉框内的滚动条 | —
resetScrollbar | 将滚动条移回到顶部 | —

### Select slots

名称 | 说明 | slot-scope 属性
--- | --- | ---
trigger | Select 组件触发元素的插槽 | selectedModel
multiple | 开启多选时，控制 input 内如何显示的插槽 | selectedList
header | 下拉框头部插槽，必须使用作用域插槽 | —
footer | 下拉框底部插槽，必须使用作用域插槽 | —
empty | 数据为空时可使用该插槽 | —

### OptionGroup props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
label | 分组类别名称 | String | — | —

### OptionGroup slots

名称 | 说明
--- | ---
content | 控制分组标签如何显示的插槽

### Option props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
value | 绑定值，可以直接绑定对象，必选项 | String / Number / Object | — | —
label | 选项文本，必选项 | String / Number | — | —
disabled | 是否不可选 | Boolean | — | false
click-handler | 覆盖默认的点击处理，使用该属性意味着需要自己处理选择逻辑 | Function(value, label) | — | —