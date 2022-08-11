.. Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

..   http://www.apache.org/licenses/LICENSE-2.0

.. Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.

How to develop
==============

When you want to create a new resource plugin, you need to add a new class in the module `mod`:`resources_plugin`. All you have to do is named your plugin, implement `__init__` and `read_file` method.

Your plugin class needs two indispensable methods, one is the __init__ method, the parameter is the prefix of type str, the other is
the read_file function, the parameter is the file suffix of type str, the return value is the file content, if it is exists and is readable.

In addition you need to add a constant with your plugin name to ResourcePluginType in dolphinscheduler-python/pydolphinscheduler/src/pydolphinscheduler/constants.py, eg LOCAL = "local".

Example
-------
- Method `__init__`: Initiation method with `param`:`prefix`
.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/local.py
    :start-after: [start init_method]
    :end-before: [end init_method]

- Method `read_file `: Get content from the given URI, The function parameter is the suffix of the file path.
The file prefix has been initialized in init of the resource plug-in. The prefix plus suffix is the absolute
path of the file in this resource
.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/local.py
    :start-after: [start read_file_method]
    :end-before: [end read_file_method]

Last but not least, you should also add new resource plugin in constants.py
.. literalinclude:: ../../../src/pydolphinscheduler/constants.py
    :start-after: [start class_resource]
    :end-before: [end class_resource]

