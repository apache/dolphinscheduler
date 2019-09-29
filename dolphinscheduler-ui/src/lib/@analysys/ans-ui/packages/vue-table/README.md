## Table

用于展示多条结构类似的数据，可对数据进行排序、筛选、对比或其他自定义操作。

### Table props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
data | 数据 | Array | — | —
default-column-width | 默认最小列宽 | Number | — | 80
fit | 未设置 width 的列是否可以平分表格剩余宽度 | Boolean | — | true
stripe | 是否斑马纹表格 | Boolean | — | false
border | 是否带纵向边框表格 | Boolean | — | false
height | Table 的高度，默认为自动高度。如果 height 为 number 类型，单位 px；如果 height 为 string 类型，则这个高度会设置为 Table 的 style.height 的值，Table 的高度会受控于外部样式。 | String / Number | — | —
restrict | 是否将 Table 限定在父容器内，当 Table 的高度超过父容器时，将出现垂直滚动条 | Boolean | — | false
affix | 吸附在距离窗口顶部固定位置的效果，可直接传距离 | Boolean / Number | — | false
empty-text | 数据为空时显示的文本 | String | — | 暂无数据
default-sort | 默认排序列的 prop 和顺序 | Object | { prop: String, order: 'asc' / 'desc' } | —
default-sort-orders | 点击表头文字，排序规则轮转顺序，数组元素可选值：asc 表示升序，desc 表示降序，null 表示还原为原始顺序 | Array | — | ['asc', 'desc']
default-expand-all | 是否默认展开所有行 | Boolean | — | false
expand-row-keys | 可以通过该属性设置 Table 目前的展开行，需要设置 row-key 属性才能使用，该属性为展开行的 keys 数组 | Array | —
cell-span-method | 合并行或列的计算方法 | Function({ row, column, rowIndex, columnIndex }) | — | —
row-key | 表格行的 key 值，用于优化渲染。当 reserve-states / internal-paging 为 true 时，必须设置该属性 | String | — | —
reserve-states | 数据实例改变后是否保存多选、展开等状态，需要设置 row-key | Boolean | — | false
children-prop | 数据结构为树结构时，叶子节点数组的对应属性名称，注意树结构的数据只能使用自定义排序 | String | — | children
default-unfold-children | 数据结构为树结构时，是否默认展开所有的叶子节点 | Boolean | — | false
show-header | 是否显示表头 | String | — | true
internal-paging | 是否启用内部分页模式：实际显示的表格行数不会超过 row-limit，建议数据量过多时开启以提升性能，需要设置 row-key | Boolean | — | false
row-limit | 最大行数限制 | Number | — | 100
paging-active-distance | 未设置 table 高度时，激活上一页/下一页时的边界距离 | Number | — | 300
reverse-scroll-y | 是否反转 Y 轴滚轮，当该值为 true 时，滚动 Y 轴将控制水平方向的滚动 | Boolean | — | true
scroll-bar-class | 自定义滚动条样式名称 | String | — | —

### Table events

事件名称 | 说明 | 回调参数
--- | --- | ---
on-select | 当用户手动勾选数据行的 Checkbox 时触发的事件 | selection, row
on-select-all | 当用户手动勾选全选 Checkbox 时触发的事件 | selection
on-selection-change | 当选择项发生变化时会触发该事件 | selection
on-expand-change | 当用户对某一行展开或者关闭的时候会触发该事件 | row, expandedRows
on-unfolded-change | 当用户展开或者关闭树节点表格时会触发该事件 | row, unfoldedRows
on-sort-change | 当表格的排序条件发生变化时触发的事件 | column, prop, order
on-hit | 当表格内容滚动到边界时触发该事件，参数为边界位置信息 | 'left' / 'right' / 'top' / 'bottom'

### Table methods

方法名 | 说明 | 参数
--- | --- | ---
doLayout | 对 Table 进行重新布局 | —
clearSelection | 用于多选表格，清空用户的选择 | —
toggleAllSelection | 用于多选表格，切换所有行的选中状态 | —
toggleRowSelection | 用于多选表格，切换某一行的选中状态，如果使用了第二个参数，则是设置这一行选中与否（selected 为 true 则选中） | row, selected
toggleRowExpansion| 用于可展开表格，切换某一行的展开状态，如果使用了第二个参数，则是设置这一行展开与否（expanded 为 true 则展开） | row, expanded
toggleRowUnfolding| 用于树结构表格，切换某一行的节点状态，如果使用了第二个参数，则是设置该节点的子节点是否显示（unfolded 为 true 则显示） | row, unfolded
clearSort | 用于清空排序条件，数据会恢复成未排序的状态 | —
sort | 手动对 Table 进行排序。参数 prop 属性指定排序列，order 指定排序顺序 | prop: String, order: asc / desc
setScrollPosition | 设置滚动条的位置 | left / right / top / bottom

### TableColumn props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
type | 列的类型 | String | selection / expand | —
label | 表头内容 | String | — | —
prop | 表格内容对应的属性，支持多层访问：如 'user.address[0].city' | String | — | —
width | 列宽，单位为 px(只支持数值型的格式，如 400) | Number / String | — | —
fixed | 是否固定列，true 等价于 left | Boolean / String | true / left / right | false
sortable | 对应列是否可以排序，如果设置为 'custom'，则代表用户希望自定义排序，需要监听 Table 的 on-sort-change 事件 | Boolean / String | true / false / 'custom' | false
sort-method | 对数据进行排序的时候使用的方法，仅当 sortable 设置为 true 的时候有效，需返回一个数字，和 Array.sort 表现一致 | Function(cellA, cellB, rowA, rowB) | — | —
sort-orders | 点击表头文字，排序规则轮转顺序，数组元素可选值：asc 表示升序，desc 表示降序，null 表示还原为原始顺序，当前列的该属性会覆盖 default-sort-orders 的值 | Array | — | ['asc', 'desc']
resizable | 是否可以通过拖拽右侧边框改变列宽 | Boolean | — | true
formatter | 格式化内容的函数 | Function(row, column, cellValue, rowIndex, columnIndex) | — | —
align | 单元格对齐方式 | String | left / center / right | left
header-align | 表头对齐方式（当值为 right 时，prepend 插槽和 append 插槽的位置互换） | String | left / center / right | left

### TableColumn scoped slots

名称 | 说明
--- | ---
header | 自定义表头的内容，参数为 { column, $index }
content | 自定义列的内容，参数为 { row, column, content, $rowIndex, $columnIndex }
expand | 展开行的内容，参数为 { row, $index }
prepend | 表头前置插槽，可以在不破坏原有表头功能的基础上进行额外扩展，参数为 { column, $index }
append | 表头后置插槽，可以在不破坏原有表头功能的基础上进行额外扩展，参数为 { column, $index }
headerText | 表头文本插槽，可以在不破坏原有表头功能的基础上进行额外扩展，参数为 { column, $index }
