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

Configuration
=============

Export Configuration File
-------------------------

pydolphinscheduler allows you to change the built-in configurations via CLI or editor you like. pydolphinscheduler
integrated built-in configurations in its package, but you could also export it locally by CLI

.. code-block:: bash

    $ pydolphinscheduler config --init

And it will create a new YAML file in the path `~/pydolphinscheduler/config.yaml` by default. If you want to export
it to another path, you should set `PYDOLPHINSCHEDULER_HOME` before you run command :code:`pydolphinscheduler config --init`.

.. code-block:: bash

    $ export PYDOLPHINSCHEDULER_HOME=<CUSTOM_PATH>
    $ pydolphinscheduler config --init

After that, your configuration file will export into `<CUSTOM_PATH>/config.yaml` instead of the default path.

Change Configuration
--------------------

In section `export configuration file`_ you export the configuration file locally, and as a local file, you could
edit it with any editor you like. After you save your change in your editor, the latest configuration will work
when you run your workflow code.

You could also query or change the configuration via CLI :code:`config --get <config>` or :code:`config --get <config> <val>`.
Both `--get` and `--set` could be call one or more times in single command, and you could only set the leaf
node of the configuration but could get the parent configuration, there are simple examples below:

.. code-block:: bash

    # Get single configuration in the leaf node
    $ pydolphinscheduler config --get java_gateway.address
    The configuration query as below:

    java_gateway.address = 127.0.0.1

    # Get multiple configuration in the leaf node
    $ pydolphinscheduler config --get java_gateway.address --get java_gateway.port
    The configuration query as below:

    java_gateway.address = 127.0.0.1
    java_gateway.port = 25333

    # Get parent configuration which contain multiple leaf nodes
    $ pydolphinscheduler config --get java_gateway
    The configuration query as below:

    java_gateway = ordereddict([('address', '127.0.0.1'), ('port', 25333), ('auto_convert', True)])

    # Set single configuration
    $ pydolphinscheduler config --set java_gateway.address 192.168.1.1
    Set configuration done.

    # Set multiple configuration
    $ pydolphinscheduler config --set java_gateway.address 192.168.1.1 --set java_gateway.port 25334
    Set configuration done.

    # Set configuration not in leaf node will fail
    $ pydolphinscheduler config --set java_gateway 192.168.1.1,25334,True
    Raise error.

For more information about our CLI, you could see document :doc:`cli`.

All Configurations
------------------

Here are all our configurations for pydolphinscheduler.

.. literalinclude:: ../../src/pydolphinscheduler/core/default_config.yaml
   :language: yaml
   :lines: 18-


