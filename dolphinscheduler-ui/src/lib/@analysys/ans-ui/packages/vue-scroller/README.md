## Scroller

滚动视图

### Scroller props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
width | 视图宽度，当插槽内容宽度超过该值时出现水平滚动条 | String / Number | — | —
max-width | 视图最大宽度，当插槽内容宽度超过该值时出现水平滚动条 | String / Number | — | —
height | 视图高度，当插槽内容高度超过该值时出现垂直滚动条 | String / Number | — | —
max-height | 视图最大高度，当插槽内容高度超过该值时出现垂直滚动条 | String / Number | — | —
scrollbar-class | 滚动条自定义类 | String | — | —
reverse-scroll-y | 是否反转 Y 轴滚轮，当该值为 true 时，滚动 Y 轴将控制水平方向的滚动 | Boolean | — | false
show-scrollbar | 是否显示滚动条，当设置为`active`时，触发滚动或者鼠标移动到滚动条轨迹上时才会显示滚动条 | Boolean / String | true / false / 'active' | true
check-on-mounted | 是否在 mounted 的时候递归调用 checkScrollable 方法，直到内容的宽度和高度不为 0 | Boolean | — | false
bar-offset-left | 水平滚动条距离视图左侧的偏移 | Number | — | 0
bar-offset-right | 水平滚动条距离视图右侧的偏移 | Number | — | 0
bar-offset-top | 垂直滚动条距离视图顶部的偏移 | Number | — | 0
bar-offset-bottom | 垂直滚动条距离视图底部的偏移 | Number | — | 0

### Scroller events

事件名称 | 说明 | 回调参数
--- | --- | ---
on-x-start | 水平滚动到达最左侧时触发 | —
on-scroll-x | 水平滚动改变时触发 | 当前的 left 值
on-x-end | 水平滚动到达最右侧时触发 | —
on-y-start | 垂直滚动到达顶部时触发 | —
on-scroll-y | 垂直滚动改变时触发 | 当前的 top 值
on-y-end | 垂直滚动到达底部时触发 | —
on-start-drag-bar | 开始拖动滚动条时触发 | 是否垂直滚动条
on-end-drag-bar | 结束拖动滚动条时触发 | 是否垂直滚动条

### Scroller methods

方法名 | 说明 | 参数
--- | --- | ---
checkScrollable | 检查当前是否需要显示滚动条 | —
setContentLeft | 设置内容区域的 left 值 | left: Number, transition: Boolean
setContentTop | 设置内容区域的 top 值 | top: Number, transition: Boolean
stickToBoundary | 将内容区域设置到指定边界 | vertical: Boolean, start: Boolean, transition: Boolean
