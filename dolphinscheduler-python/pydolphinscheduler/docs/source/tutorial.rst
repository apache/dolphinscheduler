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

Tutorial
========

This tutorial shows you the basic concept of *PyDolphinScheduler* and tells all
things you should know before you submit or run your first workflow. If you
still have not installed *PyDolphinScheduler* and start DolphinScheduler, you
could go and see :ref:`how to getting start PyDolphinScheduler <start:getting started>` firstly.

Overview of Tutorial
--------------------

Here have an overview of our tutorial, and it looks a little complex but does not
worry about that because we explain this example below as detail as possible.

There are two types of tutorials: traditional and task decorator.

- **Traditional Way**: More general, support many :doc:`built-in task types <tasks/index>`, it is convenient
  when you build your workflow at the beginning.
- **Task Decorator**: A Python decorator allow you to wrap your function into pydolphinscheduler's task. Less
  versatility to the traditional way because it only supported Python functions and without build-in tasks
  supported. But it is helpful if your workflow is all built with Python or if you already have some Python
  workflow code and want to migrate them to pydolphinscheduler.
- **YAML File**: We can use pydolphinscheduler CLI to create process using YAML file: :code:`pydolphinscheduler yaml -f tutorial.yaml`. 
  We can find more YAML file examples in `examples/yaml_define <https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-python/pydolphinscheduler/examples/yaml_define>`_

.. tab:: Tradition

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
      :dedent: 0
      :start-after: [start tutorial]
      :end-before: [end tutorial]

.. tab:: Task Decorator

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial_decorator.py
      :dedent: 0
      :start-after: [start tutorial]
      :end-before: [end tutorial]

.. tab:: YAML File

   .. literalinclude:: ../../examples/yaml_define/tutorial.yaml
      :start-after: # under the License.
      :language: yaml

Import Necessary Module
-----------------------

First of all, we should import the necessary module which we would use later just like other Python packages.

.. tab:: Tradition

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
      :dedent: 0
      :start-after: [start package_import]
      :end-before: [end package_import]

   In tradition tutorial we import :class:`pydolphinscheduler.core.process_definition.ProcessDefinition` and
   :class:`pydolphinscheduler.tasks.shell.Shell`.

   If you want to use other task type you could click and :doc:`see all tasks we support <tasks/index>`

.. tab:: Task Decorator

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial_decorator.py
      :dedent: 0
      :start-after: [start package_import]
      :end-before: [end package_import]

   In task decorator tutorial we import :class:`pydolphinscheduler.core.process_definition.ProcessDefinition` and
   :func:`pydolphinscheduler.tasks.func_wrap.task`.

Process Definition Declaration
------------------------------

We should instantiate :class:`pydolphinscheduler.core.process_definition.ProcessDefinition` object after we
import them from `import necessary module`_. Here we declare basic arguments for process definition(aka, workflow).
We define the name of :code:`ProcessDefinition`, using `Python context manager`_ and it **the only required argument**
for `ProcessDefinition`. Besides, we also declare three arguments named :code:`schedule` and :code:`start_time`
which setting workflow schedule interval and schedule start_time, and argument :code:`tenant` defines which tenant
will be running this task in the DolphinScheduler worker. See :ref:`section tenant <concept:tenant>` in
*PyDolphinScheduler* :doc:`concept` for more information.

.. tab:: Tradition

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
      :dedent: 0
      :start-after: [start workflow_declare]
      :end-before: [end workflow_declare]

.. tab:: Task Decorator

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial_decorator.py
      :dedent: 0
      :start-after: [start workflow_declare]
      :end-before: [end workflow_declare]

.. tab:: YAML File

   .. literalinclude:: ../../examples/yaml_define/tutorial.yaml
      :start-after: # under the License.
      :end-before: # Define the tasks under the workflow
      :language: yaml

We could find more detail about :code:`ProcessDefinition` in :ref:`concept about process definition <concept:process definition>`
if you are interested in it. For all arguments of object process definition, you could find in the
:class:`pydolphinscheduler.core.process_definition` API documentation.

Task Declaration
----------------

.. tab:: Tradition

   We declare four tasks to show how to create tasks, and both of them are simple tasks of
   :class:`pydolphinscheduler.tasks.shell` which runs `echo` command in the terminal. Besides the argument
   `command` with :code:`echo` command, we also need to set the argument `name` for each task
   *(not only shell task, `name` is required for each type of task)*.
   
   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
      :dedent: 0
      :start-after: [start task_declare]
      :end-before: [end task_declare]

   Besides shell task, *PyDolphinScheduler* supports multiple tasks and you could find in :doc:`tasks/index`.

.. tab:: Task Decorator

   We declare four tasks to show how to create tasks, and both of them are created by the task decorator which
   using :func:`pydolphinscheduler.tasks.func_wrap.task`. All we have to do is add a decorator named
   :code:`@task` to existing Python function, and then use them inside :class:`pydolphinscheduler.core.process_definition`

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial_decorator.py
      :dedent: 0
      :start-after: [start task_declare]
      :end-before: [end task_declare]

   It makes our workflow more Pythonic, but be careful that when we use task decorator mode mean we only use
   Python function as a task and could not use the :doc:`built-in tasks <tasks/index>` most of the cases.

.. tab:: YAML File

   .. literalinclude:: ../../examples/yaml_define/tutorial.yaml
      :start-after: # Define the tasks under the workflow 
      :language: yaml

Setting Task Dependence
-----------------------

After we declare both process definition and task, we have four tasks that are independent and will be running
in parallel. If you want to start one task until some task is finished, you have to set dependence on those
tasks.

Set task dependence is quite easy by task's attribute :code:`set_downstream` and :code:`set_upstream` or by
bitwise operators :code:`>>` and :code:`<<`

In this tutorial, task `task_parent` is the leading task of the whole workflow, then task `task_child_one` and
task `task_child_two` are its downstream tasks. Task `task_union` will not run unless both task `task_child_one`
and task `task_child_two` was done, because both two task is `task_union`'s upstream.

.. tab:: Tradition
   
   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
      :dedent: 0
      :start-after: [start task_relation_declare]
      :end-before: [end task_relation_declare]

.. tab:: Task Decorator

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial_decorator.py
      :dedent: 0
      :start-after: [start task_relation_declare]
      :end-before: [end task_relation_declare]

.. tab:: YAML File

   We can use :code:`deps:[]` to set task dependence

   .. literalinclude:: ../../examples/yaml_define/tutorial.yaml
      :start-after: # Define the tasks under the workflow 
      :language: yaml

.. note::

   We could set task dependence in batch mode if they have the same downstream or upstream by declaring those
   tasks as task groups. In tutorial, We declare task `task_child_one` and `task_child_two` as task group named
   `task_group`, then set `task_group` as downstream of task `task_parent`. You could see more detail in
   :ref:`concept:Tasks Dependence` for more detail about how to set task dependence.

Submit Or Run Workflow
----------------------

After that, we finish our workflow definition, with four tasks and task dependence, but all these things are
local, we should let the DolphinScheduler daemon know how the definition of workflow. So the last thing we
have to do is submit the workflow to the DolphinScheduler daemon.

Fortunately, we have a convenient method to submit workflow via `ProcessDefinition` attribute :code:`run` which
will create workflow definition as well as workflow schedule.

.. tab:: Tradition
   
   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
      :dedent: 0
      :start-after: [start submit_or_run]
      :end-before: [end submit_or_run]

.. tab:: Task Decorator

   .. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial_decorator.py
      :dedent: 0
      :start-after: [start submit_or_run]
      :end-before: [end submit_or_run]

.. tab:: YAML File

   pydolphinscheduler YAML CLI always submit workflow. We can run the workflow if we set :code:`run: true`

   .. code-block:: yaml

     # Define the workflow
     workflow:
       name: "tutorial"
       run: true

At last, we could execute this workflow code in your terminal like other Python scripts, running
:code:`python tutorial.py` to trigger and execute it.

.. note::

   If you do not start your DolphinScheduler API server, you could find how to start it in
   :ref:`start:start Python gateway service` for more detail. Besides attribute :code:`run`, we have attribute
   :code:`submit` for object `ProcessDefinition` which just submits workflow to the daemon but does not set
   the workflow schedule information. For more detail, you could see :ref:`concept:process definition`.

DAG Graph After Tutorial Run
----------------------------

After we run the tutorial code, you could log in DolphinScheduler web UI, go and see the
`DolphinScheduler project page`_. They is a new process definition be created by *PyDolphinScheduler* and it
named "tutorial" or "tutorial_decorator". The task graph of workflow like below:

.. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
   :language: text
   :lines: 24-28

Create Process Using YAML File
------------------------------

We can use pydolphinscheduler CLI to create process using YAML file

.. code-block:: bash

   pydolphinscheduler yaml -f Shell.yaml

We can use the following four special grammars to define workflows more flexibly.

- :code:`$FILE{"file_name"}`: Read the file (:code:`file_name`) contents and replace them to that location.
- :code:`$WORKFLOW{"other_workflow.yaml"}`: Refer to another process defined using YAML file (:code:`other_workflow.yaml`) and replace the process name in this location.
- :code:`$ENV{env_name}`: Read the environment variable (:code:`env_name`) and replace it to that location.
- :code:`${CONFIG.key_name}`: Read the configuration value of key (:code:`key_name`) and it them to that location.


In addition, when loading the file path use :code:`$FILE{"file_name"}` or :code:`$WORKFLOW{"other_workflow.yaml"}`, pydolphinscheduler will search in the path of the YAMl file if the file does not exist.

For exmaples, our file directory structure is as follows:

.. code-block:: bash

   .
   └── yaml_define
       ├── Condition.yaml
       ├── DataX.yaml
       ├── Dependent_External.yaml
       ├── Dependent.yaml
       ├── example_datax.json
       ├── example_sql.sql
       ├── example_subprocess.yaml
       ├── Flink.yaml
       ├── Http.yaml
       ├── MapReduce.yaml
       ├── MoreConfiguration.yaml
       ├── Procedure.yaml
       ├── Python.yaml
       ├── Shell.yaml
       ├── Spark.yaml
       ├── Sql.yaml
       ├── SubProcess.yaml
       └── Switch.yaml

After we run

.. code-block:: bash

   pydolphinscheduler yaml -file yaml_define/SubProcess.yaml


the :code:`$WORKFLOW{"example_sub_workflow.yaml"}` will be set to :code:`$WORKFLOW{"yaml_define/example_sub_workflow.yaml"}`, because :code:`./example_subprocess.yaml` does not exist and :code:`yaml_define/example_sub_workflow.yaml` does.

Furthermore, this feature supports recursion all the way down.


.. _`DolphinScheduler project page`: https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/guide/project.html
.. _`Python context manager`: https://docs.python.org/3/library/stdtypes.html#context-manager-types
