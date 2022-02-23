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

This tutorial show you the basic concept of *PyDolphinScheduler* and tell all
things you should know before you submit or run your first workflow. If you
still not install *PyDolphinScheduler* and start Apache DolphinScheduler, you
could go and see :ref:`how to getting start PyDolphinScheduler <start:getting started>`

Overview of Tutorial
--------------------

Here have an overview of our tutorial, and it look a little complex but do not
worry about that because we explain this example below as detailed as possible.

.. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
   :start-after: [start tutorial]
   :end-before: [end tutorial]

Import Necessary Module
-----------------------

First of all, we should importing necessary module which we would use later just
like other Python package. We just create a minimum demo here, so we just import
:class:`pydolphinscheduler.core.process_definition` and
:class:`pydolphinscheduler.tasks.shell`.

.. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
   :start-after: [start package_import]
   :end-before: [end package_import]

If you want to use other task type you could click and
:doc:`see all tasks we support <tasks/index>`

Process Definition Declaration
------------------------------

We should instantiate object after we import them from `import necessary module`_.
Here we declare basic arguments for process definition(aka, workflow). We define
the name of process definition, using `Python context manager`_ and it
**the only required argument** for object process definition. Beside that we also
declare three arguments named `schedule`, `start_time` which setting workflow schedule
interval and schedule start_time, and argument `tenant` which changing workflow's
task running user in the worker, :ref:`section tenant <concept:tenant>` in *PyDolphinScheduler*
:doc:`concept` page have more detail information.

.. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
   :start-after: [start workflow_declare]
   :end-before: [end workflow_declare]

We could find more detail about process definition in
:ref:`concept about process definition <concept:process definition>` if you interested in it.
For all arguments of object process definition, you could find in the
:class:`pydolphinscheduler.core.process_definition` api documentation.

Task Declaration
----------------

Here we declare four tasks, and bot of them are simple task of
:class:`pydolphinscheduler.tasks.shell` which running `echo` command in terminal.
Beside the argument `command`, we also need setting argument `name` for each task *(not
only shell task, `name` is required for each type of task)*.

.. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
   :dedent: 0
   :start-after: [start task_declare]
   :end-before: [end task_declare]

Beside shell task, *PyDolphinScheduler* support multiple tasks and you could
find in :doc:`tasks/index`.

Setting Task Dependence
-----------------------

After we declare both process definition and task, we have one workflow with
four tasks, both all tasks is independent so that they would run in parallel.
We should reorder the sort and the dependence of tasks. It useful when we need
run prepare task before we run actual task or we need tasks running is specific
rule. We both support attribute `set_downstream` and `set_upstream`, or bitwise
operators `>>` and `<<`.

In this example, we set task `task_parent` is the upstream task of task
`task_child_one` and `task_child_two`, and task `task_union` is the downstream
task of both these two task.

.. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
   :dedent: 0
   :start-after: [start task_relation_declare]
   :end-before: [end task_relation_declare]

Please notice that we could grouping some tasks and set dependence if they have
same downstream or upstream. We declare task `task_child_one` and `task_child_two`
as a group here, named as `task_group` and set task `task_parent` as upstream of
both of them. You could see more detail in :ref:`concept:Tasks Dependence` section in concept
documentation.

Submit Or Run Workflow
----------------------

Now we finish our workflow definition, with task and task dependence, but all
these things are in local, we should let Apache DolphinScheduler daemon know what we
define our workflow. So the last thing we have to do here is submit our workflow to
Apache DolphinScheduler daemon.

We here in the example using `ProcessDefinition` attribute `run` to submit workflow
to the daemon, and set the schedule time we just declare in `process definition declaration`_.

Now, we could run the Python code like other Python script, for the basic usage run
:code:`python tutorial.py` to trigger and run it.

.. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
   :dedent: 0
   :start-after: [start submit_or_run]
   :end-before: [end submit_or_run]

If you not start your Apache DolphinScheduler server, you could find the way in
:ref:`start:start Python gateway server` and it would have more detail about related server
start. Beside attribute `run`, we have attribute `submit` for object `ProcessDefinition`
and it just submit workflow to the daemon but not setting the schedule information. For
more detail you could see :ref:`concept:process definition`.

DAG Graph After Tutorial Run
----------------------------

After we run the tutorial code, you could login Apache DolphinScheduler web UI,
go and see the `DolphinScheduler project page`_. they is a new process definition be
created and named "Tutorial". It create by *PyDolphinScheduler* and the DAG graph as below

.. literalinclude:: ../../src/pydolphinscheduler/examples/tutorial.py
   :language: text
   :lines: 24-28

.. _`DolphinScheduler project page`: https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/guide/project.html
.. _`Python context manager`: https://docs.python.org/3/library/stdtypes.html#context-manager-types
