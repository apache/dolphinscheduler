# Global Parameter development document

After the user defines the parameter with the direction OUT, it is saved in the localParam of the task.

## Usage of parameters

Getting the direct predecessor node `preTasks` of the current `taskInstance` to be created from the DAG, get the `varPool` of `preTasks`, merge this varPool (List) into one `varPool`, and in the merging process, if parameters with the same parameter name are found, they will be handled according to the following logics:

* If all the values are null, the merged value is null
* If one and only one value is non-null, then the merged value is the non-null value
* If all the values are not null, it would be the earliest value of the endtime of taskInstance taken by VarPool.

The direction of all the merged properties is updated to IN during the merge process.

The result of the merge is saved in taskInstance.varPool.

The worker receives and parses the varPool into the format of `Map<String,Property>`, where the key of the map is property.prop, which is the parameter name.

When the processor processes the parameters, it will merge the varPool and localParam and globalParam parameters, and if there are parameters with duplicate names during the merging process, they will be replaced according to the following priorities, with the higher priority being retained and the lower priority being replaced:

* globalParam: high
* varPool: middle
* localParam: low

The parameters are replaced with the corresponding values using regular expressions compared to ${parameter name} before the node content is executed.

## Parameter setting

Currently, only SQL and SHELL nodes are supported to get parameters.

Get the parameter with direction OUT from localParam, and do the following way according to the type of different nodes.

### SQL node

The structure returned by the parameter is List<Map<String,String>>, where the elements of List are each row of data, the key of Map is the column name, and the value is the value corresponding to the column.

* The OUT parameter names defined by the user when defining the task are matched against the result column names: a `LIST` OUT parameter takes the matched column values of all rows converted to `List<String>` and serialized as a JSON array as its value, while a non-`LIST` OUT parameter takes the single row value and requires the query to return exactly one row.
* If an OUT parameter name matches no result column, or a non-`LIST` OUT parameter is used while the query returns zero or multiple rows, the task fails with a `TaskException` instead of silently discarding or passing a null value.

### SHELL node

The result of the processor execution is returned as `Map<String,String>`.

The user needs to define `${setValue(key=value)}` in the output when defining the shell script.

Remove `${setValue()}` when processing parameters, split by "=", with the 0th being the key and the 1st being the value.

Similarly match the OUT parameter name and key defined by the user when defining the task, and use value as the value of that parameter.

Return parameter processing

* The result of acquired Processor is String.
* Determine whether the processor is empty or not, and exit if it is empty.
* Determine whether the localParam is empty or not, and exit if it is empty.
* Get the parameter of localParam which is OUT, and exit if it is empty.
* Format String as per appeal format (`List<Map<String,String>>` for SQL, `Map<String,String>>` for shell).

Assign the parameters with matching values to varPool (List, which contains the original IN's parameters)

* Format the varPool as json and pass it to master.
* The parameters that are OUT would be written into the localParam after the master has received the varPool.

