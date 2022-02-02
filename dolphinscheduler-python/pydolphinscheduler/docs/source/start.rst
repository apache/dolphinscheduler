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

Getting Started
===============

To get started with *PyDolphinScheduler* you must ensure python and pip
installed on your machine, if you're already set up, you can skip straight
to `Installing PyDolphinScheduler`_, otherwise please continue with
`Installing Python`_.

Installing Python
-----------------

How to install `python` and `pip` depends on what operating system
you're using. The python wiki provides up to date
`instructions for all platforms here`_. When you entering the website
and choice your operating system, you would be offered the choice and
select python version. *PyDolphinScheduler* recommend use version above
Python 3.6 and we highly recommend you install *Stable Releases* instead
of *Pre-releases*.

After you have download and installed Python, you should open your terminal,
typing and running :code:`python --version` to check whether the installation
is correct or not. If all thing good, you could see the version in console
without error(here is a example after Python 3.8.7 installed)

.. code-block:: bash

    $ python --version
    Python 3.8.7

Installing PyDolphinScheduler
-----------------------------

After Python is already installed on your machine following section
`installing Python`_, it easy to *PyDolphinScheduler* by pip.

.. code-block:: bash

    $ pip install apache-dolphinscheduler

The latest version of *PyDolphinScheduler* would be installed after you run above
command in your terminal. You could go and `start Python Gateway Server`_ to finish
the prepare, and then go to :doc:`tutorial` to make your hand dirty. But if you
want to install the unreleased version of *PyDolphinScheduler*, you could go and see
section `installing PyDolphinScheduler in dev`_ for more detail.

Installing PyDolphinScheduler In Dev
------------------------------------

Because the project is developing and some of the features still not release.
If you want to try some thing unreleased you could install from the source code
which we hold in GitHub

.. code-block:: bash

    # Clone Apache DolphinScheduler repository
    $ git clone git@github.com:apache/dolphinscheduler.git
    # Install PyDolphinScheduler in develop mode
    $ cd dolphinscheduler-python/pydolphinscheduler && pip install -e .

After you installed *PyDolphinScheduler*, please remember `start Python Gateway Server`_
which waiting for *PyDolphinScheduler*'s workflow definition require.

Start Python Gateway Server
---------------------------

Since **PyDolphinScheduler** is Python API for `Apache DolphinScheduler`_, it
could define workflow and tasks structure, but could not run it unless you
`install Apache DolphinScheduler`_ and start Python gateway server. We only
and some key steps here and you could go `install Apache DolphinScheduler`_
for more detail

.. code-block:: bash

    # Start pythonGatewayServer
    $ ./bin/dolphinscheduler-daemon.sh start pythonGatewayServer

To check whether the server is alive or not, you could run :code:`jps`. And
the server is health if keyword `PythonGatewayServer` in the console. 

.. code-block:: bash

    $ jps
    ....
    201472 PythonGatewayServer
    ....

What's More
-----------

If you do not familiar with *PyDolphinScheduler*, you could go to :doc:`tutorial`
and see how it work. But if you already know the inside of *PyDolphinScheduler*,
maybe you could go and play with all :doc:`tasks/index` *PyDolphinScheduler* supports.

.. _`instructions for all platforms here`: https://wiki.python.org/moin/BeginnersGuide/Download
.. _`Apache DolphinScheduler`: https://dolphinscheduler.apache.org
.. _`install Apache DolphinScheduler`: https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/guide/installation/standalone.html
