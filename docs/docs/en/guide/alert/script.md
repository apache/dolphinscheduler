# Script

If you need to use `Shell script` for alerting, create an alert instance in the alert instance management and select the `Script` plugin.
The following shows the `Script` configuration example:

![dingtalk-plugin](../../../../img/alert/script-plugin.png)

## Parameter Configuration

| **Parameter** |                       **Description**                       |
|---------------|-------------------------------------------------------------|
| User Params   | User defined parameters will pass to the script.            |
| Script Path   | The file location path in the server, only support .sh file |
| Type          | Support `Shell` script.                                     |

### Note

1.Consider the script file access privileges with the executing tenant.
2.Script alerts will execute the corresponding shell script. The platform will not verify the script content and whether it has been tampered with. There is a need to have a high degree of trust in this shell script and trust that users will not abuse this function.
