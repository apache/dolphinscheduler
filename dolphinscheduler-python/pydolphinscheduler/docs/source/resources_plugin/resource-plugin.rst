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

ResourcePlugin
==============

`ResourcePlugin` is an abstract class of resource plug-in parameters of task subclass and workflow.
All resource plugins need to inherit and override its abstract methods.

Code
----
.. literalinclude:: ../../../src/pydolphinscheduler/core/resource_plugin.py
   :start-after: [start resource_plugin_definition]
   :end-before: [end resource_plugin_definition]

Dive Into
---------
It has the following key functions.

- Method `__init__`: The `__init__` function has STR type parameter `prefix`, which means the prefix of the resource.

You can rewrite this function if necessary.

.. literalinclude:: ../../../src/pydolphinscheduler/core/resource_plugin.py
    :start-after: [start init_method]
    :end-before: [end init_method]

- Method `read_file`: Get content from the given URI, The function parameter is the suffix of the file path.

The file prefix has been initialized in init of the resource plug-in.

The prefix plus suffix is the absolute path of the file in this resource.

It is an abstract function. You must rewrite it

.. literalinclude:: ../../../src/pydolphinscheduler/core/resource_plugin.py
    :start-after: [start abstractmethod read_file]
    :end-before: [end abstractmethod read_file]

.. automodule:: pydolphinscheduler.core.resource_plugin

How to use
----------
Resource plugin can be used in task subclasses and workflows. You can use the resource plugin by adding the `resource_plugin` parameter when they are initialized.
For example, local resource plugin, add `resource_plugin = Local("/tmp")`.

The resource plugin we currently support are `local`, `github`, `gitlab`, `OSS`, `S3`.

Here is an example.

.. literalinclude:: ../../../src/pydolphinscheduler/examples/tutorial_resource_plugin.py
   :start-after: [start workflow_declare]
   :end-before: [end task_declare]

When the resource_plugin parameter is defined in both the task subclass and the workflow, the resource_plugin defined in the task subclass is used first.

If the task subclass does not define resource_plugin, but the resource_plugin is defined in the workflow, the resource_plugin in the workflow is used.

Of course, if neither the task subclass nor the workflow specifies resource_plugin, the command at this time will be executed as a script,

in other words, we are forward compatible.