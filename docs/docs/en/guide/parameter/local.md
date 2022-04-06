# Local Parameter

## Scope

Parameters configured on the task definition page, the scope of this parameter is inside this task only. But if you configure according to [Refer to Parameter Context](context.md), it could pass to downstream tasks.

## Usage

Usage of local parameters is: at the task define page, click the '+' beside the 'Custom Parameters' and fill in the key and value to save:

<p align="center">
     <img src="/img/supplement_local_parameter_en.png" width="80%" />
</p>

<p align="center">
     <img src="/img/global_parameter_en.png" width="80%" />
</p>

If you want to call the [built-in parameter](built-in.md) in the local parameters, fill in thevalue of built-in parameters in `value`. As in the above figure, `${biz_date}` and `${curdate}`.