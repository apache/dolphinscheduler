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

pydolphinscheduler has a built-in module setting necessary configuration to start and run your workflow code.
You could directly use them if you only want to run a quick start or for a simple job like POC. But if you
want to deep use pydolphinscheduler and even use it in production. You should probably need to modify and
change the built-in configuration.

We have two ways to modify the configuration:

- `Using Environment Variables`_: The more lightweight way to modify the configuration. it is useful in
  containerization scenarios, like docker and k8s, or when you like to temporarily override configs in the
  configuration file.
- `Using Configuration File`_: The more general way to modify the configuration. It is useful when you want
  to persist and manage configuration files in one single file.

Using Environment Variables
---------------------------

You could change the configuration by adding or modifying the operating system's environment variables. No
matter what way you used, as long as you can successfully modify the environment variables. We use two common
ways, `Bash <by bash>`_ and `Python OS Module <by python os module>`_, as examples:

By Bash
^^^^^^^

Setting environment variables via `Bash` is the most straightforward and easiest way. We give some examples about
how to change them by Bash.

.. code-block:: bash

   # Modify Java Gateway Address
   $ export PYDS_JAVA_GATEWAY_ADDRESS="192.168.1.1"

   # Modify Workflow Default User
   $ export PYDS_WORKFLOW_USER="custom-user"

After executing the commands above, both ``PYDS_JAVA_GATEWAY_ADDRESS`` and ``PYDS_WORKFLOW_USER`` will be changed.
The next time you execute and submit your workflow, it will submit to host `192.168.1.1`, and with workflow's user
named `custom-user`.

By Python OS Module
^^^^^^^^^^^^^^^^^^^

pydolphinscheduler is a Python API for Apache DolphinScheduler, and you could modify or add system environment
variables via Python ``os`` module. In this example, we change variables as the same value as we change in
`Bash <by bash>`_. It will take effect the next time you run your workflow, and call workflow ``run`` or ``submit``
method next to ``os.environ`` statement.

.. code-block:: python

   import os
   # Modify Java Gateway Address
   os.environ["PYDS_JAVA_GATEWAY_ADDRESS"] = "192.168.1.1"

   # Modify Workflow Default User
   os.environ["PYDS_WORKFLOW_USER"] = "custom-user"

All Configurations in Environment Variables
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

All environment variables as below, and you could modify their value via `Bash <by bash>`_ or `Python OS Module <by python os module>`_

+------------------+------------------------------------+------------------------------------------------------------------------------------------------------------------+
| Variable Section | Variable Name                      | description                                                                                                      |
+==================+====================================+==================================================================================================================+
|                  | ``PYDS_JAVA_GATEWAY_ADDRESS``      | Default Java gateway address, will use its value when it is set.                                                 |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|   Java Gateway   | ``PYDS_JAVA_GATEWAY_PORT``         | Default Java gateway port, will use its value when it is set.                                                    |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_JAVA_GATEWAY_AUTO_CONVERT`` | Default boolean Java gateway auto convert, will use its value when it is set.                                    |
+------------------+------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_USER_NAME``                 | Default user name, will use when user's ``name`` when does not specify.                                          |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_USER_PASSWORD``             | Default user password, will use when user's ``password`` when does not specify.                                  |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|   Default User   | ``PYDS_USER_EMAIL``                | Default user email, will use when user's ``email`` when does not specify.                                        |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_USER_PHONE``                | Default user phone, will use when user's ``phone`` when does not specify.                                        |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_USER_STATE``                | Default user state, will use when user's ``state`` when does not specify.                                        |
+------------------+------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_WORKFLOW_PROJECT``          | Default workflow project name, will use its value when workflow does not specify the attribute ``project``.      |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_WORKFLOW_TENANT``           | Default workflow tenant, will use its value when workflow does not specify the attribute ``tenant``.             |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
| Default Workflow | ``PYDS_WORKFLOW_USER``             | Default workflow user, will use its value when workflow does not specify the attribute ``user``.                 |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_WORKFLOW_QUEUE``            | Default workflow queue, will use its value when workflow does not specify the attribute ``queue``.               |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_WORKFLOW_WORKER_GROUP``     | Default workflow worker group, will use its value when workflow does not specify the attribute ``worker_group``. |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_WORKFLOW_TIME_ZONE``        | Default workflow worker group, will use its value when workflow does not specify the attribute ``timezone``.     |
+                  +------------------------------------+------------------------------------------------------------------------------------------------------------------+
|                  | ``PYDS_WORKFLOW_WARNING_TYPE``     | Default workflow warning type, will use its value when workflow does not specify the attribute ``warning_type``. |
+------------------+------------------------------------+------------------------------------------------------------------------------------------------------------------+

.. note::

   The scope of setting configuration via environment variable is in the workflow, and it will not change the
   value of the configuration file. The :doc:`CLI <cli>` command ``config --get`` and ``config --set`` operate
   the value of the configuration file, so the command ``config --get`` may return a different value from what
   you set in the environment variable, and command ``config --get`` will never change your environment variable.

Using Configuration File
------------------------

If you want to persist and manage configuration in a file instead of environment variables, or maybe you want
want to save your configuration file to a version control system, like Git or SVN, and the way to change
configuration by file is the best choice.

Export Configuration File
^^^^^^^^^^^^^^^^^^^^^^^^^

pydolphinscheduler allows you to change the built-in configurations via CLI or editor you like. pydolphinscheduler
integrated built-in configurations in its package, but you could also export it locally by CLI

.. code-block:: bash

    $ pydolphinscheduler config --init

And it will create a new YAML file in the path `~/pydolphinscheduler/config.yaml` by default. If you want to export
it to another path, you should set `PYDS_HOME` before you run command :code:`pydolphinscheduler config --init`.

.. code-block:: bash

    $ export PYDS_HOME=<CUSTOM_PATH>
    $ pydolphinscheduler config --init

After that, your configuration file will export into `<CUSTOM_PATH>/config.yaml` instead of the default path.

Change Configuration
^^^^^^^^^^^^^^^^^^^^

In section `export configuration file`_ you export the configuration file locally, and as a local file, you could
edit it with any editor you like. After you save your change in your editor, the latest configuration will work
when you run your workflow code.

You could also query or change the configuration via CLI :code:`config --get <config>` or :code:`config --get <config> <val>`.
Both `--get` and `--set` could be called one or more times in single command, and you could only set the leaf
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

All Configurations in File
^^^^^^^^^^^^^^^^^^^^^^^^^^

Here are all our configurations for pydolphinscheduler.

.. literalinclude:: ../../src/pydolphinscheduler/core/default_config.yaml
   :language: yaml
   :lines: 18-

Priority
--------

We have two ways to modify the configuration and there is a built-in config in pydolphinscheduler too. It is
very important to understand the priority of the configuration when you use them. The overview of configuration
priority is.

``Environment Variables > Configurations File > Built-in Configurations``

This means that your setting in environment variables or configurations file will overwrite the built-in one.
And you could temporarily modify configurations by setting environment variables without modifying the global
config in the configuration file.
