## Spin

加载数据时显示遮罩和提示。

### Spin 指令修饰符

修饰符 | 说明
--- | ---
fullscreen | 全屏
lock | 锁定滚动，隐藏滚动条，仅在 fullscreen 为 true 时可用
body | 指定 DOM 节点插入 body 中

### Spin 绑定元素属性

属性名称 | 说明
--- | ---
spin-text | 加载图标下方文字
spin-background | 遮罩背景色
spin-icon-class | 自定义加载图标类名
spin-custom-class | spin 的自定义类名

### Spin 服务配置

属性 | 说明 | 类型 | 可选值 | 默认值
--- | --- | --- | --- | ---
target | spin 需要覆盖的 DOM 节点。可传入一个 DOM 对象或字符串；若传入字符串，则会将其作为参数传 document.querySelector 以获取到对应 DOM 节点 | Object / String | — | document.body
body | 同 v-spin 指令中的 body 修饰符 | Boolean | — | false
fullscreen | 同 v-spin 指令中的 fullscreen 修饰符 | Boolean | — | true
lock | 同 v-spin 指令中的 lock 修饰符 | Boolean |— | false
text | 加载图标下方文字 | String | — | —
background | 遮罩背景色 | String | — | —
iconClass | 自定义加载图标类名 | String | — | —
customClass | spin 的自定义类名 | String | — | —