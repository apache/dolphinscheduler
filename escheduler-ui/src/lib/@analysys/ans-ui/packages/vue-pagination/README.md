## Pagination

当数据量过多时，使用分页分解数据。

### Pagination props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
current | 当前页 | Number | - | 1
total | 总数据条数 | Number | - | 0
page-size | 每页数据条数 | Number | - | 10
page-size-options | 每页条数切换的配置 | Array | - | [10, 20, 30, 40, 50]
pager-count | 页码按钮的数量，当总页数超过该值时会折叠（奇数） | Number | - | 7
show-total | 是否显示总条数 | Boolean | - | false
show-elevator | 是否显示跳转页 | Boolean | - | false
show-sizer | 是否显示每页条数切换栏 | Boolean | - | false
simple | 是否使用简洁版 | Boolean | - | false
small | 是否显示迷你版 | Boolean | - | false

#### Pagination events

事件名称 | 说明 | 返回值
--- | --- | ---
on-change | 页码切换时触发 | 返回当前页码
