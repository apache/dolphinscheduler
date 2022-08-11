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

ResourcePlugin is the data type of the resources plugin parameter in task subclasses and workflow definitions.


Code
----
.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/__init__.py
   :start-after: [start resource_plugin_definition]
   :end-before: [end resource_plugin_definition]

Dive Into
---------
It has the following key functions.

- Method `__init__`: The `__init__` function requires parameters. The first is the `type` of str type, which means the plugin type of the resource,
and the second is the `prefix` of the str type, which means the prefix of the resource.

.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/__init__.py
    :start-after: [start init_method]
    :end-before: [end init_method]

- Method `get_all_modules`: The `get_all_modules` function will return the absolute path of all resource plugins defined in the resource_plugin file.

.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/__init__.py
    :start-after: [start get_all_modules]
    :end-before: [end get_all_modules]

- Method `import_module`: The `import_module` function has two parameters, `script_name` and `script_path`.
`script_name` is the name of the resource file without the suffix, such as the local resource plugin class local.py, its
`script_name` is local, and `script_path` is the absolute path of the resource plugin file

.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/__init__.py
    :start-after: [start import_module]
    :end-before: [end import_module]

- Method `resource`: The `resource` function will dynamically return the resource plugin object according to the type parameter of the `__init__` function,
for example, the type is `ResourcePluginType.LOCAL`, then the local plugin object is returned

.. literalinclude:: ../../../src/pydolphinscheduler/resources_plugin/__init__.py
    :start-after: [start resource]
    :end-before: [end resource]

.. automodule:: pydolphinscheduler.resources_plugin.__init__

How to use
----------
Resource plug-ins can be used in task subclasses and workflows. You can use the resource plug-ins by adding the `resource_plugin` parameter when they are initialized.

Using resource plug-ins in workflows

.. literalinclude:: ../../../src/pydolphinscheduler/examples/tutorial_resource_plugin.py
   :start-after: [start workflow_declare]
   :end-before: [end workflow_declare]

Using resource plug-ins in sehll tasks

.. literalinclude:: ../../../src/pydolphinscheduler/examples/tutorial_resource_plugin.py
   :start-after: [start task_declare]
   :end-before: [end task_declare]

Use resource plug-ins in both tasks and workflows

.. literalinclude:: ../../../src/pydolphinscheduler/examples/tutorial_resource_plugin.py
   :start-after: [start workflow_declare]
   :end-before: [end task_declare]

When the resource_plugin parameter is defined in both the task subclass and the workflow, the resource_plugin defined in the task subclass is used first.
If the task subclass does not define resource_plugin, but the resource_plugin is defined in the workflow, the resource_plugin in the workflow is used.

Of course, if neither the task subclass nor the workflow specifies resource_plugin, the command at this time will be executed as a script,
in other words, we are forward compatible.