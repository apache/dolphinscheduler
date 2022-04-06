# Global Parameter

## Scope

The parameters defined on the process definition page can apply to all the scope of the process tasks.

## Usage

Usage of global parameters is: at the process define page, click the '+' beside the 'Set global' and fill in the key and value to save:

<p align="center">
   <img src="/img/supplement_global_parameter_en.png" width="80%" />
 </p>

<p align="center">
   <img src="/img/local_parameter_en.png" width="80%" />
 </p>

The `global_bizdate` parameter defined here can be referenced by local parameters of any other task node, and set the value of `global_bizdate` by referencing the system parameter `system.biz.date`.