# 内置参数

## 基础内置参数

<table>
    <tr><th>变量名</th><th>声明方式</th><th>含义</th></tr>
    <tr>
        <td>system.biz.date</td>
        <td>${system.biz.date}</td>
        <td>日常调度实例定时的定时时间前一天，格式为 yyyyMMdd</td>
    </tr>
    <tr>
        <td>system.biz.curdate</td>
        <td>${system.biz.curdate}</td>
        <td>日常调度实例定时的定时时间，格式为 yyyyMMdd</td>
    </tr>
    <tr>
        <td>system.datetime</td>
        <td>${system.datetime}</td>
        <td>日常调度实例定时的定时时间，格式为 yyyyMMddHHmmss</td>
    </tr>
</table>

## 衍生内置参数

- 支持代码中自定义变量名，声明方式：${变量名}。可以是引用 "系统参数"

- 我们定义这种基准变量为 \$[...] 格式的，\$[yyyyMMddHHmmss] 是可以任意分解组合的，比如：\$[yyyyMMdd], \$[HHmmss], \$[yyyy-MM-dd] 等

- 也可以通过以下两种方式：

    1.使用add_months()函数，该函数用于加减月份，
    第一个入口参数为[yyyyMMdd]，表示返回时间的格式
    第二个入口参数为月份偏移量，表示加减多少个月
    * 后 N 年：$[add_months(yyyyMMdd,12*N)]
    * 前 N 年：$[add_months(yyyyMMdd,-12*N)]
    * 后 N 月：$[add_months(yyyyMMdd,N)]
    * 前 N 月：$[add_months(yyyyMMdd,-N)]
    *******************************************
    2.直接加减数字
    在自定义格式后直接“+/-”数字
    * 后 N 周：$[yyyyMMdd+7*N]
    * 前 N 周：$[yyyyMMdd-7*N]
    * 后 N 天：$[yyyyMMdd+N]
    * 前 N 天：$[yyyyMMdd-N]
    * 后 N 小时：$[HHmmss+N/24]
    * 前 N 小时：$[HHmmss-N/24]
    * 后 N 分钟：$[HHmmss+N/24/60]
    * 前 N 分钟：$[HHmmss-N/24/60]
