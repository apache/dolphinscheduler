# Built-in Parameter

## Basic Built-in Parameter

<table>
    <tr><th>Variable</th><th>Declaration Method</th><th>Meaning</th></tr>
    <tr>
        <td>system.biz.date</td>
        <td>${system.biz.date}</td>
        <td>The day before the schedule time of the daily scheduling instance, the format is yyyyMMdd</td>
    </tr>
    <tr>
        <td>system.biz.curdate</td>
        <td>${system.biz.curdate}</td>
        <td>The schedule time of the daily scheduling instance, the format is yyyyMMdd</td>
    </tr>
    <tr>
        <td>system.datetime</td>
        <td>${system.datetime}</td>
        <td>The schedule time of the daily scheduling instance, the format is yyyyMMddHHmmss</td>
    </tr>
</table>

## Extended Built-in Parameter

- Support custom variables in the code, declaration way: `${variable name}`. Refers to "System Parameter".

- Benchmark variable defines as `$[...]` format, time format `$[yyyyMMddHHmmss]` can be decomposed and combined arbitrarily, such as: `$[yyyyMMdd]`, `$[HHmmss]`, `$[yyyy-MM-dd]`, etc.

- Or define by the 2 following ways:

      1. Use add_month(yyyyMMdd, offset) function to add or minus number of months
      the first parameter of this function is [yyyyMMdd], represents the time format
      the second parameter is offset, represents the number of months the user wants to add or minus
      * Next N years：$[add_months(yyyyMMdd,12*N)]
      * N years before：$[add_months(yyyyMMdd,-12*N)]
      * Next N months：$[add_months(yyyyMMdd,N)]
      * N months before：$[add_months(yyyyMMdd,-N)]
      *********************************************************************************************************
      1. Add or minus numbers directly after the time format
      * Next N weeks：$[yyyyMMdd+7*N]
      * First N weeks：$[yyyyMMdd-7*N]
      * Next N days：$[yyyyMMdd+N]
      * N days before：$[yyyyMMdd-N]
      * Next N hours：$[HHmmss+N/24]
      * First N hours：$[HHmmss-N/24]
      * Next N minutes：$[HHmmss+N/24/60]
      * First N minutes：$[HHmmss-N/24/60]