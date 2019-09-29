## Poptip

以卡片的形式承载了更多的内容，比如链接、表格、按钮等。

### Poptip props

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
trigger | 触发方式 | String | hover, click, focus, 在 confirm 模式下，只有 click 有效 | click
placement | 出现位置 | String | 详见popper.js文档 | top
title | 标题 | String | — | —
content | 显示的正文内容，只在非 confirm 模式下有效 | String | — | —
disabled | 是否禁用 | Boolean | — | —
width | 宽度 | Number | — | —
visible-arrow | 是否显示箭头 | Boolean | — | true
confirm | 是否开启对话框模式 | Boolean | — | false
ok-text | 确定按钮的文字，只在 confirm 模式下有效 | String | — | 确定
cancel-text | 取消按钮的文字，只在 confirm 模式下有效 | String | — | 取消
distance | 弹出层与触发元素的距离 | Number | — | 5
popper-class | 弹出层自定义样式 | String | — | —
append-to-body | 弹出层是否插入 body | Boolean | — | false
position-fixed | 弹出层是否 fixed 定位 | Boolean | — | false
viewport | 弹出层是否基于 viewport 定位 | Boolean | — | false
popper-options | Popper.js 的可选项 | Object | — | —

### Poptip slots

名称 | 说明
--- | ---
default | 内嵌 HTML 文本
reference | 触发元素

### Poptip events

事件名称 | 说明 | 回调参数
--- | --- | ---
on-ok | 点击确定的回调 | —
on-cancel | 点击取消的回调 | —